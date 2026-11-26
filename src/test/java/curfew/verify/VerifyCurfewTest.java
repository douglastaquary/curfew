package curfew.verify;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

class VerifyCurfewTest {

    private static final List<String> HEADINGS = List.of(
            "## Sub-features",
            "## How to get to it (user POV)",
            "## Driving it with verify-curfew",
            "## Gotchas");

    @Test
    void featureMapKeepsTheFourScreens() throws Exception {
        Path features = Path.of(".cursor/skills/verify-curfew/features");
        String index = Files.readString(features.resolve("README.md"));
        for (String name : List.of("fila.md", "detalhe.md", "varredura.md", "conexoes.md")) {
            assertTrue(index.contains("./" + name));
            String body = Files.readString(features.resolve(name));
            int cursor = -1;
            for (String heading : HEADINGS) {
                int found = body.indexOf(heading);
                assertTrue(found > cursor, name + " " + heading);
                cursor = found;
            }
        }
    }

    @Test
    void dryRunDoesNotStartTheAppOrPrintASecret() throws Exception {
        Process process = new ProcessBuilder(
                "python3",
                ".cursor/skills/verify-curfew/verify-curfew.py",
                "up",
                "--dry-run")
                .start();
        String output = new String(process.getInputStream().readAllBytes());
        assertEquals(0, process.waitFor());
        assertTrue(output.contains("quarkus:dev"));
        assertTrue(output.contains("\"dryRun\": true"));
        assertFalse(output.contains("ghp_"));
        assertFalse(output.contains("AKIA"));
        assertFalse(Files.exists(Path.of(".cursor/skills/verify-curfew/run/state.json")));
    }
}
