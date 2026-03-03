package simulation;

import model.Pendulum;
import model.PhysicsState;

public class EulerIntegrator implements Integrator{

    /**
     * Führt einen Euler-Integrationsschritt durch.
     *
     * Der Algorithmus:
     * 1. Hole den aktuellen Zustand y(t) = (θ, ω)
     * 2. Berechne die Ableitung dy/dt = (ω, α)
     * 3. Berechne den neuen Zustand: y(t + Δt) = y(t) + dy/dt * Δt
     * 4. Setze das Pendel auf den neuen Zustand
     *
     * @param pendulum Das Pendel, das integriert werden soll
     * @param deltaT Der Zeitschritt in Sekunden
     */
    @Override
    public void step(Pendulum pendulum, double deltaT) {
        PhysicsState currentState = pendulum.getState();


        PhysicsState derivative = pendulum.calculateDerivative(currentState);

        PhysicsState newState = currentState.add(derivative.multiply(deltaT));

        pendulum.setState(newState);
    }

    /**
     * Gibt den Namen des Integrators zurück.
     *
     * @return "Euler"
     */
    @Override
    public String getName() {
        return "Euler";
    }
}
