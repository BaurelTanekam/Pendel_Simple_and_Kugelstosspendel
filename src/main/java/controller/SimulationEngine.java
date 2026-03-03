package controller;

import model.Pendulum;
import simulation.Integrator;
import util.Logger;

public class SimulationEngine {
    /**
     * SimulationEngine ist das Herzstück der gesamten Simulation.
     *
     * Diese Klasse orchestriert den zeitlichen Ablauf der Simulation. Sie verwaltet:
     * - Die Zeit (wie lange die Simulation schon läuft)
     * - Den Zeitschritt (wie groß die Schritte bei der numerischen Integration sind)
     * - Das Pendel oder die Pendel, die simuliert werden
     * - Den Integrator, der die Bewegungsgleichungen löst
     *
     * Die Engine arbeitet wie eine Uhr: In jedem Tick wird die Zeit um deltaT weiter geschaltet
     * und der Integrator berechnet den neuen Zustand. Dies wiederholt sich, bis die Simulation
     * gestoppt wird oder eine vorgegebene Endzeit erreicht ist.
     *
     * In Phase 1 verwenden wir diese Engine direkt von der Konsole aus.
     * In Phase 2 wird die GUI diese Engine steuern und ihre Ergebnisse visualisieren.
     */

    private double deltaT;

    Pendulum pendulum;

    /**
     * Der numerische Integrator, der die Bewegungsgleichungen löst.
     * Dies ist ein Interface, sodass wir verschiedene Integratoren austauschen können.
     */
    private Integrator integrator;


    private double currentTime;

    private double initialEnergie;


    private Logger logger;

    public SimulationEngine(Pendulum pendulum, Integrator integrator, double deltaT) {
        this.pendulum = pendulum;
        this.integrator = integrator;
        this.deltaT = deltaT;
        this.currentTime = 0.0;
        this.initialEnergie = pendulum.getTotalEnergy();
        this.logger = Logger.getInstance();
        logger.logInfo(String.format(
                "Simulation initialized: Integrator=%s, deltaT=%.4fs, Initial Energy=%.6fJ",
                integrator.getName(), deltaT, initialEnergie
        ));
    }

    /**
     * Führt einen einzelnen Simulationsschritt durch.
     *
     * Dies ist die zentrale Methode, die in jedem Frame aufgerufen wird.
     * Sie verwendet den Integrator, um den nächsten Zustand zu berechnen
     * erhöht die Simulationszeit um deltaT
     * gibt den aktualisierten Zustand für Logging oder Visualisierung zurück
     */
    public void step() {
        integrator.step(pendulum, deltaT);

        currentTime += deltaT;
    }

    /**
     * Führt mehrere Schritte hintereinander aus.
     * Dies ist praktisch für Batch-Simulationen, wo wir viele Schritte auf einmal
     * durchrechnen wollen ohne jedes Mal einzeln step() aufrufen zu müssen.
     *
     * @param numSteps Die Anzahl der durchzuführenden Schritte
     */
    public void run(int numSteps) {
        for (int i = 0; i < numSteps; i++) {
            step();
        }
    }

    /**
     * Führt die Simulation für eine bestimmte physikalische Zeit aus.
     *
     * Im Gegensatz zu run(numSteps) geben wir hier die Zeit vor, nicht die Anzahl der Schritte.
     * Die Methode berechnet automatisch, wie viele Schritte nötig sind.
     *
     * @param duration Die Dauer in Sekunden
     */
    public void runForDuration(double duration) {
        double targetTime = currentTime + duration;
        while (currentTime < targetTime) {
            step();
        }
    }

    /**
     * Gibt den aktuellen Zustand des Systems als formatierten String zurück.
     *
     * Dies ist nützlich für die Konsolenausgabe in Phase 1.
     * Der String enthält: Zeit, Winkel, Winkelgeschwindigkeit und Energien.
     *
     * @return Eine lesbare Zusammenfassung des aktuellen Zustands
     */
    public String getCurrentStateString() {
        double theta = pendulum.getState().getTheta();
        double omega = pendulum.getState().getOmega();
        double eKin = pendulum.getKineticEnergy();
        double ePot = pendulum.getPotentialEnergy();
        double eTotal = pendulum.getTotalEnergy();
        double energyError = Math.abs((eTotal - initialEnergie) / initialEnergie) * 100.0;

        return String.format(
                "t=%.3fs | θ=%.4f rad (%.2f°) | ω=%.4f rad/s | E_kin=%.6fJ | E_pot=%.6fJ | E_total=%.6fJ | Error=%.4f%%",
                currentTime, theta, Math.toDegrees(theta), omega, eKin, ePot, eTotal, energyError
        );
    }

    /**
     * Gibt eine Zusammenfassung der Simulation aus.
     *
     * Diese Methode wird am Ende einer Simulation aufgerufen und zeigt wichtige Metriken:
     * - Gesamte simulierte Zeit
     * - Anzahl der durchgeführten Schritte
     * - Energieabweichung (ein Maß für die Qualität der numerischen Integration)
     */
    public void printSummary() {
        int totalSteps = (int) (currentTime / deltaT);
        double finalEnergy = pendulum.getTotalEnergy();
        double energyDrift = ((finalEnergy - initialEnergie) / initialEnergie) * 100.0;

        String summary = String.format("""
            
            ========================================
            SIMULATION SUMMARY
            ========================================
            Integrator:        %s
            Time step:         %.4f s
            Total time:        %.2f s
            Total steps:       %d
            Initial energy:    %.6f J
            Final energy:      %.6f J
            Energy drift:      %.4f%%
            ========================================
            """,
                integrator.getName(), deltaT, currentTime, totalSteps,
                initialEnergie, finalEnergy, energyDrift
        );

        System.out.println(summary);
        logger.logInfo("Simulation completed. Energy drift: " + String.format("%.4f%%", energyDrift));
    }


    public double getCurrentTime() {
        return currentTime;
    }


    public double getDeltaT() {
        return deltaT;
    }
}
