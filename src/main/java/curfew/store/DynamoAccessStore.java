package curfew.store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import curfew.scan.CloseTarget;
import curfew.scan.ClassifiedAccess;
import curfew.scan.HumanDecision;
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
            if (id.startsWith(DECISION_PREFIX)) {
                continue;
            }
            found.add(fromAccess(item));
        }
        return List.copyOf(found);
    }

    @Override
    public Optional<ClassifiedAccess> find(String id) {
        if (id == null || id.startsWith(DECISION_PREFIX)) {
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
        for (ClassifiedAccess item : items) {
            dynamo.putItem(PutItemRequest.builder().tableName(table).item(toAccess(item)).build());
        }
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

    private static String text(Map<String, AttributeValue> item, String key) {
        AttributeValue value = item.get(key);
        return value == null || value.s() == null ? "" : value.s();
    }
}
