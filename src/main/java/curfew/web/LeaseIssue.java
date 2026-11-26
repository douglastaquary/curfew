package curfew.web;

import java.net.URI;
import java.time.Instant;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import curfew.read.SessionJson;
import curfew.read.StsSessionMint;
import curfew.scan.LeaseBook;
import curfew.scan.LeaseDelivery;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/")
public class LeaseIssue {

    private final LeaseBook book;
    private final String mode;
    private final LeaseDelivery delivery = new LeaseDelivery();

    @Inject
    public LeaseIssue(LeaseBook book, @ConfigProperty(name = "curfew.issue.mode") String mode) {
        this.book = book;
        this.mode = mode;
    }

    @POST
    @Path("chaves")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response issue(
            @HeaderParam("Accept") String accept,
            @FormParam("tarefa") String task,
            @FormParam("sistema") String system,
            @FormParam("horas") String hours) {
        Integer parsed = parse(hours);
        if (parsed == null) {
            return refused("A chave não foi entregue.");
        }
        boolean json = accept != null && accept.contains("application/json");
        boolean armed = json && "auto".equals(mode) && "sim".equals(env("CURFEW_ISSUE"));
        LeaseDelivery.Result result = delivery.deliver(
                book, new StsSessionMint(), armed, task, system, parsed, Instant.now());
        if (result.status() != 200) {
            return refused(result.message());
        }
        if (json) {
            String body = SessionJson.body(result.lease(), result.minted());
            if (body.isBlank()) {
                return refused("A chave não foi entregue.");
            }
            return Response.ok(body, MediaType.APPLICATION_JSON).build();
        }
        return Response.seeOther(URI.create("/?entregue=sim")).build();
    }

    private static Integer parse(String hours) {
        if (hours == null || !hours.matches("\\d{1,2}")) {
            return null;
        }
        return Integer.parseInt(hours);
    }

    private static Response refused(String message) {
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.TEXT_PLAIN)
                .entity(message)
                .build();
    }

    private static String env(String name) {
        String value = System.getenv(name);
        return value == null ? "" : value;
    }
}
