package model;

public class PhysicsState {
    private double theta;
    private double omega;

    public PhysicsState(double theta, double omega) {
        this.theta = theta;
        this.omega = omega;
    }

    public double getOmega() {
        return omega;
    }

    public double getTheta() {
        return theta;
    }

    @Override
    public String toString() {
        return String.format("State[θ=%.4f rad, ω=%.4f rad/s]", theta, omega);
    }

    /**
     * Erstellt einen neuen Zustand durch Addition eines anderen Zustands.
     * Dies ist nützlich für numerische Integration, wo wir Zustände addieren müssen.
     * Mathematisch: (θ₁, ω₁) + (θ₂, ω₂) = (θ₁+θ₂, ω₁+ω₂)
     *
     * @param other Der zu addierende Zustand
     * @return Ein neuer Zustand mit den addierten Werten
     */
    public PhysicsState add(PhysicsState other) {
        return new PhysicsState(
                this.theta + other.theta,
                this.omega + other.omega
        );
    }

    /**
     * Erstellt einen neuen Zustand durch Multiplikation mit einem Skalar.
     * Dies wird für die numerische Integration benötigt, wo wir Zustände mit dem Zeitschritt
     * multiplizieren müssen.
     * Mathematisch: c * (θ, ω) = (c*θ, c*ω)
     *
     * @param scalar Der Multiplikationsfaktor
     * @return Ein neuer Zustand mit den multiplizierten Werten
     */
    public PhysicsState multiply(double scalar) {
        return new PhysicsState(
                this.theta * scalar,
                this.omega * scalar
        );
    }
}
