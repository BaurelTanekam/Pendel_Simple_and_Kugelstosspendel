package simulation;

import model.Pendulum;
import model.PhysicsState;

public class HeunIntegrator implements Integrator{

    /**
     * Führt einen Heun-Integrationsschritt durch.
     *
     * Der Algorithmus:
     * 1. Berechne k₁ = f(t, y) - die Ableitung am aktuellen Zustand
     * 2. Berechne vorläufigen Zustand: y_temp = y + k₁·Δt (Euler-Prädiktor)
     * 3. Berechne k₂ = f(t+Δt, y_temp) - die Ableitung am vorläufigen Zustand
     * 4. Berechne finalen Zustand: y_neu = y + (k₁ + k₂)/2 · Δt (Heun-Korrektor)
     *
     * @param pendulum Das Pendel, das integriert werden soll
     * @param deltaT Der Zeitschritt in Sekunden
     */
    @Override
    public void step(Pendulum pendulum, double deltaT) {
        // Aktuelle Ableitung k1 berechnen
        PhysicsState currentState = pendulum.getState();
        PhysicsState k1 = pendulum.calculateDerivative(currentState);

        // Prädiktor-Schritt (vorläufiger Euler-Schritt)
        // Wir machen einen provisorischen Schritt mit der Steigung k₁
        // Dies gibt uns eine Schätzung, wo wir am Ende des Intervalls sein werden
        PhysicsState predictedState = currentState.add(k1.multiply(deltaT));

        // Ableitung am prädizierten Punkt k₂ berechnen
        // Jetzt schauen wir, wie die Steigung am Ende des Intervalls aussehen würde
        // Dafür müssen wir temporär das Pendel auf den prädizierten Zustand setzen
        PhysicsState originalState = pendulum.getState(); // Sichern für später
        pendulum.setState(predictedState); // Temporär setzen
        PhysicsState k2 = pendulum.calculateDerivative(predictedState);
        pendulum.setState(originalState); // Zurücksetzen auf Original

        // Korrektor-Schritt (Durchschnitt der Steigungen)
        // Statt nur k₁ zu verwenden (wie Euler), nehmen wir den Durchschnitt
        // von k₁ (Steigung am Anfang) und k₂ (Steigung am Ende)
        // Dies gibt uns eine viel bessere Approximation der durchschnittlichen Steigung
        PhysicsState averageDerivative = k1.add(k2).multiply(0.5);

        // Berechne den neuen Zustand mit der durchschnittlichen Steigung
        PhysicsState newState = pendulum.getState().add(averageDerivative.multiply(deltaT));

        // Neuen Zustand setzen
        pendulum.setState(newState);
    }

    @Override
    public String getName() {
        return "Heun";
    }
}
