package br.com.josenaldo.fixkitty.interfaces.tui;

import br.com.josenaldo.fixkitty.application.*;
import com.google.inject.Inject;
import com.googlecode.lanterna.screen.*;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for the FixKitty terminal user interface.
 *
 * <p>Initialises a Lanterna screen and launches the main menu.
 * All Lanterna imports are confined to {@code interfaces/tui}.
 */
public class TuiApp {

    private static final Logger log = LoggerFactory.getLogger(TuiApp.class);

    private final ExecuteRecoveryUseCase executeUseCase;
    private final ListActionsUseCase listActionsUseCase;
    private final CheckEnvironmentUseCase checkEnvironmentUseCase;

    /**
     * Creates the TUI application with all required use cases.
     *
     * @param executeUseCase         executes recovery actions
     * @param listActionsUseCase     lists available actions
     * @param checkEnvironmentUseCase detects the environment
     */
    @Inject
    public TuiApp(ExecuteRecoveryUseCase executeUseCase,
                  ListActionsUseCase listActionsUseCase,
                  CheckEnvironmentUseCase checkEnvironmentUseCase) {
        this.executeUseCase = executeUseCase;
        this.listActionsUseCase = listActionsUseCase;
        this.checkEnvironmentUseCase = checkEnvironmentUseCase;
    }

    /**
     * Starts the TUI application, blocking until the user exits.
     */
    public void start() {
        Screen screen = null;
        try {
            screen = new DefaultTerminalFactory().createScreen();
            screen.startScreen();
            MainMenuScreen menu = new MainMenuScreen(
                executeUseCase, listActionsUseCase, checkEnvironmentUseCase);
            menu.show(screen);
        } catch (Exception e) {
            log.error("TUI error: {}", e.getMessage(), e);
            System.err.println("TUI error: " + e.getMessage());
        } finally {
            if (screen != null) {
                try {
                    screen.stopScreen();
                } catch (Exception ignored) {
                    // best effort
                }
            }
        }
    }
}
