# Personal Finance Manager API

Spring Boot 3.x implementation of the Syfe Backend Intern assignment. The API supports session-based authentication, isolated user data, transaction CRUD, category management, savings goals, and monthly/yearly reports.

## Tech Stack

- Java 17
- Spring Boot 3.4.x
- Spring Security with HTTP session cookies
- Maven
- JUnit 5, MockMvc, Mockito-compatible Spring test stack
- In-memory repository layer for simple assignment deployment
- Docker-ready for Render

## Project Structure

```text
src/main/java/com/syfe/finance
  config/       Spring Security configuration
  controller/   REST API endpoints
  dto/          Request and response DTOs
  exception/    Global exception handler
  model/        Domain models
  repository/   In-memory repository layer
  security/     Current-user and UserDetails services
  service/      Business rules and calculations
```

## Run Locally

Install Java 17 and Maven, then run:

```bash
mvn spring-boot:run
```

Base URL:

```text
http://localhost:8080/api
```

Run tests and coverage:

```bash
mvn clean verify
```

JaCoCo report:

```text
target/site/jacoco/index.html
```

## API Summary

### Authentication

```http
POST /api/auth/register
POST /api/auth/login
POST /api/auth/logout
```

Login returns a `JSESSIONID` cookie. Use that cookie for every protected request.

### Categories

```http
GET    /api/categories
POST   /api/categories
DELETE /api/categories/{name}
```

Default categories:

- INCOME: `Salary`
- EXPENSE: `Food`, `Rent`, `Transportation`, `Entertainment`, `Healthcare`, `Utilities`

Custom category names are unique per user. Default categories cannot be deleted. Categories referenced by active transactions cannot be deleted.

### Transactions

```http
POST   /api/transactions
GET    /api/transactions
PUT    /api/transactions/{id}
DELETE /api/transactions/{id}
```

Supported filters:

```text
?startDate=2024-01-01&endDate=2024-01-31&categoryId=1&category=Salary&type=INCOME
```

Transactions are returned newest first. Deletes are soft deletes, so deleted transactions do not affect goals or reports.

### Savings Goals

```http
POST   /api/goals
GET    /api/goals
GET    /api/goals/{id}
PUT    /api/goals/{id}
DELETE /api/goals/{id}
```

Progress formula:

```text
total income since start date - total expenses since start date
```

### Reports

```http
GET /api/reports/monthly/{year}/{month}
GET /api/reports/yearly/{year}
```

Reports aggregate income and expenses by category and return net savings.

## Example Curl Flow

```bash
BASE=http://localhost:8080/api

curl -i -c cookies.txt -X POST "$BASE/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"username":"user@example.com","password":"password123","fullName":"John Doe","phoneNumber":"+1234567890"}'

curl -i -c cookies.txt -X POST "$BASE/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"user@example.com","password":"password123"}'

curl -b cookies.txt -X POST "$BASE/transactions" \
  -H "Content-Type: application/json" \
  -d '{"amount":50000.00,"date":"2024-01-15","category":"Salary","description":"January Salary"}'

curl -b cookies.txt "$BASE/reports/monthly/2024/1"
```

## Deployment on Render

This repository includes a `Dockerfile` and `render.yaml`.

1. Push the project to a public GitHub repository.
2. In Render, create a new Web Service from the GitHub repo.
3. Choose Docker runtime.
4. Keep the default port behavior. The app reads `PORT` from Render.
5. Set `SESSION_COOKIE_SECURE=true` in Render environment variables.
6. After deployment, use:

```text
https://your-render-service.onrender.com/api
```

Then run the assignment test script:

```bash
bash financial_manager_tests.sh https://your-render-service.onrender.com/api
```

Take a screenshot of the final test summary for submission.

## Step-by-Step Implementation Explanation

Use this order in your one-shot explanation video or interview:

1. Start with the requirements: user auth, transactions, categories, goals, reports, and user data isolation.
2. Explain the layered architecture: controllers receive API requests, services enforce business rules, repositories store data, DTOs separate API contracts from internal models.
3. Explain authentication: registration stores a BCrypt password hash, login uses Spring Security, and the authenticated session is stored in an HTTP-only cookie.
4. Explain data isolation: every user-owned object stores `userId`, and every service method receives the current authenticated user's id before reading or writing data.
5. Explain categories: defaults are shared and immutable; custom categories are per-user; referenced categories cannot be deleted.
6. Explain transactions: amount/date/category validation happens before save; deleted transactions are soft deleted and excluded from goals/reports.
7. Explain goals: progress is calculated live from transactions since the goal start date, so updates/deletes are reflected automatically.
8. Explain reports: monthly/yearly reports filter transactions by date and aggregate totals by category.
9. Explain error handling: known bad inputs return 400/401/403/404/409 with JSON messages through a global exception handler.
10. Explain testing: MockMvc tests cover auth, protected endpoints, category creation, transactions, goals, and reports; JaCoCo enforces coverage.

## Antigravity Workflow

You can use Antigravity or any Java IDE:

1. Open the `personal-finance-manager` folder.
2. Ask it to index the Maven project.
3. Run `mvn clean verify`.
4. Run `mvn spring-boot:run`.
5. Use Postman or the provided shell script against `http://localhost:8080/api`.
6. Ask Antigravity to explain one file at a time, starting with `SecurityConfig`, then each controller and service.

## Submission Checklist

- Public GitHub repository link
- Render live API URL ending with `/api`
- Screenshot of:

```text
TEST EXECUTION SUMMARY
Total Tests Executed: 86
Tests Passed: 86
Tests Failed: 0
Success Rate: 100%
```

## Design Notes

- In-memory repositories are used because the assignment marks H2 as optional and the public test script only validates API behavior.
- `JSESSIONID` is HTTP-only and can be marked secure in deployment through `SESSION_COOKIE_SECURE=true`.
- DTOs keep API request/response shapes separate from internal models.
- Global exception handling avoids accidental stack traces for known validation and access scenarios.
