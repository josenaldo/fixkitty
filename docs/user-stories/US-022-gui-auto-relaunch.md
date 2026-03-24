# US-022 — GUI Auto-Relaunch After GNOME Shell Restart

**Epic:** GUI
**Phase:** 1
**Status:** Pending

## Story

**As a** user on the desktop,
**I want** FixKitty to relaunch automatically after GNOME shell restarts,
**so that** I do not need to manually reopen the application.

## Dependencies

- **US-021** — GUI GNOME Shell Confirmation Dialog — `AppRestarter.relaunch()` is called only after the user confirms and Fix GNOME Shell execution completes

## Acceptance Criteria

- After Fix GNOME Shell completes, the result display is skipped and `AppRestarter.relaunch()` is called instead
- FixKitty relaunches approximately 3 seconds after the GNOME shell restart, giving the shell time to settle
- JavaFX is shut down cleanly before the JVM exits
- If the relaunch command cannot be determined, a warning is logged and the app exits without relaunching

## Implementation Notes

### Classes to Create

- `br.com.josenaldo.fixkitty.interfaces.gui.AppRestarter`

### Key Behavior

#### AppRestarter.relaunch()

```java
public void relaunch() {
    Optional<String> javaCommand = ProcessHandle.current().info().command();

    if (javaCommand.isEmpty()) {
        log.warn("Cannot determine JVM command for relaunch. Exiting without relaunching.");
        Platform.exit();
        System.exit(0);
        return;
    }

    String command = javaCommand.get();
    // Build the relaunch command with a 3-second delay to give GNOME shell time to settle.
    String relaunchCmd = "sleep 3 && nohup " + command + " &";

    try {
        new ProcessBuilder("bash", "-c", relaunchCmd)
            .start();
    } catch (IOException e) {
        log.warn("Failed to start relaunch process: {}", e.getMessage());
    }

    Platform.exit();
    System.exit(0);
}
```

#### Integration with MainController

In the Fix GNOME Shell button handler (after the execution task completes successfully):

```java
task.setOnSucceeded(event -> {
    enableAllButtons();
    appRestarter.relaunch();
    // ResultPanel.showResult() is intentionally NOT called for this action.
});
```

### Implementation Notes

The exact relaunch command depends on how the application is packaged. The approach above retrieves the JVM executable path from `ProcessHandle`, which returns the path to the `java` binary. When running via Gradle (`./gradlew run`), the full JVM argument list (including the classpath and main class) is not reproduced by this approach. This is a known limitation for Phase 1.

Acceptable Phase 1 workarounds:

1. Store the full launch command as a system property set by the Gradle `run` task (e.g., `-Dfixkitty.launchCommand=...`) and read it in `AppRestarter`
2. Document the limitation in the code with a `// TODO Phase 2: improve relaunch for packaged distributions` comment
3. Fall back to logging a message that the user must reopen FixKitty manually if the launch command cannot be reconstructed

For a packaged distribution (Phase 2), the relaunch command would use the native launcher script (e.g., `fixkitty` shell wrapper), making this straightforward.

### Edge Cases

- `Platform.exit()` must be called before `System.exit(0)` to give JavaFX a chance to clean up its application thread; calling `System.exit(0)` directly may leave the JavaFX thread in an inconsistent state
- The `sleep 3` delay is a best-effort approach; if GNOME shell takes longer than 3 seconds to restart, the relaunched FixKitty may fail to acquire a display. Phase 2 should implement a loop that checks for display availability before relaunching.
- Relaunch is called only for Fix GNOME Shell; no other action triggers `AppRestarter`

## Related

- **ADR-007** — GNOME Shell restart and auto-relaunch strategy
- **TC-GUI-007** — GUI test: Fix GNOME Shell relaunch mechanism triggers after execution
