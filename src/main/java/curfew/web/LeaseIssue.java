package curfew.web;

import java.net.URI;
import java.time.Instant;

import curfew.scan.LeaseBook;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/")
public class LeaseIssue {

    private final LeaseBook book;

    @Inject
    public LeaseIssue(LeaseBook book) {
        this.book = book;
    }

    @POST
    @Path("chaves")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response issue(
            @FormParam("tarefa") String task,
            @FormParam("sistema") String system,
            @FormParam("horas") String hours) {
        Integer parsed = parse(hours);
        if (parsed == null) {
            return refused("A chave não foi entregue.");
        }
        if (parsed >= 24) {
            return refused("A chave precisa acabar em menos de 24 horas.");
        }
        if (book.issue(task, system, parsed, Instant.now()).isEmpty()) {
            return refused("A chave não foi entregue.");
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
}
