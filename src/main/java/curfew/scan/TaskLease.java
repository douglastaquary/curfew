package curfew.scan;

import java.time.Instant;

/**
 * Validade de uma tarefa. Não há campo para token, chave ou senha.
 */
public record TaskLease(String id, String task, String system, int hours, Instant issuedAt, Instant expiresAt) {

    public boolean open(Instant now) {
        return now.isBefore(expiresAt);
    }
}
