-- Synthetic Seed Data for Development and Testing

INSERT OR IGNORE INTO categories (category_id, name, description) VALUES
(1, 'Beverages', 'Cold and hot drinks'),
(2, 'Snacks', 'Packaged snacks and confectionery'),
(3, 'Stationery', 'Office and school supplies');

INSERT OR IGNORE INTO products (product_id, barcode, name, category_id, unit_price, stock_quantity) VALUES
(1, '1001', 'Sparkling Water 500ml', 1, 1.50, 40),
(2, '1002', 'Orange Juice 1L', 1, 3.20, 25),
(3, '2001', 'Salted Peanuts 100g', 2, 0.90, 50),
(4, '2002', 'Dark Chocolate Bar', 2, 2.50, 30),
(5, '3001', 'A5 Spiral Notebook', 3, 4.00, 15),
(6, '3002', 'Ballpoint Pen (Blue)', 3, 0.80, 100);
