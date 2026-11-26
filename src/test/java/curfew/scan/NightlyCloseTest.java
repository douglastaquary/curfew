package curfew.scan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class NightlyCloseTest {

    @Test
    void productionAsksAndNothingCloses() {
        List<ClassifiedAccess> items = new ScanEngine(1)
                .classifyAll(new MockAccessSource().list(), new ScanRules());
        List<NightlyNote> notes = new NightlyClose().note(items);

        NightlyNote token = notes.stream().filter(note -> note.id().equals("gh-token-18")).findFirst().orElseThrow();
        NightlyNote production = notes.stream().filter(note -> note.id().equals("aws-net-9")).findFirst().orElseThrow();

        assertEquals(6, notes.size());
        assertEquals("Revogar", token.action());
        assertFalse(token.asksHuman());
        assertTrue(production.asksHuman());
        assertEquals("Trancar e pedir um humano", production.action());
        assertFalse(notes.toString().contains("ghp_"));
        assertFalse(notes.toString().contains("AKIA"));
    }
}
