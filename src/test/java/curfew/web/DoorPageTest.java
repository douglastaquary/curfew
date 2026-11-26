package curfew.web;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class DoorPageTest {

    @Test
    void doorDoesNotEchoTheAttemptAndLocalQueueStaysOpen() {
        String form = given().when().get("/porta").then().statusCode(200).extract().asString();
        assertTrue(form.contains("Porta"));
        assertTrue(form.contains("CURFEW_GATE"));
        assertFalse(form.contains("ghp_"));
        assertFalse(form.contains("AKIA"));

        var response = given()
                .contentType("application/x-www-form-urlencoded")
                .body("porta=ghp_nao_ecoar")
                .when()
                .post("/porta")
                .then()
                .extract();
        String refused = response.asString();
        String gate = System.getenv("CURFEW_GATE");
        if (gate == null || gate.isBlank()) {
            assertTrue(response.statusCode() == 503);
            assertTrue(refused.contains("A porta não foi configurada."));
        }
        assertFalse(refused.contains("ghp_"));

        given().when().get("/").then().statusCode(200);
    }
}
