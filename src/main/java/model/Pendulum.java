package model;

public class Pendulum {
    /**
     * Erdbeschleunigung in m/s².
     * Dieser Wert ist eine Konstante und gilt für alle Pendel auf der Erdoberfläche.
     */
    private static final double GRAVITY = 9.81;

    //Länge des Pendels
    private double length;

    //Masse der Kugel
    private double masse;

    //Radius der Kugel
    private double radius;

    //Zustand eines Pendels
    private PhysicsState state;

    /**
     * Flag, das angibt, ob die Kleinwinkelnäherung verwendet werden soll.
     * True = verwende sin(θ) ≈ θ (nur für θ < 15° genau)
     * False = verwende vollständige Gleichung mit sin(θ)
     */
    private boolean useSmallAngleApproximation;

    //Position des Aufhängepunkts in Metern
    private double pivotX;

    /**
     * Konstruktor für ein neues Pendel mit allen Parametern.
     *
     * @param length Länge des Pendels in Metern
     * @param masse Masse der Kugel in Kilogramm
     * @param radius Radius der Kugel in Metern
     * @param initialTheta Startwinkel in Radiant
     * @param initialOmega Anfangswinkelgeschwindigkeit in rad/s
     * @param useSmallAngleApproximation Ob Kleinwinkelnäherung verwendet werden soll
     * @param pivotX X-Position des Aufhängepunkts
     */
    public Pendulum(double length, double masse, double radius, double initialTheta,
                    double initialOmega, boolean useSmallAngleApproximation, double pivotX) {
        this.length = length;
        this.masse = masse;
        this.radius = radius;
        this.state = new PhysicsState(initialTheta, initialOmega);
        this.useSmallAngleApproximation = useSmallAngleApproximation;
        this.pivotX = pivotX;
    }

    /**
     * Berechnet die Winkelbeschleunigung (d²θ/dt²) für den aktuellen Zustand.
     *
     * Dies ist die rechte Seite der Bewegungsgleichung und gibt an, wie sich die
     * Winkelgeschwindigkeit ändert. Die Beschleunigung hängt vom aktuellen Winkel ab:
     * Je größer die Auslenkung, desto stärker die rücktreibende Kraft.
     *
     * @param theta Der aktuelle Winkel in Radiant
     * @return Die Winkelbeschleunigung in rad/s²
     */
    public double calculateAngularAcceleration(double theta) {
        if (useSmallAngleApproximation) {
            // Kleinwinkelnäherung: sin(θ) ≈ θ
            // Dies ergibt: d²θ/dt² = -(g/L) * θ
            return -(GRAVITY / length) * theta;
        } else {
            // Vollständige nichtlineare Gleichung
            // Dies ergibt: d²θ/dt² = -(g/L) * sin(θ)
            return -(GRAVITY / length) * Math.sin(theta);
        }
    }

    /**
     * Berechnet die zeitliche Ableitung des Zustands.
     * Für die numerische Integration brauchen wir dy/dt, also wie sich der Zustand
     * mit der Zeit ändert. Da unser Zustand aus (θ, ω) besteht, ist die Ableitung:
     *
     *     d/dt(θ, ω) = (dθ/dt, dω/dt) = (ω, d²θ/dt²)
     *
     * Das erste Element ist einfach die Winkelgeschwindigkeit (per Definition ist ω = dθ/dt).
     * Das zweite Element ist die Winkelbeschleunigung, die wir aus der Bewegungsgleichung bekommen.
     *
     * @param currentState Der aktuelle Zustand
     * @return Die zeitliche Ableitung des Zustands
     */
    public PhysicsState calculateDerivative(PhysicsState currentState) {
        double theta = currentState.getTheta();
        double omega = currentState.getOmega();

        // dθ/dt = ω (die Definition der Winkelgeschwindigkeit)
        double thetaDot = omega;

        // dω/dt = d²θ/dt² (die Winkelbeschleunigung aus der Bewegungsgleichung)
        double omegaDot = calculateAngularAcceleration(theta);

        return new PhysicsState(thetaDot, omegaDot);
    }

    /**
     * Aktualisiert den Zustand des Pendels.
     * Diese Methode wird vom Integrator aufgerufen, nachdem er den nächsten Zustand berechnet hat.
     *
     * @param newState Der neue Zustand
     */
    public void setState(PhysicsState newState) {
        this.state = newState;
    }

    /**
     * Gibt den aktuellen Zustand zurück.
     *
     * @return Der aktuelle physikalische Zustand
     */
    public PhysicsState getState() {
        return state;
    }

    /**
     * Berechnet die kinetische Energie des Pendels.
     *
     * Die kinetische Energie eines rotierenden Körpers ist:
     *     E_kin = (1/2) * I * ω²
     *
     * Für eine Punktmasse am Faden ist das Trägheitsmoment I = m * L².
     * Also: E_kin = (1/2) * m * L² * ω²
     *
     * @return Die kinetische Energie in Joule
     */
    public double getKineticEnergy() {
        double omega = state.getOmega();
        return 0.5 * masse * length * length * omega * omega;
    }

    /**
     * Berechnet die potentielle Energie des Pendels.
     *
     * Die potentielle Energie hängt von der Höhe der Masse über dem tiefsten Punkt ab.
     * Wenn θ = 0 (senkrecht nach unten), ist die potentielle Energie null (Referenzpunkt).
     * Bei Auslenkung um θ steigt die Masse um die Höhe h = L * (1 - cos(θ)).
     *
     * Also: E_pot = m * g * h = m * g * L * (1 - cos(θ))
     *
     * @return Die potentielle Energie in Joule
     */
    public double getPotentialEnergy() {
        double theta = state.getTheta();
        return masse * GRAVITY * length * (1.0 - Math.cos(theta));
    }

    /**
     * Berechnet die Gesamtenergie des Pendels.
     *
     * Nach dem Energieerhaltungssatz sollte die Gesamtenergie konstant bleiben,
     * solange keine Reibung vorhanden ist. Dies ist ein wichtiges Kriterium zur
     * Überprüfung der Qualität unserer numerischen Integration.
     *
     * @return Die Gesamtenergie in Joule
     */
    public double getTotalEnergy() {
        return getKineticEnergy() + getPotentialEnergy();
    }

    /**
     * Berechnet die X-Position der Kugel in Metern.
     *
     * @return X-Position relativ zum Ursprung
     */
    public double getX() {
        return pivotX - length * Math.sin(state.getTheta());
    }

    /**
     * Berechnet die Y-Position der Kugel in Metern.
     * Y zeigt nach unten, daher ist y positiv für Positionen unterhalb des Aufhängepunkts.
     *
     * @return Y-Position relativ zum Ursprung
     */
    public double getY() {
        return length * Math.cos(state.getTheta());
    }

    // Getter für die Eigenschaften

    public double getLength() {
        return length;
    }

    public double getMass() {
        return masse;
    }

    public double getRadius() {
        return radius;
    }

    public double getPivotX() {
        return pivotX;
    }

    public boolean isUsingSmallAngleApproximation() {
        return useSmallAngleApproximation;
    }

    /**
     * Gibt eine String-Repräsentation des Pendels zurück.
     * Nützlich für Debugging und Logging.
     */
    @Override
    public String toString() {
        return String.format("Pendulum[L=%.2fm, m=%.2fkg, %s, E=%.4fJ]",
                length, masse, state, getTotalEnergy());
    }
}
