package curfew.scan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import curfew.read.SessionJson;

class LeaseDeliveryTest {

    @Test
    void armedSessionStaysOutOfTheQueue() {
        Instant now = Instant.parse("2026-11-26T02:40:00Z");
        LeaseBook book = new LeaseBook();
        book.reset(now);
        AtomicInteger calls = new AtomicInteger();
        SessionMint mint = hours -> {
            calls.incrementAndGet();
            return Optional.of(new Minted(
                    "TMP123456789WXYZ",
                    "segredo-de-teste",
                    "sessao-de-teste",
                    now.plus(Duration.ofHours(hours))));
        };
        LeaseDelivery delivery = new LeaseDelivery();

        LeaseDelivery.Result local = delivery.deliver(book, mint, false, "Ler a fila", "AWS", 23, now);
        assertEquals(0, calls.get());
        assertFalse(local.calledAccount());

        LeaseDelivery.Result once = delivery.deliver(book, mint, true, "Ler a fila", "AWS", 23, now);
        assertEquals(1, calls.get());
        assertTrue(once.calledAccount());
        String queue = book.views(now).toString();
        String json = SessionJson.body(once.lease(), once.minted());
        assertTrue(json.contains("segredo-de-teste"));
        assertTrue(json.contains("\"conta\":\"sim\""));
        assertTrue(queue.contains("finais WXYZ"));
        assertFalse(queue.contains("segredo-de-teste"));
        assertFalse(queue.contains("sessao-de-teste"));
        assertFalse(once.toString().contains("segredo-de-teste"));
        assertFalse(once.minted().toString().contains("segredo-de-teste"));

        SessionMint boom = hours -> {
            throw new IllegalStateException("segredo-de-teste");
        };
        LeaseDelivery.Result failed = delivery.deliver(book, boom, true, "Outra tarefa", "AWS", 1, now);
        assertEquals(400, failed.status());
        assertFalse(failed.message().contains("segredo-de-teste"));
        assertFalse(failed.toString().contains("segredo-de-teste"));

        SessionMint unused = hours -> {
            throw new AssertionError("não devia chamar");
        };
        LeaseDelivery.Result github = delivery.deliver(book, unused, true, "Outra tarefa", "GitHub", 1, now);
        assertEquals(400, github.status());
        assertTrue(github.message().contains("sessão AWS"));
    }
}
