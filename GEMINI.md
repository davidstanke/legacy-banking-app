# Legacy Banking CIF Application

## Project Overview
This project is a legacy Customer Information File (CIF) system for a banking application. It provides a RESTful API for managing customers, accounts, and transactions, with a web-based frontend.

### Technologies
- **Backend:** Java 8, Maven
- **Framework:** Struts 2 (REST Plugin, Convention Plugin, JSON Plugin)
- **Database:** PostgreSQL (production), H2 (embedded/testing)
- **Frontend:** Backbone.js, jQuery, Underscore.js
- **Testing:** JUnit 4, Mockito

## Building and Running

### Build
To compile the project:
```bash
./mvnw compile
```

### Test
To run the unit and integration tests:
```bash
./mvnw test
```

### Run
The project uses the Jetty Maven plugin for local development. To start the application:
```bash
./mvnw jetty:run
```
The application will be available at `http://localhost:8080/`.

## Development Conventions

### Architecture
- **Controllers:** Located in `src/main/java/com/banking/cif/action/`. These classes use the Struts 2 REST plugin and `ModelDriven` interface to expose API endpoints.
- **Service Layer:** `com.banking.cif.service.BankingService` contains the business logic.
- **Data Access:** Located in `src/main/java/com/banking/cif/dao/`. Uses JDBC for database interactions.
- **Models:** POJOs located in `src/main/java/com/banking/cif/model/`.

### API Design
- Endpoints follow RESTful conventions (e.g., `GET /api/v1/customers/{id}`).
- JSON is the default exchange format.
- Error handling is managed within the Controllers using `status`, `error`, and `message` fields.

### Frontend
- The application is a Single Page Application (SPA).
- Backbone.js is used for routing, models, and views.
- Templates are defined in `src/main/webapp/index.html` using Underscore.js syntax.
- Static assets are located in `src/main/webapp/css/`, `js/`, and `images/`.

### Testing
- **Unit Tests:** `BankingServiceTest.java` tests the service layer logic.
- **API Tests:** `BankingApiTest.java` tests the REST controllers directly.
- **Mocking:** Use Mockito for dependencies. **Do not use Spring.**

### Database
- Initialization logic is in `com.banking.cif.util.DatabaseInitializer`.
- Database connections are managed by `com.banking.cif.util.DBConnection`.
