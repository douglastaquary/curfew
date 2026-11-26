package curfew.scan;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ScanLogTest {

    @Test
    void lineNamesTheAccessAndSkipsAnySecret() {
        String line = ScanLog.line(
                new ClassifiedAccess("gh-token-18", "token", "Token", "GitHub", 18, null, "Ninguém", "Revogar",
                        "Nada foi fechado.", false),
                "21:41:02");

        assertTrue(line.contains("21:41:02"));
        assertTrue(line.contains("GitHub · Token"));
        assertTrue(line.contains("18 dias"));
        assertTrue(line.contains("Revogar"));
        assertFalse(line.contains("ghp_"));
    }
}
