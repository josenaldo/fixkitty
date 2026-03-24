package br.com.josenaldo.fixkitty.interfaces.tui;

import br.com.josenaldo.fixkitty.core.domain.*;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;

/**
 * TUI screen that displays the result of a completed recovery action.
 *
 * <p>Shows overall status, individual step results, and recommendation.
 */
class ResultScreen {

    /**
     * Displays the execution result in a Lanterna dialog.
     *
     * @param gui    the active window-based text GUI
     * @param result the result to display
     */
    void show(WindowBasedTextGUI gui, ExecutionResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append("Status: ").append(result.status()).append("\n\n");

        for (StepResult step : result.steps()) {
            String icon = switch (step.status()) {
                case SUCCESS -> "[OK]";
                case FAILED -> "[FAIL]";
                case TIMEOUT -> "[TIMEOUT]";
                case SKIPPED -> "[SKIPPED]";
            };
            sb.append(icon).append(" ").append(step.step().description()).append("\n");
            if (step.status() == StepStatus.FAILED || step.status() == StepStatus.TIMEOUT) {
                if (!step.stderr().isBlank()) {
                    sb.append("  └─ ").append(step.stderr().lines().findFirst().orElse("")).append("\n");
                }
            }
        }

        if (result.recommendation() != null) {
            sb.append("\nRecommendation:\n").append(result.recommendation());
        }

        new MessageDialogBuilder()
            .setTitle("Result: " + result.action().displayName())
            .setText(sb.toString())
            .addButton(MessageDialogButton.OK)
            .build()
            .showDialog(gui);
    }
}
