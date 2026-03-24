# US-016 — TUI Result Screen

**Epic:** TUI
**Phase:** 1
**Status:** Pending

## Story

**As a** user in a TTY terminal,
**I want** to see the full `ExecutionResult` after an action completes,
**so that** I understand what worked, what failed, and what to do next.

## Dependencies

- **US-015** — TUI Execution Progress Screen — `ResultScreen` is launched from `ExecutionScreen` after execution completes

## Acceptance Criteria

- Title shows the action display name
- Overall status is displayed with appropriate label
- Each step is listed with its status
- Skipped steps are labelled `"(skipped)"`
- Recommendation text is shown when non-null
- The footer instructs the user to press `[B]` to return to the menu
- If the step list is empty (e.g., CHECK_ENVIRONMENT), the environment profile summary is shown instead

## Implementation Notes

### Classes to Create

- `br.com.josenaldo.fixkitty.interfaces.tui.ResultScreen`

### Constructor Parameters

`ResultScreen` receives the Lanterna `Screen` reference. The `ExecutionResult` is passed to `show(ExecutionResult result)`.

### Key Behavior

`show(ExecutionResult result)` must:

1. Clear the screen
2. Render the title: `"Result: " + result.action().displayName()`
3. Render a blank separator
4. Render the overall status line:
   - `"Status: SUCCESS"` — if `ResultStatus.SUCCESS`
   - `"Status: PARTIAL"` — if `ResultStatus.PARTIAL`
   - `"Status: FAILED"` — if `ResultStatus.FAILED`
   - If the terminal supports ANSI colors: SUCCESS in green, PARTIAL in yellow, FAILED in red. Use Lanterna's `TextColor.ANSI` for coloring if available; fall back to plain text on terminals that do not support color.
5. Render a blank separator
6. If `result.steps()` is non-empty: render each step result:
   - `SUCCESS`: `"  [OK]      " + step.step().description()`
   - `FAILED`: `"  [FAIL]    " + step.step().description()`
   - `TIMEOUT`: `"  [TIMEOUT] " + step.step().description()`
   - `SKIPPED`: `"  [SKIP]    " + step.step().description() + " (skipped)"`
7. If `result.steps()` is empty (CHECK_ENVIRONMENT case): display a profile summary section. Obtain the profile from a separate `CheckEnvironmentUseCase` call or from a stored reference passed by the caller. Display:
   - `"  No steps executed."`
   - `"  This was an environment check action."`
8. If `result.recommendation()` is non-null: render:
   - Blank line
   - `"Recommendation:"`
   - `"  " + result.recommendation()`
9. Render the footer: `"[B] Back to menu"`
10. Poll `screen.readInput()` until `KeyType.Character` with character `'b'` or `'B'` is received; then return to `MainMenuScreen`

### Edge Cases

- SKIPPED steps must include the `"(skipped)"` label so users understand they were not attempted
- If the terminal does not support ANSI color, the status line must still render correctly in plain text
- The footer key `[B]` is case-insensitive: both `'b'` and `'B'` must trigger the return to menu

## Related

- **ADR-006** — `ExecutionResult` and `ResultStatus` semantics
- **TC-TUI-003** — TUI test: result screen renders after successful execution
- **TC-TUI-004** — TUI test: result screen renders after partial execution
- **TC-TUI-011** — TUI test: result screen shows recommendation
- **TC-TUI-012** — TUI test: result screen for CHECK_ENVIRONMENT shows profile summary
