package br.com.josenaldo.fixkitty.interfaces.gui;

import br.com.josenaldo.fixkitty.application.CheckEnvironmentUseCase;
import br.com.josenaldo.fixkitty.core.domain.EnvironmentProfile;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JavaFX panel that displays the detected Linux environment profile.
 *
 * <p>Populated on startup and refreshed when the user triggers Check Environment.
 * Contains no business logic — delegates detection to {@link CheckEnvironmentUseCase}.
 */
public class EnvironmentPanel extends VBox {

    private static final Logger log = LoggerFactory.getLogger(EnvironmentPanel.class);

    private final CheckEnvironmentUseCase checkEnvironmentUseCase;
    private final Label distroLabel = new Label();
    private final Label desktopLabel = new Label();
    private final Label audioLabel = new Label();
    private final Label sessionLabel = new Label();

    /**
     * Creates the panel with the required use case.
     *
     * @param checkEnvironmentUseCase the use case for detecting the environment
     */
    public EnvironmentPanel(CheckEnvironmentUseCase checkEnvironmentUseCase) {
        this.checkEnvironmentUseCase = checkEnvironmentUseCase;
        setPadding(new Insets(8));
        setSpacing(4);
        getChildren().addAll(
            new Label("Environment"),
            distroLabel, desktopLabel, audioLabel, sessionLabel);
        try {
            refresh();
        } catch (Exception e) {
            log.warn("Environment detection failed on startup: {}", e.getMessage());
            distroLabel.setText("Distro:    (detection failed)");
            desktopLabel.setText("Desktop:   (detection failed)");
            audioLabel.setText("Audio:     (detection failed)");
            sessionLabel.setText("Session:   (detection failed)");
        }
    }

    /**
     * Calls {@link CheckEnvironmentUseCase} and updates all displayed fields.
     *
     * <p>Must be called on the JavaFX Application Thread.
     */
    public void refresh() {
        EnvironmentProfile profile = checkEnvironmentUseCase.execute();
        distroLabel.setText("Distro:    " + profile.distro());
        desktopLabel.setText("Desktop:   " + profile.desktop());
        audioLabel.setText("Audio:     " + profile.audioStack());
        sessionLabel.setText("Session:   " + (profile.hasGraphicalSession() ? "Yes" : "No"));
    }
}
