# Retailer Rewards Program

A Spring Boot REST API that calculates customer reward points based on purchase transactions over a three-month period.

---

## Overview

A retailer awards points to customers based on each recorded purchase:

| Purchase Amount | Points Earned |
|---|---|
| $0 – $50 | 0 points |
| $50.01 – $100 | 1 point per dollar over $50 |
| Over $100 | 50 points (for the $50–$100 tier) + 2 points per dollar over $100 |

**Example:** A $120 purchase → `2 × $20 + 1 × $50 = 90 points`

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.3.0 |
| API Docs | Springdoc OpenAPI (Swagger UI) 2.3.0 |
| JPA Repository | H2 database   
| Build Tool | Apache Maven |
| Testing | JUnit 5 (via Spring Boot Test) |

---

## Project Structure

```
### Access Points

| Resource        | URL                                          |
|-----------------|----------------------------------------------|
| H2 Console      | http://localhost:8080/api/h2-console         |
| Swagger UI      | http://localhost:8080/swagger-ui/index.html   |

### H2 Console Login
```
JDBC URL  : jdbc:h2:mem:rewarddb
Username  : sa
Password  : (leave empty)
```

---

## REST Endpoints

| Method | URL | Description | Response |
|---|---|---|---|
| `GET` | `/api/rewards` | Reward summary for **all** customers | `200 OK` |
| `GET` | `/api/rewards/{customerId}` | Reward summary for **one** customer | `200 OK` / `404` / `400` |
| `GET` | `/api/rewards//date-range?startDate=yyyy-MM-dd&endDate=yyyy-MM-dd` |'200 OK' / Reward summary based on date range for **one** customer | `200 OK` / `404` / `400` |
| `POST` | `/api/rewards/` | Creates a new customer with their transactions and calculates reward points. | `201 Created` / `404` / `400` |


### Sample Response — `GET /api/rewards/1`

```json
{
  "customerId": 1,
  "customerName": "Test One",
  "customerEmail": "test.one@example.com",
  "monthlyRewards": [
    { "year": 2026, "month": 3, "monthName": "March", "points": 365 },
    { "year": 2026, "month": 4, "monthName": "April", "points": 110 },
    { "year": 2026, "month": 5, "monthName": "May",   "points": 195 }
  ],
  "totalPoints": 670
}
```

### Error Response (e.g. `GET /api/rewards/999`)

```json
{
  "status": 404,
  "error": "NOT_FOUND",
  "message": "Customer not found with ID: 999",
  "path": "/api/rewards/999",
  "timestamp": "2024-05-30T10:15:00"
}
```
### Sample Request — `POST /api/rewards/`

```json
{
  "customerDto": {
    "customerName": "Test One",
    "customerEmail": "TestOne@example.com"
  },
  "transactionsDto": [
    {
      "amount": "120",
      "transactionDate": "2026-03-05"
    },
    {
      "amount": "75",
      "transactionDate": "2026-03-14"
    },
    {
      "amount": "200",
      "transactionDate": "2026-03-22"
    },
    {
      "amount": "130",
      "transactionDate": "2026-04-03"
    },
    {
      "amount": "40",
      "transactionDate": "2026-04-17"
    },
    {
      "amount": "150",
      "transactionDate": "2026-05-08"
    },
    {
      "amount": "95",
      "transactionDate": "2026-05-20"
    }
  ]
}
```

---

## Exception Handling

Handled globally via [`GlobalExceptionHandler`](src/main/java/com/retailer/reward/exception/GlobalExceptionHandler.java) (`@RestControllerAdvice`):

| Exception | HTTP Status | Cause |
|---|---|---|
| `CustomerNotFoundException` | `404 NOT FOUND` | Customer ID not in dataset |
| `InvalidCustomerIdException` | `400 BAD REQUEST` | ID is null, zero, or negative |
| `MethodArgumentTypeMismatchException` | `400 BAD REQUEST` | Non-numeric path variable (e.g. `/api/rewards/abc`) |
| `Exception` (catch-all) | `500 INTERNAL SERVER ERROR` | Any unhandled error |


## How to Run

**Run tests:**

mvn clean test -f pom.xml

**Start the application:**
```bash
mvn spring-boot:run -f pom.xml
```

**Access Swagger UI:**
```
http://localhost:8080/swagger-ui/index.html
```

---

## Unit Tests

24 tests in [`RewardServiceTest`,'RewardControllerTest','RewardControllerIntegrationTest'] covering:

- **Point calculation** — boundary values ($0, $50, $100, $120, $200, $300)
- **All-customer summary** — count, 3 months each, positive totals
- **Per-customer totals** — verified for all 3 customers
- **Monthly breakdown** — March / April / May points for Alice and David
- **Exception handling** — `CustomerNotFoundException`, `InvalidCustomerIdException` (null, zero, negative)
- **Sort order** — monthly rewards sorted chronologically
- **Invariant** — total points = sum of monthly points for every customer
