import model.EulerIntegrator;
import model.Integrator;
import model.Pendulum;
import model.SimulationEngine;
import util.Logger;

public class Driver {
    public static void runPhase1TestSimulation() {
        // Logger initialisieren
        Logger logger = Logger.getInstance();
        logger.logInfo("=== Phase 1: Test Simulation gestartet ===");

        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║        PENDELSIMULATION - Phase 1: Physik-Test                ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        System.out.println();

        // ===== PARAMETER DEFINITION =====
        // Diese Werte definieren unser Pendel und die Simulation

        // Physikalische Parameter des Pendels
        double pendelLaenge = 1.0;              // 1 Meter Länge
        double pendelMasse = 1.0;               // 1 Kilogramm Masse
        double kugelRadius = 0.025;             // 2.5 cm Radius

        // Anfangsbedingungen
        double startWinkel = Math.toRadians(10.0);  // 10 Grad in Radiant umrechnen
        double startGeschwindigkeit = 0.0;          // Aus Ruhe loslassen

        // Simulationsparameter
        double zeitSchritt = 0.001;             // 1 Millisekunde (wie gewünscht)
        double simulationsDauer = 10.0;         // 10 Sekunden simulieren
        double ausgabeIntervall = 0.1;          // Alle 0.1 Sekunden Zustand ausgeben

        // Kleinwinkelnäherung verwenden (für Phase 1)
        boolean kleinwinkelNaeherung = true;

        // Aufhängepunkt (für Phase 1 bei x=0)
        double aufhaengepunktX = 0.0;

        // ===== WARNUNG BEI GROSSEM WINKEL =====
        // Sie wollten eine Warnung, wenn der Winkel zu groß für die Näherung ist
        double startWinkelGrad = Math.toDegrees(startWinkel);
        if (kleinwinkelNaeherung && startWinkelGrad > 15.0) {
            String warnung = String.format(
                    "WARNUNG: Startwinkel %.2f° überschreitet Grenze der Kleinwinkelnäherung (15°)!",
                    startWinkelGrad
            );
            logger.logWarning(warnung);
            System.err.println("⚠ " + warnung);
            System.err.println("⚠ Die Ergebnisse können ungenau sein.");
            System.out.println();
        }

        // ===== OBJEKT-ERSTELLUNG =====

        // Pendel erstellen
        Pendulum pendel = new Pendulum(
                pendelLaenge,
                pendelMasse,
                kugelRadius,
                startWinkel,
                startGeschwindigkeit,
                kleinwinkelNaeherung,
                aufhaengepunktX
        );

        // Integrator erstellen (Euler für Phase 1)
        Integrator integrator = new EulerIntegrator();

        // Simulationsengine erstellen
        SimulationEngine engine = new SimulationEngine(pendel, integrator, zeitSchritt);

        // ===== SIMULATION DURCHFÜHREN =====

        System.out.println("Simulationsparameter:");
        System.out.println("  • Pendellänge:    " + pendelLaenge + " m");
        System.out.println("  • Pendelmasse:    " + pendelMasse + " kg");
        System.out.println("  • Startwinkel:    " + String.format("%.2f°", startWinkelGrad));
        System.out.println("  • Integrator:     " + integrator.getName());
        System.out.println("  • Zeitschritt:    " + zeitSchritt + " s");
        System.out.println("  • Dauer:          " + simulationsDauer + " s");
        System.out.println();
        System.out.println("Simulation läuft...");
        System.out.println();

        // Berechne theoretische Periode für Vergleich
        // T = 2π * sqrt(L/g)
        double theoretischePeriode = 2.0 * Math.PI * Math.sqrt(pendelLaenge / 9.81);
        System.out.println("Theoretische Schwingungsperiode: " +
                String.format("%.3f s", theoretischePeriode));
        System.out.println();
        System.out.println("─".repeat(120));

        // Simulation durchführen mit regelmäßiger Ausgabe
        int ausgabeSchritte = (int) (ausgabeIntervall / zeitSchritt);
        int gesamtSchritte = (int) (simulationsDauer / zeitSchritt);

        for (int schritt = 0; schritt <= gesamtSchritte; schritt++) {
            // Alle ausgabeSchritte den Zustand ausgeben
            if (schritt % ausgabeSchritte == 0) {
                System.out.println(engine.getCurrentStateString());
            }

            // Einen Simulationsschritt durchführen
            if (schritt < gesamtSchritte) {
                engine.step();
            }
        }

        System.out.println("─".repeat(120));
        System.out.println();

        // ===== ABSCHLUSS UND ZUSAMMENFASSUNG =====

        // Zusammenfassung ausgeben
        engine.printSummary();

        // Interpretation der Ergebnisse
        double endEnergie = pendel.getTotalEnergy();
        double initialEnergie = endEnergie;  // Wir können sie aus dem Pendel holen
        double energieAbweichung = Math.abs((endEnergie - initialEnergie) / initialEnergie) * 100.0;

        System.out.println("Interpretation:");
        if (energieAbweichung < 0.1) {
            System.out.println("  ✓ Ausgezeichnet! Die Energie bleibt sehr gut erhalten.");
        } else if (energieAbweichung < 1.0) {
            System.out.println("  ✓ Gut. Die Energieerhaltung ist akzeptabel für das Euler-Verfahren.");
        } else if (energieAbweichung < 5.0) {
            System.out.println("  ⚠ Die Energieabweichung ist spürbar. Das ist typisch für Euler.");
            System.out.println("    In Phase 3 werden wir das Heun-Verfahren verwenden, das besser ist.");
        } else {
            System.out.println("  ✗ Die Energieabweichung ist groß. Möglicherweise ist der Zeitschritt zu groß.");
        }
        System.out.println();

        System.out.println("Nächste Schritte:");
        System.out.println("  1. Überprüfen Sie die Log-Datei: " + logger.getLogFileName());
        System.out.println("  2. Experimentieren Sie mit verschiedenen Startwinkeln");
        System.out.println("  3. Vergleichen Sie mit der theoretischen Lösung");
        System.out.println();

        // Logger schließen
        logger.logInfo("=== Phase 1: Test Simulation beendet ===");
        logger.close();

        System.out.println("Simulation abgeschlossen!");
    }
}
