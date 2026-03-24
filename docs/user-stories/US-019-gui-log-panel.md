# US-019 — GUI Log Panel

**Epic:** GUI
**Phase:** 1
**Status:** Pending

## Story

**As a** user on the desktop,
**I want** to see execution progress in a scrollable log area as steps complete,
**so that** I can follow what is happening in real time.

## Dependencies

- **US-018** — GUI Main Window — `LogPanel` is embedded in the main window layout
- **US-006** — Execute Recovery Use Case — execution results drive the log entries

## Acceptance Criteria

- The log area is scrollable and read-only
- The log auto-scrolls to the latest entry on each update
- Each step completion appends a timestamped status line
- FAILED steps show stderr output indented below their status line
- Large stderr output is truncated to 10 lines with a truncation note
- The log is cleared when a new action starts
- All log updates are performed on the JavaFX Application Thread

## Implementation Notes

### Classes to Create

- `br.com.josenaldo.fixkitty.interfaces.gui.LogPanel` — a custom JavaFX component (extends `VBox` or wraps a `TextArea`)

### Key Behavior

#### Component Structure

Use a `TextArea` configured as read-only with a monospaced font:

```java
TextArea logArea = new TextArea();
logArea.setEditable(false);
logArea.setWrapText(false);
logArea.setFont(Font.font("Monospace", 12));
```

Wrap it in a `VBox` or `ScrollPane` and make it fill the available vertical space.

#### LogPanel.clear()

Clears the `TextArea` content. Must be called on the JavaFX Application Thread. If called from a background thread, wrap with `Platform.runLater()`.

#### LogPanel.appendStepResult(StepResult stepResult)

Appends a log line for the step. The format is:

- SUCCESS: `[HH:mm:ss] [OK]      Restart PipeWire`
- FAILED: `[HH:mm:ss] [FAIL]    Restart Bluetooth (exit 1)`
- TIMEOUT: `[HH:mm:ss] [TIMEOUT] Restart PipeWire`
- SKIPPED: `[HH:mm:ss] [SKIP]    Restart PipeWire (skipped)`

After the status line, if the step is FAILED or TIMEOUT and `stepResult.stderr()` is non-blank:

- Append up to 10 lines of stderr, each indented with `"           "` (11 spaces)
- If the stderr has more than 10 lines: append `"           ... (output truncated)"`

After each append, call `logArea.setScrollTop(Double.MAX_VALUE)` to scroll to the bottom.

All calls to `appendStepResult` must be dispatched on the JavaFX Application Thread via `Platform.runLater()` if called from a background thread.

#### LogPanel.appendMessage(String message)

Appends a plain informational line with a timestamp prefix: `[HH:mm:ss] [INFO]    ` + message. Used for action start notifications.

#### Timestamp Format

Use `java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))`.

#### Integration with Main Window

In `MainController`, before starting execution:

1. Call `logPanel.clear()`
2. Call `logPanel.appendMessage("Starting: " + action.displayName())`

After the `Task` completes, call `logPanel.appendStepResult(stepResult)` for each result in `executionResult.steps()`.

Since the use case is synchronous and runs in a `Task`, results are only available after the task finishes. For Phase 1, appending all step results at once after task completion is acceptable. For Phase 2, a progress callback approach could stream results as they are produced.

### Edge Cases

- `Platform.runLater()` must be used for any log update triggered from the `Task` body (background thread); updates in `Task.setOnSucceeded()` and `Task.setOnFailed()` are already on the Application Thread and do not need it
- Stderr truncation at 10 lines is based on line count after splitting on the system line separator; do not truncate mid-line
- Empty stdout or stderr fields must not produce blank indented lines in the log

## Related

- **ADR-006** — `StepResult` structure; log formatting is derived from its fields
- **TC-GUI-002** — GUI test: log shows SUCCESS line after successful step
- **TC-GUI-003** — GUI test: log shows FAILED line and stderr after failed step
- **TC-GUI-008** — GUI test: log shows all 5 steps for FIX_ALL
- **TC-GUI-009** — GUI test: log shows truncated stderr for long output
