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
        PhysicsState currentState = pendulum.getState();
        PhysicsState k1 = pendulum.calculateDerivative(currentState);


        PhysicsState predictedState = currentState.add(k1.multiply(deltaT));


        PhysicsState originalState = pendulum.getState();
        pendulum.setState(predictedState);
        PhysicsState k2 = pendulum.calculateDerivative(predictedState);
        pendulum.setState(originalState);


        PhysicsState averageDerivative = k1.add(k2).multiply(0.5);

        PhysicsState newState = pendulum.getState().add(averageDerivative.multiply(deltaT));

        pendulum.setState(newState);
    }

    @Override
    public String getName() {
        return "Heun";
    }
}
