package com.example.deals.api;

import io.restassured.RestAssured;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

import com.example.deals.integration.AbstractIntegrationTest;

import java.io.File;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GetDealsApiIT extends AbstractIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeAll
    void setup() {
        RestAssured.port = port;
    }

    @BeforeEach
    void cleanup() {
        // Clear all deals before each test
        jdbcTemplate.execute("TRUNCATE TABLE deals RESTART IDENTITY CASCADE");
    }

    @Test
    void getAllDealsViaApi() {
        File file = new File("src/test/resources/samples/deals.csv");
        RestAssured.given().multiPart("file", file)
                .when().post("/api/v1/deals/import")
                .then().statusCode(200);

        RestAssured.get("/api/v1/deals")
                .then().statusCode(200)
                .body("dealId", hasItem("D1"))
                .body("size()", equalTo(3));
    }

    @Test
    void getDealByIdViaApi() {
        File file = new File("src/test/resources/samples/deals.csv");
        RestAssured.given().multiPart("file", file)
                .when().post("/api/v1/deals/import")
                .then().statusCode(200);

        RestAssured.get("/api/v1/deals/D2")
                .then().statusCode(200)
                .body("dealId", equalTo("D2"))
                .body("fromCurrency", equalTo("GBP"))
                .body("toCurrency", equalTo("USD"));
    }

    @Test
    void getDealByIdNotFoundViaApi() {
        RestAssured.get("/api/v1/deals/NON_EXISTENT")
                .then().statusCode(404);
    }
}