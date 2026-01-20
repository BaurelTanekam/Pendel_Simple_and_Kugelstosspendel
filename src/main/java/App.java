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
        SimulationStarter starter = new SimulationStarter();
        starter.launchSimulation(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
