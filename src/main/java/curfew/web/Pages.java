package curfew.web;

import java.time.Instant;
import java.util.List;

import curfew.read.ReadCoordinator;
import curfew.scan.ClassifiedAccess;
import curfew.scan.LeaseBook;
import curfew.scan.LeaseView;
import curfew.scan.NightlyClose;
import curfew.scan.NightlyNote;
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
        public static native TemplateInstance queue(
                String nav,
                String state,
                List<ClassifiedAccess> items,
                List<LeaseView> leases,
                String leaseNote);

        public static native TemplateInstance detail(String nav, ClassifiedAccess item);

        public static native TemplateInstance missing(String nav);

        public static native TemplateInstance scan(
                String nav,
                String state,
                TokenReport report,
                int items,
                List<NightlyNote> notes,
                String passage);

        public static native TemplateInstance connections(String nav, String github, String aws);
    }

    private final AccessStore store;
    private final ReadCoordinator reading;
    private final LeaseBook leases;

    @Inject
    public Pages(AccessStore store, ReadCoordinator reading, LeaseBook leases) {
        this.store = store;
        this.reading = reading;
        this.leases = leases;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance queue(@QueryParam("estado") String estado, @QueryParam("entregue") String entregue) {
        ViewState state = ViewState.from(estado);
        List<ClassifiedAccess> items = state == ViewState.DATA ? store.list() : List.of();
        List<LeaseView> views = state == ViewState.DATA ? leases.views(Instant.now()) : List.of();
        return Templates.queue("fila", state.name(), items, views, note(entregue, views));
    }

    private static String note(String entregue, List<LeaseView> views) {
        if (!"sim".equals(entregue) || views.isEmpty()) {
            return "";
        }
        LeaseView last = views.getLast();
        return last.task() + " " + last.until() + ". A conta não foi chamada.";
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
    public TemplateInstance scan(
            @QueryParam("estado") String estado,
            @QueryParam("saidas") String saidas,
            @QueryParam("perguntados") String perguntados,
            @QueryParam("conta") String conta) {
        ViewState state = ViewState.from(estado);
        List<NightlyNote> notes = state == ViewState.DATA
                ? new NightlyClose().note(store.list())
                : List.of();
        return Templates.scan(
                "varredura", state.name(), new TokenLedger().report(0), 0, notes, passage(saidas, perguntados, conta));
    }

    private static String passage(String saidas, String perguntados, String conta) {
        if (saidas == null || !saidas.matches("\\d+") || perguntados == null || !perguntados.matches("\\d+")) {
            return "";
        }
        String account = "sim".equals(conta)
                ? "A conta foi chamada."
                : "nao".equals(conta) ? "A conta não foi chamada." : "";
        if (account.isEmpty()) {
            return "";
        }
        return saidas + " saíram da fila. " + perguntados + " pedem um humano. " + account;
    }

    @GET
    @Path("conexoes")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance connections() {
        return Templates.connections("conexoes", reading.githubLabel(), reading.awsLabel());
    }
}
