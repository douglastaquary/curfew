package curfew.scan;

import java.time.Instant;

/**
 * Sessão entregue uma vez. O texto desta classe não leva o segredo.
 */
public final class Minted {

    private final String accessKeyId;
    private final String secretAccessKey;
    private final String sessionToken;
    private final Instant expires;

    public Minted(String accessKeyId, String secretAccessKey, String sessionToken, Instant expires) {
        this.accessKeyId = accessKeyId == null ? "" : accessKeyId;
        this.secretAccessKey = secretAccessKey == null ? "" : secretAccessKey;
        this.sessionToken = sessionToken == null ? "" : sessionToken;
        this.expires = expires;
    }

    public String accessKeyId() {
        return accessKeyId;
    }

    public String secretAccessKey() {
        return secretAccessKey;
    }

    public String sessionToken() {
        return sessionToken;
    }

    public Instant expires() {
        return expires;
    }

    public String tail() {
        if (!accessKeyId.matches("[A-Z0-9]{16,128}")) {
            return "";
        }
        return accessKeyId.substring(accessKeyId.length() - 4);
    }

    @Override
    public String toString() {
        return "Minted{finais=" + tail() + ", expires=" + expires + "}";
    }
}
