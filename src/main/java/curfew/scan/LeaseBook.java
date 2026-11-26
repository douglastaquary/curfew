package curfew.scan;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Chaves desta execução. Começa vazia. 24 horas ou mais não entra.
 */
@ApplicationScoped
public class LeaseBook {

    static final int CAP = 20;
    private static final ZoneId BRASILIA = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("dd/MM HH:mm").withZone(BRASILIA);

    private final List<TaskLease> leases = new CopyOnWriteArrayList<>();

    public LeaseBook() {
        reset(Instant.now());
    }

    public void reset(Instant now) {
        leases.clear();
    }

    public Optional<TaskLease> issue(String task, String system, int hours, Instant now) {
        return issue(task, system, hours, now, "");
    }

    public Optional<TaskLease> issue(String task, String system, int hours, Instant now, String tail) {
        if (leases.size() >= CAP) {
            return Optional.empty();
        }
        String id = uniqueId(slug(task));
        Optional<TaskLease> lease = LeaseTerm.issue(id, task, system, hours, now, tail);
        lease.ifPresent(leases::add);
        return lease;
    }

    public List<LeaseView> views(Instant now) {
        List<LeaseView> views = new ArrayList<>();
        for (TaskLease lease : leases) {
            String when = CLOCK.format(lease.expiresAt());
            String until = lease.open(now)
                    ? lease.system() + " · " + lease.hours() + " horas · vale até " + when
                    : lease.system() + " · acabou às " + when;
            if (!lease.tail().isBlank()) {
                until = until + " · finais " + lease.tail();
            }
            views.add(new LeaseView(lease.task(), until));
        }
        return List.copyOf(views);
    }

    public int size() {
        return leases.size();
    }

    private String uniqueId(String slug) {
        String id = "lease-" + slug;
        int suffix = 2;
        while (taken(id)) {
            id = "lease-" + slug + "-" + suffix;
            suffix++;
        }
        return id;
    }

    private boolean taken(String id) {
        return leases.stream().anyMatch(lease -> lease.id().equals(id));
    }

    private static String slug(String task) {
        StringBuilder out = new StringBuilder();
        String source = task == null ? "" : task.toLowerCase(Locale.ROOT);
        for (int i = 0; i < source.length(); i++) {
            char character = source.charAt(i);
            if ((character >= 'a' && character <= 'z') || (character >= '0' && character <= '9')) {
                out.append(character);
            } else if (out.length() > 0 && out.charAt(out.length() - 1) != '-') {
                out.append('-');
            }
        }
        while (out.length() > 0 && out.charAt(out.length() - 1) == '-') {
            out.setLength(out.length() - 1);
        }
        if (out.length() == 0) {
            return "tarefa";
        }
        return out.length() > 32 ? out.substring(0, 32) : out.toString();
    }
}
