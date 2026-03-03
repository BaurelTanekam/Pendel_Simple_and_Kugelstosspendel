import javafx.stage.Stage;
import util.Logger;
import view.MainWindow;
import view.SimulationConfig;
import view.StartupDialog;

public class SimulationStarter {
    public void launchSimulation(Stage primaryStage) {
        Logger logger = Logger.getInstance();
        logger.logInfo("=== Pendelsimulation gestartet ===");
        logger.logInfo("JavaFX initialized successfully");

        StartupDialog dialog = new StartupDialog();
        dialog.showAndWait();

        SimulationConfig config = dialog.getConfig();

        if (config == null) {
            logger.logInfo("User cancelled startup dialog. Exiting.");
            logger.close();
            System.exit(0);
            return;
        }

        logConfiguration(logger, config);

        MainWindow mainWindow = new MainWindow(config);
        mainWindow.show();

        logger.logInfo("Main window opened. Simulation ready.");

        mainWindow.setOnCloseRequest(e -> {
            logger.logInfo("Application closing");
            logger.close();
        });
    }

    private void logConfiguration(Logger logger, SimulationConfig config) {
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
    }
}
