package model;

public interface Integrator {
    /**
     * Führt einen Integrationsschritt durch.
     *
     * Diese Methode berechnet den neuen Zustand des Pendels nach einem Zeitschritt deltaT.
     * Sie aktualisiert das Pendel-Objekt direkt mit dem neuen Zustand.
     *
     * Der Integrator ruft dabei die calculateDerivative()-Methode des Pendels auf,
     * um zu erfahren, wie sich der Zustand mit der Zeit ändert, und verwendet dann
     * sein spezifisches numerisches Verfahren, um den nächsten Zustand zu approximieren.
     *
     * @param pendulum Das Pendel, dessen Zustand aktualisiert werden soll
     * @param deltaT Der Zeitschritt in Sekunden
     */
    void step(Pendulum pendulum, double deltaT);

    /**
     * Gibt den Namen des Integrators zurück.
     * Dies ist nützlich für Logging und für die Anzeige in der GUI.
     *
     * @return Der Name des Integrationsverfahrens (z.B. "Euler" oder "Heun")
     */
    String getName();
}
