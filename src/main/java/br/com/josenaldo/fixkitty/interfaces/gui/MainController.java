package br.com.josenaldo.fixkitty.interfaces.gui;

import br.com.josenaldo.fixkitty.application.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Main JavaFX controller for FixKitty.
 * Full implementation in Task 19.
 */
public class MainController {

    public MainController(ExecuteRecoveryUseCase executeUseCase,
                          ListActionsUseCase listActionsUseCase,
                          CheckEnvironmentUseCase checkEnvironmentUseCase,
                          Stage stage) {
    }

    public BorderPane buildLayout() {
        return new BorderPane();
    }
}
