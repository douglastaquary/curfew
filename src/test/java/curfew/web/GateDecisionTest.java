package curfew.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class GateDecisionTest {

    private static final String SECRET = "porta-de-teste-1234";

    @Test
    void prodWithoutASecretStaysLocked() throws Exception {
        assertEquals(GateDecision.Outcome.LOCKED, GateDecision.decide("prod", "", "", ""));
        assertEquals(GateDecision.Outcome.LOCKED, GateDecision.decide("prod", "   ", SECRET, ""));
        assertEquals(GateDecision.Outcome.DOOR, GateDecision.decide("prod", SECRET, "", ""));
        assertEquals(GateDecision.Outcome.DOOR, GateDecision.decide("prod", SECRET, "outra-porta-123456", ""));
        assertEquals(GateDecision.Outcome.OPEN, GateDecision.decide("prod", SECRET, SECRET, ""));
        assertEquals(
                GateDecision.Outcome.OPEN,
                GateDecision.decide("prod", SECRET, "", GateDecision.hash(SECRET)));
        assertEquals(GateDecision.Outcome.DOOR, GateDecision.decide("prod", SECRET, "", SECRET));
        assertEquals(GateDecision.Outcome.OPEN, GateDecision.decide("dev", "", "", ""));
        assertEquals(GateDecision.Outcome.OPEN, GateDecision.decide("test", "", "", ""));

        assertTrue(GateDecision.bearerMatches(SECRET, SECRET));
        assertFalse(GateDecision.bearerMatches(SECRET, ""));
        assertFalse(GateDecision.cookieMatches(SECRET, SECRET));
        assertTrue(GateDecision.cookieMatches(SECRET, GateDecision.hash(SECRET)));
        assertFalse(GateDecision.hash(SECRET).contains(SECRET));

        String filter = Files.readString(Path.of("src/main/java/curfew/web/ProdGateFilter.java"));
        assertTrue(filter.contains("@IfBuildProfile(\"prod\")"));
    }
}
