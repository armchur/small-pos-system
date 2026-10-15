package app.ui;

import app.dao.ProductDao;
import app.model.DailyTakingsReport;
import app.model.Product;
import app.model.Sale;
import app.model.SaleItem;
import app.service.SaleService;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class PosController {

    // Tab 1: POS & Catalog controls
    @FXML private TextField searchField;
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, String> colProdBarcode;
    @FXML private TableColumn<Product, String> colProdName;
    @FXML private TableColumn<Product, Double> colProdPrice;
    @FXML private TableColumn<Product, Integer> colProdStock;
    @FXML private Spinner<Integer> quantitySpinner;

    @FXML private TableView<SaleItem> cartTable;
    @FXML private TableColumn<SaleItem, String> colCartName;
    @FXML private TableColumn<SaleItem, Integer> colCartQty;
    @FXML private TableColumn<SaleItem, Double> colCartPrice;
    @FXML private TableColumn<SaleItem, Double> colCartTotal;

    @FXML private ComboBox<String> paymentMethodCombo;
    @FXML private Label totalAmountLabel;
    @FXML private Label statusMessageLabel;

    // Tab 2: Daily Takings Report controls
    @FXML private DatePicker reportDatePicker;
    @FXML private Label reportSalesCountLabel;
    @FXML private Label reportTotalRevenueLabel;
    @FXML private TableView<DailyTakingsReport.PaymentBreakdown> breakdownTable;
    @FXML private TableColumn<DailyTakingsReport.PaymentBreakdown, String> colMethod;
    @FXML private TableColumn<DailyTakingsReport.PaymentBreakdown, Integer> colMethodCount;
    @FXML private TableColumn<DailyTakingsReport.PaymentBreakdown, Double> colMethodSubtotal;
    @FXML private Label reportStatusLabel;

    private final ObservableList<Product> productList = FXCollections.observableArrayList();
    private final ObservableList<SaleItem> cartList = FXCollections.observableArrayList();
    private final ObservableList<DailyTakingsReport.PaymentBreakdown> breakdownList = FXCollections.observableArrayList();

    private ProductDao productDao;
    private SaleService saleService;

    public void setDependencies(ProductDao productDao, SaleService saleService) {
        this.productDao = productDao;
        this.saleService = saleService;
        loadProductCatalog();
        loadDailyTakings(LocalDate.now());
    }

    @FXML
    public void initialize() {
        // Configure Product Table
        colProdBarcode.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBarcode()));
        colProdName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colProdPrice.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getUnitPrice()).asObject());
        colProdStock.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getStockQuantity()).asObject());
        productTable.setItems(productList);

        // Configure Cart Table
        colCartName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProductName()));
        colCartQty.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getQuantity()).asObject());
        colCartPrice.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getUnitPrice()).asObject());
        colCartTotal.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getLineTotal()).asObject());
        cartTable.setItems(cartList);

        // Spinner range: 1 to 100
        quantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));

        // Payment Methods
        paymentMethodCombo.setItems(FXCollections.observableArrayList("CASH", "CARD", "TRANSFER"));
        paymentMethodCombo.setValue("CASH");

        // Tab 2: Daily Takings Report Table & DatePicker
        reportDatePicker.setValue(LocalDate.now());
        colMethod.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPaymentMethod()));
        colMethodCount.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getTransactionCount()).asObject());
        colMethodSubtotal.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getSubtotal()).asObject());
        breakdownTable.setItems(breakdownList);
    }

    @FXML
    public void handleAddToCart() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusMessageLabel.setText("Please select a product from the catalog.");
            return;
        }

        int quantity = quantitySpinner.getValue();
        if (quantity > selected.getStockQuantity()) {
            statusMessageLabel.setText("Cannot add: requested quantity exceeds available stock (" + selected.getStockQuantity() + ").");
            return;
        }

        SaleItem item = new SaleItem(selected.getProductId(), selected.getName(), quantity, selected.getUnitPrice());
        cartList.add(item);
        updateCartTotalDisplay();
        statusMessageLabel.setText("Added " + selected.getName() + " to cart.");
    }

    @FXML
    public void handleRemoveCartItem() {
        SaleItem selected = cartTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            cartList.remove(selected);
            updateCartTotalDisplay();
        }
    }

    @FXML
    public void handleClearCart() {
        cartList.clear();
        updateCartTotalDisplay();
        statusMessageLabel.setText("Cart cleared.");
    }

    @FXML
    public void handleCheckout() {
        if (cartList.isEmpty()) {
            statusMessageLabel.setText("Cart is empty.");
            return;
        }

        String paymentMethod = paymentMethodCombo.getValue();
        try {
            // Delegate checkout entirely to the Service layer
            Sale completedSale = saleService.checkout(List.copyOf(cartList), paymentMethod);

            statusMessageLabel.setText("Sale Completed! Receipt: " + completedSale.getReceiptNumber() + 
                                       " | Total: $" + String.format("%.2f", completedSale.getTotalAmount()));
            cartList.clear();
            updateCartTotalDisplay();
            loadProductCatalog(); // Refresh catalog stock display
            
            // Automatically refresh today's daily takings
            if (LocalDate.now().equals(reportDatePicker.getValue())) {
                loadDailyTakings(LocalDate.now());
            }
        } catch (IllegalArgumentException | SQLException e) {
            statusMessageLabel.setText("Checkout Failed: " + e.getMessage());
        }
    }

    @FXML
    public void handleSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            loadProductCatalog();
            return;
        }
        try {
            List<Product> results = productDao.searchByNameOrBarcode(query);
            productList.setAll(results);
        } catch (SQLException e) {
            statusMessageLabel.setText("Search error: " + e.getMessage());
        }
    }

    @FXML
    public void handleResetCatalog() {
        searchField.clear();
        loadProductCatalog();
    }

    @FXML
    public void handleGenerateReport() {
        LocalDate selectedDate = reportDatePicker.getValue();
        if (selectedDate == null) {
            reportStatusLabel.setText("Please pick a date.");
            return;
        }
        loadDailyTakings(selectedDate);
    }

    private void loadDailyTakings(LocalDate date) {
        if (saleService == null || date == null) return;
        try {
            DailyTakingsReport report = saleService.getDailyTakings(date);
            reportSalesCountLabel.setText(String.valueOf(report.getTotalSalesCount()));
            reportTotalRevenueLabel.setText("$" + String.format("%.2f", report.getTotalRevenue()));
            breakdownList.setAll(report.getPaymentBreakdowns());
            reportStatusLabel.setText("Report loaded for " + date + ".");
        } catch (IllegalArgumentException | SQLException e) {
            reportStatusLabel.setText("Error generating report: " + e.getMessage());
        }
    }

    private void loadProductCatalog() {
        if (productDao == null) return;
        try {
            List<Product> products = productDao.findAll();
            productList.setAll(products);
        } catch (SQLException e) {
            statusMessageLabel.setText("Failed to load catalog: " + e.getMessage());
        }
    }

    private void updateCartTotalDisplay() {
        double total = cartList.stream().mapToDouble(SaleItem::getLineTotal).sum();
        totalAmountLabel.setText("$" + String.format("%.2f", total));
    }
}
