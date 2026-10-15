# AI Interaction Log

## Entry 1: Phase 1 — Project Scaffolding & Build Configuration
- **Phase & Tool:** Phase 1 (Requirements & Setup), Lovable AI Assistant
- **Prompt:** "Set up project directory structure and configure Maven pom.xml with JavaFX 21, SQLite JDBC, and JUnit 5 targeting Java 17 for a 3-layer desktop POS application."
- **Outcome & Judgement:** Accepted with modification. Configured `<maven.compiler.release>17</maven.compiler.release>` to guarantee Java 17 LTS compatibility while building under JDK 25. Created `.gitignore` excluding `config.properties` and `*.db` to strictly satisfy credential and secret protection constraints.
## Entry 2: Phase 2 — Relational Schema Design (3NF) & Constraints
- **Phase & Tool:** Phase 2 (System Design), Lovable AI Assistant
- **Prompt:** "Design a 3NF normalized SQLite schema for the Small Business POS including categories, products, sales, and sale_items with constraints to support transactional stock decrement and receipt numbering."
- **Outcome & Judgement:** Accepted with verification. Verified that `stock_quantity >= 0` check constraint guards against negative stock at the database level. Confirmed `sales` and `sale_items` split eliminates repeating groups (1NF) and satisfies 3NF. Prepared foreign key cascades and indexes on `barcode` and `sale_timestamp` for quick lookups and daily takings queries.


Entry 3
## Entry 3: Phase 3 — Domain Model & Database Connectivity
- **Phase & Tool:** Phase 3 (Architecture & Model), Lovable AI Assistant
- **Prompt:** "Create POJO domain models for Product, Category, Sale, and SaleItem, and a DatabaseConnection manager reading config.properties with SQLite foreign key enforcement."
- **Outcome & Judgement:** Accepted with verification. Ensured `PRAGMA foreign_keys = ON;` is executed on every SQLite connection so foreign key constraints are strictly respected. Confirmed domain entities follow clean encapsulation with getters and setters.


Entry 4
## Entry 4: Phase 3 — DAO Implementation & Parameterized SQL
- **Phase & Tool:** Phase 3 (Data Access Layer), Lovable AI Assistant
- **Prompt:** "Generate ProductDao and SaleDao interfaces and SQLite implementations with 100% PreparedStatement parameterization and connection-passing for transactional writes."
- **Outcome & Judgement:** Accepted with modification. Ensured `decrementStock` uses an atomic update (`WHERE product_id = ? AND stock_quantity >= ?`) returning rows affected. If another process or previous sale exhausted stock, the row count is 0 and the calling transaction aborts immediately, preventing race conditions.
