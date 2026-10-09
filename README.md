# SmartMetrix – Smart Weighing Instrument Inspection System

SmartMetrix is a Spring Boot-based backend application designed to manage weighing instrument inspections, evaluate test results, and generate digitally verifiable inspection certificates.

The project follows an OIML R-76-oriented research prototype approach for non-automatic weighing instruments (NAWI). It provides APIs for instrument registration, inspection management, test record evaluation, role-based access control, and certificate verification.

## Key Features

* **Instrument Management:** Register, retrieve, and manage weighing instruments.
* **Inspection Management:** Create inspections and track their status throughout the inspection workflow.
* **Multiple Inspection Tests:** Support for eccentricity, repeatability, and weighing performance tests.
* **Error Calculation:** Calculate the difference between reference weight and observed weight.
* **MPE-Based Evaluation:** Compare absolute measurement error against the applicable Maximum Permissible Error (MPE).
* **Environmental Data:** Record temperature, humidity, and vibration-related readings during tests.
* **Role-Based Access Control:** Restrict sensitive operations, including certificate issuance, to authorized roles.
* **Inspection Workflow:** Support inspection review, approval, and pass/fail outcomes.
* **PDF Certificates:** Generate inspection certificates containing relevant instrument, inspection, test, and environmental information.
* **QR Code Verification:** Support certificate verification using a certificate number and QR code.
* **SHA-256 Integrity Check:** Use a cryptographic hash to help detect changes to certificate-related data.
* **Exception Handling:** Return structured error responses for validation failures, missing resources, and other handled exceptions.

## Technology Stack

* **Language:** Java
* **Backend Framework:** Spring Boot
* **Database:** PostgreSQL
* **Persistence:** Spring Data JPA / Hibernate
* **Build Tool:** Maven
* **API Testing:** Postman
* **IDE:** IntelliJ IDEA
* **Certificate Generation:** OpenPDF
* **QR Code Generation:** ZXing, if configured in the project
* **Security:** Spring Security with role-based authorization

## Architecture

The application follows a layered backend architecture:

```text
Client / Postman
       |
       v
REST Controllers
       |
       v
Service Layer
       |
       v
Repository Layer
       |
       v
PostgreSQL Database
```

DTOs handle request data, entity classes represent database records, and exception handlers provide consistent API error responses.

## Main Modules

### 1. Instrument Management

Stores weighing instrument information, including:

* Manufacturer and model
* Serial number
* Instrument class
* Maximum and minimum capacity
* Scale interval
* Instrument status

### 2. Inspection Management

Creates and tracks inspection records associated with an instrument and an inspector.

Inspection records support workflow statuses such as `IN_PROGRESS` and `CONTROLLER_APPROVED`.

### 3. Test Record Management

Supports the following test types:

* Eccentricity
* Repeatability
* Weighing Performance

The backend records reference weight, observed weight, calculated error, MPE, environmental readings, and the test result.

**Basic calculation:**

`Error = Observed Weight - Reference Weight`

The absolute error is compared with the applicable MPE to evaluate the measurement result. Correct evaluation depends on the applicable instrument class, capacity, test conditions, and relevant rules.

### 4. Certificate Generation and Verification

Certificates are generated only for inspections that meet the configured approval and pass-result requirements.

Certificate functionality includes:

* Unique certificate numbers
* PDF certificate generation
* QR-code-based verification
* SHA-256 hash-based integrity checking
* Public verification through the certificate verification endpoint

## API Overview

The following are the main API routes implemented in the backend:

| Method | Endpoint                                      | Purpose                                                                               |
| ------ | --------------------------------------------- | ------------------------------------------------------------------------------------- |
| GET    | `/api/instruments`                            | Retrieve instruments                                                                  |
| GET    | `/api/certificates/inspection/{inspectionId}` | Generate or retrieve a certificate for an inspection, according to the implementation |
| GET    | `/api/verify/{certificateNumber}`             | Verify a certificate                                                                  |

The project also contains endpoints for student-independent inspection workflows and test records. Refer to the controller classes for the complete route list, supported HTTP methods, and request formats.

### Security

Certificate issuance is restricted to authorized controller or administrator roles. Configure and test all authentication and authorization rules before deployment.

## Database Configuration

Create a PostgreSQL database named `smartmetrix`:

```sql
CREATE DATABASE smartmetrix;
```

Configure the database connection in `src/main/resources/application.properties`.

Example configuration:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/smartmetrix
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

Set `DB_USERNAME` and `DB_PASSWORD` in your local environment before running the application. Do not commit real credentials to GitHub.

## Run Locally

### Prerequisites

* Java version compatible with the project's Maven configuration
* PostgreSQL
* Maven or the included Maven Wrapper

### Start the application

On Windows, from the project root, run:

```powershell
.\mvnw.cmd spring-boot:run
```

If the Maven Wrapper is not included, use:

```powershell
mvn spring-boot:run
```

Alternatively, run `SmartmetrixBackendApplication` from IntelliJ IDEA.

## Testing

Use Postman to test the REST APIs, including:

* Instrument registration and retrieval
* Inspection creation and status transitions
* Test record creation and result evaluation
* Role-based certificate issuance
* PDF certificate generation
* Certificate verification

Verify that failed inspections and unapproved inspections cannot receive issued certificates.

## Research and Future Enhancements

* Integration with physical weighing instruments through Web Serial or Bluetooth
* Automated environmental data collection
* Improved calibration and measurement workflows
* Automated unit and integration testing
* Swagger/OpenAPI documentation
* Cloud deployment and production monitoring

## Project Status

SmartMetrix is a research-oriented backend prototype for weighing instrument inspection and certificate management. It is not a claim of official OIML certification or regulatory approval.

## Author

**Rajnish Rai**
