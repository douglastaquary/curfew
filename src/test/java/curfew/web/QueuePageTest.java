package curfew.web;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class QueuePageTest {

    @Test
    void queueShowsTheMockAndNoSecret() {
        String html = given().when().get("/").then().statusCode(200).extract().asString();

        assertTrue(html.contains("Token"));
        assertTrue(html.contains("Ninguém"));
        assertTrue(html.contains("Trancar e pedir um humano"));
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
        assertTrue(body("/acessos/gh-token-18").contains("Revogar"));
        given().when().get("/acessos/nao-existe").then().statusCode(404);
        String scan = body("/varredura");
        assertTrue(scan.contains("Chamadas de modelo"));
        assertTrue(scan.contains(">0<"));
        assertTrue(body("/conexoes").contains("Leitura ainda não ligada"));
    }

    private static String body(String path) {
        return given().when().get(path).then().statusCode(200).extract().asString();
    }
}
