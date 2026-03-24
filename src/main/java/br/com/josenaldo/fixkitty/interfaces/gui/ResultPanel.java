package br.com.josenaldo.fixkitty.interfaces.gui;

import br.com.josenaldo.fixkitty.core.domain.*;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * JavaFX panel that displays the overall result of a recovery action.
 *
 * <p>Shows overall status with colour coding and a recommendation message
 * when the action did not fully succeed.
 */
public class ResultPanel extends VBox {

    private final Label statusLabel = new Label();
    private final Label recommendationLabel = new Label();

    /**
     * Creates an initially empty result panel.
     */
    public ResultPanel() {
        setPadding(new Insets(8));
        setSpacing(4);
        recommendationLabel.setWrapText(true);
        getChildren().addAll(statusLabel, recommendationLabel);
    }

    /**
     * Updates the panel to display the given execution result.
     * Must be called on the JavaFX Application Thread.
     *
     * @param result the result to display
     */
    public void display(ExecutionResult result) {
        statusLabel.setText("Status: " + result.status());
        statusLabel.setStyle(switch (result.status()) {
            case SUCCESS -> "-fx-text-fill: #50fa7b;";  // green
            case PARTIAL -> "-fx-text-fill: #ffb86c;";  // orange
            case FAILED -> "-fx-text-fill: #ff5555;";   // red
        });

        if (result.recommendation() != null) {
            recommendationLabel.setText("Recommendation: " + result.recommendation());
            recommendationLabel.setVisible(true);
        } else {
            recommendationLabel.setVisible(false);
        }
    }

    /**
     * Clears the panel. Call before starting a new action.
     */
    public void clear() {
        statusLabel.setText("");
        recommendationLabel.setVisible(false);
    }
}
