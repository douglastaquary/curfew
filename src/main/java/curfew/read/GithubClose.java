package curfew.read;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

import curfew.scan.CloseTarget;

/**
 * Apaga um webhook, uma chave de deploy ou um nome de segredo no GitHub.
 * O token só vai no cabeçalho. A URL não leva segredo.
 */
public final class GithubClose {

    @FunctionalInterface
    public interface Sender {
        int delete(URI uri, String token);
    }

    public Optional<URI> uri(String target) {
        if (!CloseTarget.safe(target)) {
            return Optional.empty();
        }
        String[] parts = target.split("/");
        String path = switch (parts[0]) {
            case "hooks" -> "hooks/" + parts[3];
            case "keys" -> "keys/" + parts[3];
            case "secrets" -> "actions/secrets/" + parts[3];
            default -> "";
        };
        if (path.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(URI.create(
                "https://api.github.com/repos/" + parts[1] + "/" + parts[2] + "/" + path));
    }

    public boolean delete(Sender sender, String token, String target) {
        if (sender == null || token == null || token.isBlank()) {
            return false;
        }
        Optional<URI> uri = uri(target);
        if (uri.isEmpty() || uri.get().toString().contains(token)) {
            return false;
        }
        try {
            int status = sender.delete(uri.get(), token);
            return status == 204 || status == 404;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    public static Sender http() {
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        return (uri, token) -> {
            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(10))
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "application/vnd.github+json")
                    .header("User-Agent", "curfew")
                    .header("X-GitHub-Api-Version", "2022-11-28")
                    .DELETE()
                    .build();
            try {
                return client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return 0;
            } catch (Exception exception) {
                return 0;
            }
        };
    }
}
