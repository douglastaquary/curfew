package curfew.scan;

/**
 * Conta o que uma chamada de modelo teria gasto. Neste corte a chamada não existe.
 */
public final class TokenLedger {

    public static final int TOKENS_PER_ITEM_IF_MODELED = 120;

    public TokenReport report(int itemCount) {
        int items = Math.max(0, itemCount);
        return new TokenReport(0, items * TOKENS_PER_ITEM_IF_MODELED, items);
    }
}
