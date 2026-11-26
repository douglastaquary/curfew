package curfew.scan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class ReadingGuardTest {

    @Test
    void secretMarkersStayOutOfTheReading() {
        ReadingNote note = new ReadingNote(
                "2026-11-26 09:18",
                1,
                1,
                0,
                120,
                List.of("GitHub · Token · aberto há 2 dias", "chave AKIAIOSFODNN7EXAMPLE", "ghp_exemplo"))
                .kept();

        assertEquals(1, note.lines().size());
        assertTrue(note.lines().getFirst().contains("Token"));
        assertFalse(note.lines().toString().contains("AKIA"));
        assertFalse(note.lines().toString().contains("ghp_"));
        assertEquals(0, note.modelCalls());
    }
}
