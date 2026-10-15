# Software Requirements Specification (SRS)
## Small Business Point of Sale (POS) System

**Course:** CSC 508 Structured and Object-Oriented Programming Languages II  
**Project:** Small Business Point of Sale (POS) Desktop System  
**Architecture:** 3-Tier Layered Architecture (JavaFX UI, Service Layer, SQLite DAO)  
**Target Platform:** Java 17+ LTS Desktop (Windows / macOS / Linux)

---

## 1. Introduction & Problem Statement

Small retail establishments require a dependable, low-latency, and crash-resilient desktop Point of Sale (POS) system to handle fast customer checkout, track inventory in real-time, and summarize daily takings. Commercial cloud-based POS platforms frequently introduce unnecessary complexity, ongoing subscription costs, and vulnerability to Internet outages. 

This system provides a lean, standalone desktop application built with JavaFX and SQLite that guarantees local transactional consistency, prevents stock discrepancies, and provides accurate financial reconciliation without external network dependencies.

---

## 2. Stakeholder Assumptions & Constraints

1. **Hardware & Operating Environment**: The system operates on standard retail desktop or point-of-sale terminal hardware running Windows 10/11, macOS, or Linux, equipped with a minimum of 4 GB RAM and Java 17+ JRE.
2. **Peripherals**: Product input is driven by a standard USB/Bluetooth barcode scanner configured in keyboard-wedge mode (sending alphanumeric strings followed by an Enter key) or direct manual numeric entry.
3. **Connectivity**: The application runs completely offline; all transactional data persists to a local SQLite database file (`pos.db`).
4. **Single-Register Deployment**: The application is optimized for single-terminal concurrent checkouts where atomic ACID guarantees prevent data corruption.

---

## 3. Functional Requirements (FR)

- **FR-01 (Product Catalog & Barcode Lookup)**: The system shall query products by barcode or product ID, displaying product name, unit price, and available stock level.
- **FR-02 (Cart Management)**: The system shall maintain an active shopping cart for the current transaction, allowing items to be added, quantities incremented, and real-time running subtotal, tax, and grand totals to be displayed.
- **FR-03 (Stock Availability Enforcement)**: The system shall verify available stock before and during checkout, preventing transactions that exceed on-hand inventory.
- **FR-04 (Atomic Transactional Checkout)**: The system shall record the sale header, insert individual line items, and decrement product stock within a single atomic database transaction. If any step fails, all changes must roll back completely.
- **FR-05 (Payment Validation & Change Calculation)**: The system shall validate payment method (Cash, Card) and amount tendered. For cash payments, the system shall compute change due and reject tendered amounts below the total.
- **FR-06 (Daily Takings & Reconciliation Reporting)**: The system shall aggregate total sales count, total gross revenue, and a breakdown by payment method for any selected calendar date.

---

## 4. Non-Functional Requirements (NFR)

- **NFR-01 (Architectural Separation)**: The system shall maintain strict 3-tier decoupling. Presentation controllers must contain zero business calculations, tax logic, or SQL statements.
- **NFR-02 (Data Integrity & ACID Compliance)**: SQLite foreign keys and database transactions must guarantee atomicity; partial checkouts or orphaned sale line items are strictly forbidden.
- **NFR-03 (Security & Parameterization)**: Every database query must use parameterized `PreparedStatement` objects. No dynamic string concatenation is permitted in SQL construction.
- **NFR-04 (Performance & Latency)**: Product barcode lookup and cart additions must resolve in under 50 ms. Checkout transaction commitment must complete in under 200 ms.
- **NFR-05 (Configuration Isolation)**: Database paths and environment settings must reside in an external or ignored configuration file (`config.properties`), with a tracked template (`config.properties.example`).

---

## 5. Use Cases

### UC-01: Product Lookup by Barcode
- **Primary Actor**: Cashier
- **Preconditions**: POS screen is active and database is initialized.
- **Main Flow**:
  1. Cashier scans barcode or enters numeric code (e.g., `890123456001`) into the Barcode input field.
  2. Cashier clicks **Lookup** (or presses Enter).
  3. System queries the data access layer using a parameterized query.
  4. System displays product name, unit price, and available inventory.
- **Alternate Flow (A1: Not Found)**:
  - At step 3, if no matching barcode exists, system displays an alert: `"Product not found."` and clears product details.

---

### UC-02: Add Product to Cart
- **Primary Actor**: Cashier
- **Preconditions**: Product has been successfully retrieved (UC-01).
- **Main Flow**:
  1. Cashier specifies desired quantity (default is 1).
  2. Cashier clicks **Add to Cart**.
  3. System checks requested quantity against current stock.
  4. System adds item to active cart table and updates subtotal and total balance.
- **Alternate Flow (A1: Quantity Exceeds Stock)**:
  - System detects `quantity > available_stock`, displays `"Requested quantity exceeds available stock"`, and rejects addition.

---

### UC-03: Process Checkout (Atomic Transaction)
- **Primary Actor**: Cashier
- **Preconditions**: Cart contains at least one item; total is greater than zero.
- **Main Flow**:
  1. Cashier selects payment method (Cash or Card).
  2. Cashier enters amount tendered.
  3. Cashier clicks **Complete Sale**.
  4. Service layer validates tender amount against total.
  5. Service layer opens a transactional database connection (`setAutoCommit(false)`).
  6. Service inserts `sales` master record.
  7. Service inserts `sale_items` child records.
  8. Service executes atomic stock decrement for each product.
  9. Connection commits (`commit()`).
  10. System displays change due (for cash) and resets cart.
- **Alternate Flow (A1: Stock Depleted Mid-Transaction)**:
  - If a product's stock is insufficient during write execution, service triggers `connection.rollback()`, throws `IllegalStateException`, and notifies the cashier without altering the database.

---

### UC-04: Handle Insufficient Payment Tender
- **Primary Actor**: Cashier
- **Preconditions**: Cart contains items; total balance is computed.
- **Main Flow**:
  1. Cashier enters an amount tendered less than the grand total.
  2. Cashier clicks **Complete Sale**.
  3. Service layer detects `amountTendered < totalAmount`.
  4. Service throws `IllegalArgumentException("Insufficient payment amount tendered.")`.
  5. UI displays an error dialogue indicating the required minimum amount.
  6. Cart and pending items remain unchanged.

---

### UC-05: Empty Cart Protection
- **Primary Actor**: Cashier
- **Preconditions**: POS screen active, 0 items in cart.
- **Main Flow**:
  1. Cashier accidentally clicks **Complete Sale**.
  2. Service layer validates cart size is `0`.
  3. System rejects checkout before initiating any database connection.
  4. UI alerts cashier: `"Cart is empty."`

---

### UC-06: Daily Takings & Reconciliation Report
- **Primary Actor**: Store Manager / Cashier
- **Preconditions**: At least one completed sale exists in database.
- **Main Flow**:
  1. User navigates to the **Daily Takings Report** tab.
  2. User selects a calendar date via DatePicker (defaults to current date).
  3. User clicks **Generate Report**.
  4. System executes parameterized aggregate queries:
     - Total number of transactions (`COUNT(*)`)
     - Gross revenue (`SUM(total_amount)`)
     - Grouped totals and transaction counts by payment method (`CASH`, `CARD`).
  5. System populates KPI cards and breakdowns table.
- **Alternate Flow (A1: Future Date Selected)**:
  - User selects a date beyond today.
  - Service layer rejects input: `"Report date cannot be in the future."`

---

## 6. Acceptance Criteria

| Requirement | Acceptance Test | Expected Outcome |
|---|---|---|
| **AC-01** | Enter barcode `890123456001` and click Lookup. | Product details for "Organic Milk 1L" load with unit price $2.50. |
| **AC-02** | Add 60 units of Milk when stock is 50. | System blocks addition with out-of-stock warning. |
| **AC-03** | Checkout cart totaling $10.00 with $10.00 cash. | Sale commits, stock decrements by purchased quantity, change = $0.00. |
| **AC-04** | Checkout cart totaling $10.00 with $5.00 cash. | System rejects sale, rollback verified, zero database changes. |
| **AC-05** | Simulate database write failure during item insertion. | Sale master record and inventory decrements roll back completely. |
| **AC-06** | Query Daily Takings for current date. | Total transactions, revenue, and cash/card breakdown match database records exactly. |
| **AC-07** | Query Daily Takings for tomorrow's date. | System blocks request with validation alert. |
