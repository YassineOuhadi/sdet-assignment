package com.example.deals.api;

import io.restassured.RestAssured;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.example.deals.integration.AbstractIntegrationTest;

import java.io.File;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ImportAndGetErrorScenariosApiIT extends AbstractIntegrationTest {

    @LocalServerPort
    int port;

    @BeforeAll
    void setup() {
        RestAssured.port = port;
    }

    @Test
    void importWithEmptyFileShouldFail() {
        File emptyFile;
        try {
            emptyFile = File.createTempFile("empty", ".csv");
            emptyFile.deleteOnExit();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create temp empty file", e);
        }

        RestAssured.given()
                .multiPart("file", emptyFile)
                .when()
                .post("/api/v1/deals/import")
                .then()
                .statusCode(400)
                .body("error", equalTo("CSV file is required"));
    }

    @Test
    void importWithoutFileShouldFail() {
        RestAssured.given()
                .when().post("/api/v1/deals/import")
                .then().statusCode(500);
    }

    @Test
    void importWithJsonFileShouldFail() {
        File file = new File("src/test/resources/samples/sample_fx_deals.json");

        RestAssured.given().multiPart("file", file)
                .when().post("/api/v1/deals/import")
                .then().statusCode(400)
                .body("error", containsString("Only CSV files are allowed"));
    }

    @Test
    void getDealByIdNotFound() {
        RestAssured.get("/api/v1/deals/NON_EXISTENT")
                .then().statusCode(404);
    }
}