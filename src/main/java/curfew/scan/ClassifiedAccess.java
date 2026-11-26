package curfew.scan;

/**
 * Acesso já classificado por regra. O motivo é texto, não uma nota de modelo.
 */
public record ClassifiedAccess(
        String id,
        String kind,
        String kindLabel,
        String system,
        int ageDays,
        Integer lastUsedDays,
        String owner,
        String action,
        String reason,
        boolean needsHuman) {
}
