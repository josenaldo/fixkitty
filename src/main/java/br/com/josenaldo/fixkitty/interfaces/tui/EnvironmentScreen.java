package br.com.josenaldo.fixkitty.interfaces.tui;

import br.com.josenaldo.fixkitty.application.CheckEnvironmentUseCase;
import br.com.josenaldo.fixkitty.core.domain.EnvironmentProfile;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;

/**
 * TUI screen that displays the detected environment profile.
 *
 * <p>Calls {@link CheckEnvironmentUseCase} on display and renders each
 * profile field as a labelled row.
 *
 * <p>Lanterna classes are confined to this package and must not be imported
 * by any other layer.
 */
class EnvironmentScreen {

    private final CheckEnvironmentUseCase checkEnvironmentUseCase;

    /**
     * Creates the screen with the required use case.
     *
     * @param checkEnvironmentUseCase the use case used to detect the environment
     */
    EnvironmentScreen(CheckEnvironmentUseCase checkEnvironmentUseCase) {
        this.checkEnvironmentUseCase = checkEnvironmentUseCase;
    }

    /**
     * Displays the environment profile in a Lanterna dialog.
     *
     * @param gui the active window-based text GUI
     */
    void show(WindowBasedTextGUI gui) {
        EnvironmentProfile profile = checkEnvironmentUseCase.execute();
        String content = String.format(
            "Distro:            %s%nDesktop:           %s%nAudio Stack:       %s%nGraphical Session: %s",
            profile.distro(), profile.desktop(), profile.audioStack(),
            profile.hasGraphicalSession() ? "Yes" : "No");

        new MessageDialogBuilder()
            .setTitle("Environment Profile")
            .setText(content)
            .addButton(MessageDialogButton.OK)
            .build()
            .showDialog(gui);
    }
}
