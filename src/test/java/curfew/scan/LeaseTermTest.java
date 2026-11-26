package curfew.scan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;

class LeaseTermTest {

    @Test
    void rejectsAFullDayAndKeepsNoSecret() {
        Instant now = Instant.parse("2026-11-26T02:40:00Z");
        TaskLease lease = LeaseTerm.issue("lease-abrir", "Abrir o pull request", "GitHub", 23, now).orElseThrow();

        assertEquals(23, lease.hours());
        assertTrue(lease.open(lease.expiresAt().minusSeconds(1)));
        assertFalse(lease.open(lease.expiresAt()));
        assertTrue(lease.expiresAt().isBefore(now.plus(Duration.ofHours(24))));
        assertTrue(LeaseTerm.issue("lease-dia", "Abrir o pull request", "GitHub", 24, now).isEmpty());
        assertTrue(LeaseTerm.issue("lease-segredo", "ghp_teste", "AWS", 1, now).isEmpty());
        assertFalse(lease.toString().contains("ghp_"));
        assertFalse(lease.toString().contains("AKIA"));
    }
}
