package curfew.web;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class CountPageTest {

    @Test
    void aReadingBecomesCountsWithoutAModelCall() {
        String before = given().when().get("/contagem").then().statusCode(200).extract().asString();
        assertTrue(before.contains("Uma instalação por cliente"));
        assertTrue(before.contains("Nenhuma chamada de modelo"));

        given().when().get("/varredura/eventos").then().statusCode(200);

        String after = given().when().get("/contagem").then().statusCode(200).extract().asString();
        assertTrue(after.contains("6 itens"));
        assertTrue(after.contains("0 chamadas de modelo"));
        assertTrue(after.contains("720 tokens evitados"));
        assertFalse(after.contains("ghp_"));
        assertFalse(after.contains("AKIA"));
    }
}
