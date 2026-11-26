package curfew.web;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * A porta de produção. Dev e teste ficam abertos para a prova local.
 * O segredo não entra em mensagem de erro.
 */
public final class GateDecision {

    public static final String COOKIE = "curfew_porta";

    public enum Outcome {
        OPEN,
        DOOR,
        LOCKED
    }

    private GateDecision() {
    }

    public static Outcome decide(String profile, String configured, String bearer, String cookie) {
        if (!"prod".equals(profile)) {
            return Outcome.OPEN;
        }
        if (configured == null || configured.isBlank()) {
            return Outcome.LOCKED;
        }
        if (cookieMatches(configured, cookie) || bearerMatches(configured, bearer)) {
            return Outcome.OPEN;
        }
        return Outcome.DOOR;
    }

    public static boolean bearerMatches(String configured, String presented) {
        if (configured == null || configured.isBlank() || presented == null || presented.isBlank()) {
            return false;
        }
        return MessageDigest.isEqual(digest(configured), digest(presented));
    }

    public static boolean cookieMatches(String configured, String cookie) {
        if (configured == null || configured.isBlank() || cookie == null || cookie.isBlank()) {
            return false;
        }
        byte[] expected = hash(configured).getBytes(StandardCharsets.UTF_8);
        byte[] actual = cookie.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expected, actual);
    }

    public static String hash(String configured) {
        return HexFormat.of().formatHex(digest(configured));
    }

    private static byte[] digest(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IllegalStateException("SHA-256 indisponível");
        }
    }
}
