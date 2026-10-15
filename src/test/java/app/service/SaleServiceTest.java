package app.service;

import app.dao.DatabaseConnection;
import app.dao.SqliteProductDao;
import app.dao.SqliteSaleDao;
import app.model.DailyTakingsReport;
import app.model.Product;
import app.model.Sale;
import app.model.SaleItem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class SaleServiceTest {

    private static final String TEST_DB_FILE = "target/test_pos.db";
    private SqliteProductDao productDao;
    private SqliteSaleDao saleDao;
    private SaleService saleService;

    @BeforeEach
    void setUp() throws SQLException {
        // Point to an isolated test database in the target folder
        DatabaseConnection.setCustomUrl("jdbc:sqlite:" + TEST_DB_FILE);

        productDao = new SqliteProductDao();
        saleDao = new SqliteSaleDao();
        saleService = new SaleService(productDao, saleDao, DatabaseConnection::getConnection);

        // Create schema and seed with clean test data
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS sale_items;");
            stmt.execute("DROP TABLE IF EXISTS sales;");
            stmt.execute("DROP TABLE IF EXISTS products;");
            stmt.execute("DROP TABLE IF EXISTS categories;");

            stmt.execute("CREATE TABLE categories (category_id INTEGER PRIMARY KEY, name TEXT);");
            stmt.execute("CREATE TABLE products (" +
                    "product_id INTEGER PRIMARY KEY, " +
                    "barcode TEXT UNIQUE, " +
                    "name TEXT, " +
                    "category_id INTEGER, " +
                    "unit_price REAL, " +
                    "stock_quantity INTEGER CHECK(stock_quantity >= 0)" +
                    ");");
            stmt.execute("CREATE TABLE sales (" +
                    "sale_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "receipt_number TEXT UNIQUE, " +
                    "sale_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "subtotal REAL, " +
                    "tax_amount REAL, " +
                    "discount_amount REAL, " +
                    "total_amount REAL, " +
                    "payment_method TEXT" +
                    ");");
            stmt.execute("CREATE TABLE sale_items (" +
                    "sale_item_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "sale_id INTEGER, " +
                    "product_id INTEGER, " +
                    "quantity INTEGER, " +
                    "unit_price REAL, " +
                    "line_total REAL" +
                    ");");

            // Seed two test products: one with ample stock, one with 1 in stock
            stmt.execute("INSERT INTO products VALUES (1, 'B1', 'Test Soda', 1, 2.50, 10);");
            stmt.execute("INSERT INTO products VALUES (2, 'B2', 'Rare Chocolate', 1, 5.00, 1);");
        }
    }

    @AfterEach
    void tearDown() {
        // Clean up test DB file
        File dbFile = new File(TEST_DB_FILE);
        if (dbFile.exists()) {
            dbFile.delete();
        }
    }

    @Test
    void testSuccessfulCheckoutDecrementsStockAndCalculatesTotal() throws SQLException {
        SaleItem cartItem = new SaleItem();
        cartItem.setProductId(1);
        cartItem.setQuantity(2);

        Sale sale = saleService.checkout(List.of(cartItem), "CASH");

        assertNotNull(sale);
        assertNotNull(sale.getReceiptNumber());
        assertEquals(5.00, sale.getTotalAmount(), 0.001);
        assertEquals("CASH", sale.getPaymentMethod());

        // Verify stock decremented from 10 to 8
        Optional<Product> updatedProduct = productDao.findById(1);
        assertTrue(updatedProduct.isPresent());
        assertEquals(8, updatedProduct.get().getStockQuantity());
    }

    @Test
    void testCheckoutRollbackOnInsufficientStock() {
        // Product 2 only has 1 in stock, request 3
        SaleItem cartItem = new SaleItem();
        cartItem.setProductId(2);
        cartItem.setQuantity(3);

        // Must throw SQLException due to insufficient stock
        assertThrows(SQLException.class, () -> {
            saleService.checkout(List.of(cartItem), "CARD");
        });

        // Verify rollback: stock remains at 1
        Optional<Product> product = assertDoesNotThrow(() -> productDao.findById(2));
        assertTrue(product.isPresent());
        assertEquals(1, product.get().getStockQuantity(), "Stock should not change after aborted sale");
    }

    @Test
    void testCheckoutRejectsInvalidPaymentMethod() {
        SaleItem cartItem = new SaleItem();
        cartItem.setProductId(1);
        cartItem.setQuantity(1);

        assertThrows(IllegalArgumentException.class, () -> {
            saleService.checkout(List.of(cartItem), "BITCOIN");
        });
    }

    @Test
    void testCheckoutRejectsEmptyCart() {
        assertThrows(IllegalArgumentException.class, () -> {
            saleService.checkout(List.of(), "CASH");
        });
    }

    @Test
    void testDailyTakingsReportValidation() {
        assertThrows(IllegalArgumentException.class, () -> saleService.getDailyTakings(null));
        assertThrows(IllegalArgumentException.class, () -> saleService.getDailyTakings(LocalDate.now().plusDays(1)));
    }

    @Test
    void testDailyTakingsReportCalculations() throws Exception {
        LocalDate today = LocalDate.now();
        DailyTakingsReport reportBefore = saleService.getDailyTakings(today);
        int initialSales = reportBefore.getTotalSalesCount();

        SaleItem item = new SaleItem();
        item.setProductId(1);
        item.setQuantity(1);
        saleService.checkout(List.of(item), "CASH");

        DailyTakingsReport reportAfter = saleService.getDailyTakings(today);
        assertEquals(initialSales + 1, reportAfter.getTotalSalesCount());
        assertTrue(reportAfter.getTotalRevenue() >= reportBefore.getTotalRevenue());
    }
}
