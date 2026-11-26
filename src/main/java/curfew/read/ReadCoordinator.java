package curfew.read;

import java.time.Instant;
import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import curfew.scan.AccessSource;
import curfew.scan.MockAccessSource;
import curfew.scan.RawAccess;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Sem variável, a fila continua o exemplo. Com variável, a varredura lê de verdade.
 * O valor fica no ambiente. Não entra em propriedade, log ou fila.
 */
@ApplicationScoped
public class ReadCoordinator implements AccessSource {

    static final String GITHUB = "CURFEW_GITHUB_TOKEN";
    static final String AWS_ID = "CURFEW_AWS_ACCESS_KEY_ID";
    static final String AWS_SECRET = "CURFEW_AWS_SECRET_ACCESS_KEY";

    private final String mode;

    public ReadCoordinator(@ConfigProperty(name = "curfew.read.mode") String mode) {
        this.mode = mode;
    }

    @Override
    public List<RawAccess> list() {
        if (!live()) {
            return new MockAccessSource().list();
        }
        return new LiveAccessSource(env(GITHUB), env(AWS_ID), env(AWS_SECRET), Instant.now()).list();
    }

    public boolean githubLinked() {
        return live() && !env(GITHUB).isBlank();
    }

    public boolean awsLinked() {
        return live() && !env(AWS_ID).isBlank() && !env(AWS_SECRET).isBlank();
    }

    public String githubLabel() {
        return githubLinked()
                ? "Leitura ligada. Entram webhook, nome de segredo e chave de deploy."
                : "Leitura ainda não ligada";
    }

    public String awsLabel() {
        return awsLinked()
                ? "Leitura ligada. Entram chaves ativas, com os quatro últimos caracteres."
                : "Leitura ainda não ligada";
    }

    private boolean live() {
        if ("mock".equals(mode)) {
            return false;
        }
        return !env(GITHUB).isBlank() || (!env(AWS_ID).isBlank() && !env(AWS_SECRET).isBlank());
    }

    private static String env(String name) {
        String value = System.getenv(name);
        return value == null ? "" : value;
    }
}
