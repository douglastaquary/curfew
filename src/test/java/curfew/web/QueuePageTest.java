package curfew.web;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import curfew.scan.LeaseBook;
import curfew.store.AccessStore;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
class QueuePageTest {

    @Inject
    AccessStore store;

    @Inject
    LeaseBook book;

    @BeforeEach
    void emptyQueue() {
        store.replace(List.of());
        store.forgetDecisions();
        book.reset(Instant.now());
    }

    @Test
    void queueStartsEmptyWithNoSecret() {
        String html = given().when().get("/").then().statusCode(200).extract().asString();

        assertTrue(html.contains("Sem isso a fila fica vazia"));
        assertTrue(html.contains("Conexões"));
        assertFalse(html.contains("gh-token-18"));
        assertFalse(html.contains("Abrir o pull request"));
        assertFalse(html.contains("ghp_"));
        assertFalse(html.contains("AKIA"));
    }

    @Test
    void emptyLoadingAndErrorAreExplicit() {
        assertTrue(body("/?estado=vazio").contains("Nenhum acesso esquecido"));
        assertTrue(body("/?estado=carregando").contains("Lendo GitHub e AWS"));
        assertTrue(body("/?estado=erro").contains("Nada foi fechado"));
    }

    @Test
    void detailScanAndConnectionsRender() {
        store.replace(new curfew.scan.ScanEngine(4).classifyAll(
                new curfew.scan.MockAccessSource().list(), new curfew.scan.ScanRules()));
        assertTrue(body("/acessos/gh-token-18").contains("Revogar"));
        given().when().get("/acessos/nao-existe").then().statusCode(404);
        String scan = body("/varredura");
        assertTrue(scan.contains("Acompanhamento"));
        assertTrue(scan.contains("Passagem da noite"));
        assertTrue(scan.contains("gh-token-18"));
        assertTrue(scan.contains("Trancar e pedir um humano"));
        assertTrue(scan.contains("id=\"scan-log\""));
        assertTrue(scan.contains(">0<"));
        assertFalse(body("/varredura?estado=vazio").contains("Passagem da noite"));
        assertTrue(body("/conexoes").contains("Leitura ainda não ligada"));
    }

    @Test
    void scanLogStreamsEachStepWithoutSecrets() {
        String events = given().when().get("/varredura/eventos").then().statusCode(200).extract().asString();

        assertTrue(events.contains("Início da leitura"));
        assertTrue(events.contains("GitHub · Token"));
        assertTrue(events.contains("Revogar"));
        assertTrue(events.contains("Nada foi fechado"));
        assertTrue(events.contains("720"));
        assertFalse(events.contains("ghp_"));
        assertFalse(events.contains("AKIA"));
    }

    private static String body(String path) {
        return given().when().get(path).then().statusCode(200).extract().asString();
    }
}
