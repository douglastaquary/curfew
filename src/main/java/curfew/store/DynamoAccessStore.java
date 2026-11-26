package curfew.store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import curfew.scan.CloseTarget;
import curfew.scan.ClassifiedAccess;
import curfew.scan.HumanDecision;
import curfew.scan.ReadingNote;
import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;

/**
 * Persistência de produção. Guarda identificador, idade, dono e ação.
 * Não há atributo para o valor do segredo.
 */
@ApplicationScoped
@IfBuildProfile("prod")
public class DynamoAccessStore implements AccessStore {

    static final String DECISION_PREFIX = "decision#";
    static final String READING_PREFIX = "reading#";

    private final DynamoDbClient dynamo;
    private final String table;
    private final ConcurrentHashMap<String, String> targets = new ConcurrentHashMap<>();

    @Inject
    public DynamoAccessStore(DynamoDbClient dynamo, @ConfigProperty(name = "curfew.table-name") String table) {
        this.dynamo = dynamo;
        this.table = table;
    }

    @Override
    public List<ClassifiedAccess> list() {
        var response = dynamo.scan(ScanRequest.builder().tableName(table).build());
        List<ClassifiedAccess> found = new ArrayList<>();
        for (Map<String, AttributeValue> item : response.items()) {
            String id = text(item, "id");
            if (keptAside(id)) {
                continue;
            }
            found.add(fromAccess(item));
        }
        return List.copyOf(found);
    }

    @Override
    public Optional<ClassifiedAccess> find(String id) {
        if (id == null || keptAside(id)) {
            return Optional.empty();
        }
        var response = dynamo.getItem(GetItemRequest.builder()
                .tableName(table)
                .key(Map.of("id", AttributeValue.fromS(id)))
                .build());
        if (!response.hasItem() || response.item().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(fromAccess(response.item()));
    }

    @Override
    public void replace(List<ClassifiedAccess> items) {
        Set<String> keep = new HashSet<>();
        for (ClassifiedAccess item : items) {
            keep.add(item.id());
        }
        var existing = dynamo.scan(ScanRequest.builder().tableName(table).build());
        List<String> ids = new ArrayList<>();
        for (Map<String, AttributeValue> item : existing.items()) {
            ids.add(text(item, "id"));
        }
        for (String id : idsToDrop(ids, keep)) {
            dynamo.deleteItem(DeleteItemRequest.builder()
                    .tableName(table)
                    .key(Map.of("id", AttributeValue.fromS(id)))
                    .build());
        }
        for (ClassifiedAccess item : items) {
            dynamo.putItem(PutItemRequest.builder().tableName(table).item(toAccess(item)).build());
        }
    }

    static List<String> idsToDrop(List<String> existing, Set<String> keep) {
        List<String> drop = new ArrayList<>();
        for (String id : existing) {
            if (id == null || id.isBlank() || keptAside(id) || keep.contains(id)) {
                continue;
            }
            drop.add(id);
        }
        return drop;
    }

    private static boolean keptAside(String id) {
        return id.startsWith(DECISION_PREFIX) || id.startsWith(READING_PREFIX);
    }

    @Override
    public void remember(HumanDecision decision) {
        dynamo.putItem(PutItemRequest.builder().tableName(table).item(toDecision(decision)).build());
    }

    @Override
    public List<HumanDecision> decisions() {
        var response = dynamo.scan(ScanRequest.builder().tableName(table).build());
        List<HumanDecision> found = new ArrayList<>();
        for (Map<String, AttributeValue> item : response.items()) {
            if (text(item, "id").startsWith(DECISION_PREFIX)) {
                found.add(fromDecision(item));
            }
        }
        return List.copyOf(found);
    }

    @Override
    public void forgetDecisions() {
        var response = dynamo.scan(ScanRequest.builder().tableName(table).build());
        for (Map<String, AttributeValue> item : response.items()) {
            String id = text(item, "id");
            if (id.startsWith(DECISION_PREFIX)) {
                dynamo.deleteItem(DeleteItemRequest.builder()
                        .tableName(table)
                        .key(Map.of("id", AttributeValue.fromS(id)))
                        .build());
            }
        }
    }

    @Override
    public void rememberTarget(String id, String target) {
        if (id != null && CloseTarget.safe(target)) {
            targets.put(id, target);
        }
    }

    @Override
    public Optional<String> target(String id) {
        return Optional.ofNullable(targets.get(id));
    }

    @Override
    public void rememberReading(ReadingNote note) {
        ReadingNote kept = note.kept();
        Map<String, AttributeValue> values = new HashMap<>();
        values.put("id", AttributeValue.fromS(READING_PREFIX + System.currentTimeMillis()));
        values.put("at", AttributeValue.fromS(kept.at()));
        values.put("items", AttributeValue.fromN(Integer.toString(kept.items())));
        values.put("byRule", AttributeValue.fromN(Integer.toString(kept.byRule())));
        values.put("modelCalls", AttributeValue.fromN(Integer.toString(kept.modelCalls())));
        values.put("tokensAvoided", AttributeValue.fromN(Integer.toString(kept.tokensAvoided())));
        values.put("lines", AttributeValue.fromS(String.join("\n", kept.lines())));
        dynamo.putItem(PutItemRequest.builder().tableName(table).item(values).build());
    }

    @Override
    public List<ReadingNote> readings() {
        var response = dynamo.scan(ScanRequest.builder().tableName(table).build());
        List<ReadingNote> found = new ArrayList<>();
        for (Map<String, AttributeValue> item : response.items()) {
            if (!text(item, "id").startsWith(READING_PREFIX)) {
                continue;
            }
            String raw = text(item, "lines");
            List<String> lines = raw.isBlank() ? List.of() : List.of(raw.split("\n"));
            found.add(new ReadingNote(
                    text(item, "at"),
                    number(item, "items"),
                    number(item, "byRule"),
                    number(item, "modelCalls"),
                    number(item, "tokensAvoided"),
                    lines));
        }
        return List.copyOf(found);
    }

    private Map<String, AttributeValue> toAccess(ClassifiedAccess item) {
        Map<String, AttributeValue> values = new HashMap<>();
        values.put("id", AttributeValue.fromS(item.id()));
        values.put("kind", AttributeValue.fromS(item.kind()));
        values.put("kindLabel", AttributeValue.fromS(item.kindLabel()));
        values.put("system", AttributeValue.fromS(item.system()));
        values.put("ageDays", AttributeValue.fromN(Integer.toString(item.ageDays())));
        if (item.lastUsedDays() != null) {
            values.put("lastUsedDays", AttributeValue.fromN(Integer.toString(item.lastUsedDays())));
        }
        values.put("owner", AttributeValue.fromS(item.owner()));
        values.put("action", AttributeValue.fromS(item.action()));
        values.put("reason", AttributeValue.fromS(item.reason()));
        values.put("needsHuman", AttributeValue.fromBool(item.needsHuman()));
        return values;
    }

    private ClassifiedAccess fromAccess(Map<String, AttributeValue> item) {
        Integer lastUsed = item.containsKey("lastUsedDays") ? Integer.valueOf(item.get("lastUsedDays").n()) : null;
        return new ClassifiedAccess(
                text(item, "id"),
                text(item, "kind"),
                text(item, "kindLabel"),
                text(item, "system"),
                Integer.parseInt(item.get("ageDays").n()),
                lastUsed,
                text(item, "owner"),
                text(item, "action"),
                text(item, "reason"),
                item.containsKey("needsHuman") && item.get("needsHuman").bool());
    }

    private Map<String, AttributeValue> toDecision(HumanDecision decision) {
        Map<String, AttributeValue> values = new HashMap<>();
        values.put("id", AttributeValue.fromS(DECISION_PREFIX + decision.accessId() + "#" + decision.choice()));
        values.put("accessId", AttributeValue.fromS(decision.accessId()));
        values.put("kind", AttributeValue.fromS(decision.kind()));
        values.put("ageDays", AttributeValue.fromN(Integer.toString(decision.ageDays())));
        values.put("hadOwner", AttributeValue.fromBool(decision.hadOwner()));
        values.put("choice", AttributeValue.fromS(decision.choice()));
        return values;
    }

    private HumanDecision fromDecision(Map<String, AttributeValue> item) {
        return new HumanDecision(
                text(item, "accessId"),
                text(item, "kind"),
                Integer.parseInt(item.get("ageDays").n()),
                item.containsKey("hadOwner") && item.get("hadOwner").bool(),
                text(item, "choice"));
    }

    private static int number(Map<String, AttributeValue> item, String key) {
        AttributeValue value = item.get(key);
        if (value == null || value.n() == null || value.n().isBlank()) {
            return 0;
        }
        return Integer.parseInt(value.n());
    }

    private static String text(Map<String, AttributeValue> item, String key) {
        AttributeValue value = item.get(key);
        return value == null || value.s() == null ? "" : value.s();
    }
}
