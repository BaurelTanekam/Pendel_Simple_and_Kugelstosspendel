package util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Logger-Klasse für strukturiertes Logging der Simulation.
 *
 * Diese Klasse implementiert das Singleton-Pattern, was bedeutet, dass es nur eine einzige
 * Instanz des Loggers in der gesamten Anwendung gibt. Das ist sinnvoll, weil alle Teile
 * der Simulation in dieselbe Log-Datei schreiben sollen und wir die Datei nicht mehrfach
 * öffnen wollen.
 *
 * Der Logger schreibt sowohl in eine Datei als auch optional in die Konsole.
 * Die Datei enthält alle Details für die Auswertung, während die Konsole nur wichtige
 * Ereignisse zeigt für Live-Debugging.
 *
 * Log-Levels:
 * - INFO: Normale Ereignisse (Start, Stop, Parameteränderungen)
 * - WARNING: Potentielle Probleme (große Energieabweichungen, zu große Zeitschritte)
 * - ERROR: Schwerwiegende Fehler (Simulation instabil, Berechnungsfehler)
 * - DEBUG: Detaillierte Informationen für Entwicklung (nur bei aktiviertem Debug-Modus)
 */
public class Logger {

    /**
     * Die einzige Instanz des Loggers (Singleton-Pattern).
     */
    private static Logger instance;

    /**
     * Der FileWriter zum Schreiben in die Log-Datei.
     */
    private PrintWriter fileWriter;

    /**
     * Der Name der Log-Datei.
     */
    private final String logFileName;

    /**
     * Flag, das angibt, ob auch in die Konsole geloggt werden soll.
     */
    private boolean consoleLoggingEnabled;

    /**
     * Flag für den Debug-Modus.
     * Wenn true, werden auch DEBUG-Nachrichten ausgegeben.
     */
    private boolean debugMode;

    /**
     * Formatter für Zeitstempel im Format: 2025-01-12 14:30:45
     */
    private final DateTimeFormatter timeFormatter;

    /**
     * Privater Konstruktor (Singleton-Pattern).
     * Niemand kann direkt new Logger() aufrufen, sondern muss getInstance() verwenden.
     */
    private Logger() {
        String timestamp = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
        );
        this.logFileName = "pendelsimulation_" + timestamp + ".md";
        this.consoleLoggingEnabled = true;
        this.debugMode = false;
        this.timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try {
            fileWriter = new PrintWriter(new FileWriter(logFileName, true), true);
            logInfo("Logger initialized. Log file: " + logFileName);
        } catch (IOException e) {
            System.err.println("ERROR: Could not create log file: " + e.getMessage());

            fileWriter = null;
        }
    }

    /**
     * Gibt die einzige Instanz des Loggers zurück (Singleton-Pattern).
     * Beim ersten Aufruf wird der Logger erstellt, danach wird immer dieselbe Instanz zurückgegeben.
     *
     * @return Die Logger-Instanz
     */
    public static synchronized Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }

    /**
     * Loggt eine INFO-Nachricht.
     * Wird verwendet für normale Ereignisse wie Start, Stop, Parameteränderungen.
     *
     * @param message Die zu loggende Nachricht
     */
    public void logInfo(String message) {
        log("INFO", message, true);
    }

    /**
     * Loggt eine WARNING-Nachricht.
     * Wird verwendet für potentielle Probleme, die keine Fehler sind aber Aufmerksamkeit verdienen.
     *
     * @param message Die Warnung
     */
    public void logWarning(String message) {
        log("WARNING", message, true);
    }

    /**
     * Loggt eine ERROR-Nachricht.
     * Wird verwendet für schwerwiegende Fehler, die die Simulation beeinträchtigen.
     *
     * @param message Die Fehlermeldung
     */
    public void logError(String message) {
        log("ERROR", message, true);
    }

    /**
     * Loggt eine DEBUG-Nachricht.
     * Wird nur ausgegeben, wenn der Debug-Modus aktiviert ist.
     * Nützlich für detaillierte Informationen während der Entwicklung.
     *
     * @param message Die Debug-Nachricht
     */
    public void logDebug(String message) {
        if (debugMode) {
            log("DEBUG", message, false);
        }
    }

    /**
     * Interne Methode zum Formatieren und Ausgeben von Log-Nachrichten.
     *
     * @param level Das Log-Level (INFO, WARNING, ERROR, DEBUG)
     * @param message Die Nachricht
     * @param showInConsole Ob die Nachricht auch in der Konsole erscheinen soll
     */
    private void log(String level, String message, boolean showInConsole) {
        String timestamp = LocalDateTime.now().format(timeFormatter);
        String logLine = String.format("[%s] %s: %s", timestamp, level, message);

        // In Datei schreiben (falls vorhanden)
        if (fileWriter != null) {
            fileWriter.println(logLine);
        }

        // In Konsole ausgeben (falls aktiviert)
        if (consoleLoggingEnabled && showInConsole) {
            if (level.equals("ERROR")) {
                System.err.println(logLine);
            } else {
                System.out.println(logLine);
            }
        }
    }

    /**
     * Aktiviert oder deaktiviert die Konsolen-Ausgabe.
     *
     * @param enabled true = Ausgabe in Konsole, false = nur in Datei
     */
    public void setConsoleLoggingEnabled(boolean enabled) {
        this.consoleLoggingEnabled = enabled;
    }

    /**
     * Aktiviert oder deaktiviert den Debug-Modus.
     *
     * @param enabled true = DEBUG-Nachrichten werden ausgegeben
     */
    public void setDebugMode(boolean enabled) {
        this.debugMode = enabled;
        logInfo("Debug mode " + (enabled ? "enabled" : "disabled"));
    }

    /**
     * Schließt die Log-Datei ordnungsgemäß.
     * Sollte aufgerufen werden, bevor die Anwendung beendet wird.
     */
    public void close() {
        if (fileWriter != null) {
            logInfo("Closing logger");
            fileWriter.close();
        }
    }

    /**
     * Gibt den Namen der Log-Datei zurück.
     *
     * @return Der Dateiname der Log-Datei
     */
    public String getLogFileName() {
        return logFileName;
    }
}

