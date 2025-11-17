package com.example.deals.api;

import io.restassured.RestAssured;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.example.deals.integration.AbstractIntegrationTest;

import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HealthCheckIT extends AbstractIntegrationTest {

    @LocalServerPort
    int port;

    @BeforeAll
    void setup() {
        RestAssured.port = port;
    }

    @Test
    void healthCheck() {
        RestAssured.get("/api/v1/deals/health")
                .then().statusCode(200)
                .body("status", equalTo("UP"));
    }
}