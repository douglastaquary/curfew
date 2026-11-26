package curfew.scan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;

import org.junit.jupiter.api.Test;

class TokenLedgerTest {

    @Test
    void rulesAvoidAFixedBudgetAndNeverCallAModel() {
        TokenReport report = new TokenLedger().report(6);

        assertEquals(0, report.modelCalls());
        assertEquals(6, report.itemsClassifiedByRule());
        assertEquals(6 * TokenLedger.TOKENS_PER_ITEM_IF_MODELED, report.tokensAvoided());
    }

    @Test
    void recordsHaveNoSecretField() {
        List<Class<?>> types = List.of(
                RawAccess.class, ClassifiedAccess.class, HumanDecision.class, ReadingNote.class);
        for (Class<?> type : types) {
            for (var component : type.getRecordComponents()) {
                String name = component.getName().toLowerCase();
                assertFalse(name.contains("secret"));
                assertFalse(name.contains("password"));
                assertFalse(name.contains("tokenvalue"));
            }
        }
    }
}
