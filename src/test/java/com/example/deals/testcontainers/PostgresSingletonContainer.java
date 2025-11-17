package com.example.deals.testcontainers;

import org.testcontainers.containers.PostgreSQLContainer;

public class PostgresSingletonContainer extends PostgreSQLContainer<PostgresSingletonContainer> {

    private static final String IMAGE_VERSION = "postgres:16.2";
    private static PostgresSingletonContainer container;

    private PostgresSingletonContainer() {
        super(IMAGE_VERSION);
        withDatabaseName("testdb");
        withUsername("user");
        withPassword("pass");
    }

    public static PostgresSingletonContainer getInstance() {
        if (container == null) {
            container = new PostgresSingletonContainer();
            container.start();
        }
        return container;
    }

    @Override
    public void stop() {
    }
}