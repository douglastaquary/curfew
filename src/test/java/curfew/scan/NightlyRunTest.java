package curfew.scan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class NightlyRunTest {

    @Test
    void disarmedPassageLeavesProductionAndSkipsTheAccount() {
        List<ClassifiedAccess> items = new ScanEngine(1)
                .classifyAll(new MockAccessSource().list(), new ScanRules());
        NightlyRun.Outcome outcome = new NightlyRun().apply(items, Map.of(), false, target -> {
            throw new AssertionError("não devia chamar");
        });

        assertEquals(5, outcome.left());
        assertEquals(1, outcome.asked());
        assertFalse(outcome.calledAccount());
        assertEquals("aws-net-9", outcome.staying().getFirst().id());
        assertTrue(outcome.decisions().stream().anyMatch(decision -> "anotado".equals(decision.choice())));
        assertFalse(outcome.toString().contains("ghp_"));
    }
}
