-- POS System Relational Schema (3NF)

-- Table 1: Categories (for product classification)
CREATE TABLE IF NOT EXISTS categories (
    category_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    description TEXT
);

-- Table 2: Products
CREATE TABLE IF NOT EXISTS products (
    product_id INTEGER PRIMARY KEY AUTOINCREMENT,
    barcode TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    category_id INTEGER NOT NULL,
    unit_price REAL NOT NULL CHECK(unit_price >= 0),
    stock_quantity INTEGER NOT NULL DEFAULT 0 CHECK(stock_quantity >= 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE RESTRICT
);

-- Table 3: Sales (Header record for a completed checkout)
CREATE TABLE IF NOT EXISTS sales (
    sale_id INTEGER PRIMARY KEY AUTOINCREMENT,
    receipt_number TEXT NOT NULL UNIQUE,
    sale_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    subtotal REAL NOT NULL CHECK(subtotal >= 0),
    tax_amount REAL NOT NULL CHECK(tax_amount >= 0),
    discount_amount REAL NOT NULL DEFAULT 0.0 CHECK(discount_amount >= 0),
    total_amount REAL NOT NULL CHECK(total_amount >= 0),
    payment_method TEXT NOT NULL CHECK(payment_method IN ('CASH', 'CARD', 'TRANSFER'))
);

-- Table 4: Sale Items (Line items for each sale)
CREATE TABLE IF NOT EXISTS sale_items (
    sale_item_id INTEGER PRIMARY KEY AUTOINCREMENT,
    sale_id INTEGER NOT NULL,
    product_id INTEGER NOT NULL,
    quantity INTEGER NOT NULL CHECK(quantity > 0),
    unit_price REAL NOT NULL CHECK(unit_price >= 0),
    line_total REAL NOT NULL CHECK(line_total >= 0),
    FOREIGN KEY (sale_id) REFERENCES sales(sale_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE RESTRICT
);

-- Indexes for performance & search
CREATE INDEX IF NOT EXISTS idx_products_barcode ON products(barcode);
CREATE INDEX IF NOT EXISTS idx_sales_timestamp ON sales(sale_timestamp);
CREATE INDEX IF NOT EXISTS idx_sale_items_sale_id ON sale_items(sale_id);
