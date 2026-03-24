# US-015 — TUI Execution Progress Screen

**Epic:** TUI
**Phase:** 1
**Status:** Pending

## Story

**As a** user in a TTY terminal,
**I want** to see each step's progress as it executes,
**so that** I know what is happening in real time.

## Dependencies

- **US-014** — TUI Main Menu — `ExecutionScreen` is launched from `MainMenuScreen`
- **US-006** — Execute Recovery Use Case — the use case that drives execution

## Acceptance Criteria

- The current action name is displayed as the screen title
- Each step shows a "Running..." status line before it executes
- Each step's line is updated to show `[SUCCESS]`, `[FAILED]`, `[SKIPPED]`, or `[TIMEOUT]` after completion
- For FAILED or TIMEOUT steps, stderr output is shown indented below the step line
- After all steps complete, the screen transitions to `ResultScreen`

## Implementation Notes

### Classes to Create

- `br.com.josenaldo.fixkitty.interfaces.tui.ExecutionScreen`

### Constructor Parameters

`ExecutionScreen` receives the Lanterna `Screen` reference and the `ExecuteRecoveryUseCase` via constructor injection or direct passing from `MainMenuScreen`.

### Key Behavior

`show(RecoveryAction action)` must:

1. Clear the screen
2. Render the title: `"Executing: " + action.displayName()`
3. Render a blank separator line
4. Run the use case: because `ExecuteRecoveryUseCase.execute()` is synchronous, execute it in a background thread (using a virtual thread: `Thread.ofVirtual().start(...)`)
5. Display a "Running..." placeholder for the current step before the thread starts each step:
   - Since the use case is opaque (no callback API in Phase 1), the simplest approach is to render all step descriptions as "Pending..." at the start, then re-render after the use case returns
   - An enhanced approach (Phase 2) would use a progress callback interface; for Phase 1, blocking is acceptable
6. After the background thread completes:
   - Re-render all steps with their final status
   - For each `StepResult`:
     - `SUCCESS`: `"  [OK]      " + step.description()`
     - `FAILED`: `"  [FAIL]    " + step.description() + " (exit " + exitCode + ")"`
     - `TIMEOUT`: `"  [TIMEOUT] " + step.description()`
     - `SKIPPED`: `"  [SKIP]    " + step.description()`
   - For `FAILED` or `TIMEOUT` steps where `stderr` is non-blank: render up to 5 lines of stderr, each indented with `"           "` (11 spaces to align under the status tag)
7. Display a footer: `"Press any key to continue..."`
8. Call `screen.readInput()` to wait for a keypress
9. Transition to `ResultScreen.show(executionResult)`

### Implementation Note on Synchronous Execution

`ExecuteRecoveryUseCase` runs synchronously in Phase 1. Running it in a background thread prevents the Lanterna event loop from blocking. After the thread finishes, the main thread re-renders the screen. A simple `Thread.join()` on the background thread before re-rendering is acceptable.

For a better user experience in Phase 2, introduce a `ProgressCallback` interface that `ExecutionScreen` implements and that the use case calls after each step. This is out of scope for this story.

### Edge Cases

- If `execute()` returns a result with an empty steps list (e.g., for `CHECK_ENVIRONMENT`): skip the step list rendering and go directly to `ResultScreen`
- If the background thread is interrupted: log the interruption at WARN level and render an error line on screen before transitioning to `ResultScreen` with a FAILED status
- Stderr lines longer than the terminal width must be wrapped or truncated to avoid Lanterna rendering artifacts

## Related

- **ADR-001** — No business logic in interface layer
- **ADR-006** — Structured result; `StepResult` status values drive the rendering labels
- **TC-TUI-003** — TUI test: step lines updated after execution
- **TC-TUI-004** — TUI test: FAILED step shows stderr
- **TC-TUI-008** — TUI test: FIX_ALL shows all 5 steps
- **TC-TUI-009** — TUI test: recommendation shown on result screen
