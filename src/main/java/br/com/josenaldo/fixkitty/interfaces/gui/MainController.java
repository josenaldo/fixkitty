package br.com.josenaldo.fixkitty.interfaces.gui;

import br.com.josenaldo.fixkitty.application.CheckEnvironmentUseCase;
import br.com.josenaldo.fixkitty.application.ExecuteRecoveryUseCase;
import br.com.josenaldo.fixkitty.application.ListActionsUseCase;
import br.com.josenaldo.fixkitty.core.domain.ExecutionResult;
import br.com.josenaldo.fixkitty.core.domain.RecoveryAction;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.kordamp.ikonli.fontawesome5.FontAwesomeBrands;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Main JavaFX controller for FixKitty.
 *
 * <p>Builds the application layout and wires action buttons to use cases.
 * Contains no business logic — all execution is delegated to use cases.
 * Buttons are disabled during execution to prevent concurrent actions.
 */
public class MainController {

    private final ExecuteRecoveryUseCase executeUseCase;
    private final ListActionsUseCase listActionsUseCase;
    private final CheckEnvironmentUseCase checkEnvironmentUseCase;
    private final Stage stage;

    private final EnvironmentPanel environmentPanel;
    private final LogPanel logPanel;
    private final ResultPanel resultPanel;
    private final GnomeShellConfirmationDialog gnomeConfirmDialog;
    private final AppRestarter appRestarter;

    private List<Button> actionButtons;

    /**
     * Creates the main controller with all required dependencies.
     *
     * @param executeUseCase          executes recovery actions
     * @param listActionsUseCase      lists available actions
     * @param checkEnvironmentUseCase detects the current environment
     * @param stage                   the primary JavaFX stage
     */
    public MainController(ExecuteRecoveryUseCase executeUseCase,
                          ListActionsUseCase listActionsUseCase,
                          CheckEnvironmentUseCase checkEnvironmentUseCase,
                          Stage stage) {
        this.executeUseCase = executeUseCase;
        this.listActionsUseCase = listActionsUseCase;
        this.checkEnvironmentUseCase = checkEnvironmentUseCase;
        this.stage = stage;
        this.environmentPanel = new EnvironmentPanel(checkEnvironmentUseCase);
        this.logPanel = new LogPanel();
        this.resultPanel = new ResultPanel();
        this.gnomeConfirmDialog = new GnomeShellConfirmationDialog();
        this.appRestarter = new AppRestarter();
    }

    /**
     * Builds and returns the root layout node.
     *
     * @return the root {@link BorderPane} for the main scene
     */
    public BorderPane buildLayout() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(12));

        // Left sidebar: environment + action buttons
        VBox sidebar = new VBox(8);
        sidebar.setPadding(new Insets(0, 12, 0, 0));
        sidebar.setPrefWidth(200);
        sidebar.getChildren().add(environmentPanel);
        sidebar.getChildren().add(new Separator(Orientation.HORIZONTAL));

        List<RecoveryAction> actions = listActionsUseCase.execute();
        actionButtons = actions.stream().map(this::createButton).toList();
        sidebar.getChildren().addAll(actionButtons);

        // Center: log + result
        VBox center = new VBox(8);
        center.getChildren().addAll(logPanel, resultPanel);

        root.setLeft(sidebar);
        root.setCenter(center);
        return root;
    }

    private Button createButton(RecoveryAction action) {
        FontIcon icon = switch (action) {
            case FIX_AUDIO -> FontIcon.of(FontAwesomeSolid.VOLUME_UP);
            case FIX_BLUETOOTH -> FontIcon.of(FontAwesomeBrands.BLUETOOTH_B);
            case FIX_NETWORK -> FontIcon.of(FontAwesomeSolid.WIFI);
            case FIX_GNOME_SHELL -> FontIcon.of(FontAwesomeSolid.DESKTOP);
            case FIX_ALL -> FontIcon.of(FontAwesomeSolid.TOOLS);
            case CHECK_ENVIRONMENT -> FontIcon.of(FontAwesomeSolid.INFO_CIRCLE);
        };

        Button btn = new Button(action.displayName(), icon);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> handleAction(action));
        return btn;
    }

    private void handleAction(RecoveryAction action) {
        if (!action.isExecutable()) {
            environmentPanel.refresh();
            return;
        }
        if (action == RecoveryAction.FIX_GNOME_SHELL) {
            if (!gnomeConfirmDialog.confirm()) return;
            runAction(action, true);
            return;
        }
        runAction(action, false);
    }

    private void runAction(RecoveryAction action, boolean relaunchAfter) {
        setButtonsDisabled(true);
        logPanel.clear();
        resultPanel.clear();

        Executors.newVirtualThreadPerTaskExecutor().execute(() -> {
            ExecutionResult result = executeUseCase.execute(action);
            Platform.runLater(() -> {
                result.steps().forEach(logPanel::appendStep);
                if (relaunchAfter) {
                    appRestarter.relaunch();
                } else {
                    resultPanel.display(result);
                    setButtonsDisabled(false);
                }
            });
        });
    }

    private void setButtonsDisabled(boolean disabled) {
        if (actionButtons != null) {
            actionButtons.forEach(b -> b.setDisable(disabled));
        }
    }
}
