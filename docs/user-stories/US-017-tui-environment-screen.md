# US-017 — TUI Environment Screen

**Epic:** TUI
**Phase:** 1
**Status:** Pending

## Story

**As a** user in a TTY terminal,
**I want** to run Check Environment and see the detected system profile,
**so that** I confirm the app has correctly identified my system.

## Dependencies

- **US-014** — TUI Main Menu — `EnvironmentScreen` is launched when the user selects `CHECK_ENVIRONMENT` from the main menu
- **US-008** — Check Environment Use Case — provides the environment profile data

## Acceptance Criteria

- The screen is launched when the user selects CHECK_ENVIRONMENT from the main menu
- `checkEnvironmentUseCase.execute()` is called to obtain a fresh profile on each visit
- All four profile fields are displayed: Distro, Desktop, Audio Stack, and Graphical Session
- Graphical Session is shown as `"Yes"` or `"No"`
- The footer shows `"[B] Back to menu"` and pressing B returns to the main menu

## Implementation Notes

### Classes to Create

- `br.com.josenaldo.fixkitty.interfaces.tui.EnvironmentScreen`

### Constructor Parameters

`EnvironmentScreen` receives the Lanterna `Screen` reference and the `CheckEnvironmentUseCase` via constructor or direct passing from `MainMenuScreen`.

### Key Behavior

`show()` must:

1. Call `checkEnvironmentUseCase.execute()` → `profile`
2. Clear the screen
3. Render the title: `"Environment Profile"`
4. Render a blank separator
5. Render each profile field on its own line:
   - `"  Distro:            " + profile.distro()`
   - `"  Desktop:           " + profile.desktop()`
   - `"  Audio Stack:       " + profile.audioStack()`
   - `"  Graphical Session: " + (profile.hasGraphicalSession() ? "Yes" : "No")`
6. Render a blank separator
7. Render the footer: `"[B] Back to menu"`
8. Poll `screen.readInput()` until `KeyType.Character` with character `'b'` or `'B'` is received; then return to `MainMenuScreen`

Note: because `CHECK_ENVIRONMENT` is selected from the main menu like any other action, `MainMenuScreen` must handle it as a special case: instead of launching `ExecutionScreen`, it launches `EnvironmentScreen` directly.

### Edge Cases

- If `checkEnvironmentUseCase.execute()` throws an unchecked exception: display an error line `"Error detecting environment: " + exception.getMessage()` and still show the footer so the user can navigate back
- The `[B]` key is case-insensitive
- Field labels are padded to align the values in a column; use fixed-width formatting as shown above

## Related

- **ADR-001** — Clean Architecture; no detection logic in the TUI screen class
- **ADR-005** — Environment detection design; `EnvironmentProfile` fields defined there
- **TC-TUI-010** — TUI test: environment screen displays all four profile fields correctly
