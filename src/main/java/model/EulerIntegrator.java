package model;

public class EulerIntegrator implements Integrator {

    @Override
    public void step(Pendulum pendulum, double deltaT) {
        PhysicsState currentState = pendulum.getState();

        //Ableitung berechnen
        PhysicsState derivate = pendulum.calculateDerivative(currentState);

        //Neuer Zustand berechnen
        PhysicsState newState = currentState.add(derivate.multiply(deltaT));

        //Pendel auf neuen Zustand setzen
        pendulum.setState(newState);
    }

    @Override
    public String getName() {
        return "Euler";
    }
}
