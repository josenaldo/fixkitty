package br.com.josenaldo.fixkitty.interfaces.gui;

import br.com.josenaldo.fixkitty.core.domain.*;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * JavaFX panel that displays real-time step progress during execution.
 *
 * <p>Must be updated on the JavaFX Application Thread via {@link Platform#runLater}.
 */
public class LogPanel extends VBox {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final int MAX_STDERR_LINES = 10;

    private final TextArea area = new TextArea();

    /**
     * Creates a scrollable, read-only log panel.
     */
    public LogPanel() {
        area.setEditable(false);
        area.setWrapText(true);
        area.setPrefRowCount(12);
        getChildren().add(area);
    }

    /**
     * Clears the log content. Call before starting a new action.
     * Must be called on the JavaFX Application Thread.
     */
    public void clear() {
        area.clear();
    }

    /**
     * Appends a step result to the log.
     * Must be called on the JavaFX Application Thread.
     *
     * @param result the step result to display
     */
    public void appendStep(StepResult result) {
        String time = LocalTime.now().format(TIME_FMT);
        String icon = switch (result.status()) {
            case SUCCESS -> "[OK]";
            case FAILED -> "[FAIL]";
            case TIMEOUT -> "[TIMEOUT]";
            case SKIPPED -> "[SKIPPED]";
        };
        area.appendText(String.format("%s %s %s%n", time, icon, result.step().description()));

        if (result.status() == StepStatus.FAILED || result.status() == StepStatus.TIMEOUT) {
            if (!result.stderr().isBlank()) {
                String[] lines = result.stderr().split("\n");
                int shown = Math.min(lines.length, MAX_STDERR_LINES);
                for (int i = 0; i < shown; i++) {
                    area.appendText("      " + lines[i] + "\n");
                }
                if (lines.length > MAX_STDERR_LINES) {
                    area.appendText("      ... (truncated)\n");
                }
            }
        }
    }
}
