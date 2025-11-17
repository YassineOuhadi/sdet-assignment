# FX Deals Import Service

This project implements an FX Deals Import Service that accepts CSV files containing FX deal information and persists valid rows into a PostgreSQL database. It supports:

* Row-level validation
* Deduplication (no deal is imported twice)
* Partial success without rollback
* Parsing, validation, and persistence workflow
* REST API endpoints for import and retrieval

---

## 📨 Get in Touch

* **GitHub Codespace** [Open in Codespace](https://github.com/codespaces/new?repo=YassineOuhadi/sdet-assignment)
* **HTML Report (GitHub Pages)** [View Reports](https://yassinouhadi.gitlab.io/sdet-assignment/)

---

## Features

### ✅ Deal Fields

Each row contains:

* Deal ID (unique)
* From Currency (ISO3)
* To Currency (ISO3)
* Timestamp (ISO-8601)
* Amount (BigDecimal)

### ✅ Validation

* Missing fields
* Invalid currencies
* Invalid timestamp
* Invalid amount
* CSV structure validation
* Per-row contextual logging using MDC

### ✅ Deduplication

* Duplicate Deal IDs inside the same file are skipped
* Duplicate Deal IDs already in DB are not imported
* System ensures idempotent imports

### ✅ Partial Success

* Each valid row is inserted independently
* Errors are reported per row in `RowResult`
* No rollback is performed

### ✅ Logging & Error Handling

All parsing, validation, and persistence steps are logged with proper exceptions. Logs are written to `/logs/deals-app.log`. Example:

```
2025-11-15 13:55:57 INFO  [dealId=D10049 rowNum=] c.e.deals.service.DealImportService - Imported successfully
2025-11-15 13:55:57 INFO  [dealId=D10050 rowNum=] c.e.deals.service.DealImportService - Imported successfully
2025-11-15 13:56:45 ERROR [dealId=D10007 rowNum=8] com.example.deals.parser.DealParser - Validation error: ToCurrency must be 3-letter ISO
2025-11-15 13:56:45 ERROR [dealId=D10008 rowNum=9] com.example.deals.parser.DealParser - Validation error: FromCurrency must be 3-letter ISO
2025-11-15 13:56:45 ERROR [dealId=D10009 rowNum=10] com.example.deals.parser.DealParser - Validation error: Timestamp is required
2025-11-15 13:56:45 WARN  [dealId=D10001 rowNum=] c.e.deals.service.DealImportService - Duplicate deal in DB, skipping
```

---

## Architecture

`Controller → Parser → Service → Validator → Persistence`

Components:

* `DealController` – REST endpoints and file handling
* `DealParser` – CSV parsing using OpenCSV
* `DealImportService` – Main business logic
* `DealValidator` – Field validation rules
* `DealRepository` – JPA repository for persistence
* `GlobalExceptionHandler` – Error handling with MDC
* `Deal` – JPA Entity

---

## API Endpoints

| Endpoint                 | Method | Description       |
| ------------------------ | ------ | ----------------- |
| `/api/v1/deals/import`   | POST   | Import CSV file   |
| `/api/v1/deals`          | GET    | Get all deals     |
| `/api/v1/deals/{dealId}` | GET    | Get a single deal |
| `/api/v1/deals/health`   | GET    | Health check      |

---

## Running With Dev Container

1. Clone the repository:

   ```bash
   git clone https://github.com/YassineOuhadi/sdet-assignment.git
   cd sdet-assignment
   ```
2. Install the **Dev Container** extension in VS Code.
3. Open the project in VS Code.
4. Press **Ctrl+Shift+P → “Remote-Containers: Open Folder in Container”**.

**Important:** Update the base URL in K6 scripts and Postman collections:

```
http://localhost:8080/api/v1/deals → http://deals-app:8080/api/v1/deals
```

Run tests and imports:

```bash
make prepare
make import
make test
make coverage
make serve-unit-report
make k6-all
```

---

## Running With Docker Compose

```bash
make up
make import
make test
make integration
make coverage
make k6-all
make down
```

---

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

---

## Postman Collection

* Import API tests
* Get All Deals
* Get Single Deal
* Negative scenarios

---

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

---

## Testing Strategy

* ✅ Unit Tests: validation, parsing, deduplication, import flow
* ✅ Integration Tests: DB boundaries, repository, end-to-end import
* ✅ API Tests (RestAssured): CSV import, validation errors, duplicate detection
* ✅ Performance Tests (K6): stress test, concurrent imports, large files

Coverage is enforced via JaCoCo; build fails if coverage is below target.

---

## License

No license specified.