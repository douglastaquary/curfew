package curfew.scan;

/**
 * O que a passagem da noite anotaria. Não carrega segredo e não fecha o acesso.
 */
public record NightlyNote(String id, String action, boolean asksHuman) {
}
