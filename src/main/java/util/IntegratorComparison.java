package util;

import model.Pendulum;
import simulation.EulerIntegrator;
import simulation.HeunIntegrator;
import simulation.Integrator;
import simulation.SimulationEngine;

import java.util.ArrayList;
import java.util.List;

/**
        * IntegratorComparison - Ein Werkzeug zum Vergleich verschiedener Integratoren.
        *
        * Diese Klasse ermöglicht es, zwei identische Pendel parallel mit verschiedenen
 * Integratoren zu simulieren und die Unterschiede zu analysieren.
        *
        * Typische Vergleichskriterien:
        * - Energieabweichung über die Zeit
 * - Positions- und Winkelabweichung
 * - Rechenzeit pro Schritt
 * - Stabilität bei verschiedenen Zeitschritten
 *
         * Verwendung:
        * 1. Erstelle zwei identische Pendel
 * 2. Simuliere beide parallel
 * 3. Sammle Daten über Energieabweichung
 * 4. Visualisiere die Ergebnisse
 */
public class IntegratorComparison {
    private final double length = 1.0;
    private final double mass = 1.0;
    private final double radius = 0.025;
    private final double initialTheta = Math.toRadians(10.0);
    private final double duration = 20.0;
    private final double deltaT = 0.001;
    private final double sampleInterval = 0.1;

    public void runComparison() {
        System.out.println("Starte Integrator-Vergleich...\n");

        List<ComparisonResult> results = performComparison();

        String report = generateReport(results);
        System.out.println(report);

        printDetailedData(results);
    }

    private List<ComparisonResult> performComparison() {
        Pendulum eulerPendulum = new Pendulum(length, mass, radius, initialTheta, 0.0, false, 0.0);
        Pendulum heunPendulum = new Pendulum(length, mass, radius, initialTheta, 0.0, false, 0.0);

        SimulationEngine eulerEngine = new SimulationEngine(eulerPendulum, new EulerIntegrator(), deltaT);
        SimulationEngine heunEngine = new SimulationEngine(heunPendulum, new HeunIntegrator(), deltaT);

        double initialEnergy = eulerPendulum.getTotalEnergy();
        List<ComparisonResult> results = new ArrayList<>();

        int totalSteps = (int) (duration / deltaT);
        int stepsPerSample = (int) (sampleInterval / deltaT);

        for (int step = 0; step <= totalSteps; step++) {
            if (step % stepsPerSample == 0) {
                results.add(new ComparisonResult(
                        step * deltaT,
                        eulerPendulum.getTotalEnergy(),
                        heunPendulum.getTotalEnergy(),
                        initialEnergy,
                        eulerPendulum.getState().getTheta(),
                        heunPendulum.getState().getTheta()
                ));
            }

            if (step < totalSteps) {
                eulerEngine.step();
                heunEngine.step();
            }
        }
        return results;
    }
    /**
     * Datenklasse für Vergleichsergebnisse.
     */
    public static class ComparisonResult {
        public double time;
        public double eulerEnergy;
        public double heunEnergy;
        public double eulerEnergyError;
        public double heunEnergyError;
        public double eulerTheta;
        public double heunTheta;

        public ComparisonResult(double time, double eulerEnergy, double heunEnergy,
                                double initialEnergy, double eulerTheta, double heunTheta) {
            this.time = time;
            this.eulerEnergy = eulerEnergy;
            this.heunEnergy = heunEnergy;
            this.eulerEnergyError = Math.abs((eulerEnergy - initialEnergy) / initialEnergy) * 100.0;
            this.heunEnergyError = Math.abs((heunEnergy - initialEnergy) / initialEnergy) * 100.0;
            this.eulerTheta = eulerTheta;
            this.heunTheta = heunTheta;
        }
    }

    /**
     * Führt einen Vergleich zwischen Euler und Heun durch.
     *
     * @param length Pendellänge in Metern
     * @param mass Masse in Kilogramm
     * @param radius Kugelradius in Metern
     * @param initialTheta Startwinkel in Radiant
     * @param duration Simulationsdauer in Sekunden
     * @param deltaT Zeitschritt in Sekunden
     * @param sampleInterval Wie oft sollen Daten gesammelt werden (in Sekunden)
     * @return Liste von Vergleichsergebnissen
     */
    public static List<ComparisonResult> compareIntegrators(
            double length, double mass, double radius, double initialTheta,
            double duration, double deltaT, double sampleInterval) {

        // Erstelle zwei identische Pendel
        Pendulum eulerPendulum = new Pendulum(length, mass, radius, initialTheta, 0.0, false, 0.0);
        Pendulum heunPendulum = new Pendulum(length, mass, radius, initialTheta, 0.0, false, 0.0);

        // Erstelle Integratoren
        Integrator eulerIntegrator = new EulerIntegrator();
        Integrator heunIntegrator = new HeunIntegrator();

        // Erstelle Simulation-Engines
        SimulationEngine eulerEngine = new SimulationEngine(eulerPendulum, eulerIntegrator, deltaT);
        SimulationEngine heunEngine = new SimulationEngine(heunPendulum, heunIntegrator, deltaT);

        // Speichere initiale Energie
        double initialEnergy = eulerPendulum.getTotalEnergy();

        // Ergebnisliste
        List<ComparisonResult> results = new ArrayList<>();

        // Simulation durchführen
        int stepsPerSample = (int) (sampleInterval / deltaT);
        int totalSteps = (int) (duration / deltaT);

        for (int step = 0; step <= totalSteps; step++) {
            // Sample speichern
            if (step % stepsPerSample == 0) {
                double time = step * deltaT;
                double eulerEnergy = eulerPendulum.getTotalEnergy();
                double heunEnergy = heunPendulum.getTotalEnergy();
                double eulerTheta = eulerPendulum.getState().getTheta();
                double heunTheta = heunPendulum.getState().getTheta();

                results.add(new ComparisonResult(time, eulerEnergy, heunEnergy,
                        initialEnergy, eulerTheta, heunTheta));
            }

            // Beide einen Schritt weiter
            if (step < totalSteps) {
                eulerEngine.step();
                heunEngine.step();
            }
        }

        return results;
    }

    /**
     * Gibt einen formatierten Vergleichsbericht aus.
     *
     * @param results Die Vergleichsergebnisse
     */
    public static String generateReport(List<ComparisonResult> results) {
        if (results.isEmpty()) return "Keine Daten vorhanden.";

        StringBuilder report = new StringBuilder();
        report.append("╔════════════════════════════════════════════════════════════════╗\n");
        report.append("║        INTEGRATOR-VERGLEICH: EULER vs. HEUN                   ║\n");
        report.append("╚════════════════════════════════════════════════════════════════╝\n\n");

        ComparisonResult last = results.get(results.size() - 1);

        report.append("Simulationsdauer: ").append(String.format("%.2f", last.time)).append(" s\n");
        report.append("Anzahl Samples: ").append(results.size()).append("\n\n");

        report.append("─── EULER-VERFAHREN ───\n");
        report.append("  Finale Energieabweichung: ").append(String.format("%.6f%%", last.eulerEnergyError)).append("\n");
        report.append("  Finaler Winkel: ").append(String.format("%.6f rad", last.eulerTheta)).append("\n\n");

        report.append("─── HEUN-VERFAHREN ───\n");
        report.append("  Finale Energieabweichung: ").append(String.format("%.6f%%", last.heunEnergyError)).append("\n");
        report.append("  Finaler Winkel: ").append(String.format("%.6f rad", last.heunTheta)).append("\n\n");

        report.append("─── VERBESSERUNG ───\n");
        double improvement = (last.eulerEnergyError - last.heunEnergyError) / last.eulerEnergyError * 100.0;
        report.append("  Energieerhaltung: ").append(String.format("%.1f%% besser", improvement)).append("\n");

        double thetaDiffEuler = Math.abs(last.eulerTheta - results.get(0).eulerTheta);
        double thetaDiffHeun = Math.abs(last.heunTheta - results.get(0).heunTheta);
        report.append("  Euler Theta-Drift: ").append(String.format("%.6f rad", thetaDiffEuler)).append("\n");
        report.append("  Heun Theta-Drift: ").append(String.format("%.6f rad", thetaDiffHeun)).append("\n");

        return report.toString();
    }

    private void printDetailedData(List<ComparisonResult> results) {
        System.out.println("\n─── DETAILLIERTE DATEN ───\n");
        System.out.println("Zeit [s] | Euler Energie [J] | Heun Energie [J] | Euler Fehler [%] | Heun Fehler [%]");
        System.out.println("─".repeat(100));

        for (int i = 0; i < Math.min(5, results.size()); i++) {
            ComparisonResult r = results.get(i);
            System.out.printf("%.3f    | %.8f         | %.8f        | %.6f         | %.6f%n",
                    r.time, r.eulerEnergy, r.heunEnergy, r.eulerEnergyError, r.heunEnergyError);
        }

        if (results.size() > 10) {
            System.out.println("...");
            for (int i = results.size() - 5; i < results.size(); i++) {
                ComparisonResult r = results.get(i);
                System.out.printf("%.3f    | %.8f         | %.8f        | %.6f         | %.6f%n",
                        r.time, r.eulerEnergy, r.heunEnergy, r.eulerEnergyError, r.heunEnergyError);
            }
        }
    }

    /**
     * Hauptmethode zum Testen des Vergleichs.
     */
    public static void main(String[] args) {
        new IntegratorComparison().runComparison();
    }
}
