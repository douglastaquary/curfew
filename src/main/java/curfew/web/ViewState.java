package curfew.web;

public enum ViewState {
    DATA,
    EMPTY,
    ERROR,
    LOADING;

    public static ViewState from(String raw) {
        if (raw == null || raw.isBlank()) {
            return DATA;
        }
        return switch (raw) {
            case "vazio" -> EMPTY;
            case "erro" -> ERROR;
            case "carregando" -> LOADING;
            default -> DATA;
        };
    }
}
