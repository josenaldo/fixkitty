# US-023 — GUI Environment Panel

**Epic:** GUI
**Phase:** 1
**Status:** Pending

## Story

**As a** user on the desktop,
**I want** to see the detected system profile in the interface,
**so that** I confirm the app has correctly identified my system.

## Dependencies

- **US-018** — GUI Main Window — `EnvironmentPanel` is embedded in the main window layout
- **US-008** — Check Environment Use Case — provides the environment profile data

## Acceptance Criteria

- The panel is populated on application startup by calling `checkEnvironmentUseCase.execute()`
- The panel is refreshed when the user clicks the Check Environment button
- All four profile fields are displayed: Distro, Desktop, Audio Stack, and Graphical Session
- Graphical Session is displayed as `"Yes"` or `"No"`
- If detection fails, `"Unknown"` is shown for all fields and a warning is appended to `LogPanel`
- Panel updates run on the JavaFX Application Thread

## Implementation Notes

### Classes to Create

- `br.com.josenaldo.fixkitty.interfaces.gui.EnvironmentPanel` — a custom JavaFX component (extends `VBox`)

### Key Behavior

#### Component Structure

```
EnvironmentPanel (VBox)
├── titleLabel             (Label) — "Environment"
├── distroLabel            (Label) — "Distro: Ubuntu 24.04 LTS"
├── desktopLabel           (Label) — "Desktop: GNOME"
├── audioStackLabel        (Label) — "Audio: PipeWire"
└── graphicalSessionLabel  (Label) — "Graphical: Yes"
```

Use a monospaced or compact font for the field labels to give a terminal-like appearance consistent with a technical tool.

#### EnvironmentPanel.populate(EnvironmentProfile profile)

Updates the label text:

```java
public void populate(EnvironmentProfile profile) {
    distroLabel.setText("Distro:   " + profile.distro());
    desktopLabel.setText("Desktop:  " + profile.desktop());
    audioStackLabel.setText("Audio:    " + profile.audioStack());
    graphicalSessionLabel.setText("Graphical: " + (profile.hasGraphicalSession() ? "Yes" : "No"));
}
```

Must be called on the JavaFX Application Thread. If called from a background thread, wrap with `Platform.runLater()`.

#### EnvironmentPanel.showUnknown()

Called when detection fails:

```java
public void showUnknown() {
    distroLabel.setText("Distro:   Unknown");
    desktopLabel.setText("Desktop:  Unknown");
    audioStackLabel.setText("Audio:    Unknown");
    graphicalSessionLabel.setText("Graphical: Unknown");
}
```

#### Integration with Main Window

On startup, after the primary stage is shown:

```java
Task<EnvironmentProfile> envTask = new Task<>() {
    @Override
    protected EnvironmentProfile call() {
        return checkEnvironmentUseCase.execute();
    }
};
envTask.setOnSucceeded(e -> environmentPanel.populate(envTask.getValue()));
envTask.setOnFailed(e -> {
    log.error("Environment detection failed", envTask.getException());
    environmentPanel.showUnknown();
    logPanel.appendMessage("Warning: environment detection failed. Showing Unknown.");
});
new Thread(envTask).start();
```

When the user clicks the Check Environment button, the same task pattern is used to refresh the panel. The execution flow (ExecuteRecoveryUseCase for CHECK_ENVIRONMENT) is not used here; the panel refresh calls the use case directly.

### Edge Cases

- Detection runs on a background thread; all label updates must go through `Platform.runLater()` or be placed in `Task.setOnSucceeded()` which already runs on the Application Thread
- If `checkEnvironmentUseCase.execute()` throws an exception: call `environmentPanel.showUnknown()` and log the error; do not propagate the exception to the user as a crash
- The Check Environment button in the main window triggers both an environment panel refresh (via `EnvironmentPanel`) and a log panel entry (via `LogPanel`); it does not run the full `ExecuteRecoveryUseCase` execution flow

## Related

- **ADR-001** — Clean Architecture; no detection logic in the GUI component
- **ADR-005** — Environment detection design; `EnvironmentProfile` fields defined there
- **TC-GUI-010** — GUI test: environment panel populated on startup
- **TC-GUI-011** — GUI test: environment panel refreshed on Check Environment button click
