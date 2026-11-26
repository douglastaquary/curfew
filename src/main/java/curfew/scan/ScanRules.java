package curfew.scan;

public final class ScanRules {

    public ClassifiedAccess classify(RawAccess raw) {
        String action;
        boolean needsHuman;
        if (raw.maybeProduction()) {
            action = "Trancar e pedir um humano";
            needsHuman = true;
        } else if (raw.wideScope()) {
            action = "Reduzir";
            needsHuman = false;
        } else {
            action = switch (raw.kind()) {
                case "token" -> "Revogar";
                case "key" -> "Expirar";
                case "webhook" -> "Desligar";
                case "permission" -> "Reduzir";
                case "secret" -> "Apagar do texto";
                default -> "Trancar e pedir um humano";
            };
            needsHuman = "Trancar e pedir um humano".equals(action);
        }

        String owner = raw.owner() == null || raw.owner().isBlank() ? "Ninguém" : raw.owner();
        return new ClassifiedAccess(
                raw.id(),
                raw.kind(),
                raw.kindLabel(),
                raw.system(),
                raw.ageDays(),
                raw.lastUsedDays(),
                owner,
                action,
                reason(raw, owner),
                needsHuman);
    }

    private static String reason(RawAccess raw, String owner) {
        StringBuilder text = new StringBuilder();
        if ("Ninguém".equals(owner)) {
            text.append("Ninguém é dono. ");
        } else {
            text.append("Dono registrado: ").append(owner).append(". ");
        }
        text.append("Aberto há ").append(raw.ageDays()).append(" dias. ");
        if (raw.lastUsedDays() == null) {
            text.append("Sem uso registrado depois da tarefa. ");
        } else {
            text.append("Último uso há ").append(raw.lastUsedDays()).append(" dias. ");
        }
        if (raw.wideScope()) {
            text.append("O escopo é mais largo do que a tarefa pedia. ");
        }
        if (raw.maybeProduction()) {
            text.append("Pode ser de produção, então a regra pede uma pessoa. ");
        }
        text.append("Nada foi fechado.");
        return text.toString();
    }
}
