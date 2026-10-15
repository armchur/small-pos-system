package app.dao;

import app.model.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteProductDao implements ProductDao {

    @Override
    public Optional<Product> findById(int productId) throws SQLException {
        String sql = "SELECT product_id, barcode, name, category_id, unit_price, stock_quantity " +
                    "FROM products WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProduct(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Product> findByBarcode(String barcode) throws SQLException {
        String sql = "SELECT product_id, barcode, name, category_id, unit_price, stock_quantity " +
                    "FROM products WHERE barcode = ?";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, barcode);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProduct(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Product> findAll() throws SQLException {
        String sql = "SELECT product_id, barcode, name, category_id, unit_price, stock_quantity " +
                    "FROM products ORDER BY name ASC";
        List<Product> products = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        }
        return products;
    }

    @Override
    public List<Product> searchByNameOrBarcode(String query) throws SQLException {
        String sql = "SELECT product_id, barcode, name, category_id, unit_price, stock_quantity " +
                    "FROM products WHERE name LIKE ? OR barcode LIKE ? ORDER BY name ASC";
        List<Product> products = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            String wildcard = "%" + query + "%";
            stmt.setString(1, wildcard);
            stmt.setString(2, wildcard);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        }
        return products;
    }

    @Override
    public void save(Product product) throws SQLException {
        String sql = "INSERT INTO products (barcode, name, category_id, unit_price, stock_quantity) " +
                    "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, product.getBarcode());
            stmt.setString(2, product.getName());
            stmt.setInt(3, product.getCategoryId());
            stmt.setDouble(4, product.getUnitPrice());
            stmt.setInt(5, product.getStockQuantity());
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    product.setProductId(generatedKeys.getInt(1));
                }
            }
        }
    }

    @Override
    public void update(Product product) throws SQLException {
        String sql = "UPDATE products SET barcode = ?, name = ?, category_id = ?, unit_price = ?, stock_quantity = ? " +
                    "WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, product.getBarcode());
            stmt.setString(2, product.getName());
            stmt.setInt(3, product.getCategoryId());
            stmt.setDouble(4, product.getUnitPrice());
            stmt.setInt(5, product.getStockQuantity());
            stmt.setInt(6, product.getProductId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(int productId) throws SQLException {
        String sql = "DELETE FROM products WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            stmt.executeUpdate();
        }
    }

    @Override
    public boolean decrementStock(int productId, int quantity, Connection conn) throws SQLException {
        // Enforces that stock must be >= quantity at update time
        String sql = "UPDATE products SET stock_quantity = stock_quantity - ? " +
                    "WHERE product_id = ? AND stock_quantity >= ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantity);
            stmt.setInt(2, productId);
            stmt.setInt(3, quantity);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        return new Product(
                rs.getInt("product_id"),
                rs.getString("barcode"),
                rs.getString("name"),
                rs.getInt("category_id"),
                rs.getDouble("unit_price"),
                rs.getInt("stock_quantity")
        );
    }
}
