package curfew.scan;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Tira da fila o que não pede humano. A conta só é chamada quando a passagem está armada.
 */
public final class NightlyRun {

    @FunctionalInterface
    public interface Remote {
        boolean remove(String target);
    }

    public Outcome apply(List<ClassifiedAccess> items, Map<String, String> targets, boolean armed, Remote remote) {
        List<ClassifiedAccess> staying = new ArrayList<>();
        List<HumanDecision> decisions = new ArrayList<>();
        int left = 0;
        int asked = 0;
        boolean calledAccount = false;
        for (ClassifiedAccess item : items) {
            if (item.needsHuman()) {
                staying.add(item);
                decisions.add(decision(item, "perguntado"));
                asked++;
                continue;
            }
            String target = targets == null ? "" : targets.getOrDefault(item.id(), "");
            if (armed && CloseTarget.safe(target) && remote != null) {
                calledAccount = true;
                if (remote.remove(target)) {
                    decisions.add(decision(item, "fechado"));
                    left++;
                    continue;
                }
                staying.add(item);
                decisions.add(decision(item, "sem-destino"));
                continue;
            }
            if (armed) {
                staying.add(item);
                decisions.add(decision(item, "sem-destino"));
                continue;
            }
            decisions.add(decision(item, "anotado"));
            left++;
        }
        return new Outcome(List.copyOf(staying), List.copyOf(decisions), left, asked, calledAccount);
    }

    private static HumanDecision decision(ClassifiedAccess item, String choice) {
        boolean hadOwner = item.owner() != null && !item.owner().isBlank() && !"Ninguém".equals(item.owner());
        return new HumanDecision(item.id(), item.kind(), item.ageDays(), hadOwner, choice);
    }

    public record Outcome(
            List<ClassifiedAccess> staying,
            List<HumanDecision> decisions,
            int left,
            int asked,
            boolean calledAccount) {
    }
}
