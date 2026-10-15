# Defect & Bug Resolution Log
## Small Business Point of Sale (POS) System

**Coursework:** CSC 508 Structured and Object-Oriented Programming Languages II  
**Module:** Testing & Quality Assurance  

This log records real defects encountered during implementation, integration, and testing, detailing symptoms, root causes, corrective actions, and verification.

---

| Defect ID | Phase / Component | Severity | Description / Symptom | Root Cause | Corrective Action & Fix | Verification Status |
|---|---|---|---|---|---|---|
| **DEF-01** | UI / FXML Loading | High | `LoadException: No controller specified.` on app startup. | Root `<BorderPane>` in `pos_view.fxml` lacked `fx:controller`. | Added `fx:controller="app.ui.PosController"` to root FXML node. | **Resolved** — FXML loaded controller bindings successfully. |
| **DEF-02** | UI / FXML Binding | High | `LoadException: Invalid path` at line 75 of `pos_view.fxml`. | Label text was set to `$0.00`. FXML interprets `$` as an expression binding. | Changed text to `0.00`; format currency programmatically in controller. | **Resolved** — View rendered without expression parser errors. |
| **DEF-03** | DAO / Build | Critical | `duplicate class: app.dao.SaleDao` and `cannot find symbol getConnection()`. | Interface and implementation code were inadvertently combined during paste. | Restored clean `SaleDao.java` interface and separate `SqliteSaleDao.java`. | **Resolved** — `mvn compile` succeeded cleanly. |
| **DEF-04** | Service / Model | Medium | `cannot find symbol class CartItem` during test compilation. | Test suite referenced an unexported inner class instead of domain `SaleItem`. | Refactored `SaleServiceTest` to use standard domain model `SaleItem`. | **Resolved** — Unit tests compiled with standard domain models. |
| **DEF-05** | Service / Tests | Medium | `double cannot be dereferenced` in `SaleServiceTest.java`. | Invoked `.compareTo()` on primitive `double` values. | Replaced `.compareTo()` with `assertEquals(expected, actual, 0.001)`. | **Resolved** — All 5 unit tests passed with 0 failures. |
| **DEF-06** | Deployment / JAR | High | Standalone fat JAR failed to launch via `java -jar` with JavaFX runtime missing error. | JavaFX `Application` subclass cannot be direct main-class in an executable JAR without module flags. | Created `app.Launcher` bootstrap class calling `MainApp.main(args)` and packaged via `maven-shade-plugin`. | **Resolved** — Standalone fat JAR launches with double-click or `java -jar`. |
| **DEF-07** | Git / Security | High | Untracked database `pos.db` and build files appearing in `git status`. | Root `.gitignore` was absent, exposing local database and build state. | Created root `.gitignore` excluding `*.db`, `config.properties`, and `target/`. | **Resolved** — Verified sensitive and local files remain excluded. |
