package curfew.web;

import java.net.URI;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

@Path("/porta")
public class Door {

    @CheckedTemplate
    static class Templates {
        public static native TemplateInstance door(String nav, String notice);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance form() {
        return Templates.door("porta", "");
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public Response enter(@FormParam("porta") String presented) {
        String configured = env("CURFEW_GATE");
        if (configured.isBlank()) {
            return Response.status(503).entity(Templates.door("porta", "A porta não foi configurada.")).build();
        }
        if (!GateDecision.bearerMatches(configured, presented)) {
            return Response.status(400).entity(Templates.door("porta", "A porta não abriu.")).build();
        }
        NewCookie cookie = new NewCookie.Builder(GateDecision.COOKIE)
                .value(GateDecision.hash(configured))
                .path("/")
                .httpOnly(true)
                .secure(true)
                .sameSite(NewCookie.SameSite.LAX)
                .maxAge(3600)
                .build();
        return Response.seeOther(URI.create("/")).cookie(cookie).build();
    }

    private static String env(String name) {
        String value = System.getenv(name);
        return value == null ? "" : value;
    }
}
