package curfew.web;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import curfew.read.GithubClose;
import curfew.scan.ClassifiedAccess;
import curfew.scan.NightlyRun;
import curfew.store.AccessStore;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/")
public class NightPassage {

    private final AccessStore store;

    @Inject
    public NightPassage(AccessStore store) {
        this.store = store;
    }

    @POST
    @Path("noite")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response run(@FormParam("confirmo") String confirmo) {
        if (!"sim".equals(confirmo)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.TEXT_PLAIN)
                    .entity("A passagem não rodou.")
                    .build();
        }
        String token = env("CURFEW_GITHUB_TOKEN");
        boolean armed = "sim".equals(env("CURFEW_CLOSE"));
        Map<String, String> targets = new HashMap<>();
        for (ClassifiedAccess item : store.list()) {
            store.target(item.id()).ifPresent(target -> targets.put(item.id(), target));
        }
        GithubClose github = new GithubClose();
        NightlyRun.Remote remote = token.isBlank() ? null : target -> github.delete(GithubClose.http(), token, target);
        NightlyRun.Outcome outcome = new NightlyRun().apply(store.list(), targets, armed, remote);
        outcome.decisions().forEach(store::remember);
        store.replace(outcome.staying());
        String conta = outcome.calledAccount() ? "sim" : "nao";
        return Response.seeOther(URI.create(
                "/varredura?saidas=" + outcome.left() + "&perguntados=" + outcome.asked() + "&conta=" + conta))
                .build();
    }

    private static String env(String name) {
        String value = System.getenv(name);
        return value == null ? "" : value;
    }
}
