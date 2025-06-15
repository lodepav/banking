# Banking Service - RESTful API

A Spring Boot application for managing bank accounts, transactions, and currency conversions.

## Features

* List accounts by client ID
* View transaction history with pagination
* Transfer funds between accounts
* Automatic currency conversion

## Prerequisites

* Java 24
* Docker (or Podman)
* PostgreSQL
* RabbitMQ
* [Open Exchange Rates API Key](https://openexchangerates.org/signup/free)

## Installation

### 1. Clone Repository

```bash
git clone https://github.com/your-username/banking-service.git
cd banking-service
```

### 2. Configure Environment

Create `.env` file:

```bash
echo "API_KEYS=test-key" > .env
```

### 3. Configure API Key

Edit `src/main/resources/application.yml`:

```yaml
exchange-rate:
  api:
    url: https://openexchangerates.org/api/latest.json
    key: your-api-key-here  # ← REPLACE WITH YOUR KEY
```

### 4. Build Application

```bash
./gradlew build
```

## Running with Docker Compose

### 1. Start Dependencies

```bash
docker-compose up -d
```

### 2. Run Application

```bash
./gradlew bootRun
```

## API Access

### 1. Get API Key

Use the key from `.env` file (default: `test-key`)

### 2. Access Endpoints

* Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
* Health Check: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

### 3. Example Requests

**List client accounts:**

```bash
curl -H "X-API-KEY: test-key" http://localhost:8080/clients/client-1/accounts
```

**Transfer funds:**

```bash
curl -X POST -H "X-API-KEY: test-key" -H "Content-Type: application/json" \
  -d '{
    "fromAccountId": "550e8400-e29b-41d4-a716-446655440000",
    "toAccountId": "123e4567-e89b-12d3-a456-426614174000",
    "amount": 100.00,
    "currency": "USD"
  }' http://localhost:8080/transfers
```

## Testing

### Run Unit Tests

```bash
./gradlew test
```

### Run Integration Tests

```bash
./gradlew integrationTest
```

### Run End-to-End Tests

```bash
./gradlew cucumber
```

### Generate Coverage Report

```bash
./gradlew jacocoTestReport
# Open build/reports/jacoco/test/html/index.html
```

## API Documentation

Swagger Docs: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### Key Endpoints

* `GET /clients/{clientId}/accounts` - List accounts by client
* `GET /accounts/{accountId}/transactions?offset=0&limit=10` - Get transactions
* `POST /transfers` - Transfer funds between accounts

## Troubleshooting

### Common Issues

**Exchange Rate API Errors:**

* Verify your API key at [Open Exchange Rates](https://openexchangerates.org)
* Check usage limits (free tier: 1000 requests/month)

**Database Connection Issues:**

```bash
docker exec -it banking-postgres psql -U banking -d banking
```

**RabbitMQ Management Console:**

* [http://localhost:15672](http://localhost:15672) (guest/guest)

## Logs Location

```bash
tail -f build/logs/application.log
```

## License

This project is licensed under the MIT License - see [LICENSE](./LICENSE) for details.

## Getting Open Exchange Rates API Key

1. Go to [Open Exchange Rates](https://openexchangerates.org/signup/free)
2. Sign up for a free account
3. Get your App ID from the dashboard
4. Replace in `application.yml`:

```yaml
exchange-rate.api.key=YOUR_APP_ID_HERE
```

## Key Files Structure

```
banking-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/banking/
│   │   │       ├── config/          # Configuration classes
│   │   │       ├── controller/      # Controllers
│   │   │       ├── domain/          # JPA entities
│   │   │       ├── dto/             # DTOs
│   │   │       ├── exception/       # Exceptions
│   │   │       ├── repository/      # Spring Data repositories
│   │   │       ├── service/         # Business logic
│   │   │       └── Application.java # Main class
│   │   └── resources/
│   │       ├── db/changelog/        # Liquibase scripts
│   │       ├── application.properties  # API key config
│   │       ├── application.yml      # Main configuration
│   └── test/                        # Tests
├── docker-compose.yml               # DB/RabbitMQ setup
├── build.gradle                     # Build configuration
└── README.md                        # This documentation
```

## Recommended Workflow

### Setup

```bash
git clone https://github.com/lodepav/banking.git
cd banking
docker-compose up -d
```

### Configure

* Get API key from [Open Exchange Rates](https://openexchangerates.org)
* Update `src/main/resources/application.yml`

### Run

```bash
./gradlew bootRun
```

### Test

* Access Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
* Use example requests from this README

### Develop

```bash
./gradlew test --continuous
tail -f build/logs/application.log
```
