package curfew.web;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import curfew.scan.MockAccessSource;
import curfew.scan.ScanEngine;
import curfew.scan.ScanRules;
import curfew.store.AccessStore;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
class NightPageTest {

    @Inject
    AccessStore store;

    @Test
    void passageRemovesLocalRowsAndDoesNotCallTheAccount() {
        try {
            given().formParam("outro", "nao").post("/noite").then().statusCode(400);
            assertTrue(queue().contains("/acessos/gh-token-18"));

            String location = given()
                    .redirects().follow(false)
                    .formParam("confirmo", "sim")
                    .post("/noite")
                    .then()
                    .statusCode(303)
                    .extract()
                    .header("Location");

            assertTrue(location.contains("conta=nao"));
            assertFalse(queue().contains("/acessos/gh-token-18"));
            assertTrue(queue().contains("/acessos/aws-net-9"));
            String page = given().get(location).then().statusCode(200).extract().asString();
            assertTrue(page.contains("A conta não foi chamada."));
            assertTrue(page.contains("aws-net-9"));
            assertFalse(page.contains("gh-token-18"));
            assertFalse(page.contains("ghp_"));
            assertFalse(page.contains("AKIA"));
        } finally {
            store.forgetDecisions();
            store.replace(new ScanEngine(4).classifyAll(new MockAccessSource().list(), new ScanRules()));
        }
    }

    private static String queue() {
        return given().get("/").then().statusCode(200).extract().asString();
    }
}
