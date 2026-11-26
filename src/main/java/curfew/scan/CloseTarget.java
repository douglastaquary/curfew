package curfew.scan;

import java.util.regex.Pattern;

/**
 * Destino de um acesso no GitHub. Não aceita valor de segredo nem caminho livre.
 */
public final class CloseTarget {

    private static final Pattern SAFE = Pattern.compile(
            "^(hooks|keys|secrets)/[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+$");

    private CloseTarget() {
    }

    public static boolean safe(String target) {
        return target != null && SAFE.matcher(target).matches();
    }
}
