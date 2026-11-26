package curfew.scan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import curfew.read.SessionJson;

/**
 * Percorre o limite inteiro que os testes de exemplo não cobrem.
 * Uma chave aceita acaba antes de 24 horas. O segredo não entra na fila.
 * Quem pede humano permanece na passagem desarmada.
 */
class LeaseAndNightPropertyTest {

    private static final String SECRET = "segredo-de-teste";
    private static final String SESSION = "sessao-de-teste";
    private static final Instant NOW = Instant.parse("2026-11-26T03:00:00Z");

    @Test
    void everyLeaseEndsInsideADayAndTheQueueHidesTheSecret() {
        String[] tasks = {
                "Abrir o pull request",
                "Ler a fila",
                "tarefa_ok",
                "Tarefa-1",
                "",
                " ",
                "a".repeat(49),
                "ghp_teste",
                "AKIATESTE"
        };
        String[] systems = {"GitHub", "AWS", "Slack", ""};
        LeaseDelivery delivery = new LeaseDelivery();
        SessionMint mint = hours -> Optional.of(new Minted(
                "TMP123456789WXYZ", SECRET, SESSION, NOW.plus(Duration.ofHours(1))));

        for (int hours = -1; hours <= 48; hours++) {
            for (String task : tasks) {
                for (String system : systems) {
                    for (boolean armed : new boolean[] {false, true}) {
                        LeaseBook book = new LeaseBook();
                        book.reset(NOW);
                        int before = book.size();
                        LeaseDelivery.Result result = delivery.deliver(book, mint, armed, task, system, hours, NOW);
                        String queue = book.views(NOW).toString();
                        assertFalse(queue.contains(SECRET), queue);
                        assertFalse(queue.contains(SESSION), queue);
                        assertFalse(result.toString().contains(SECRET));
                        if (result.lease() != null) {
                            assertFalse(result.lease().toString().contains(SECRET));
                            assertTrue(result.lease().expiresAt().isBefore(NOW.plus(Duration.ofHours(24))));
                            assertTrue(result.lease().hours() >= 1 && result.lease().hours() <= 23);
                        }
                        if (hours < 1 || hours >= 24) {
                            assertEquals(before, book.size());
                            assertTrue(result.minted() == null);
                        }
                        if (result.minted() != null) {
                            String once = SessionJson.body(result.lease(), result.minted());
                            assertTrue(once.contains(SECRET));
                            assertFalse(queue.contains(SECRET));
                        }
                    }
                }
            }
        }
    }

    @Test
    void everyDisarmedSubsetKeepsWhoeverAsksForAHuman() {
        List<ClassifiedAccess> all = new ScanEngine(1)
                .classifyAll(new MockAccessSource().list(), new ScanRules());
        NightlyRun run = new NightlyRun();
        int masks = 1 << all.size();
        for (int mask = 0; mask < masks; mask++) {
            List<ClassifiedAccess> subset = new ArrayList<>();
            for (int index = 0; index < all.size(); index++) {
                if ((mask & (1 << index)) != 0) {
                    subset.add(all.get(index));
                }
            }
            NightlyRun.Outcome outcome = run.apply(subset, Map.of(), false, target -> {
                throw new AssertionError("não devia chamar");
            });
            assertFalse(outcome.calledAccount());
            assertFalse(outcome.toString().contains("ghp_"));
            assertFalse(outcome.toString().contains("AKIA"));
            for (ClassifiedAccess item : subset) {
                boolean stayed = outcome.staying().stream().anyMatch(row -> row.id().equals(item.id()));
                if (item.needsHuman()) {
                    assertTrue(stayed, item.id());
                } else {
                    assertFalse(stayed, item.id());
                }
            }
        }
    }
}
