package curfew.read;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class GithubCloseTest {

    @Test
    void tokenStaysOutOfTheUrl() {
        GithubClose close = new GithubClose();
        String uri = close.uri("hooks/acme/app/9").orElseThrow().toString();
        assertEquals("https://api.github.com/repos/acme/app/hooks/9", uri);

        List<String> seen = new ArrayList<>();
        assertTrue(close.delete((target, token) -> {
            seen.add(target.toString());
            return 204;
        }, "token-de-teste", "hooks/acme/app/9"));
        assertEquals(uri, seen.getFirst());
        assertFalse(seen.getFirst().contains("token-de-teste"));

        assertFalse(close.delete((target, token) -> {
            throw new AssertionError("não devia chamar");
        }, "app", "hooks/acme/app/9"));
        assertTrue(close.uri("hooks/acme/app/../../admin").isEmpty());
    }
}
