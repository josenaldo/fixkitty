package br.com.josenaldo.fixkitty.interfaces.tui;

import br.com.josenaldo.fixkitty.application.ExecuteRecoveryUseCase;
import br.com.josenaldo.fixkitty.core.domain.*;
import com.googlecode.lanterna.gui2.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TUI screen that runs a recovery action and shows progress.
 *
 * <p>Executes the use case synchronously and delegates result rendering
 * to {@link ResultScreen}.
 */
class ExecutionScreen {

    private static final Logger log = LoggerFactory.getLogger(ExecutionScreen.class);
    private final ExecuteRecoveryUseCase executeUseCase;
    private final ResultScreen resultScreen;

    /**
     * Creates the execution screen.
     *
     * @param executeUseCase the use case for executing recovery actions
     * @param resultScreen   the screen for rendering results
     */
    ExecutionScreen(ExecuteRecoveryUseCase executeUseCase, ResultScreen resultScreen) {
        this.executeUseCase = executeUseCase;
        this.resultScreen = resultScreen;
    }

    /**
     * Executes the given action and shows the result.
     *
     * <p>Displays a progress window before execution so the user receives
     * immediate feedback (Phase 1: synchronous execution).
     *
     * @param gui    the active window-based text GUI
     * @param action the action to execute
     */
    void run(WindowBasedTextGUI gui, RecoveryAction action) {
        log.info("Executing action: {}", action);

        // Show progress window (Phase 1: replaced after synchronous execution)
        BasicWindow progressWindow = new BasicWindow("Running: " + action.displayName());
        progressWindow.setHints(java.util.List.of(Window.Hint.CENTERED));
        Panel panel = new Panel();
        panel.addComponent(new Label("Running " + action.displayName() + "..."));
        panel.addComponent(new Label("Please wait."));
        progressWindow.setComponent(panel);
        gui.addWindow(progressWindow);
        try {
            gui.updateScreen();
        } catch (java.io.IOException e) {
            log.warn("Failed to render progress window: {}", e.getMessage());
        }

        ExecutionResult result = executeUseCase.execute(action);

        progressWindow.close();
        resultScreen.show(gui, result);
    }
}
