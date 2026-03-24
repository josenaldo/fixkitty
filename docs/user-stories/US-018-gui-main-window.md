# US-018 — GUI Main Window

**Epic:** GUI
**Phase:** 1
**Status:** Pending

## Story

**As a** user on the desktop,
**I want** to see a window with clearly labelled buttons for each recovery action, with icons and a dark theme,
**so that** the interface is clear and visually appropriate for a technical tool.

## Dependencies

- **US-003** — Bootstrap — `GuiApp` must be launchable from the bootstrap entry point
- **US-006** — Execute Recovery Use Case — button clicks trigger execution
- **US-007** — List Actions Use Case — action list drives which buttons are shown
- **US-008** — Check Environment Use Case — environment panel populated on startup
- **US-009** — ProcessBuilder Command Runner — execution infrastructure must exist
- **US-011** — Pkexec Privilege Manager — privileged steps use pkexec in GUI
- **US-012** — Ubuntu 24 Environment Detector — environment data source
- **US-013** — Ubuntu 24 Action Catalog — action definitions and execution plans

## Acceptance Criteria

- The application window opens with the AtlantaFX dark theme applied
- Six action buttons are displayed, each with an Ikonli icon and a text label
- On startup, `checkEnvironmentUseCase.execute()` is called and the result populates `EnvironmentPanel`
- Buttons are disabled while an action is running
- Buttons are re-enabled when the action completes
- No business logic exists in `MainController`

## Implementation Notes

### Classes to Create

- `br.com.josenaldo.fixkitty.interfaces.gui.GuiApp` extends `javafx.application.Application`
- `br.com.josenaldo.fixkitty.interfaces.gui.MainController` — FXML or programmatic controller

### Constructor Injection into MainController

```java
@Inject
public MainController(
    ExecuteRecoveryUseCase executeRecoveryUseCase,
    ListActionsUseCase listActionsUseCase,
    CheckEnvironmentUseCase checkEnvironmentUseCase
) { ... }
```

### Key Behavior

#### GuiApp.start(Stage primaryStage)

1. Apply the AtlantaFX Dracula theme:
   ```java
   Application.setUserAgentStylesheet(new Dracula().getUserAgentStylesheet());
   ```
2. Obtain `MainController` from the Guice injector (passed via a static reference or `Application.launch` parameters)
3. Build the scene and set it on the primary stage
4. Set the window title: `"FixKitty Recovery Console"`
5. Show the stage

#### MainController — Button Layout

Six buttons arranged vertically or in a grid, one per action:

| Action | Icon (Ikonli FontAwesome 5) | Label |
| --- | --- | --- |
| FIX_AUDIO | `FontAwesomeSolid.VOLUME_UP` | Fix Audio |
| FIX_BLUETOOTH | `FontAwesomeSolid.BLUETOOTH_B` | Fix Bluetooth |
| FIX_NETWORK | `FontAwesomeSolid.WIFI` | Fix Network |
| FIX_GNOME_SHELL | `FontAwesomeSolid.DESKTOP` | Fix GNOME Shell |
| FIX_ALL | `FontAwesomeSolid.TOOLS` | Fix All |
| CHECK_ENVIRONMENT | `FontAwesomeSolid.INFO_CIRCLE` | Check Environment |

Each button is created as a `javafx.scene.control.Button` with an `FontIcon` set as its graphic:

```java
FontIcon icon = new FontIcon(FontAwesomeSolid.VOLUME_UP);
icon.setIconSize(20);
Button button = new Button("Fix Audio", icon);
```

#### Button Click Handler

On any action button click (except Fix GNOME Shell, which has a confirmation dialog in US-021):

1. Disable all 6 buttons
2. Clear `LogPanel` and `ResultPanel`
3. Run `executeRecoveryUseCase.execute(action)` on a JavaFX background thread (use `javafx.concurrent.Task`)
4. On task success: pass `ExecutionResult` to `LogPanel` and `ResultPanel`; re-enable all buttons
5. On task failure (unexpected exception): log the exception at ERROR level; re-enable all buttons; show an error `Alert`

#### On Startup

After the stage is shown, call `checkEnvironmentUseCase.execute()` on a background thread and populate `EnvironmentPanel` with the result.

### Edge Cases

- All buttons must be disabled with `button.setDisable(true)` before execution starts and re-enabled in the `Task.setOnSucceeded` and `Task.setOnFailed` callbacks
- JavaFX `Task` callbacks (`setOnSucceeded`, `setOnFailed`) run on the JavaFX Application Thread, so UI updates inside them do not need `Platform.runLater()`
- If `GuiApp` cannot obtain the Guice injector reference (e.g., due to static reference not being set), throw an `IllegalStateException` with a descriptive message rather than a NullPointerException

## Related

- **ADR-001** — Clean Architecture; no business logic in controller
- **ADR-009** — JavaFX + AtlantaFX selection; Ikonli for icons
- **TC-GUI-001** — GUI test: main window renders with all 6 buttons
- **TC-GUI-012** — GUI test: buttons disabled during execution
