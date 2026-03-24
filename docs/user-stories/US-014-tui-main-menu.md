# US-014 — TUI Main Menu

**Epic:** TUI
**Phase:** 1
**Status:** Pending

## Story

**As a** user in a TTY terminal,
**I want** to see a navigable menu listing all available recovery actions,
**so that** I can select what to fix without a graphical interface.

## Dependencies

- **US-003** — Bootstrap — `TuiApp` must be launchable via `--tui` argument
- **US-006** — Execute Recovery Use Case — required for wiring action selection to execution
- **US-007** — List Actions Use Case — provides the menu items
- **US-008** — Check Environment Use Case — provides the environment header data
- **US-009** — ProcessBuilder Command Runner — execution infrastructure must exist
- **US-010** — Sudo Privilege Manager — privileged steps must be escalatable in TUI
- **US-012** — Ubuntu 24 Environment Detector — environment header data source
- **US-013** — Ubuntu 24 Action Catalog — provides the action definitions displayed in the menu

## Acceptance Criteria

- On startup, the environment profile is displayed in the menu header
- All available actions are listed as numbered menu items
- An "Exit" option is always present
- Arrow UP/DOWN keys and number keys (1–6 for actions, 0 for exit) navigate and select
- Selecting an action transitions to `ExecutionScreen`
- Selecting Exit closes the application cleanly
- No Lanterna import appears outside the `interfaces.tui` package

## Implementation Notes

### Classes to Create

- `br.com.josenaldo.fixkitty.interfaces.tui.TuiApp` — entry point for the TUI; creates the Lanterna terminal and screen, then starts `MainMenuScreen`
- `br.com.josenaldo.fixkitty.interfaces.tui.MainMenuScreen` — renders the menu and handles input

### Constructor Injection into TuiApp

```java
@Inject
public TuiApp(
    ListActionsUseCase listActionsUseCase,
    CheckEnvironmentUseCase checkEnvironmentUseCase,
    ExecuteRecoveryUseCase executeRecoveryUseCase
) { ... }
```

### Key Behavior

#### TuiApp.start()

1. Create a Lanterna `DefaultTerminalFactory`
2. Call `terminalFactory.createScreen()` to obtain a `Screen`
3. Call `screen.startScreen()`
4. Instantiate `MainMenuScreen` with the screen and the three use cases
5. Call `mainMenuScreen.show()` in a loop until the user exits
6. Call `screen.stopScreen()` on exit

#### MainMenuScreen.show()

1. Obtain the environment profile: `checkEnvironmentUseCase.execute()`
2. Obtain the action list: `listActionsUseCase.execute()`
3. Render the screen:
   - Line 0: application title `"FixKitty Recovery Console"`
   - Line 1: environment summary: `"Distro: [distro]  Desktop: [desktop]  Audio: [audioStack]"`
   - Line 2: blank separator
   - Lines 3–N: numbered action items from the list, e.g. `"  1. Fix Audio"`
   - Last item: `"  0. Exit"`
4. Highlight the currently selected item with reverse video or a `>` prefix
5. Poll `screen.readInput()` in a loop:
   - `KeyType.ArrowUp` — move selection up (wraps)
   - `KeyType.ArrowDown` — move selection down (wraps)
   - Character `'0'` — exit
   - Characters `'1'` through `'6'` — select the corresponding action
   - `KeyType.Enter` — confirm current selection
6. On action selected: instantiate `ExecutionScreen` and call `executionScreen.show(selectedAction)`
7. On exit selected: return from `show()` to trigger clean shutdown

### Lanterna Isolation

Lanterna classes (`com.googlecode.lanterna.*`) must not be imported in any class outside `br.com.josenaldo.fixkitty.interfaces.tui`. If `TuiApp` is instantiated from `bootstrap`, it must be referenced through an interface or via Guice injection with a binding declared in `AppModule`.

### Edge Cases

- If the terminal dimensions are smaller than 40 columns or 10 rows: render a single warning line `"Terminal too small. Please resize."` and wait for resize before rendering the full menu
- If `DefaultTerminalFactory.createScreen()` throws an `IOException`: log the error at ERROR level and call `System.exit(1)` with a message printed to stderr
- If `listActionsUseCase.execute()` returns an empty list: display `"No actions available for this environment."` and only show the Exit option

## Related

- **ADR-001** — Clean Architecture; TUI screens must not contain business logic
- **ADR-003** — Lanterna selected for TUI; isolation rule defined here
- **TC-TUI-001** — TUI test: menu displays correct action list
- **TC-TUI-002** — TUI test: keyboard navigation works correctly
