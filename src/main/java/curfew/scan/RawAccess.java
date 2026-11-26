package curfew.scan;

/**
 * Leitura bruta de um acesso. Não carrega valor de token, chave ou senha.
 */
public record RawAccess(
        String id,
        String kind,
        String kindLabel,
        String system,
        int ageDays,
        Integer lastUsedDays,
        String owner,
        boolean wideScope,
        boolean maybeProduction,
        String target) {

    public RawAccess(
            String id,
            String kind,
            String kindLabel,
            String system,
            int ageDays,
            Integer lastUsedDays,
            String owner,
            boolean wideScope,
            boolean maybeProduction) {
        this(id, kind, kindLabel, system, ageDays, lastUsedDays, owner, wideScope, maybeProduction, null);
    }
}
