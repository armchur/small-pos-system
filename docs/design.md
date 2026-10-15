# System Design Document
## Small Business Point of Sale (POS) System

**Course:** CSC 508 Structured and Object-Oriented Programming Languages II  
**Project:** Small Business Point of Sale (POS) Desktop System  
**Pattern:** 3-Tier Layered Architecture with DAO & Data Transfer Objects (DTO)

---

## 1. High-Level Architectural Design

The application follows a strict 3-tier architectural decomposition:

+------------------------------------------------------------------------+
|                       PRESENTATION LAYER (UI)                          |
|  - app.ui.MainApp               (JavaFX Application Entry)             |
|  - app.ui.PosController         (Event handling, UI updates, FXML)     |
|  - pos_view.fxml / style.css    (Declarative layout and styling)       |
+-----------------------------------+------------------------------------+
| Calls service methods (no SQL)
v
+------------------------------------------------------------------------+
|                       BUSINESS LOGIC LAYER (SERVICE)                   |
|  - app.service.SaleService      (Business rules, validation, totals)   |
|  - ConnectionFactory            (Transaction boundary coordination)    |
|  - app.model.*                  (Domain entities: Sale, Product, etc.) |
+-----------------------------------+------------------------------------+
| Delegates persistence via DAO
v
+------------------------------------------------------------------------+
|                       DATA ACCESS LAYER (DAO)                          |
|  - app.dao.ProductDao           (Interface for product queries/stock)  |
|  - app.dao.SqliteProductDao     (JDBC implementation: PreparedStmts)   |
|  - app.dao.SaleDao              (Interface for sales & reports)        |
|  - app.dao.SqliteSaleDao        (JDBC implementation: PreparedStmts)   |
|  - app.dao.DatabaseConnection   (Connection lifecycle & config)        |
+-----------------------------------+------------------------------------+
| JDBC Driver (xerial sqlite-jdbc)
v
+------------------------------------------------------------------------+
|                       PERSISTENCE STORAGE                              |
|  - pos.db                       (Normalized SQLite database)           |
+------------------------------------------------------------------------+




### Architectural Principles:
1. **Controller Isolation**: `PosController` never imports `java.sql.*` or performs arithmetic calculations on money.
2. **Transactional Boundary in Service**: The service layer controls `setAutoCommit(false)`, `commit()`, and `rollback()`, passing the managed `Connection` to the transactional DAO methods.
3. **Pure Parameterization**: All SQL statements use `?` placeholders.

---

## 2. Relational Database Design (3NF)

The database schema is normalized to **Third Normal Form (3NF)**:
- **1NF**: Atomic attributes, distinct column names, primary keys defined.
- **2NF**: No partial functional dependencies on composite keys.
- **3NF**: No transitive dependencies (e.g., category description is isolated in `categories` rather than duplicated in `products`).





+--------------------+       +------------------------------------+
|     categories     |       |              products              |
+--------------------+       +------------------------------------+
| id (PK) INTEGER    |<------| id (PK) INTEGER                    |
| name VARCHAR(50)   | 1   * | category_id (FK) INTEGER           |
| description TEXT   |       | barcode VARCHAR(30) UNIQUE         |
+--------------------+       | name VARCHAR(100)                  |
| unit_price DECIMAL(10,2)           |
| stock_quantity INTEGER             |
+------------------------------------+
^
| 1
| *
+--------------------+       +------------------------------------+
|       sales        |       |             sale_items             |
+--------------------+       +------------------------------------+
| id (PK) INTEGER    |<------| id (PK) INTEGER                    |
| sale_timestamp TEXT| 1   * | sale_id (FK) INTEGER               |
| total_amount REAL  |       | product_id (FK) INTEGER            |
| payment_method TEXT|       | quantity INTEGER                   |
| amount_tendered REAL       | unit_price REAL                    |
| change_due REAL    |       | subtotal REAL                      |
+--------------------+       +------------------------------------+




### Schema DDL Summary:
- **`categories`**: `id`, `name`, `description`.
- **`products`**: `id`, `category_id` references `categories(id)`, `barcode` (Indexed), `name`, `unit_price`, `stock_quantity`.
- **`sales`**: `id`, `sale_timestamp`, `total_amount`, `payment_method` (`CHECK IN ('CASH', 'CARD')`), `amount_tendered`, `change_due`.
- **`sale_items`**: `id`, `sale_id` references `sales(id)`, `product_id` references `products(id)`, `quantity`, `unit_price`, `subtotal`.

---

## 3. Sequence Flow: Transactional Checkout

This diagram illustrates how ACID transactional integrity is enforced across multiple table operations:





PosController            SaleService              SqliteSaleDao          SqliteProductDao        pos.db
|                        |                         |                       |                   |
|-- checkout(cart, pay)->|                         |                       |                   |
|                        |-- setAutoCommit(false)->|                       |------------------>|
|                        |                         |                       |                   |
|                        |-- insertSale(conn)----->|------------------------------------------>|
|                        |<-- return saleId -------|                       |                   |
|                        |                                                 |                   |
|                        |-- [Loop each CartItem]                          |                   |
|                        |-- insertSaleItem(conn, saleId, item)----------->|------------------>|
|                        |-- decrementStock(conn, productId, qty)--------->|------------------>|
|                        |                                                 |                   |
|                        |-- [If any write fails or stock < 0]             |                   |
|                        |   connection.rollback() ------------------------------------------->|
|                        |   throw IllegalStateException                   |                   |
|                        |                                                 |                   |
|                        |-- [If all writes succeed]                       |                   |
|                        |   connection.commit() --------------------------------------------->|
|<-- return Sale --------|                         |                       |                   |




---

## 4. UI Layout & View Structure

The JavaFX interface is defined in `pos_view.fxml` and organized into two functional tabs:

1. **Tab 1: Point of Sale Checkout (`tabPos`)**
   - **Header**: Terminal Branding and Operational Status.
   - **Product Entry Pane**: Barcode `TextField`, "Lookup" `Button`, Product Name, Unit Price, and Current Stock indicators.
   - **Cart TableView**: Columns for Product Name, Unit Price, Quantity, Subtotal, and Action.
   - **Payment & Totals Summary**: Subtotal, Tax (0.00), Grand Total, Payment Method Selector (`ComboBox`), Amount Tendered (`TextField`), Change Due Label, and **Complete Sale** action button.

2. **Tab 2: Daily Takings Report (`tabReport`)**
   - **Header Bar**: Date selection (`DatePicker`) and **Generate Report** action button.
   - **KPI Cards**: Total Transactions Count (`lblReportTotalSales`), Total Revenue Display (`lblReportTotalRevenue`).
   - **Payment Method Breakdown TableView**: Columns for Payment Method (`Cash`, `Card`), Number of Transactions, and Aggregated Revenue.

---

## 5. Computational Complexity & Data Structure Analysis

| Component / Method | Primary Data Structure | Time Complexity | Space Complexity | Justification |
|---|---|---|---|---|
| **Product Lookup by Barcode** | B-Tree index on SQLite `barcode` | $O(\log N)$ | $O(1)$ | Indexed lookup on `products(barcode)` avoids full table scan. |
| **Cart Storage** | `ObservableList<CartItem>` / `ArrayList` | $O(1)$ append, $O(K)$ search | $O(K)$ | In-memory cart holds $K$ distinct line items (typically $K < 50$). |
| **Cart Total Calculation** | Linear iteration over cart | $O(K)$ | $O(1)$ | Single pass accumulating `quantity * unitPrice`. |
| **Checkout Execution** | JDBC Batch / Sequential Statements | $O(K \log N)$ | $O(K)$ | $K$ line item inserts and $K$ index-backed inventory updates inside one transaction lock. |
| **Daily Takings Query** | B-Tree index on `sale_timestamp` + Aggregate | $O(M)$ | $O(1)$ | Scans $M$ transactions for the target calendar date and aggregates sums. |




