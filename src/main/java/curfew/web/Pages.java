package curfew.web;

import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import curfew.scan.ClassifiedAccess;
import curfew.scan.MockAccessSource;
import curfew.scan.ScanEngine;
import curfew.scan.ScanRules;
import curfew.scan.TokenLedger;
import curfew.scan.TokenReport;
import curfew.store.AccessStore;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/")
public class Pages {

    @CheckedTemplate
    static class Templates {
        public static native TemplateInstance queue(String nav, String state, List<ClassifiedAccess> items);

        public static native TemplateInstance detail(String nav, ClassifiedAccess item);

        public static native TemplateInstance missing(String nav);

        public static native TemplateInstance scan(String nav, String state, TokenReport report, int items);

        public static native TemplateInstance connections(String nav);
    }

    private final AccessStore store;
    private final int parallelism;

    @Inject
    public Pages(AccessStore store, @ConfigProperty(name = "curfew.scan.parallelism") int parallelism) {
        this.store = store;
        this.parallelism = parallelism;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance queue(@QueryParam("estado") String estado) {
        ViewState state = ViewState.from(estado);
        List<ClassifiedAccess> items = state == ViewState.DATA ? store.list() : List.of();
        return Templates.queue("fila", state.name(), items);
    }

    @GET
    @Path("acessos/{id}")
    @Produces(MediaType.TEXT_HTML)
    public Response detail(String id) {
        return store.find(id)
                .map(item -> Response.ok(Templates.detail("fila", item)).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).entity(Templates.missing("fila")).build());
    }

    @GET
    @Path("varredura")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance scan(@QueryParam("estado") String estado) {
        ViewState state = ViewState.from(estado);
        if (state == ViewState.ERROR || state == ViewState.LOADING) {
            return Templates.scan("varredura", state.name(), new TokenLedger().report(0), 0);
        }
        var classified = new ScanEngine(parallelism).classifyAll(new MockAccessSource().list(), new ScanRules());
        if (state == ViewState.DATA) {
            store.replace(classified);
        }
        int count = state == ViewState.EMPTY ? 0 : classified.size();
        return Templates.scan("varredura", state.name(), new TokenLedger().report(count), count);
    }

    @GET
    @Path("conexoes")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance connections() {
        return Templates.connections("conexoes");
    }
}
