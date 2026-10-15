package app.ui;

import app.dao.DatabaseConnection;
import app.dao.DatabaseInitializer;
import app.dao.ProductDao;
import app.dao.SaleDao;
import app.dao.SqliteProductDao;
import app.dao.SqliteSaleDao;
import app.service.SaleService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Ensure database tables and synthetic seed data exist
        DatabaseInitializer.initializeDatabase();
        DatabaseInitializer.seedDatabase();

        // Instantiate DAO and Service layers
        ProductDao productDao = new SqliteProductDao();
        SaleDao saleDao = new SqliteSaleDao();
        SaleService saleService = new SaleService(productDao, saleDao, DatabaseConnection::getConnection);

        // Load FXML and inject dependencies into controller
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/pos_view.fxml"));
        Parent root = loader.load();

        PosController controller = loader.getController();
        controller.setDependencies(productDao, saleService);

        primaryStage.setTitle("Campus POS - Small Business Point of Sale");
        primaryStage.setScene(new Scene(root, 950, 650));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
