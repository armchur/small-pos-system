# Small Business Point of Sale (POS) System

**Coursework Project: CSC 508 Structured and Object-Oriented Programming Languages II**

A desktop Point of Sale (POS) application built strictly adhering to the 3-tier architectural pattern (Presentation, Service/Business Logic, Data Access Object), featuring atomic transactions, SQLite persistence, parameterized SQL queries, and a JavaFX user interface.

---

## 1. System Architecture

The application enforces strict separation of concerns across three layers:

[ Presentation Layer ]  -->  app.ui.PosController (JavaFX / FXML)
|
v
[ Business Logic Layer ] -->  app.service.SaleService
|
v
[ Data Access Layer ]    -->  app.dao.ProductDao, app.dao.SaleDao (SQLite JDBC)
|
v
[ Persistence ]          -->  pos.db (SQLite database)




- **Presentation Layer (`app.ui`)**: Handles user events and view updates only. Contains zero business calculations, tax logic, or SQL queries.
- **Service Layer (`app.service`)**: Enforces business validation and coordinates atomic checkout transactions using JDBC commit/rollback.
- **Data Access Layer (`app.dao`)**: Implements DAO interfaces. Every query is 100% parameterized via `PreparedStatement`.
- **Domain Models (`app.model`)**: Plain POJOs (`Product`, `Sale`, `SaleItem`, `Category`, `DailyTakingsReport`).

---

## 2. Prerequisites

- **Java Development Kit (JDK)**: 17 LTS or higher (tested on Eclipse Adoptium JDK 25)
- **Apache Maven**: 3.8.0 or higher
- **Git**

Verify your environment:
```cmd
java -version
mvn -version





3. Configuration & Database Setup







Local Configuration:
Copy the example configuration to create your local config:



copy src\main\resources\config.properties.example src\main\resources\config.properties




Note: config.properties and pos.db are git-ignored to prevent credential and state leakage.








Database Initialization:
The database schema and initial seed data are located in db/schema.sql and db/seed.sql.
On first launch, DatabaseInitializer automatically creates and populates pos.db.









4. Building and Running


Option A: Run via Maven (Development)


mvn clean compile
mvn javafx:run



Option B: Build and Run Executable Fat JAR (Production)


mvn clean package
java -jar target/pos-system-1.0-SNAPSHOT.jar





5. Automated Tests



Run the JUnit 5 test suite:



mvn test



Key Scenarios Covered (SaleServiceTest):




Successful Checkout: Validates price lookup from DB, sale insertion, line item recording, and inventory stock decrement within a single transaction.


Insufficient Stock Rollback: Verifies transaction rollback when requested quantity exceeds stock.


Insufficient Payment Validation: Verifies rejection when amount tendered is less than total.


Empty Cart Validation: Asserts empty checkout attempts are rejected before database access.


Daily Takings Aggregation: Tests revenue, sale counts, and payment method breakdowns for specific dates.






6. Seed Data & Test Barcodes


Barcode	Product Name	Category	Unit Price	Initial Stock
890123456001	Organic Milk 1L	Dairy	$2.50	50
890123456002	Whole Wheat Bread	Bakery	$1.80	40
890123456003	Fair Trade Coffee 250g	Beverages	$5.99	25
890123456004	Free Range Eggs (12pk)	Dairy	$3.20	30
890123456005	Mineral Water 500ml	Beverages	$0.99	100