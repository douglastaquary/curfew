package curfew.scan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ScanRulesTest {

    private final ScanRules rules = new ScanRules();

    @Test
    void tokenWithoutOwnerIsRevokedByRule() {
        ClassifiedAccess access = rules.classify(
                new RawAccess("gh-token-18", "token", "Token", "GitHub", 18, null, null, false, false));

        assertEquals("Revogar", access.action());
        assertEquals("Ninguém", access.owner());
        assertTrue(access.reason().contains("Ninguém é dono"));
        assertTrue(access.reason().contains("Nada foi fechado"));
        assertFalse(access.needsHuman());
    }

    @Test
    void wideScopeIsReduced() {
        ClassifiedAccess access = rules.classify(new RawAccess(
                "oauth-admin-27", "permission", "Permissão", "App OAuth", 27, 2, "Instalado por bot", true, false));

        assertEquals("Reduzir", access.action());
        assertTrue(access.reason().contains("escopo é mais largo"));
    }

    @Test
    void productionResourceAsksForAHuman() {
        ClassifiedAccess access = rules.classify(
                new RawAccess("aws-net-9", "resource", "Recurso", "AWS", 9, 9, " ", false, true));

        assertEquals("Trancar e pedir um humano", access.action());
        assertTrue(access.needsHuman());
        assertEquals("Ninguém", access.owner());
    }

    @Test
    void secretInLogIsRemovedFromTextOnly() {
        ClassifiedAccess access = rules.classify(
                new RawAccess("log-secret-4", "secret", "Senha no log", "Sessão do agente", 4, 4, "Log", false, false));

        assertEquals("Apagar do texto", access.action());
    }
}
