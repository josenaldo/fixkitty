package br.com.josenaldo.fixkitty.interfaces.tui;

import br.com.josenaldo.fixkitty.application.*;
import br.com.josenaldo.fixkitty.core.domain.*;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.screen.Screen;

import java.util.List;

/**
 * TUI main menu screen.
 *
 * <p>Lists all available recovery actions and the environment profile in the header.
 * Delegates action execution to {@link ExecutionScreen} and environment display
 * to {@link EnvironmentScreen}.
 */
class MainMenuScreen {

    private final ExecuteRecoveryUseCase executeUseCase;
    private final ListActionsUseCase listActionsUseCase;
    private final CheckEnvironmentUseCase checkEnvironmentUseCase;

    /**
     * Creates the main menu screen.
     *
     * @param executeUseCase          executes recovery actions
     * @param listActionsUseCase      lists available actions
     * @param checkEnvironmentUseCase detects environment
     */
    MainMenuScreen(ExecuteRecoveryUseCase executeUseCase,
                   ListActionsUseCase listActionsUseCase,
                   CheckEnvironmentUseCase checkEnvironmentUseCase) {
        this.executeUseCase = executeUseCase;
        this.listActionsUseCase = listActionsUseCase;
        this.checkEnvironmentUseCase = checkEnvironmentUseCase;
    }

    /**
     * Displays the main menu, blocking until the user exits.
     *
     * @param screen the active Lanterna screen
     */
    void show(Screen screen) {
        MultiWindowTextGUI gui = new MultiWindowTextGUI(screen);
        EnvironmentProfile profile = checkEnvironmentUseCase.execute();
        List<RecoveryAction> actions = listActionsUseCase.execute();

        ExecutionScreen executionScreen = new ExecutionScreen(executeUseCase, new ResultScreen());
        EnvironmentScreen environmentScreen = new EnvironmentScreen(checkEnvironmentUseCase);

        BasicWindow window = new BasicWindow("FixKitty — " + profile.distro() + " / " + profile.desktop());
        window.setHints(List.of(Window.Hint.FULL_SCREEN));

        Panel panel = new Panel();
        panel.setLayoutManager(new LinearLayout(Direction.VERTICAL));

        panel.addComponent(new Label("  Graphical: " + (profile.hasGraphicalSession() ? "Yes" : "No")
            + "  |  Audio: " + profile.audioStack()));
        panel.addComponent(new EmptySpace());

        for (RecoveryAction action : actions) {
            Button btn = new Button(action.displayName(), () -> {
                if (action.isExecutable()) {
                    executionScreen.run(gui, action);
                } else {
                    environmentScreen.show(gui);
                }
            });
            panel.addComponent(btn);
        }

        panel.addComponent(new EmptySpace());
        panel.addComponent(new Button("Exit", window::close));

        window.setComponent(panel);
        gui.addWindowAndWait(window);
    }
}
