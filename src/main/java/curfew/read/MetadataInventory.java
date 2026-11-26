package curfew.read;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import curfew.scan.RawAccess;

/**
 * Converte o JSON de leitura em linhas da fila. Descarta URL, chave pública e valor de segredo.
 */
public final class MetadataInventory {

    private static final ObjectMapper JSON = new ObjectMapper();

    private MetadataInventory() {
    }

    public static List<RawAccess> hooks(String body, Instant now) {
        List<RawAccess> rows = new ArrayList<>();
        for (JsonNode node : array(body)) {
            int id = node.path("id").asInt();
            if (id == 0) {
                continue;
            }
            int events = node.path("events").size();
            rows.add(row(
                    "gh-hook-" + id,
                    "webhook",
                    "Webhook",
                    "GitHub",
                    age(node.path("created_at").asText(""), now),
                    null,
                    events > 3));
        }
        return rows;
    }

    public static List<RawAccess> secretNames(String body, Instant now) {
        List<RawAccess> rows = new ArrayList<>();
        JsonNode secrets = root(body).path("secrets");
        if (!secrets.isArray()) {
            return rows;
        }
        for (JsonNode node : secrets) {
            String name = node.path("name").asText("");
            if (name.isBlank() || looksLikeSecret(name)) {
                continue;
            }
            rows.add(row(
                    "gh-secret-" + slug(name),
                    "token",
                    "Segredo nomeado",
                    "GitHub",
                    age(node.path("created_at").asText(""), now),
                    name,
                    false));
        }
        return rows;
    }

    public static List<RawAccess> deployKeys(String body, Instant now) {
        List<RawAccess> rows = new ArrayList<>();
        for (JsonNode node : array(body)) {
            int id = node.path("id").asInt();
            if (id == 0) {
                continue;
            }
            String title = node.path("title").asText("");
            rows.add(row(
                    "gh-key-" + id,
                    "key",
                    "Chave de deploy",
                    "GitHub",
                    age(node.path("created_at").asText(""), now),
                    title.isBlank() ? null : title,
                    !node.path("read_only").asBoolean(true)));
        }
        return rows;
    }

    public static List<RawAccess> accessKeys(String body, Instant now) {
        List<RawAccess> rows = new ArrayList<>();
        JsonNode keys = root(body).path("AccessKeyMetadata");
        if (!keys.isArray()) {
            return rows;
        }
        for (JsonNode node : keys) {
            String accessKeyId = node.path("AccessKeyId").asText("");
            if (accessKeyId.length() < 4 || !"Active".equals(node.path("Status").asText())) {
                continue;
            }
            String owner = node.path("UserName").asText("");
            rows.add(row(
                    "aws-key-" + accessKeyId.substring(accessKeyId.length() - 4).toLowerCase(Locale.ROOT),
                    "key",
                    "Chave de acesso",
                    "AWS",
                    age(node.path("CreateDate").asText(""), now),
                    owner.isBlank() ? null : owner,
                    false));
        }
        return rows;
    }

    private static RawAccess row(
            String id,
            String kind,
            String kindLabel,
            String system,
            int ageDays,
            String owner,
            boolean wideScope) {
        return new RawAccess(id, kind, kindLabel, system, ageDays, null, owner, wideScope, false);
    }

    private static Iterable<JsonNode> array(String body) {
        JsonNode root = root(body);
        if (!root.isArray()) {
            return List.of();
        }
        List<JsonNode> nodes = new ArrayList<>();
        root.forEach(nodes::add);
        return nodes;
    }

    private static JsonNode root(String body) {
        try {
            return JSON.readTree(body == null ? "" : body);
        } catch (Exception exception) {
            return JSON.createObjectNode();
        }
    }

    private static int age(String createdAt, Instant now) {
        if (createdAt == null || createdAt.isBlank()) {
            return 0;
        }
        try {
            Instant created = Instant.parse(createdAt);
            long days = ChronoUnit.DAYS.between(created, now);
            return (int) Math.max(0, days);
        } catch (RuntimeException exception) {
            return 0;
        }
    }

    private static boolean looksLikeSecret(String value) {
        String text = value.toLowerCase(Locale.ROOT);
        return text.contains("ghp_") || text.contains("github_pat_") || text.contains("akia");
    }

    private static String slug(String name) {
        String slug = name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
        if (slug.length() > 24) {
            slug = slug.substring(0, 24);
        }
        return slug.replaceAll("^-|-$", "");
    }
}
