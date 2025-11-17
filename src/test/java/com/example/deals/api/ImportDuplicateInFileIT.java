package com.example.deals.api;

import io.restassured.RestAssured;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

import com.example.deals.integration.AbstractIntegrationTest;

import java.io.File;

import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ImportDuplicateInFileIT extends AbstractIntegrationTest {

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
    void importDuplicateInFileViaApi() {
        File file = new File("src/test/resources/samples/deals_with_duplicates.csv");

        RestAssured.given().multiPart("file", file)
                .when().post("/api/v1/deals/import")
                .then().statusCode(200)
                .body("results.status", hasItems("SUCCESS", "DUPLICATE"));

        RestAssured.get("/api/v1/deals")
                .then().statusCode(200)
                .body("size()", equalTo(1));
    }
}