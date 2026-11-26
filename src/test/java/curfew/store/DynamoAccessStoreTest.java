package curfew.store;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

class DynamoAccessStoreTest {

    @Test
    void replaceDropsStaleAccessAndKeepsReadings() {
        List<String> drop = DynamoAccessStore.idsToDrop(
                List.of("gh-token-18", "decision#gh-token-18#anotado", "reading#1", "aws-key-11", ""),
                Set.of("aws-key-11"));

        assertEquals(List.of("gh-token-18"), drop);
    }
}
