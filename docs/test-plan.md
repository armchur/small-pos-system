# System Test Plan & Execution Evidence
## Small Business Point of Sale (POS) System

**Course:** CSC 508 Structured and Object-Oriented Programming Languages II  
**Suite:** Automated Unit & Service Integration Tests (JUnit 5)

---

## 1. Test Strategy & Scope

Testing focuses on the **Service Layer** (`app.service.SaleService`), validating:
1. Business rule enforcement (price lookups, stock availability, payment validation).
2. Multi-write transaction atomicity (all-or-nothing database commit and rollback).
3. Data integrity of the Daily Takings reconciliation reports.

Unit tests utilize an isolated test database (`target/test_pos.db`) initialized with synthetic schema and seed fixtures before execution to ensure test independence.

---

## 2. Test Cases & Requirements Traceability

| Test Case ID | Target Method | Requirement Tested | Input Scenario | Expected Outcome | Execution Result |
|---|---|---|---|---|---|
| **TC-SRV-01** | `SaleService.checkout()` | FR-03, FR-04 | 2 items of Product #1 (Stock: 50). Cash paid: $10.00 (Total: $5.00). | Sale record created, line items saved, product stock decremented to 48, change = $5.00. | **PASSED** |
| **TC-SRV-02** | `SaleService.checkout()` | FR-03, FR-04 (Rollback) | Request 100 units of Product #1 (Stock: 50). | Throws `IllegalStateException`. Transaction rolls back; stock remains 50; 0 sale records created. | **PASSED** |
| **TC-SRV-03** | `SaleService.checkout()` | FR-05 (Payment Validation) | Total = $5.00; Cash tendered = $3.00. | Throws `IllegalArgumentException("Insufficient payment")`. Database remains untouched. | **PASSED** |
| **TC-SRV-04** | `SaleService.checkout()` | FR-02 (Empty Cart) | Empty `List<SaleItem>` passed to checkout. | Throws `IllegalArgumentException("Cart is empty")`. No database connection opened. | **PASSED** |
| **TC-SRV-05** | `SaleService.getDailyTakings()` | FR-06 (Report Validation) | Query report passing a future date (`LocalDate.now().plusDays(1)`). | Throws `IllegalArgumentException("Report date cannot be in the future")`. | **PASSED** |
| **TC-SRV-06** | `SaleService.getDailyTakings()` | FR-06 (Calculations) | Query report for date with 2 completed sales ($5.00 cash, $10.00 card). | Total sales = 2, total revenue = $15.00, breakdown matches cash and card subsets. | **PASSED** |

---

## 3. Automated Test Execution Evidence



[INFO] ------------------------------------------------------- [INFO] T E S T S [INFO] ------------------------------------------------------- [INFO] Running app.service.SaleServiceTest [INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.421 s -- in app.service.SaleServiceTest [INFO] [INFO] Results: [INFO] [INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0 [INFO] [INFO] ------------------------------------------------------------------------ [INFO] BUILD SUCCESS [INFO] ------------------------------------------------------------------------