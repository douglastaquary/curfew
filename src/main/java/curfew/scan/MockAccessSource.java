package curfew.scan;

import java.util.List;

/**
 * Fila fictícia de uma empresa que não existe. Serve para a tela e para o teste.
 */
public final class MockAccessSource implements AccessSource {

    public List<RawAccess> list() {
        return List.of(
                new RawAccess("gh-token-18", "token", "Token", "GitHub", 18, null, null, false, false),
                new RawAccess("gh-hook-6", "webhook", "Webhook", "GitHub", 6, 6, null, false, false),
                new RawAccess("aws-key-11", "key", "Chave", "AWS", 11, 11, "Conta de serviço", false, false),
                new RawAccess("oauth-admin-27", "permission", "Permissão", "App OAuth", 27, 2, "Instalado por bot", true, false),
                new RawAccess("log-secret-4", "secret", "Senha no log", "Sessão do agente", 4, 4, "Log", false, false),
                new RawAccess("aws-net-9", "resource", "Recurso", "AWS", 9, 9, null, false, true));
    }
}
