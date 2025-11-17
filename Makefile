# ========================
# FX Deals DevContainer Makefile
# ========================

.PHONY: up down build clean test integration verify api api-tests coverage k6 \
        k6-all k6-stress k6-concurrent k6-large run-all prepare \
        test-name test-it serve-unit-report serve-integration-report serve-coverage \
        import postman

DB_CONTAINER_NAME=fxdeals_postgres
BASE_URL=http://deals-app:8080/api/v1/deals

# ========================
# Target to run all steps
# ========================
run-all: up build verify coverage down
	@printf "All tasks completed.\n"

# ========================
# Docker Compose
# ========================
up:
	@printf "Starting DB and services...\n"
	docker compose up -d --build

down:
	@printf "Stopping DB and services...\n"
	docker compose down -v

# ========================
# Maven commands
# ========================

api:
	@printf "Running Spring Boot app...\n"
	mvn spring-boot:run

build:
	@printf "Building project (skip tests)...\n"
	mvn -B -DskipTests package

clean:
	@printf "Cleaning Maven project...\n"
	mvn clean

# ========================
# Prepare environment for tests
# ========================
prepare:
	@printf "Connecting devcontainer and deals-app to 'deals-network'...\n"
# 	docker network create deals-network || true
	docker network connect deals-network sdet-assignment-dev-1 || true
	docker network connect deals-network deals-app || true

# ========================
# Import CSV / Postman instructions
# ========================
# Please be sure to run make prepare cmd before
import:
	@printf "Importing CSV to deals-app (%s)...\n" $(BASE_URL)
	curl -v -F "file=@fixtures/deals.csv" $(BASE_URL)/import
	@printf "\n"

# Update BASE_URL to localhost:8080/api/v1/deals if running outside devcontainer
# Please be sure to run make prepare cmd before
# Re-import CSV files in request body after collection import if needed
postman:
	@printf "Use Postman to run FX_Deals_API.postman_collection.json\n"

# ========================
# Unit / Integration tests
# ========================
test:
	@printf "Running unit tests...\n"
	mvn -B test

integration:
	@printf "Running integration tests...\n"
	mvn -B -Pintegration verify

verify:
	@printf "Running all tests...\n"
	mvn -B verify

# Run a specific test class
# Usage: make test-name name=DealImportServiceTest
test-name:
	@printf "Running test: %s\n" $(name)
	mvn -B test -Dtest=$(name)

# Run a specific integration test class
# Usage: make test-it name=DealApiIT
test-it:
	@printf "Running integration test: %s\n" $(name)
	mvn -B -Pintegration verify -Dit.test=$(name)

# ========================
# Coverage reports
# ========================
coverage:
	@printf "Generating coverage report...\n"
	mvn -B verify jacoco:report

# Html Reports can be opened with (Alt+L then Alt+O)
serve-unit-report:
	@printf "Opening unit test report...\n"
	code target/surefire-report/unit/surefire-report.html

serve-integration-report:
	@printf "Opening integration test report...\n"
	code target/surefire-report/integration/surefire-report.html

serve-coverage:
	@printf "Opening coverage report...\n"
	code target/site/jacoco/index.html

# ========================
# K6 performance tests
# ========================
k6-all:
	@printf "Running all K6 tests...\n"
	k6 run k6/perf_api_stress.js
	k6 run k6/erf_concurrent_imports.js
	k6 run k6/perf_large_file.js

k6-stress:
	@printf "Running K6 stress test...\n"
	k6 run k6/perf_api_stress.js

k6-concurrent:
	@printf "Running K6 concurrent imports test...\n"
	k6 run k6/erf_concurrent_imports.js

k6-large:
	@printf "Running K6 large file test...\n"
	k6 run k6/perf_large_file.js

k6:
	@printf "Must specify which K6 target to run:\n"
	@printf "  make k6-all        # run all tests\n"
	@printf "  make k6-stress     # run only stress test\n"
	@printf "  make k6-concurrent # run only concurrent imports test\n"
	@printf "  make k6-large      # run only large file test\n"