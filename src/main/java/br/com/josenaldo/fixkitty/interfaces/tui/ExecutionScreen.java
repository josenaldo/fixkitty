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
     * @param gui    the active window-based text GUI
     * @param action the action to execute
     */
    void run(WindowBasedTextGUI gui, RecoveryAction action) {
        log.info("Executing action: {}", action);
        // Phase 1: synchronous execution (progress shown after completion)
        ExecutionResult result = executeUseCase.execute(action);
        resultScreen.show(gui, result);
    }
}
