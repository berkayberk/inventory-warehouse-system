package com.warehouse;

import com.warehouse.util.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * JavaFX application entry point for the Inventory Warehouse Information
 * System.
 *
 * <p>
 * Bootstraps the database connection, then loads the login screen.
 * On exit it cleanly closes the JDBC connection.
 * </p>
 */
public class MainApp extends Application {

    private static final Logger LOG = LogManager.getLogger(MainApp.class);

    /** Globally shared primary stage so controllers can swap scenes. */
    public static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        LOG.info("=== Inventory Warehouse IS – starting up ===");

        // Verify DB connection at startup
        DatabaseConnection.getInstance().getConnection();

        loadScene("/fxml/login.fxml", "Inventory Warehouse – Login", 480, 340);
        stage.setResizable(false);
        stage.show();
    }

    /**
     * Loads an FXML scene and sets it on the primary stage.
     *
     * @param fxmlPath classpath path to the FXML resource
     * @param title    window title
     * @param width    preferred width in pixels
     * @param height   preferred height in pixels
     */
    public static void loadScene(String fxmlPath, String title, double width, double height) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root, width, height);
            scene.getStylesheets().add(
                    MainApp.class.getResource("/css/styles.css").toExternalForm());
            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
        } catch (Exception ex) {
            LOG.error("Failed to load FXML [{}]: {}", fxmlPath, ex.getMessage(), ex);
            throw new RuntimeException("Cannot load scene: " + fxmlPath, ex);
        }
    }

    @Override
    public void stop() {
        DatabaseConnection.getInstance().close();
        LOG.info("=== Inventory Warehouse IS – shut down ===");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
