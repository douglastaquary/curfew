package curfew.learn;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class LearningLedgerTest {

    private static final Pattern LESSON = Pattern.compile("`([A-Z][A-Za-z0-9_]*)\\.([A-Za-z][A-Za-z0-9_]*)`");

    @Test
    void everyLessonNamesARealTest() throws IOException {
        String ledger = Files.readString(Path.of("docs/aprendizado.md"));
        Matcher matcher = LESSON.matcher(ledger);
        List<String> missing = new ArrayList<>();
        int lessons = 0;
        while (matcher.find()) {
            lessons++;
            String type = matcher.group(1);
            String method = matcher.group(2);
            if (!methodExists(type, method)) {
                missing.add(type + "." + method);
            }
        }
        assertTrue(lessons > 0, "o aprendizado precisa citar ao menos um teste");
        assertTrue(missing.isEmpty(), "teste citado e inexistente: " + String.join(", ", missing));
    }

    private static boolean methodExists(String type, String method) throws IOException {
        Path root = Path.of("src/test/java");
        try (Stream<Path> files = Files.walk(root)) {
            return files
                    .filter(path -> path.getFileName().toString().equals(type + ".java"))
                    .findFirst()
                    .map(path -> read(path).contains("void " + method + "("))
                    .orElse(false);
        }
    }

    private static String read(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException exception) {
            return "";
        }
    }
}
