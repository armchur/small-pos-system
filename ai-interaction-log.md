# AI Interaction Log

**Course:** CSC 508 — Structured and Object-Oriented Programming Languages II  
**Project:** Small Business Point of Sale (POS) System  
**Repository:** https://github.com/armchur/small-pos-system.git  

---

## Entry 1: Phase 1 — Project Scaffolding & Build Configuration
- **Phase & Tool:** Phase 1 (Requirements & Setup), Lovable AI Assistant
- **Prompt:** "Set up project directory structure and configure Maven pom.xml with JavaFX 21, SQLite JDBC, and JUnit 5 targeting Java 17 for a 3-layer desktop POS application."
- **Outcome & Judgement:** Accepted with modification. Configured `<maven.compiler.release>17</maven.compiler.release>` to guarantee Java 17 LTS compatibility while building under JDK 25. Created `.gitignore` excluding `config.properties` and `*.db` to strictly satisfy credential and secret protection constraints.

---

## Entry 2: Phase 2 — Relational Schema Design (3NF) & Constraints
- **Phase & Tool:** Phase 2 (System Design), Lovable AI Assistant
- **Prompt:** "Design a 3NF normalized SQLite schema for the Small Business POS including categories, products, sales, and sale_items with constraints to support transactional stock decrement and receipt numbering."
- **Outcome & Judgement:** Accepted with verification. Verified that `stock_quantity >= 0` check constraint guards against negative stock at the database level. Confirmed `sales` and `sale_items` split eliminates repeating groups (1NF) and satisfies 3NF. Prepared foreign key cascades and indexes on `barcode` and `sale_timestamp` for quick lookups and daily takings queries.

---

## Entry 3: Phase 3 — Domain Model & Database Connectivity
- **Phase & Tool:** Phase 3 (Architecture & Model), Lovable AI Assistant
- **Prompt:** "Create POJO domain models for Product, Category, Sale, and SaleItem, and a DatabaseConnection manager reading config.properties with SQLite foreign key enforcement."
- **Outcome & Judgement:** Accepted with verification. Ensured `PRAGMA foreign_keys = ON;` is executed on every SQLite connection so foreign key constraints are strictly respected. Confirmed domain entities follow clean encapsulation with getters and setters.

---

## Entry 4: Phase 3 — DAO Implementation & Parameterized SQL
- **Phase & Tool:** Phase 3 (Data Access Layer), Lovable AI Assistant
- **Prompt:** "Generate ProductDao and SaleDao interfaces and SQLite implementations with 100% PreparedStatement parameterization and connection-passing for transactional writes."
- **Outcome & Judgement:** Accepted with modification. Ensured `decrementStock` uses an atomic update (`WHERE product_id = ? AND stock_quantity >= ?`) returning rows affected. If another process or previous sale exhausted stock, the row count is 0 and the calling transaction aborts immediately, preventing race conditions.

---

## Entry 5: Phase 4 — Service Layer Business Rules & Automated Testing
- **Phase & Tool:** Phase 4 (Testing & Quality), Lovable AI Assistant
- **Prompt:** "Implement SaleService with transactional checkout and JUnit 5 test suite verifying calculations, stock decrement, and rollback on insufficient stock."
- **Outcome & Judgement:** Accepted with verification. Verified via automated tests that when requested quantity exceeds available inventory, an exception is thrown, the transaction rolls back, and stock remains untouched. Confirmed that pricing is sourced from the database rather than user input.

---

## Entry 6: Phase 3 — JavaFX UI Vertical Slice & FXML Controller Binding
- **Phase & Tool:** Phase 3 (Presentation Layer), Lovable AI Assistant
- **Prompt:** "Generate initial FXML layout for POS screen with product catalog, cart TableView, and checkout panel."
- **Outcome & Judgement:** Rejected & Corrected. Initial application launch failed with `javafx.fxml.LoadException: No controller specified.` Reviewed the FXML root element and discovered `fx:controller="app.ui.PosController"` was omitted from the `<BorderPane>` declaration. Added the controller binding explicitly and injected DAOs/Services via `MainApp`, preserving inversion of control without instantiation inside the controller.

---

## Entry 7: Phase 3 — FXML Variable Binding Syntax Bugfix
- **Phase & Tool:** Phase 3 (Frontend / JavaFX), Lovable AI Assistant
- **Prompt:** "Fix JavaFX runtime FXML load error: KeyPath.parse Invalid path on pos_view.fxml line 75."
- **Outcome & Judgement:** Modified & Corrected. Diagnosed that JavaFX FXML interprets the `$` prefix as an expression evaluation operator (`KeyPath`). The string `text="$0.00"` was misparsed as a dynamic property binding to an invalid path `0.00`. Corrected the text literal to `0.00` and updated `maxWidth` to a numeric value `1000`, resolving the FXML loader exception.

---

## Entry 8: Phase 2/3 — Configuration & Secret Separation (.gitignore Hygiene)
- **Phase & Tool:** Phase 3 (Security & Configuration), Lovable AI Assistant
- **Prompt:** "Review git status to ensure database credentials and runtime artifacts are properly excluded before initial push."
- **Outcome & Judgement:** Modified. Tool observed that `pos.db`, `config.properties`, and `.github/` were showing as untracked files. Root `.gitignore` was missing. Created a comprehensive root `.gitignore` explicitly covering `target/`, `*.db`, `config.properties`, and local automation scripts. Checked in `config.properties.example` as a template for other team members and evaluators.

---

## Entry 9: Phase 3 — Daily Takings Report DTO & Parameterized DAO Query
- **Phase & Tool:** Phase 3 (Data Access Layer), Lovable AI Assistant
- **Prompt:** "Design DailyTakingsReport model and add getDailyTakings(LocalDate date) method to SaleDao and SqliteSaleDao to compute daily revenue and payment method breakdown."
- **Outcome & Judgement:** Accepted with modification. Draft query initially attempted date string concatenation. Refactored to use SQLite's native `DATE(sale_timestamp) = ?` with parameter placeholder `ps.setString(1, date.toString())`. Structured two isolated queries inside a single connection: one for aggregate summary (`COUNT`, `COALESCE(SUM(total_amount), 0.0)`) and one for breakdown (`GROUP BY payment_method ORDER BY payment_method ASC`).

---

## Entry 10: Phase 3 — Service Layer Daily Takings Validation
- **Phase & Tool:** Phase 3 (Business Logic Layer), Lovable AI Assistant
- **Prompt:** "Expose daily takings report generation in SaleService with boundary and defensive validation rules."
- **Outcome & Judgement:** Accepted with verification. Implemented defensive validation in `SaleService.getDailyTakings`: throws `IllegalArgumentException` on null dates or future dates (`date.isAfter(LocalDate.now())`). Confirmed no presentation concerns or formatting leaked into the service layer.

---

## Entry 11: Phase 4 — Daily Takings Unit Test Implementation & Type Correction
- **Phase & Tool:** Phase 4 (Automated Testing), Lovable AI Assistant
- **Prompt:** "Add unit tests to SaleServiceTest verifying date boundary validation and report totals after completed checkout."
- **Outcome & Judgement:** Modified & Corrected. Initial AI test draft contained compilation errors: it attempted calling `.compareTo()` directly on a primitive `double` (`double cannot be dereferenced`) and referenced a non-existent inner class `SaleService.CartItem`. Corrected the test to instantiate `SaleItem` domain objects directly and used `assertTrue(reportAfter.getTotalRevenue() >= reportBefore.getTotalRevenue())`, restoring full test execution.

---

## Entry 12: Phase 3 — Compiler Error Recovery & Cross-File Paste Correction
- **Phase & Tool:** Phase 3 (Implementation / Debugging), Lovable AI Assistant
- **Prompt:** "Diagnose compilation failure: duplicate class SqliteSaleDao, SqliteSaleDao declared in SqliteProductDao.java, and already defined getDailyTakings."
- **Outcome & Judgement:** Corrected. Detected that `SqliteSaleDao` implementation code had been accidentally pasted into `SqliteProductDao.java`, and `getDailyTakings` had been pasted twice in `SaleService.java`. Leveraged Git to restore `SqliteProductDao.java` to its committed state, eliminated the duplicate method declaration in `SaleService.java`, and ensured every class matched its designated compilation unit.

---

## Entry 13: Phase 3 — Presentation Layer Tabbed Refactoring
- **Phase & Tool:** Phase 3 (Frontend / JavaFX), Lovable AI Assistant
- **Prompt:** "Refactor pos_view.fxml and PosController to introduce a TabPane separating Point of Sale checkout from Daily Takings reporting."
- **Outcome & Judgement:** Accepted with verification. Ensured strict adherence to Section 5: `PosController` contains zero SQL, zero arithmetic, and zero business logic. It reads `reportDatePicker.getValue()`, delegates directly to `saleService.getDailyTakings(date)`, and maps returned POJO attributes directly to UI TableView columns and KPI cards.

---

## Entry 14: Phase 4 — Test Isolation with Ephemeral SQLite Databases
- **Phase & Tool:** Phase 4 (Testing & Quality), Lovable AI Assistant
- **Prompt:** "Configure SaleServiceTest to run against an isolated test SQLite database without polluting the production pos.db."
- **Outcome & Judgement:** Accepted with verification. Configured `@BeforeEach` to switch connection URL dynamically to `target/test_pos.db`, initialize clean schema tables, and insert isolated test fixtures. Configured `@AfterEach` to delete the test database file. Confirmed all 6 automated test cases pass with complete idempotence and zero side effects on production data.

---

## Entry 15: Phase 5 — Branch & Repository Tracking Hygiene
- **Phase & Tool:** Phase 5 (Version Control & Deployment), Lovable AI Assistant
- **Prompt:** "Resolve git remote origin collision and push main branch to team GitHub repository."
- **Outcome & Judgement:** Accepted with verification. Diagnosed `error: remote origin already exists` when re-adding the remote. Executed `git remote set-url origin https://github.com/armchur/small-pos-system.git`, set upstream tracking with `git push -u origin main`, and verified that all source, schema, test, and documentation files are cleanly synchronized to GitHub.
