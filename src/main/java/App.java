import javafx.application.Application;
import javafx.stage.Stage;
import simulation.EulerIntegrator;
import simulation.Integrator;
import model.Pendulum;
import simulation.SimulationEngine;
import util.Logger;
import view.MainWindow;
import view.SimulationConfig;
import view.StartupDialog;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Logger initialisieren
        Logger logger = Logger.getInstance();
        logger.logInfo("=== Pendelsimulation (Phase 2) gestartet ===");
        logger.logInfo("JavaFX initialized successfully");

        // Zeige Startdialog
        StartupDialog dialog = new StartupDialog();
        dialog.showAndWait();

        // Hole Konfiguration
        SimulationConfig config = dialog.getConfig();

        if (config == null) {
            // Benutzer hat abgebrochen
            logger.logInfo("User cancelled startup dialog. Exiting.");
            logger.close();
            System.exit(0);
            return;
        }

        // Logge die gewählte Konfiguration
        logger.logInfo("Configuration selected:");
        logger.logInfo("  Mode: " + (config.istKugelstosspendel ? "Newton's Cradle" : "Single Pendulum"));
        logger.logInfo("  Mass: " + config.masse + " kg");
        logger.logInfo("  Radius: " + config.radius + " m");
        logger.logInfo("  Start angle: " + config.startWinkelGrad + "°");
        if (config.istKugelstosspendel) {
            logger.logInfo("  Number of pendulums: " + config.anzahlKugeln);
            logger.logInfo("  Deflected: " + config.anzahlAusgelenkt);
            logger.logInfo("  Restitution coefficient: " + config.restitutionskoeffizient);
        }

        // Erstelle und zeige Hauptfenster
        MainWindow mainWindow = new MainWindow(config);
        mainWindow.show();

        logger.logInfo("Main window opened. Simulation ready.");

        // Cleanup beim Schließen
        mainWindow.setOnCloseRequest(e -> {
            logger.logInfo("Application closing");
            logger.close();
        });
    }

    public static void main(String[] args) {
        //runPhase1TestSimulation();
        //Driver.runPhase1TestSimulation();
        launch(args);
    }

}
