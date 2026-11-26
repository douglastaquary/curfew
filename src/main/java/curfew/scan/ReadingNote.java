package curfew.scan;

import java.util.ArrayList;
import java.util.List;

/**
 * Uma leitura já terminada. Quantidades e linhas permitidas.
 * Não há campo para o valor do segredo.
 */
public record ReadingNote(
        String at,
        int items,
        int byRule,
        int modelCalls,
        int tokensAvoided,
        List<String> lines) {

    private static final int CAP = 40;

    public ReadingNote {
        lines = lines == null ? List.of() : List.copyOf(lines);
    }

    public ReadingNote kept() {
        List<String> safe = new ArrayList<>();
        for (String line : lines) {
            if (safe.size() == CAP) {
                break;
            }
            if (ReadingGuard.allows(line)) {
                safe.add(line);
            }
        }
        return new ReadingNote(at, items, byRule, modelCalls, tokensAvoided, safe);
    }
}
