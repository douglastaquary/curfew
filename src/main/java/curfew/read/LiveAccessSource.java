package curfew.read;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import curfew.scan.AccessSource;
import curfew.scan.RawAccess;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.AccessKeyMetadata;
import software.amazon.awssdk.services.iam.model.ListAccessKeysResponse;

/**
 * Lê GitHub e AWS em nome de quem configurou a variável. Não grava o valor do segredo.
 */
public final class LiveAccessSource implements AccessSource {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final int REPO_CAP = 5;

    private final String githubToken;
    private final String awsAccessKeyId;
    private final String awsSecretAccessKey;
    private final Instant now;

    public LiveAccessSource(String githubToken, String awsAccessKeyId, String awsSecretAccessKey, Instant now) {
        this.githubToken = githubToken == null ? "" : githubToken;
        this.awsAccessKeyId = awsAccessKeyId == null ? "" : awsAccessKeyId;
        this.awsSecretAccessKey = awsSecretAccessKey == null ? "" : awsSecretAccessKey;
        this.now = now;
    }

    @Override
    public List<RawAccess> list() {
        List<RawAccess> rows = new ArrayList<>();
        if (!githubToken.isBlank()) {
            rows.addAll(github());
        }
        if (!awsAccessKeyId.isBlank() && !awsSecretAccessKey.isBlank()) {
            rows.addAll(aws());
        }
        return List.copyOf(rows);
    }

    private List<RawAccess> github() {
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        List<RawAccess> rows = new ArrayList<>();
        for (String repo : repoNames(client)) {
            rows.addAll(MetadataInventory.hooks(get(client, "https://api.github.com/repos/" + repo + "/hooks"), now, repo));
            rows.addAll(MetadataInventory.secretNames(
                    get(client, "https://api.github.com/repos/" + repo + "/actions/secrets"), now, repo));
            rows.addAll(MetadataInventory.deployKeys(get(client, "https://api.github.com/repos/" + repo + "/keys"), now, repo));
        }
        return rows;
    }

    private List<String> repoNames(HttpClient client) {
        String body = get(client, "https://api.github.com/user/repos?per_page=" + REPO_CAP + "&sort=pushed");
        List<String> names = new ArrayList<>();
        try {
            JsonNode root = JSON.readTree(body);
            if (!root.isArray()) {
                return names;
            }
            for (JsonNode node : root) {
                String name = node.path("full_name").asText("");
                if (name.matches("[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+")) {
                    names.add(name);
                }
            }
        } catch (Exception exception) {
            throw new ReadingFailed("A leitura do GitHub não completou. Nada foi fechado.");
        }
        return names;
    }

    private String get(HttpClient client, String url) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("Authorization", "Bearer " + githubToken)
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "curfew")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new ReadingFailed("A leitura do GitHub não completou. Nada foi fechado.");
            }
            return response.body();
        } catch (ReadingFailed exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ReadingFailed("A leitura do GitHub não completou. Nada foi fechado.");
        }
    }

    private List<RawAccess> aws() {
        try (IamClient iam = IamClient.builder()
                .region(Region.AWS_GLOBAL)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(awsAccessKeyId, awsSecretAccessKey)))
                .httpClient(UrlConnectionHttpClient.create())
                .build()) {
            ListAccessKeysResponse response = iam.listAccessKeys();
            return MetadataInventory.accessKeys(accessKeysJson(response), now);
        } catch (ReadingFailed exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ReadingFailed("A leitura da AWS não completou. Nada foi fechado.");
        }
    }

    private static String accessKeysJson(ListAccessKeysResponse response) {
        StringBuilder json = new StringBuilder("{\"AccessKeyMetadata\":[");
        boolean first = true;
        for (AccessKeyMetadata meta : response.accessKeyMetadata()) {
            if (!first) {
                json.append(',');
            }
            first = false;
            json.append("{\"AccessKeyId\":\"")
                    .append(escape(meta.accessKeyId()))
                    .append("\",\"Status\":\"")
                    .append(escape(meta.statusAsString()))
                    .append("\",\"CreateDate\":\"")
                    .append(meta.createDate() == null ? "" : meta.createDate().toString())
                    .append("\",\"UserName\":\"")
                    .append(escape(meta.userName()))
                    .append("\"}");
        }
        json.append("]}");
        return json.toString();
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "");
    }
}
