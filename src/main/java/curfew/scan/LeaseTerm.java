package curfew.scan;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

/**
 * A chave da tarefa acaba em menos de 24 horas. 24 horas ou mais não é entregue.
 */
public final class LeaseTerm {

    public static final int MAX_HOURS = 23;

    private LeaseTerm() {
    }

    public static Optional<TaskLease> issue(String id, String task, String system, int hours, Instant now) {
        if (id == null || id.isBlank() || hours < 1 || hours > MAX_HOURS) {
            return Optional.empty();
        }
        if (!"GitHub".equals(system) && !"AWS".equals(system)) {
            return Optional.empty();
        }
        if (!safeTask(task)) {
            return Optional.empty();
        }
        Instant expires = now.plus(Duration.ofHours(hours));
        if (!expires.isBefore(now.plus(Duration.ofHours(24)))) {
            return Optional.empty();
        }
        return Optional.of(new TaskLease(id, task.trim(), system, hours, now, expires));
    }

    static boolean safeTask(String task) {
        if (task == null) {
            return false;
        }
        String trimmed = task.trim();
        if (trimmed.isEmpty() || trimmed.length() > 48 || !trimmed.matches("[\\p{L}\\p{N} ._-]+")) {
            return false;
        }
        String lowered = trimmed.toLowerCase(Locale.ROOT);
        return !lowered.contains("ghp_") && !lowered.contains("github_pat_") && !lowered.contains("akia");
    }
}
