# US-021 — GUI GNOME Shell Confirmation Dialog

**Epic:** GUI
**Phase:** 1
**Status:** Pending

## Story

**As a** user on the desktop,
**I want** to see a warning dialog before Fix GNOME Shell executes,
**so that** I am not surprised by the graphical interface briefly closing.

## Dependencies

- **US-018** — GUI Main Window — the dialog is triggered from the Fix GNOME Shell button in `MainController`

## Acceptance Criteria

- Clicking Fix GNOME Shell shows a confirmation dialog before any execution begins
- The dialog displays the correct title, header, and content text
- Clicking Confirm proceeds to execution; clicking Cancel cancels without any side effects
- The dialog is modal (blocks interaction with the main window while open)
- Cancel leaves the app state unchanged: no log entries added, no buttons disabled

## Implementation Notes

### Classes to Create

- `br.com.josenaldo.fixkitty.interfaces.gui.GnomeShellConfirmationDialog` — a utility class that creates and shows the dialog

### Key Behavior

#### GnomeShellConfirmationDialog.showAndWait()

Returns `true` if the user clicked Confirm, `false` if the user clicked Cancel or closed the dialog.

```java
public boolean showAndWait() {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Fix GNOME Shell");
    alert.setHeaderText("This action will restart the GNOME shell.");
    alert.setContentText(
        "The graphical interface will briefly close and FixKitty will relaunch " +
        "automatically. Continue?"
    );

    ButtonType confirmButton = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
    ButtonType cancelButton  = new ButtonType("Cancel",  ButtonBar.ButtonData.CANCEL_CLOSE);
    alert.getButtonTypes().setAll(confirmButton, cancelButton);

    Optional<ButtonType> result = alert.showAndWait();
    return result.isPresent() && result.get() == confirmButton;
}
```

#### Integration in MainController

The Fix GNOME Shell button handler must:

1. Instantiate `GnomeShellConfirmationDialog`
2. Call `dialog.showAndWait()`
3. If `true` returned: proceed with normal execution flow (disable buttons, run task)
4. After execution completes: call `AppRestarter.relaunch()` instead of showing `ResultPanel` (see US-022)
5. If `false` returned: do nothing — no log entries, no button state changes

### Edge Cases

- The dialog is modal by default when created with `Alert`; no additional modality configuration is needed
- If the user presses Escape or clicks the window close button on the dialog, it is treated as Cancel (the `showAndWait()` returns `Optional.empty()`, which the implementation maps to `false`)
- The dialog must not be shown if a different action is already running (buttons are disabled in that case, so the Fix GNOME Shell button cannot be clicked)

## Related

- **ADR-007** — GNOME Shell restart strategy; confirmation dialog is required before the action executes
- **TC-GUI-006** — GUI test: clicking Fix GNOME Shell shows the confirmation dialog
- **TC-GUI-007** — GUI test: clicking Cancel on the dialog does not start execution
