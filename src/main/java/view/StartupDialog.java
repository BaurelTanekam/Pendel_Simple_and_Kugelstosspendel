package view;
import javafx.scene.*;

import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
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
    private TextField textResolution;

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

    }
}
