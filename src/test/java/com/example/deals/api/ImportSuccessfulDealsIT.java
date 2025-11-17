package com.example.deals.api;

import io.restassured.RestAssured;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.example.deals.integration.AbstractIntegrationTest;

import java.io.File;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ImportSuccessfulDealsIT extends AbstractIntegrationTest {

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
        jdbcTemplate.execute("TRUNCATE TABLE deals RESTART IDENTITY CASCADE");
    }

    @Test
    void importSuccessfulDealsViaApi() {
        File file = new File("src/test/resources/samples/deals.csv");

        RestAssured.given().multiPart("file", file)
                .when().post("/api/v1/deals/import")
                .then().statusCode(200)
                .body("results.status", everyItem(equalTo("SUCCESS")));

        RestAssured.get("/api/v1/deals")
                .then().statusCode(200)
                .body("size()", greaterThan(0));
    }
}