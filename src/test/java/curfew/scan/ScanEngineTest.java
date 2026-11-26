package curfew.scan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

class ScanEngineTest {

    @Test
    void classifiesEveryItemOnceUnderTheConcurrencyCap() {
        List<RawAccess> raw = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            raw.add(new RawAccess("id-" + i, "token", "Token", "GitHub", i, null, null, false, false));
        }
        AtomicInteger inFlight = new AtomicInteger();
        AtomicInteger maxInFlight = new AtomicInteger();
        ScanRules rules = new ScanRules();

        List<ClassifiedAccess> classified = new ScanEngine(4).classifyAll(raw, item -> {
            int now = inFlight.incrementAndGet();
            maxInFlight.accumulateAndGet(now, Math::max);
            try {
                Thread.sleep(20);
                return rules.classify(item);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(exception);
            } finally {
                inFlight.decrementAndGet();
            }
        });

        assertEquals(20, classified.size());
        assertEquals(20, classified.stream().map(ClassifiedAccess::id).distinct().count());
        assertTrue(maxInFlight.get() <= 4);
    }

    @Test
    void emptyInputDoesNotCallTheRule() {
        assertTrue(new ScanEngine(4).classifyAll(List.of(), new ScanRules()).isEmpty());
    }
}
