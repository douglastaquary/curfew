package curfew.scan;

/**
 * A linha da contagem é o texto já mostrado na varredura.
 * Marcador de segredo não entra no registro.
 */
public final class ReadingGuard {

    private static final String[] MARKERS = {
            "ghp_",
            "github_pat_",
            "AKIA",
            "aws_secret",
            "secretAccessKey",
            "BEGIN PRIVATE"
    };

    private ReadingGuard() {
    }

    public static boolean allows(String line) {
        if (line == null || line.isBlank() || line.length() > 200) {
            return false;
        }
        for (String marker : MARKERS) {
            if (line.contains(marker)) {
                return false;
            }
        }
        return true;
    }
}
