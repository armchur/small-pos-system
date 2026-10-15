package app.service;

import app.dao.ConnectionFactory;
import app.dao.ProductDao;
import app.dao.SaleDao;
import app.model.Product;
import app.model.Sale;
import app.model.SaleItem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class SaleService {
    private static final Set<String> PAYMENT_METHODS =
            Set.of("CASH", "CARD", "TRANSFER");

    private final ProductDao productDao;
    private final SaleDao saleDao;
    private final ConnectionFactory connectionFactory;

    public SaleService(
            ProductDao productDao,
            SaleDao saleDao,
            ConnectionFactory connectionFactory) {
        this.productDao = productDao;
        this.saleDao = saleDao;
        this.connectionFactory = connectionFactory;
    }

    public Sale checkout(List<SaleItem> cartItems, String paymentMethod)
            throws SQLException {
        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalArgumentException("A sale must contain at least one item.");
        }

        String normalizedPayment = paymentMethod == null
                ? ""
                : paymentMethod.trim().toUpperCase();

        if (!PAYMENT_METHODS.contains(normalizedPayment)) {
            throw new IllegalArgumentException("Choose CASH, CARD, or TRANSFER.");
        }

        Sale sale = new Sale();
        sale.setReceiptNumber("POS-" + UUID.randomUUID());
        sale.setPaymentMethod(normalizedPayment);
        sale.setTaxAmount(0.0);
        sale.setDiscountAmount(0.0);

        List<SaleItem> pricedItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (SaleItem requestedItem : cartItems) {
            if (requestedItem == null || requestedItem.getQuantity() <= 0) {
                throw new IllegalArgumentException("Each item must have a positive quantity.");
            }

            Optional<Product> productResult =
                    productDao.findById(requestedItem.getProductId());

            Product product = productResult.orElseThrow(
                    () -> new IllegalArgumentException(
                            "Product not found: " + requestedItem.getProductId()));

            BigDecimal unitPrice = BigDecimal.valueOf(product.getUnitPrice())
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal lineTotal = unitPrice
                    .multiply(BigDecimal.valueOf(requestedItem.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);

            SaleItem pricedItem = new SaleItem(
                    product.getProductId(),
                    product.getName(),
                    requestedItem.getQuantity(),
                    unitPrice.doubleValue());
            pricedItem.setLineTotal(lineTotal.doubleValue());

            pricedItems.add(pricedItem);
            subtotal = subtotal.add(lineTotal);
        }

        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);
        sale.setSubtotal(subtotal.doubleValue());
        sale.setTotalAmount(subtotal.doubleValue());
        sale.setItems(pricedItems);

        try (Connection connection = connectionFactory.open()) {
            connection.setAutoCommit(false);

            try {
                int saleId = saleDao.saveSale(sale, connection);

                for (SaleItem item : pricedItems) {
                    saleDao.saveSaleItem(item, saleId, connection);

                    boolean stockUpdated = productDao.decrementStock(
                            item.getProductId(),
                            item.getQuantity(),
                            connection);

                    if (!stockUpdated) {
                        throw new SQLException(
                                "Insufficient stock for product " + item.getProductId());
                    }
                }

                connection.commit();
                return sale;
            } catch (SQLException | RuntimeException error) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    error.addSuppressed(rollbackError);
                }
                throw error;
            }
        }
    }
}
