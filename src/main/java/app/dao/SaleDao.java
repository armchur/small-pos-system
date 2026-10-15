package app.dao;

import app.model.DailyTakingsReport;
import app.model.Sale;
import app.model.SaleItem;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SaleDao {
    int saveSale(Sale sale, Connection conn) throws SQLException;
    void saveSaleItem(SaleItem item, int saleId, Connection conn) throws SQLException;
    Optional<Sale> findById(int saleId) throws SQLException;
    List<Sale> findSalesByDate(LocalDate date) throws SQLException;
    DailyTakingsReport getDailyTakings(LocalDate date) throws SQLException;
}
