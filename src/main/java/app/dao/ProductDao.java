package app.dao;

import app.model.Product;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ProductDao {
    Optional<Product> findById(int productId) throws SQLException;
    Optional<Product> findByBarcode(String barcode) throws SQLException;
    List<Product> findAll() throws SQLException;
    List<Product> searchByNameOrBarcode(String query) throws SQLException;
    void save(Product product) throws SQLException;
    void update(Product product) throws SQLException;
    void delete(int productId) throws SQLException;

    // Method participating in external transactions
    boolean decrementStock(int productId, int quantity, Connection conn) throws SQLException;
}
