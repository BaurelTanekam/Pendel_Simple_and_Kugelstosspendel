package view;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.*;

import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * StartupDialog - Der Konfigurationsdialog beim Programmstart.
 *
 * Dieser Dialog erscheint beim Start der Anwendung und erlaubt dem Benutzer,
 * alle wichtigen Parameter der Simulation einzustellen:
 * - Modus: Einzelpendel oder Kugelstoßpendel
 * - Physikalische Parameter: Masse, Radius, Startwinkel
 * - Für Kugelstoßpendel: Anzahl der Kugeln, Restitutionskoeffizient
 *
 * Der Dialog validiert alle Eingaben in Echtzeit und zeigt Warnungen bei
 * ungültigen oder problematischen Werten.
 */
public class StartupDialog extends Stage {
    private SimulationConfig config;

    //UI-Komponent
    private RadioButton radioEinzelpendel;
    private RadioButton radioKugelstosspendel;

    //Paramater
    private TextField textMasse;
    private TextField textRadius;
    private TextField textStartwinkel;

    //Parameter für Kugelstoßpendel
    private Spinner<Integer> spinnerAnzahlKugeln;
    private Spinner<Integer> spinnerAusgelenkt;
    private TextField textRestitution;

    // Container für Kugelstoßpendel-Parameter
    private VBox kugelstosspendelBox;

    // Warnungs-Label für große Winkel
    private Label warningLabel;

    /**
     * Konstruktor für den Startdialog.
     */
    public StartupDialog() {
        initUI();
    }

    private void initUI() {
        setTitle("Pendelsimulation - Konfiguration");
        initModality(Modality.APPLICATION_MODAL);   //
        setResizable(false);

        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: #f0f0f0");

        Label titleLabel = new Label("Willkommen zur Pendesimulation");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label subtitleLabel = new Label("Bitte wählen Sie den Simulationsmodus und die Parameter:");
        subtitleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        // Trennlinie
        Separator separator1 = new Separator();

        // === MODUS-AUSWAHL ===
        VBox modusBox = createModusSelection();

        Separator separator2 = new Separator();

        // === GEMEINSAME PARAMETER ===
        VBox gemeinsameBox = createGemeinsameParameter();

        // === KUGELSTOSSPENDEL-SPEZIFISCHE PARAMETER ===
        kugelstosspendelBox = createKugelstossppendelParameter();
        kugelstosspendelBox.setVisible(false);
        kugelstosspendelBox.setManaged(false);

        // === WARNUNGS-LABEL ===
        warningLabel = new Label();
        warningLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
        warningLabel.setVisible(false);
        warningLabel.setWrapText(true);

        // === BUTTONS ===
        HBox buttonBox = createButtons();

        // Alles zusammenfügen
        mainLayout.getChildren().addAll(
                titleLabel,
                subtitleLabel,
                separator1,
                modusBox,
                separator2,
                gemeinsameBox,
                kugelstosspendelBox,
                warningLabel,
                buttonBox
        );

        Scene scene = new Scene(mainLayout);
        setScene(scene);
    }

    /**
     * Erstellt die Modus-Auswahl (Radio-Buttons).
     */
    private VBox createModusSelection() {
        VBox box = new VBox(10);

        Label label = new Label("Simulationsmodus:");
        label.setStyle("-fx-font-weight: bold;");

        ToggleGroup modusGroup = new ToggleGroup();

        radioEinzelpendel = new RadioButton("Einzelpendel (Kleinwinkelnäherung)");
        radioEinzelpendel.setToggleGroup(modusGroup);
        radioEinzelpendel.setSelected(true);

        radioKugelstosspendel = new RadioButton("Kugelstoßpendel");
        radioKugelstosspendel.setToggleGroup(modusGroup);

        // Event-Handler für Modus-Wechsel
        radioEinzelpendel.setOnAction(e -> {
            kugelstosspendelBox.setVisible(false);
            kugelstosspendelBox.setManaged(false);
            sizeToScene();
        });

        radioKugelstosspendel.setOnAction(e -> {
            kugelstosspendelBox.setVisible(true);
            kugelstosspendelBox.setManaged(true);
            sizeToScene();
        });

        box.getChildren().addAll(label, radioEinzelpendel, radioKugelstosspendel);
        return box;
    }

    /**
     * Erstellt die gemeinsamen Parameter-Eingabefelder.
     */
    private VBox createGemeinsameParameter() {
        VBox box = new VBox(10);
        box.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-border-color: #ccc; -fx-border-radius: 5;");

        Label label = new Label("Physikalische Parameter:");
        label.setStyle("-fx-font-weight: bold;");

        // Masse
        HBox masseBox = new HBox(10);
        masseBox.setAlignment(Pos.CENTER_LEFT);
        Label massLabel = new Label("Masse der Kugel (kg):");
        massLabel.setPrefWidth(180);
        textMasse = createValidatedTextField("1.0", "^[0-9]*\\.?[0-9]+$");
        masseBox.getChildren().addAll(massLabel, textMasse);

        // Radius
        HBox radiusBox = new HBox(10);
        radiusBox.setAlignment(Pos.CENTER_LEFT);
        Label radiusLabel = new Label("Kugelradius (m):");
        radiusLabel.setPrefWidth(180);
        textRadius = createValidatedTextField("0.025", "^[0-9]*\\.?[0-9]+$");
        radiusBox.getChildren().addAll(radiusLabel, textRadius);

        // Startwinkel
        HBox winkelBox = new HBox(10);
        winkelBox.setAlignment(Pos.CENTER_LEFT);
        Label winkelLabel = new Label("Startwinkel (Grad):");
        winkelLabel.setPrefWidth(180);
        textStartwinkel = createValidatedTextField("10.0", "^[0-9]*\\.?[0-9]+$");

        // Event-Handler für Winkel-Warnung
        textStartwinkel.textProperty().addListener((obs, oldVal, newVal) -> {
            validateWinkel();
        });

        winkelBox.getChildren().addAll(winkelLabel, textStartwinkel);

        box.getChildren().addAll(label, masseBox, radiusBox, winkelBox);
        return box;
    }


    /**
     * Erstellt die Kugelstoßpendel-spezifischen Parameter.
     */
    private VBox createKugelstossppendelParameter() {
        VBox box = new VBox(10);
        box.setStyle("-fx-background-color: #e3f2fd; -fx-padding: 10; -fx-border-color: #2196f3; -fx-border-radius: 5;");

        Label label = new Label("Kugelstoßpendel-Parameter:");
        label.setStyle("-fx-font-weight: bold;");

        // Anzahl der Kugeln
        HBox anzahlBox = new HBox(10);
        anzahlBox.setAlignment(Pos.CENTER_LEFT);
        Label anzahlLabel = new Label("Anzahl der Kugeln:");
        anzahlLabel.setPrefWidth(180);
        spinnerAnzahlKugeln = new Spinner<>(1, 5, 5);
        spinnerAnzahlKugeln.setEditable(true);
        spinnerAnzahlKugeln.setPrefWidth(100);
        anzahlBox.getChildren().addAll(anzahlLabel, spinnerAnzahlKugeln);

        // Anzahl ausgelenkte Kugeln
        HBox ausgBox = new HBox(10);
        ausgBox.setAlignment(Pos.CENTER_LEFT);
        Label ausgLabel = new Label("Ausgelenkte Kugeln:");
        ausgLabel.setPrefWidth(180);
        spinnerAusgelenkt = new Spinner<>(1, 5, 1);
        spinnerAusgelenkt.setEditable(true);
        spinnerAusgelenkt.setPrefWidth(100);
        ausgBox.getChildren().addAll(ausgLabel, spinnerAusgelenkt);

        // Automatische Korrektur: Wenn Anzahl geändert wird, passe ausgelenkt an
        spinnerAnzahlKugeln.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (spinnerAusgelenkt.getValue() > newVal) {
                spinnerAusgelenkt.getValueFactory().setValue(newVal);
            }
            spinnerAusgelenkt.setValueFactory(
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(1, newVal,
                            Math.min(spinnerAusgelenkt.getValue(), newVal))
            );
        });

        // Restitutionskoeffizient
        HBox restBox = new HBox(10);
        restBox.setAlignment(Pos.CENTER_LEFT);
        Label restLabel = new Label("Restitutionskoeffizient:");
        restLabel.setPrefWidth(180);
        textRestitution = createValidatedTextField("1.0", "^[0-1]?\\.?[0-9]*$");
        restBox.getChildren().addAll(restLabel, textRestitution);

        Label infoLabel = new Label("(1.0 = perfekt elastisch, 0.0 = perfekt inelastisch)");
        infoLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");

        box.getChildren().addAll(label, anzahlBox, ausgBox, restBox, infoLabel);
        return box;
    }

    /**
     * Erstellt ein validiertes Textfeld, das nur bestimmte Zeichen erlaubt.
     *
     * @param defaultValue Der Standardwert
     * @param regex Der reguläre Ausdruck für erlaubte Zeichen
     * @return Das konfigurierte TextField
     */
    private TextField createValidatedTextField(String defaultValue, String regex) {
        TextField textField = new TextField(defaultValue);
        textField.setPrefWidth(100);

        // Validierung während der Eingabe
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches(regex)) {
                textField.setText(oldValue);
            }

            // Visuelles Feedback
            if (newValue.isEmpty() || !isValidNumber(newValue)) {
                textField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            } else {
                textField.setStyle("");
            }
        });

        return textField;
    }

    /**
     * Überprüft, ob ein String eine gültige Zahl ist.
     */
    private boolean isValidNumber(String str) {
        try {
            double val = Double.parseDouble(str);
            return val >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validiert den eingegebenen Winkel und zeigt ggf. eine Warnung.
     */
    private void validateWinkel() {
        try {
            double winkel = Double.parseDouble(textStartwinkel.getText());
            if (radioEinzelpendel.isSelected() && winkel > 15.0) {
                warningLabel.setText("⚠ WARNUNG: Winkel > 15° überschreitet die Kleinwinkelnäherung!\n" +
                        "Die Ergebnisse können ungenau sein.");
                warningLabel.setVisible(true);
            } else {
                warningLabel.setVisible(false);
            }
        } catch (NumberFormatException e) {
            warningLabel.setVisible(false);
        }
    }

    /**
     * Erstellt die Button-Leiste.
     */
    private HBox createButtons() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_RIGHT);

        Button btnStart = new Button("Simulation starten");
        btnStart.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
        btnStart.setOnAction(e -> onStart());

        Button btnAbbrechen = new Button("Abbrechen");
        btnAbbrechen.setStyle("-fx-padding: 10 20;");
        btnAbbrechen.setOnAction(e -> {
            config = null;
            close();
        });

        box.getChildren().addAll(btnAbbrechen, btnStart);
        return box;
    }

    /**
     * Wird aufgerufen, wenn der Benutzer auf "Starten" klickt.
     */
    private void onStart() {
        // Validierung
        if (!validateInputs()) {
            return;
        }

        // Konfiguration erstellen
        config = new SimulationConfig();
        config.istKugelstosspendel = radioKugelstosspendel.isSelected();
        config.masse = Double.parseDouble(textMasse.getText());
        config.radius = Double.parseDouble(textRadius.getText());
        config.startWinkelGrad = Double.parseDouble(textStartwinkel.getText());

        if (config.istKugelstosspendel) {
            config.anzahlKugeln = spinnerAnzahlKugeln.getValue();
            config.anzahlAusgelenkt = spinnerAusgelenkt.getValue();
            config.restitutionskoeffizient = Double.parseDouble(textRestitution.getText());
        } else {
            config.anzahlKugeln = 1;
            config.anzahlAusgelenkt = 1;
            config.restitutionskoeffizient = 1.0;
        }

        close();
    }

    /**
     * Validiert alle Eingaben.
     */
    private boolean validateInputs() {
        try {
            double masse = Double.parseDouble(textMasse.getText());
            double radius = Double.parseDouble(textRadius.getText());
            double winkel = Double.parseDouble(textStartwinkel.getText());

            if (masse <= 0 || radius <= 0 || winkel < 0) {
                showError("Alle Werte müssen positiv sein!");
                return false;
            }

            if (radioKugelstosspendel.isSelected()) {
                double rest = Double.parseDouble(textRestitution.getText());
                if (rest < 0 || rest > 1) {
                    showError("Restitutionskoeffizient muss zwischen 0 und 1 liegen!");
                    return false;
                }
            }

            return true;
        } catch (NumberFormatException e) {
            showError("Bitte geben Sie gültige Zahlenwerte ein!");
            return false;
        }
    }

    /**
     * Zeigt eine Fehlermeldung an.
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ungültige Eingabe");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Gibt die erstellte Konfiguration zurück.
     *
     * @return Die Konfiguration oder null, wenn abgebrochen wurde
     */
    public SimulationConfig getConfig() {
        return config;
    }
}
