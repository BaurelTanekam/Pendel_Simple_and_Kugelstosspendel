package view;

import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.Pendulum;
import model.PhysicsState;
import simulation.EulerIntegrator;
import simulation.HeunIntegrator;
import simulation.Integrator;
import controller.SimulationEngine;
import util.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * MainWindow - Das Hauptfenster der Pendelsimulation.
 *
 * Dieses Fenster enthält:
 * - Oben: Kontrollbuttons und Optionen
 * - Mitte: Canvas mit der Animation
 * - Rechts: Energieanzeigen und Status
 * - Unten: Textfeld für Konsolenausgabe
 *
 * Die Simulation läuft über einen AnimationTimer, der 60x pro Sekunde
 * aufgerufen wird und die Physik-Engine mehrfach updated sowie die
 * Visualisierung aktualisiert.
 */
public class MainWindow extends Stage {

    private static final double GRAVITY = 9.81;
    private static final double PENDULUM_LENGTH = 1.0;
    private static final double TIME_STEP = 0.001;

    private static final int CANVAS_WIDTH = 500;
    private static final int CANVAS_HEIGHT = 300;

    private double scale;

    private List<Pendulum> pendulums;
    private List<SimulationEngine> engines;
    private Integrator integrator;
    private SimulationConfig config;

    // GUI-Komponenten
    private Canvas canvas;
    private GraphicsContext gc;
    private AnimationTimer timer;
    private TextArea consoleOutput;
    private Label timeLabel;
    private Label energyLabel;
    private List<EnergyBar> energyBars;

    // Kontroll
    private Button btnStart;
    private Button btnPause;
    private Button btnStop;
    private Button btnReset;
    private Button btnStep;
    private Slider speedSlider;
    private CheckBox checkboxGewichtskraft;

    // Status
    private boolean isRunning = false;
    private boolean isPaused = false;
    private double simulationSpeed = 1.0;
    private boolean showGravityForce = false;
    private double initialTotalEnergy = 0.0;

    private Logger logger;

    /**
     * Konstruktor für das Hauptfenster.
     *
     * @param config Die Konfiguration aus dem Startdialog
     */
    public MainWindow(SimulationConfig config) {
        this.config = config;
        this.logger = Logger.getInstance();

        initSimulation();
        initUI();
        setupAnimationTimer();
    }

    /**
     * Initialisiert die Simulation mit den Pendeln.
     */
    private void initSimulation() {
        pendulums = new ArrayList<>();
        engines = new ArrayList<>();

        // Wähle den Integrator basierend auf der Konfiguration (NEU in Phase 3)
        if (config.integratorName.equals("Heun-Verfahren")) {
            integrator = new HeunIntegrator();
        } else {
            integrator = new EulerIntegrator();
        }

        double startWinkelRad = Math.toRadians(config.startWinkelGrad);

        if (config.istKugelstosspendel) {
            double spacing = 2.0 * config.radius; // Kugeln berühren sich fast
            double totalWidth = (config.anzahlKugeln - 1) * spacing;
            double startX = -totalWidth / 2.0;

            // Die linken Kugeln (kleinerer Index)
            for (int i = 0; i < config.anzahlKugeln; i++) {
                double pivotX = startX + i * spacing;
                // Die ersten anzahlAusgelenkt Kugeln werden ausgelenkt
                double initialTheta = (i < config.anzahlAusgelenkt) ? startWinkelRad : 0.0;

                Pendulum pendulum = new Pendulum(
                        PENDULUM_LENGTH,
                        config.masse,
                        config.radius,
                        initialTheta,
                        0.0,
                        false, // Keine Kleinwinkelnäherung für Kugelstoß
                        pivotX
                );

                pendulums.add(pendulum);
                engines.add(new SimulationEngine(pendulum, integrator, TIME_STEP));
            }
        } else {

            Pendulum pendulum = new Pendulum(
                    PENDULUM_LENGTH,
                    config.masse,
                    config.radius,
                    startWinkelRad,
                    0.0,
                    true, // Kleinwinkelnäherung
                    0.0
            );

            pendulums.add(pendulum);
            engines.add(new SimulationEngine(pendulum, integrator, TIME_STEP));
        }


        for (Pendulum p : pendulums) {
            initialTotalEnergy += p.getTotalEnergy();
        }

        // Berechne Skalierung
        calculateScale();

        logger.logInfo("Simulation initialized: " +
                (config.istKugelstosspendel ? "Newton's Cradle" : "Single Pendulum") +
                ", Pendulums: " + pendulums.size());
    }

    /**
     * Berechnet den Skalierungsfaktor basierend auf Anzahl und Größe der Kugeln.
     */
    private void calculateScale() {

        double maxLength = PENDULUM_LENGTH + config.radius;
        scale = (CANVAS_HEIGHT * 0.4) / maxLength;
    }

    /**
     * Initialisiert die Benutzeroberfläche.
     */
    private void initUI() {
        setTitle("Pendelsimulation - " +
                (config.istKugelstosspendel ? "Kugelstoßpendel" : "Einzelpendel") +
                " (" + integrator.getName() + ")"); // Zeige Integrator im Titel

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f5;");

        root.setTop(createControlPanel());

        root.setCenter(createCanvasPanel());

        root.setRight(createStatusPanel());

        root.setBottom(createConsolePanel());

        Scene scene = new Scene(root, 600, 400);
        setScene(scene);

        drawSimulation();
    }

    /**
     * Erstellt das Kontrollpanel oben.
     */
    private VBox createControlPanel() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: white; -fx-border-color: #ccc; -fx-border-width: 0 0 1 0;");

        HBox buttonRow = new HBox(10);
        buttonRow.setAlignment(Pos.CENTER_LEFT);

        btnStart = new Button("▶ Start");
        btnStart.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-weight: bold;");
        btnStart.setOnAction(e -> startSimulation());

        btnPause = new Button("⏸ Pause");
        btnPause.setDisable(true);
        btnPause.setOnAction(e -> pauseSimulation());

        btnStop = new Button("⏹ Stop");
        btnStop.setDisable(true);
        btnStop.setOnAction(e -> stopSimulation());

        btnReset = new Button("↻ Reset");
        btnReset.setOnAction(e -> resetSimulation());

        btnStep = new Button("⏭ Step");
        btnStep.setOnAction(e -> stepSimulation());

        buttonRow.getChildren().addAll(btnStart, btnPause, btnStop, btnReset, btnStep);

        HBox optionsRow = new HBox(20);
        optionsRow.setAlignment(Pos.CENTER_LEFT);

        Label speedLabel = new Label("Geschwindigkeit:");
        speedSlider = new Slider(0.1, 3.0, 1.0);
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(0.5);
        speedSlider.setPrefWidth(200);
        Label speedValueLabel = new Label("1.0x");
        speedValueLabel.setPrefWidth(50);

        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            simulationSpeed = newVal.doubleValue();
            speedValueLabel.setText(String.format("%.1fx", simulationSpeed));
        });

        // Gewichtskraft-Checkbox
        checkboxGewichtskraft = new CheckBox("Gewichtskraft anzeigen");
        checkboxGewichtskraft.setOnAction(e -> {
            showGravityForce = checkboxGewichtskraft.isSelected();
            drawSimulation();
        });

        optionsRow.getChildren().addAll(speedLabel, speedSlider, speedValueLabel,
                new Separator(), checkboxGewichtskraft);

        box.getChildren().addAll(buttonRow, optionsRow);
        return box;
    }

    /**
     * Erstellt das Canvas-Panel in der Mitte.
     */
    private StackPane createCanvasPanel() {
        StackPane pane = new StackPane();
        pane.setStyle("-fx-background-color: white;");

        canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        gc = canvas.getGraphicsContext2D();

        pane.getChildren().add(canvas);
        return pane;
    }

    /**
     * Erstellt das Statuspanel rechts.
     */
    private VBox createStatusPanel() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(15));
        box.setPrefWidth(300);
        box.setStyle("-fx-background-color: white; -fx-border-color: #ccc; -fx-border-width: 0 0 0 1;");

        // Titel
        Label titleLabel = new Label("Status & Energie");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Zeit
        timeLabel = new Label("Zeit: 0.000 s");
        timeLabel.setStyle("-fx-font-size: 14px;");

        energyLabel = new Label("Gesamtenergie: 0.000000 J\nAbweichung: 0.00%");
        energyLabel.setStyle("-fx-font-size: 12px;");

        Separator sep1 = new Separator();

        // Energiebalken für jedes Pendel
        Label energyBarsLabel = new Label("Energie pro Kugel:");
        energyBarsLabel.setStyle("-fx-font-weight: bold;");

        VBox barsContainer = new VBox(10);
        energyBars = new ArrayList<>();

        for (int i = 0; i < pendulums.size(); i++) {
            EnergyBar bar = new EnergyBar("Kugel " + (i + 1));
            energyBars.add(bar);
            barsContainer.getChildren().add(bar.getNode());
        }

        ScrollPane scrollPane = new ScrollPane(barsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        box.getChildren().addAll(titleLabel, timeLabel, energyLabel, sep1,
                energyBarsLabel, scrollPane);
        return box;
    }

    /**
     * Erstellt das Konsolen-Panel unten.
     */
    private VBox createConsolePanel() {
        VBox box = new VBox(5);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: white; -fx-border-color: #ccc; -fx-border-width: 1 0 0 0;");

        Label label = new Label("Konsolenausgabe:");
        label.setStyle("-fx-font-weight: bold;");

        consoleOutput = new TextArea();
        consoleOutput.setEditable(false);
        consoleOutput.setPrefHeight(150);
        consoleOutput.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 11px;");

        box.getChildren().addAll(label, consoleOutput);
        return box;
    }

    /**
     * Richtet den AnimationTimer ein.
     *
     * Der Timer läuft mit der natürlichen Framerate des Systems (typisch 60 FPS).
     * In jedem Frame werden mehrere Simulationsschritte durchgeführt, abhängig
     * von der verstrichenen Zeit und der gewählten Geschwindigkeit.
     *
     * WICHTIG: Wir begrenzen die maximale Anzahl von Schritten pro Frame,
     * um zu verhindern, dass die GUI einfriert bei Leistungsproblemen.
     */
    private void setupAnimationTimer() {
        final int MAX_STEPS_PER_FRAME = 50; // Begrenze auf 50 Schritte pro Frame

        timer = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (!isRunning || isPaused) return;

                if (lastUpdate == 0) {
                    lastUpdate = now;
                    return;
                }

                double elapsedSeconds = (now - lastUpdate) / 1_000_000_000.0;
                lastUpdate = now;

                // Maximum 0.1 Sekunden pro Frame
                if (elapsedSeconds > 0.1) {
                    elapsedSeconds = 0.1;
                }

                int stepsToPerform = (int) ((elapsedSeconds / TIME_STEP) * simulationSpeed);


                stepsToPerform = Math.min(stepsToPerform, MAX_STEPS_PER_FRAME);

                // Führe Simulationsschritte durch
                for (int i = 0; i < stepsToPerform; i++) {
                    performSimulationStep();
                }

                updateGUI();
            }
        };
    }

    /**
     * Führt einen einzelnen Simulationsschritt für alle Pendel durch.
     */
    private void performSimulationStep() {
        // Integriere alle Pendel
        for (SimulationEngine engine : engines) {
            engine.step();
        }

        // Kollisionserkennung für Kugelstoßpendel
        if (config.istKugelstosspendel) {
            checkAndHandleCollisions();
        }
    }

    /**
     * Behandelt Kollisionen mit einem vektorbasierten Ansatz (Impuls + Positionskorrektur).
     * Basierend auf der bereitgestellten C++-Logik und eigener korrigierter Implementierung.
     *
     * Diese Methode implementiert die physikalisch korrekte Stoßbehandlung für das Kugelstoßpendel:
     * 1. Konversion von Polar- zu kartesischen Koordinaten
     * 2. Kollisionserkennung mit Überlappungsprüfung
     * 3. Positionskorrektur um "Steckenbleiben" zu verhindern
     * 4. Impulsbasierter Geschwindigkeitsaustausch
     * 5. Rückkonversion zu Polarkoordinaten (Winkel und Winkelgeschwindigkeit)
     */
    private void checkAndHandleCollisions() {
        // Durchlaufe alle benachbarten Paare (optimiert für eine 1D-Kette wie beim Newton-Pendel)
        for (int i = 0; i < pendulums.size() - 1; i++) {
            Pendulum p1 = pendulums.get(i);
            Pendulum p2 = pendulums.get(i + 1);

            // --- SCHRITT 1: Konversion Polar -> Kartesisch ---
            // Hole die Positionen (P)
            double x1 = p1.getX();
            double y1 = p1.getY();
            double x2 = p2.getX();
            double y2 = p2.getY();

            // Berechne die Geschwindigkeitsvektoren (V) basierend auf der Winkelgeschwindigkeit
            // V_tangential = L * omega
            // Vx = Ableitung von x = -L * omega * cos(theta) (da x = pivot - L*sin(theta))
            // Vy = Ableitung von y = -L * omega * sin(theta) (da y = L*cos(theta))
            double omega1 = p1.getState().getOmega();
            double theta1 = p1.getState().getTheta();
            double vx1 = -p1.getLength() * omega1 * Math.cos(theta1);
            double vy1 = -p1.getLength() * omega1 * Math.sin(theta1);

            double omega2 = p2.getState().getOmega();
            double theta2 = p2.getState().getTheta();
            double vx2 = -p2.getLength() * omega2 * Math.cos(theta2);
            double vy2 = -p2.getLength() * omega2 * Math.sin(theta2);

            // --- SCHRITT 2: C++-Logik (Vektor P12 und Abstand) ---
            double dx = x1 - x2; // Hinweis: C++ macht p - other (p1 - p2)
            double dy = y1 - y2;
            double dist = Math.sqrt(dx * dx + dy * dy);
            double r_sum = p1.getRadius() + p2.getRadius();

            // Prüfe ob sich die Kugeln überlappen
            if (dist < r_sum) {

                // Berechne den Normalenvektor (Einheitsvektor)
                double nx = dx / dist;
                double ny = dy / dist;

                // Berechne die Eindringtiefe
                double penetration_depth = r_sum - dist;

                // --- SCHRITT 3: Positionskorrektur (Anti-Stick) ---
                // Wie in C++: particle.p += 0.5 * depth * normal
                // Verschiebe die Mittelpunkte virtuell, sodass sie sich nicht mehr berühren
                double moveX = 0.5 * penetration_depth * nx;
                double moveY = 0.5 * penetration_depth * ny;

                // Neue temporäre Positionen
                double newX1 = x1 + moveX;
                double newY1 = y1 + moveY;
                double newX2 = x2 - moveX; // Die andere Kugel bewegt sich in die Gegenrichtung
                double newY2 = y2 - moveY;

                // --- SCHRITT 4: Berechnung des Impulses (Geschwindigkeit) ---
                // v_rel = dot(v1 - v2, normal)
                double vRelX = vx1 - vx2;
                double vRelY = vy1 - vy2;
                double v_rel = vRelX * nx + vRelY * ny;

                // Stelle sicher, dass die Kollision eine Annäherung ist (v_rel < 0)
                // Wenn v_rel > 0, entfernen sie sich bereits, wir wenden nur die Positionskorrektur an
                double newVx1 = vx1;
                double newVy1 = vy1;
                double newVx2 = vx2;
                double newVy2 = vy2;

                if (v_rel < 0) {
                    double e = config.restitutionskoeffizient; // z.B. 1.0 oder 0.9

                    // Formel für den skalaren Impuls j
                    // j = -(1 + e) * v_rel / (1/m1 + 1/m2)
                    double invMass1 = 1.0 / p1.getMass();
                    double invMass2 = 1.0 / p2.getMass();
                    double j = -(1 + e) * v_rel / (invMass1 + invMass2);

                    // Aktualisiere die Geschwindigkeiten (Vektoriell)
                    // v1 = v1 + (j / m1) * normal
                    double impulseX1 = (j * invMass1) * nx;
                    double impulseY1 = (j * invMass1) * ny;
                    newVx1 += impulseX1;
                    newVy1 += impulseY1;

                    // v2 = v2 - (j / m2) * normal
                    double impulseX2 = (j * invMass2) * nx;
                    double impulseY2 = (j * invMass2) * ny;
                    newVx2 -= impulseX2;
                    newVy2 -= impulseY2;
                }

                // --- SCHRITT 5: Rückkonversion Kartesisch -> Polar ---
                // Hier speichern wir das Ergebnis in unserem Pendulum-Modell

                // 5a. Wende die Positionskorrektur an (Neuberechnung von Theta)
                // Wir müssen den neuen Winkel finden, der der neuen Position (newX, newY) entspricht
                // Geometrie: x = pivotX - L*sin(theta)  => sin(theta) = (pivotX - x) / L
                // Für Präzision verwenden wir atan2 mit der Position relativ zum Aufhängepunkt
                double relX1 = p1.getPivotX() - newX1; // Achtung auf die Richtung
                double relY1 = newY1; // Der Pivot ist bei y=0 (relativ) oder wir behalten y wie es ist wenn pivotY=0
                // Trick: Math.asin ist einfacher wenn wir annehmen dass L konstant ist
                // sin(theta) = (pivotX - x) / L
                double sinTheta1 = (p1.getPivotX() - newX1) / p1.getLength();
                // Begrenze um Rundungsfehler außerhalb von [-1, 1] zu vermeiden
                sinTheta1 = Math.max(-1, Math.min(1, sinTheta1));
                double newTheta1 = Math.asin(sinTheta1);

                double sinTheta2 = (p2.getPivotX() - newX2) / p2.getLength();
                sinTheta2 = Math.max(-1, Math.min(1, sinTheta2));
                double newTheta2 = Math.asin(sinTheta2);

                // 5b. Wende die Geschwindigkeitskorrektur an (Neuberechnung von Omega)
                // Omega ist die Tangentialkomponente der Geschwindigkeit geteilt durch L.
                // Wir projizieren den Geschwindigkeitsvektor auf den Tangentialvektor.
                // Einheitstangentialvektor bei theta: T = (-cos(theta), -sin(theta))
                // Überprüfung: Pos = (-sin, cos). Ableitung = (-cos, -sin). Ja.
                // Aber einfacher: Omega = (x*vy - y*vx) / (x*x + y*y) für eine Kreisbewegung?
                // Oder einfach: V_tangential = V dot TangentVector.

                // Berechnung des Einheitstangentialvektors für den NEUEN Winkel theta
                // Wenn Pos = (Pivot - L sin, L cos)
                // Tangente (Richtung theta positiv) = (-L cos, -L sin) normiert -> (-cos, -sin)
                double tx1 = -Math.cos(newTheta1);
                double ty1 = -Math.sin(newTheta1);
                double vTangential1 = newVx1 * tx1 + newVy1 * ty1;
                double newOmega1 = vTangential1 / p1.getLength();

                double tx2 = -Math.cos(newTheta2);
                double ty2 = -Math.sin(newTheta2);
                double vTangential2 = newVx2 * tx2 + newVy2 * ty2;
                double newOmega2 = vTangential2 / p2.getLength();

                // --- SCHRITT 6: Finale Aktualisierung der Objekte ---
                p1.setState(new PhysicsState(newTheta1, newOmega1));
                p2.setState(new PhysicsState(newTheta2, newOmega2));

                // Logge die Kollision
                logger.logInfo(String.format("Kollision zwischen Pendel %d und %d", i, i+1));
            }
        }
    }

    /**
     * Aktualisiert alle GUI-Komponenten.
     */
    private void updateGUI() {
        // Canvas neu zeichnen
        drawSimulation();

        // Zeit aktualisieren
        double currentTime = engines.get(0).getCurrentTime();
        timeLabel.setText(String.format("Zeit: %.3f s", currentTime));

        // Energie aktualisieren
        double totalEnergy = 0.0;
        for (Pendulum p : pendulums) {
            totalEnergy += p.getTotalEnergy();
        }
        double energyError = Math.abs((totalEnergy - initialTotalEnergy) / initialTotalEnergy) * 100.0;
        energyLabel.setText(String.format("Gesamtenergie: %.6f J\nAbweichung: %.4f%%",
                totalEnergy, energyError));

        // Energiebalken aktualisieren
        double maxEnergy = initialTotalEnergy / pendulums.size() * 1.5; // Für Skalierung
        for (int i = 0; i < pendulums.size(); i++) {
            Pendulum p = pendulums.get(i);
            energyBars.get(i).update(p.getKineticEnergy(), p.getPotentialEnergy(),
                    p.getTotalEnergy(), maxEnergy);
        }

        // Konsolenausgabe (nur alle 0.1s)
        if (currentTime - Math.floor(currentTime / 0.1) * 0.1 < TIME_STEP) {
            appendToConsole(getStateString());
        }
    }

    /**
     * Zeichnet die Simulation auf das Canvas.
     */
    private void drawSimulation() {
        // Canvas leeren
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        // Koordinatensystem: Ursprung in der Mitte, oben
        double originX = CANVAS_WIDTH / 2.0;
        double originY = 100;

        // Zeichne jedes Pendel
        for (Pendulum pendulum : pendulums) {
            drawPendulum(pendulum, originX, originY);
        }
    }

    /**
     * Zeichnet ein einzelnes Pendel.
     *
     * Geometrie-Konvention:
     * - Positive Winkel θ bedeuten Auslenkung gegen den Uhrzeigersinn (nach links)
     * - Die Position wird berechnet als: x = pivotX - L*sin(θ), y = L*cos(θ)
     */
    private void drawPendulum(Pendulum pendulum, double originX, double originY) {
        double theta = pendulum.getState().getTheta();
        double pivotX = pendulum.getPivotX();

        // Aufhängepunkt in Pixel
        double pivotScreenX = originX + pivotX * scale;
        double pivotScreenY = originY;

        // Kugelposition in Pixel
        // WICHTIG: getX() gibt bereits die korrekte Position mit der Formel x = pivotX - L*sin(θ)
        double ballX = pendulum.getX();
        double ballY = pendulum.getY();
        double ballScreenX = originX + ballX * scale;
        double ballScreenY = originY + ballY * scale;

        // Zeichne Aufhängepunkt
        gc.setFill(Color.DARKGRAY);
        gc.fillOval(pivotScreenX - 5, pivotScreenY - 5, 10, 10);

        // Zeichne Pendelstange
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeLine(pivotScreenX, pivotScreenY, ballScreenX, ballScreenY);

        // Zeichne Kugel
        double radiusPixel = pendulum.getRadius() * scale;
        gc.setFill(Color.STEELBLUE);
        gc.fillOval(ballScreenX - radiusPixel, ballScreenY - radiusPixel,
                2 * radiusPixel, 2 * radiusPixel);

        // Rand der Kugel
        gc.setStroke(Color.DARKBLUE);
        gc.setLineWidth(2);
        gc.strokeOval(ballScreenX - radiusPixel, ballScreenY - radiusPixel,
                2 * radiusPixel, 2 * radiusPixel);

        // Gewichtskraftvektor (wenn aktiviert)
        if (showGravityForce) {
            drawGravityForce(ballScreenX, ballScreenY);
        }
    }

    /**
     * Zeichnet den Gewichtskraftvektor.
     */
    private void drawGravityForce(double x, double y) {
        gc.setStroke(Color.RED);
        gc.setLineWidth(3);

        double arrowLength = 50; // Fixe Länge
        gc.strokeLine(x, y, x, y + arrowLength);

        // Pfeilspitze
        gc.strokeLine(x, y + arrowLength, x - 5, y + arrowLength - 10);
        gc.strokeLine(x, y + arrowLength, x + 5, y + arrowLength - 10);

        // Label
        gc.setFill(Color.RED);
        gc.fillText("Fg", x + 10, y + arrowLength / 2);
    }

    /**
     * Erstellt einen String mit dem aktuellen Zustand aller Pendel.
     */
    private String getStateString() {
        StringBuilder sb = new StringBuilder();
        double time = engines.get(0).getCurrentTime();
        sb.append(String.format("t=%.3fs | ", time));

        for (int i = 0; i < pendulums.size(); i++) {
            Pendulum p = pendulums.get(i);
            double theta = p.getState().getTheta();
            sb.append(String.format("Kugel%d: θ=%.3f° ", i + 1, Math.toDegrees(theta)));
        }

        return sb.toString();
    }

    /**
     * Fügt Text zur Konsolenausgabe hinzu.
     */
    private void appendToConsole(String text) {
        consoleOutput.appendText(text + "\n");
        consoleOutput.setScrollTop(Double.MAX_VALUE);
    }

    // === KONTROLL-METHODEN ===

    private void startSimulation() {
        isRunning = true;
        isPaused = false;
        timer.start();

        btnStart.setDisable(true);
        btnPause.setDisable(false);
        btnStop.setDisable(false);
        btnStep.setDisable(true);

        logger.logInfo("Simulation started");
        appendToConsole("=== Simulation gestartet ===");
    }

    private void pauseSimulation() {
        isPaused = !isPaused;
        btnPause.setText(isPaused ? "▶ Fortsetzen" : "⏸ Pause");
        btnStep.setDisable(!isPaused);

        logger.logInfo("Simulation " + (isPaused ? "paused" : "resumed"));
        appendToConsole(isPaused ? "=== Pausiert ===" : "=== Fortgesetzt ===");
    }

    private void stopSimulation() {
        isRunning = false;
        isPaused = false;
        timer.stop();

        btnStart.setDisable(false);
        btnPause.setDisable(true);
        btnStop.setDisable(true);
        btnStep.setDisable(false);

        logger.logInfo("Simulation stopped");
        appendToConsole("=== Simulation gestoppt ===");
    }

    private void resetSimulation() {
        stopSimulation();
        initSimulation();
        drawSimulation();
        updateGUI();
        consoleOutput.clear();

        logger.logInfo("Simulation reset");
        appendToConsole("=== Simulation zurückgesetzt ===");
    }

    private void stepSimulation() {
        performSimulationStep();
        updateGUI();

        logger.logDebug("Single step performed");
    }

    /**
     * Innere Klasse für Energiebalken-Darstellung.
     */
    private static class EnergyBar {
        private VBox container;
        private Label label;
        private ProgressBar barKin;
        private ProgressBar barPot;
        private ProgressBar barTotal;
        private Label valueLabel;

        public EnergyBar(String name) {
            container = new VBox(3);

            label = new Label(name);
            label.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");

            HBox barsBox = new HBox(5);

            barKin = new ProgressBar(0);
            barKin.setPrefWidth(60);
            barKin.setStyle("-fx-accent: #2196f3;");

            barPot = new ProgressBar(0);
            barPot.setPrefWidth(60);
            barPot.setStyle("-fx-accent: #f44336;");

            barTotal = new ProgressBar(0);
            barTotal.setPrefWidth(60);
            barTotal.setStyle("-fx-accent: #4caf50;");

            barsBox.getChildren().addAll(barKin, barPot, barTotal);

            valueLabel = new Label("E=0.000 J");
            valueLabel.setStyle("-fx-font-size: 10px;");

            container.getChildren().addAll(label, barsBox, valueLabel);
        }

        public void update(double eKin, double ePot, double eTotal, double maxEnergy) {
            barKin.setProgress(eKin / maxEnergy);
            barPot.setProgress(ePot / maxEnergy);
            barTotal.setProgress(eTotal / maxEnergy);
            valueLabel.setText(String.format("E=%.6f J", eTotal));
        }

        public VBox getNode() {
            return container;
        }
    }
}
