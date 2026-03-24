package br.com.josenaldo.fixkitty.interfaces.gui;

import atlantafx.base.theme.Dracula;
import br.com.josenaldo.fixkitty.application.*;
import com.google.inject.Injector;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX application entry point for FixKitty.
 *
 * <p>Applies the AtlantaFX Dracula theme and constructs the main window
 * from injected use cases. The Guice injector is passed via a static field
 * set by {@code Main} before {@code launch()} is called.
 */
public class GuiApp extends Application {

    /** Set by Main before Application.launch() is called. */
    public static Injector injector;

    /**
     * Starts the JavaFX application, applies the Dracula theme, and shows the main window.
     *
     * @param stage the primary JavaFX stage
     */
    @Override
    public void start(Stage stage) {
        Application.setUserAgentStylesheet(new Dracula().getUserAgentStylesheet());

        ExecuteRecoveryUseCase executeUseCase = injector.getInstance(ExecuteRecoveryUseCase.class);
        ListActionsUseCase listActionsUseCase = injector.getInstance(ListActionsUseCase.class);
        CheckEnvironmentUseCase checkEnvironmentUseCase = injector.getInstance(CheckEnvironmentUseCase.class);

        MainController controller = new MainController(
            executeUseCase, listActionsUseCase, checkEnvironmentUseCase, stage);

        Scene scene = new Scene(controller.buildLayout(), 800, 600);
        stage.setTitle("FixKitty");
        stage.setScene(scene);
        stage.show();
    }
}
