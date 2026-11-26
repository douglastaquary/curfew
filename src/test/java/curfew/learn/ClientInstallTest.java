package curfew.learn;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class ClientInstallTest {

    @Test
    void oneInstallPerClientAndNoSharedLogin() throws Exception {
        String decision = Files.readString(Path.of("docs/clientes.md"));
        String agents = Files.readString(Path.of("AGENTS.md"));
        String page = Files.readString(Path.of("src/main/resources/templates/Pages/contagem.html"));

        assertTrue(decision.contains("Uma instalação por cliente. Sem login."));
        assertTrue(decision.contains("Cinco clientes"));
        assertTrue(agents.contains("Uma instalação por cliente. Sem login."));
        assertTrue(page.contains("Uma instalação por cliente."));
        assertFalse(Files.exists(Path.of("src/main/java/curfew/web/Login.java")));
    }
}
