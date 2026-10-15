package app.dao;

import app.model.Sale;
import app.model.SaleItem;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteSaleDao implements SaleDao {

    @Override
    public int saveSale(Sale sale, Connection conn) throws SQLException {
        String sql = "INSERT INTO sales (receipt_number, subtotal, tax_amount, discount_amount, total_amount, payment_method) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, sale.getReceiptNumber());
            stmt.setDouble(2, sale.getSubtotal());
            stmt.setDouble(3, sale.getTaxAmount());
            stmt.setDouble(4, sale.getDiscountAmount());
            stmt.setDouble(5, sale.getTotalAmount());
            stmt.setString(6, sale.getPaymentMethod());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int generatedId = keys.getInt(1);
                    sale.setSaleId(generatedId);
                    return generatedId;
                }
            }
        }
        throw new SQLException("Failed to retrieve generated sale_id");
    }

    @Override
    public void saveSaleItem(SaleItem item, int saleId, Connection conn) throws SQLException {
        String sql = "INSERT INTO sale_items (sale_id, product_id, quantity, unit_price, line_total) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, saleId);
            stmt.setInt(2, item.getProductId());
            stmt.setInt(3, item.getQuantity());
            stmt.setDouble(4, item.getUnitPrice());
            stmt.setDouble(5, item.getLineTotal());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    item.setSaleItemId(keys.getInt(1));
                    item.setSaleId(saleId);
                }
            }
        }
    }

    @Override
    public Optional<Sale> findById(int saleId) throws SQLException {
        String sql = "SELECT sale_id, receipt_number, sale_timestamp, subtotal, tax_amount, discount_amount, total_amount, payment_method " +
                     "FROM sales WHERE sale_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, saleId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Sale sale = mapResultSetToSale(rs);
                    sale.setItems(findItemsBySaleId(saleId, conn));
                    return Optional.of(sale);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Sale> findSalesByDate(LocalDate date) throws SQLException {
        String sql = "SELECT sale_id, receipt_number, sale_timestamp, subtotal, tax_amount, discount_amount, total_amount, payment_method " +
                     "FROM sales WHERE DATE(sale_timestamp) = ? ORDER BY sale_timestamp DESC";
        List<Sale> sales = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, date.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sales.add(mapResultSetToSale(rs));
                }
            }
        }
        return sales;
    }

    private List<SaleItem> findItemsBySaleId(int saleId, Connection conn) throws SQLException {
        String sql = "SELECT si.sale_item_id, si.sale_id, si.product_id, p.name AS product_name, " +
                     "si.quantity, si.unit_price, si.line_total " +
                     "FROM sale_items si " +
                     "JOIN products p ON si.product_id = p.product_id " +
                     "WHERE si.sale_id = ?";
        List<SaleItem> items = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, saleId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    SaleItem item = new SaleItem();
                    item.setSaleItemId(rs.getInt("sale_item_id"));
                    item.setSaleId(rs.getInt("sale_id"));
                    item.setProductId(rs.getInt("product_id"));
                    item.setProductName(rs.getString("product_name"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getDouble("unit_price"));
                    item.setLineTotal(rs.getDouble("line_total"));
                    items.add(item);
                }
            }
        }
        return items;
    }

    private Sale mapResultSetToSale(ResultSet rs) throws SQLException {
        Sale sale = new Sale();
        sale.setSaleId(rs.getInt("sale_id"));
        sale.setReceiptNumber(rs.getString("receipt_number"));
        String tsString = rs.getString("sale_timestamp");
        if (tsString != null) {
            sale.setSaleTimestamp(LocalDateTime.parse(tsString.replace(" ", "T")));
        }
        sale.setSubtotal(rs.getDouble("subtotal"));
        sale.setTaxAmount(rs.getDouble("tax_amount"));
        sale.setDiscountAmount(rs.getDouble("discount_amount"));
        sale.setTotalAmount(rs.getDouble("total_amount"));
        sale.setPaymentMethod(rs.getString("payment_method"));
        return sale;
    }
}
