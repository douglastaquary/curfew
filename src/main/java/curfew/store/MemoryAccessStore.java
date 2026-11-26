package curfew.store;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import curfew.scan.CloseTarget;
import curfew.scan.ClassifiedAccess;
import curfew.scan.HumanDecision;
import curfew.scan.ReadingNote;
import io.quarkus.arc.profile.UnlessBuildProfile;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@UnlessBuildProfile("prod")
public class MemoryAccessStore implements AccessStore {

    private final List<ClassifiedAccess> items = new CopyOnWriteArrayList<>();
    private final List<HumanDecision> decisions = new CopyOnWriteArrayList<>();
    private final List<ReadingNote> readings = new CopyOnWriteArrayList<>();
    private final ConcurrentHashMap<String, String> targets = new ConcurrentHashMap<>();

    @Override
    public List<ClassifiedAccess> list() {
        return List.copyOf(items);
    }

    @Override
    public Optional<ClassifiedAccess> find(String id) {
        return items.stream().filter(item -> item.id().equals(id)).findFirst();
    }

    @Override
    public void replace(List<ClassifiedAccess> next) {
        items.clear();
        items.addAll(next);
    }

    @Override
    public void remember(HumanDecision decision) {
        decisions.add(decision);
    }

    @Override
    public List<HumanDecision> decisions() {
        return List.copyOf(decisions);
    }

    @Override
    public void forgetDecisions() {
        decisions.clear();
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
        readings.add(note.kept());
    }

    @Override
    public List<ReadingNote> readings() {
        return List.copyOf(readings);
    }
}
