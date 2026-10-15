# Engineering Retrospective & AI Tool Reflection
## Small Business Point of Sale (POS) System

**Course:** CSC 508 Structured and Object-Oriented Programming Languages II  
**Student:** Individual Project Submission  
**Role:** Full-Stack Developer (Presentation, Service Logic, Data Access)

---

## 1. Project Retrospective

### What Went Well
- **Strict 3-Tier Layering**: The separation of `app.ui` (JavaFX), `app.service` (business logic/transactions), and `app.dao` (SQLite JDBC) prevented tight coupling. Testing the service layer in complete isolation without launching the JavaFX runtime was straightforward.
- **ACID Transaction Handling**: Managing the JDBC connection boundary within `SaleService` using `setAutoCommit(false)`, `commit()`, and `rollback()` reliably prevented orphaned line items or stock discrepancies.
- **Fast Local Feedback**: SQLite provided a zero-configuration, robust embedded database that accelerated development and testing without requiring external database servers.

### What the Initial Design Got Wrong & How It Evolved
- **UI Architecture**: The initial single-pane layout was insufficient for business daily operations. It was expanded into a 2-tab interface supporting both active cashier checkout and administrative Daily Takings reporting.
- **Packaging Strategy**: Standard Maven compilation did not account for JavaFX runtime requirements in a shaded fat JAR. Introducing `app.Launcher` and the `maven-shade-plugin` resolved this.

---

## 2. AI Tool Reflection (Critical Evaluation)

Throughout this project, AI assistance was used for scaffolding, boilerplate generation, and debugging. Under Section 8 of the CSC 508 specification, this reflection critically examines where the tools succeeded and where they misled the implementation.

### Where the AI Tools Succeeded
1. **Scaffolding Repetitive Boilerplate**: Rapidly generated 3NF SQL schemas, domain POJOs (`Product`, `Sale`, `SaleItem`), and parameterized DAO method structures.
2. **JUnit Test Case Design**: Formulated boundary test cases (e.g., verifying rollback when requested quantity exceeds stock) and setting up isolated test database fixtures.
3. **Transaction Architecture**: Accurately guided the placement of connection boundaries so that multiple DAO operations execute under a single transaction commit/rollback context.

### Where the AI Tools Misled & Required Engineering Intervention
1. **FXML Expression Parser Bug (DEF-02)**: The AI generated FXML containing `text="$0.00"`. JavaFX interprets a leading `$` as an FXML expression or resource bundle key, crashing the application with `IllegalArgumentException: Invalid path`. Resolving this required manual diagnosis to recognize the FXML syntax rule and change the static label to `0.00`.
2. **JavaFX Fat JAR Execution (DEF-06)**: Early build instructions assumed `java -jar` would launch the `MainApp` class directly. The AI initially overlooked JavaFX's restriction requiring a separate bootstrap class (`Launcher.java` without extending `Application`) to prevent runtime module errors.
3. **Compilation & Context Mismatches (DEF-03, DEF-04)**: During iterative prompts, the AI generated code snippets that accidentally duplicated existing class declarations or referenced non-existent DTO types (`CartItem` instead of `SaleItem`). As the human engineer, I had to review compiler diagnostics, reject hallucinated types, and restore proper package boundaries.

---

## 3. Plan for the Next Iteration

If this system were extended for commercial production:
1. **Barcode Hardware Integration**: Implement direct serial/USB event listeners for hardware barcode scanners and thermal receipt printers.
2. **User Roles & Authentication**: Introduce Cashier vs. Manager role-based access control (RBAC) to restrict Daily Takings reports to supervisory personnel.
3. **Refunds & Returns**: Implement a transactional return workflow that increments stock levels and records negative sale adjustments.
