package curfew.scan;

/**
 * Gancho para uma decisão humana futura. Não guarda segredo e não fecha nada.
 */
public record HumanDecision(
        String accessId,
        String kind,
        int ageDays,
        boolean hadOwner,
        String choice) {
}
