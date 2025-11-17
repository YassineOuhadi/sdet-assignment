# FX Deals Import Service

This document provides an overview of the FX Deals Import Service implementation, including architecture, validation, deduplication logic, testing strategy, performance testing, Docker deployment, and Makefile usage.

## 📨 Get in Touch

* 📱 **GitHub Codespace**
* 📊 **HTML Report (GitHub Pages)**

## Overview

The service accepts CSV files containing FX deal information and persists valid rows into a PostgreSQL database. It supports:

* Row-level validation
* Deduplication (no deal is imported twice)
* Partial success with no rollback
* Parsing, validation, persistence workflow
* REST API endpoints for import and retrieval

## GitHub Codespace Environment

The GitHub Codespace for this project is fully prepared using a **Dev Container** configuration. It includes:

* Preinstalled **Makefile tools**, allowing test commands to be executed directly from the Makefile.
* The **Postman extension**, enabling execution of the full Postman collection inside the Codespace without leaving the editor.
* All required **Docker images** (PostgreSQL + Deals REST API) running inside the Codespace.
* A full **Maven-based test environment**, so API tests and integration tests run seamlessly.

Before using the Codespace, run:

```sh
make prepare
```

Once prepared, tests and reports can be executed directly:

```sh
make import
make test
make coverage
make serve-unit-report   # then press Alt+L → Alt+O to open in browser
```

The Codespace is therefore **ready out-of-the-box** to run imports, execute tests, generate coverage, and serve test reports.

## Features

### ✅ Deal Fields

Each row contains:

* **Deal ID** (unique)
* **From Currency** (ISO3)
* **To Currency** (ISO3)
* **Timestamp** (ISO-8601)
* **Amount** (BigDecimal)

### ✅ Validation

* Missing fields
* Invalid currencies
* Invalid timestamp
* Invalid amount
* CSV structure validation
* Per-row contextual logging using MDC

### ✅ Deduplication

* Duplicate Deal IDs inside the **same file** are skipped
* Duplicate Deal IDs already in DB are not imported
* System ensures **idempotent imports**

### ✅ Partial Success

* No rollback allowed
* Each valid row is inserted independently
* Errors are reported per row in `RowResult`

## Architecture

```
Controller → Parser → Service → Validator → Persistence
```

### Components

* **DealController** -- REST endpoints and file handling
* **DealParser** -- CSV parsing using OpenCSV
* **DealImportService** -- Main business logic
* **DealValidator** -- Field validation rules
* **DealRepository** -- JPA repository for persistence
* **GlobalExceptionHandler** -- Error handling with MDC
* **Deal** -- JPA Entity

## API Endpoints

### Import Deals

```
POST /api/v1/deals/import
Content-Type: multipart/form-data
file=@deals.csv
```

### Get All Deals

```
GET /api/v1/deals
```

### Get Single Deal

```
GET /api/v1/deals/{dealId}
```

### Health Check

```
GET /api/v1/deals/health
```

## Running with Docker Compose

```sh
make up
```

Services started:

* PostgreSQL
* Spring Boot application

To stop:

```sh
make down
```

## Makefile Commands

| Command         | Description            |
| --------------- | ---------------------- |
| `make up`       | Start containers       |
| `make down`     | Stop containers        |
| `make build`    | Build project          |
| `make test`     | Run unit tests         |
| `make verify`   | Run all tests          |
| `make coverage` | Generate JaCoCo report |
| `make k6-all`   | Run performance tests  |
| `make import`   | Import sample CSV file |

## Testing Strategy

### ✅ Unit Tests

* Validation logic
* Parsing logic
* Deduplication
* Import flow
* Error scenarios

### ✅ Integration Tests

* DB boundary behavior
* Repository tests
* End-to-end import processing

### ✅ API Tests with RestAssured

Covers:

* Import CSV success
* Validation errors
* Duplicate detection
* Row-level error reporting
* Partial insert behavior

### ✅ Performance Tests (K6)

* Stress test
* Concurrent imports
* Large file imports

Run:

```sh
make k6-all
```

## Coverage Requirements

* **100% coverage** for parsing, validation, deduplication, and import flow
* Checked with **JaCoCo**
* Build fails if coverage is below target

Report:

```
target/site/jacoco/index.html
```

Open:

```sh
make serve-coverage
```

## Postman Collection

A full Postman collection is included:

* Import API tests
* Get All Deals
* Get Single Deal
* Negative scenarios

## Folder Structure

```
.
├── src/main/java/com/example/deals
│   ├── controller
│   ├── service
│   ├── parser
│   ├── validation
│   ├── exception
│   ├── model
│   └── repository
├── docker-compose.yml
├── Makefile
├── k6/
├── fixtures/
└── README.md
```

## How to Reproduce

From a clean checkout:

```sh
make clean
make build
make up
make test
make verify
make coverage
make k6-all
```

Everything is automated and reproducible.