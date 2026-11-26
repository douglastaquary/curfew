package curfew.web;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import curfew.scan.LeaseBook;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
class LeasePageTest {

    @Inject
    LeaseBook book;

    @Test
    void exampleLeaseEndsInsideADay() {
        try {
            String html = given().when().get("/").then().statusCode(200).extract().asString();
            assertTrue(html.contains("Chave com prazo"));
            assertTrue(html.contains("menos de 24 horas"));
            assertTrue(html.contains("Abrir o pull request"));
            assertTrue(html.contains("23 horas · vale até"));
            assertTrue(html.contains("Entregar a chave"));
            assertFalse(body("/?estado=vazio").contains("Entregar a chave"));
            assertFalse(html.contains("ghp_"));
            assertFalse(html.contains("AKIA"));
            assertFalse(html.contains("secretAccessKey"));

            String json = given()
                    .accept("application/json")
                    .formParam("tarefa", "Ler a fila")
                    .formParam("sistema", "AWS")
                    .formParam("horas", "23")
                    .post("/chaves")
                    .then()
                    .statusCode(200)
                    .extract()
                    .asString();
            assertTrue(json.contains("\"conta\":\"nao\""));
            assertTrue(json.contains("Ler a fila"));
            assertFalse(json.contains("secretAccessKey"));
            assertFalse(json.contains("sessionToken"));
            assertFalse(json.contains("ghp_"));
            assertFalse(json.contains("AKIA"));
            String after = body("/");
            assertTrue(after.contains("Ler a fila"));
            assertFalse(after.contains("secretAccessKey"));

            int before = book.size();
            given().formParam("tarefa", "Conferir o log")
                    .formParam("sistema", "AWS")
                    .formParam("horas", "24")
                    .post("/chaves")
                    .then()
                    .statusCode(400);
            assertEquals(before, book.size());

            String location = given()
                    .redirects().follow(false)
                    .formParam("tarefa", "Conferir o log")
                    .formParam("sistema", "AWS")
                    .formParam("horas", "23")
                    .post("/chaves")
                    .then()
                    .statusCode(303)
                    .extract()
                    .header("Location");
            assertTrue(location.contains("entregue=sim"));
            String page = given().get(location).then().statusCode(200).extract().asString();
            assertTrue(page.contains("Conferir o log"));
            assertTrue(page.contains("AWS · 23 horas · vale até"));
            assertTrue(page.contains("A conta não foi chamada."));
            assertFalse(page.contains("ghp_"));
            assertFalse(page.contains("AKIA"));
        } finally {
            book.reset(Instant.now());
        }
    }

    private static String body(String path) {
        return given().when().get(path).then().statusCode(200).extract().asString();
    }
}
