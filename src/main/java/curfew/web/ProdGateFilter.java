package curfew.web;

import java.io.IOException;

import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

/**
 * Só existe no perfil prod. Sem CURFEW_GATE a função recusa todo mundo.
 */
@Provider
@ApplicationScoped
@IfBuildProfile("prod")
public class ProdGateFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext request) throws IOException {
        String configured = env("CURFEW_GATE");
        String path = path(request);
        GateDecision.Outcome outcome = GateDecision.decide(
                "prod", configured, bearer(request), cookie(request));
        if (outcome == GateDecision.Outcome.OPEN) {
            return;
        }
        if (outcome == GateDecision.Outcome.LOCKED) {
            request.abortWith(Response.status(503)
                    .type(MediaType.TEXT_PLAIN)
                    .entity("A porta não foi configurada.")
                    .build());
            return;
        }
        if ("porta".equals(path)) {
            return;
        }
        String accept = request.getHeaderString("Accept");
        if (accept == null || accept.contains(MediaType.TEXT_HTML)) {
            request.abortWith(Response.seeOther(java.net.URI.create("/porta")).build());
            return;
        }
        request.abortWith(Response.status(401)
                .type(MediaType.TEXT_PLAIN)
                .entity("A porta está fechada.")
                .build());
    }

    private static String path(ContainerRequestContext request) {
        String path = request.getUriInfo().getPath();
        if (path == null) {
            return "";
        }
        return path.startsWith("/") ? path.substring(1) : path;
    }

    private static String bearer(ContainerRequestContext request) {
        String header = request.getHeaderString("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return "";
        }
        return header.substring("Bearer ".length()).trim();
    }

    private static String cookie(ContainerRequestContext request) {
        String header = request.getHeaderString("Cookie");
        if (header == null) {
            return "";
        }
        String prefix = GateDecision.COOKIE + "=";
        for (String part : header.split(";")) {
            String item = part.trim();
            if (item.startsWith(prefix)) {
                return item.substring(prefix.length());
            }
        }
        return "";
    }

    private static String env(String name) {
        String value = System.getenv(name);
        return value == null ? "" : value;
    }
}
