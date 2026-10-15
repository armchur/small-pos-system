package app;

import app.ui.MainApp;

/**
 * Bootstrap launcher for the standalone executable JAR.
 * Avoids JavaFX module path runtime issues when launched via 'java -jar'.
 */
public class Launcher {
    public static void main(String[] args) {
        MainApp.main(args);
    }
}
