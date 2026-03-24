# US-020 — GUI Result Display

**Epic:** GUI
**Phase:** 1
**Status:** Pending

## Story

**As a** user on the desktop,
**I want** to see the overall result and recommendation after an action completes,
**so that** I know whether the issue was resolved.

## Dependencies

- **US-019** — GUI Log Panel — `ResultPanel` appears in the same main window as `LogPanel`
- **US-006** — Execute Recovery Use Case — `ExecutionResult` drives the result display

## Acceptance Criteria

- The overall status is displayed as a prominent label with a color corresponding to the result
- SUCCESS is displayed in green, PARTIAL in yellow/orange, FAILED in red
- The recommendation label is visible and non-empty when `recommendation` is non-null
- The recommendation label is hidden when `recommendation` is null
- The panel resets when a new action starts

## Implementation Notes

### Classes to Create

- `br.com.josenaldo.fixkitty.interfaces.gui.ResultPanel` — a custom JavaFX component (extends `VBox`)

### Key Behavior

#### Component Structure

```
ResultPanel (VBox)
├── statusLabel   (Label) — large font, colored text
└── recommendationLabel (Label) — smaller font, visible only when non-null
```

#### ResultPanel.showResult(ExecutionResult result)

1. Set `statusLabel.setText("Status: " + result.status().name())`
2. Apply CSS style classes for color:
   - `SUCCESS` → apply style class `"success-label"` (green text)
   - `PARTIAL` → apply style class `"partial-label"` (orange text)
   - `FAILED` → apply style class `"failed-label"` (red text)
3. If `result.recommendation()` is non-null:
   - Set `recommendationLabel.setText("Recommendation: " + result.recommendation())`
   - Set `recommendationLabel.setVisible(true)`
   - Set `recommendationLabel.setManaged(true)`
4. If `result.recommendation()` is null:
   - Set `recommendationLabel.setVisible(false)`
   - Set `recommendationLabel.setManaged(false)` — prevents invisible label from taking up layout space

All calls to `showResult` must be made on the JavaFX Application Thread. They are typically called from `Task.setOnSucceeded()`, which already runs on the Application Thread.

#### ResultPanel.reset()

Called before a new action starts:

1. Set `statusLabel.setText("")`
2. Remove all CSS style classes from `statusLabel`
3. Set `recommendationLabel.setVisible(false)`
4. Set `recommendationLabel.setManaged(false)`

#### CSS Style Classes

Define the following in the application stylesheet (or inline):

```css
.success-label {
    -fx-text-fill: #4caf50;
    -fx-font-size: 16px;
    -fx-font-weight: bold;
}

.partial-label {
    -fx-text-fill: #ff9800;
    -fx-font-size: 16px;
    -fx-font-weight: bold;
}

.failed-label {
    -fx-text-fill: #f44336;
    -fx-font-size: 16px;
    -fx-font-weight: bold;
}
```

#### Integration with Main Window

In `MainController.handleActionButton(RecoveryAction action)`:

1. Call `resultPanel.reset()` before starting the task
2. In `Task.setOnSucceeded()`: call `resultPanel.showResult(executionResult)`

### Edge Cases

- `setManaged(false)` is required alongside `setVisible(false)` to prevent the hidden recommendation label from reserving vertical space in the layout; omitting `setManaged` causes the panel to appear taller than expected when the recommendation is hidden
- CSS style classes must be cleared before applying a new one; use `statusLabel.getStyleClass().removeAll("success-label", "partial-label", "failed-label")` before adding the new class
- The result panel must be updated only on the JavaFX Application Thread

## Related

- **ADR-006** — `ExecutionResult` and `ResultStatus` semantics
- **TC-GUI-002** — GUI test: result panel shows SUCCESS in green
- **TC-GUI-003** — GUI test: result panel shows FAILED in red with recommendation
- **TC-GUI-008** — GUI test: result panel shows PARTIAL for FIX_ALL with some failures
