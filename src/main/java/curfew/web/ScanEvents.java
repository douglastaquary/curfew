package curfew.web;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import curfew.scan.ClassifiedAccess;
import curfew.scan.MockAccessSource;
import curfew.scan.ScanEngine;
import curfew.scan.ScanLog;
import curfew.scan.ScanRules;
import curfew.scan.TokenLedger;
import curfew.scan.TokenReport;
import curfew.store.AccessStore;
import io.smallrye.common.annotation.Blocking;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;

@Path("/")
public class ScanEvents {

    private final AccessStore store;
    private final int parallelism;
    private final int pauseMs;

    @Inject
    public ScanEvents(
            AccessStore store,
            @ConfigProperty(name = "curfew.scan.parallelism") int parallelism,
            @ConfigProperty(name = "curfew.scan.log-pause-ms") int pauseMs) {
        this.store = store;
        this.parallelism = parallelism;
        this.pauseMs = pauseMs;
    }

    @GET
    @Path("varredura/eventos")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @Blocking
    public void stream(@Context SseEventSink sink, @Context Sse sse, @QueryParam("estado") String estado) {
        try {
            ViewState state = ViewState.from(estado);
            if (state == ViewState.ERROR) {
                send(sink, sse, "fault", "A leitura não completou. Nada foi fechado.");
                return;
            }
            if (state == ViewState.EMPTY || state == ViewState.LOADING) {
                send(sink, sse, "line", state == ViewState.LOADING
                        ? "Lendo GitHub e AWS."
                        : "Nenhum acesso esquecido nesta leitura.");
                send(sink, sse, "done", "0 0 0 0");
                return;
            }

            send(sink, sse, "line", ScanLog.clock(Instant.now()) + "  Início da leitura. GitHub e AWS.");
            List<ClassifiedAccess> classified = new ScanEngine(parallelism)
                    .classifyAll(new MockAccessSource().list(), new ScanRules());
            store.replace(classified);
            for (ClassifiedAccess item : classified) {
                if (sink.isClosed()) {
                    return;
                }
                pause();
                send(sink, sse, "line", ScanLog.line(item, ScanLog.clock(Instant.now())));
            }
            pause();
            TokenReport report = new TokenLedger().report(classified.size());
            send(sink, sse, "line", ScanLog.clock(Instant.now())
                    + "  Fim. " + report.itemsClassifiedByRule()
                    + " itens por regra. " + report.modelCalls()
                    + " chamadas de modelo. " + report.tokensAvoided()
                    + " tokens evitados. Nada foi fechado.");
            send(sink, sse, "done", report.itemsClassifiedByRule() + " "
                    + report.itemsClassifiedByRule() + " "
                    + report.modelCalls() + " "
                    + report.tokensAvoided());
        } finally {
            try {
                sink.close();
            } catch (IOException exception) {
                // O navegador fechou a leitura. Não há o que registrar sem o corpo do pedido.
            }
        }
    }

    private void send(SseEventSink sink, Sse sse, String name, String data) {
        if (!sink.isClosed()) {
            sink.send(sse.newEventBuilder().name(name).data(data).build()).toCompletableFuture().join();
        }
    }

    private void pause() {
        if (pauseMs <= 0) {
            return;
        }
        try {
            Thread.sleep(pauseMs);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}
