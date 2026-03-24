# FixKitty — TUI Test Cases

**Date:** 2026-03-23
**Interface:** Lanterna TUI
**Phase:** 1 MVP
**Status:** Manual (Phase 1) → Automatable with Lanterna VirtualTerminal (Phase 2+)

Each test case is structured to map directly to a VirtualTerminal-based test when automation
is added. Fields: **Given** (precondition), **When** (action/input), **Then** (expected output).

---

## TC-TUI-001: Application launches in TUI mode

**Related:** US-003, US-014

**Given** the application is started with `--tui` argument
**When** the TUI initializes
**Then**

- A full-screen Lanterna interface is rendered in the terminal
- The main menu header shows the detected environment profile
  (distro, desktop, audio stack, graphical session status)
- The menu lists all 6 actions:
  - [1] Fix Audio
  - [2] Fix Bluetooth
  - [3] Fix Network
  - [4] Fix GNOME Shell
  - [5] Fix All
  - [6] Check Environment
  - [0] Exit
- No Lanterna class is imported outside `interfaces/tui`

**Manual test:** Run `./gradlew run --args="--tui"` in a real terminal (not IDE console).

**Automation hint:** `DefaultTerminalFactory` with virtual terminal; assert rendered screen content.

---

## TC-TUI-002: Menu keyboard navigation

**Related:** US-014

**Given** the main menu is displayed
**When** the user presses arrow keys UP/DOWN
**Then**

- The cursor/highlight moves between menu items
- The selected item changes visually

**When** the user presses a number key (e.g., "1")
**Then**

- The corresponding action is immediately selected

**Automation hint:** Inject key events via `VirtualTerminal`; assert highlighted row changes.

---

## TC-TUI-003: Fix Audio — successful execution

**Related:** US-015, US-016

**Given** PipeWire and WirePlumber services are running
**When** the user selects "Fix Audio" from the menu and confirms
**Then**

- The execution screen is shown
- Each step is displayed as it runs:
  - `Restarting PipeWire... SUCCESS`
  - `Restarting WirePlumber... SUCCESS`
- After completion, the result screen shows:
  - Overall status: SUCCESS
  - Both step results listed
  - No recommendation text
- A "Back to menu" option is available

**Automation hint:** Mock `CommandRunner`; assert screen text line by line.

---

## TC-TUI-004: Fix Audio — partial failure

**Related:** US-015, US-016, ADR-006

**Given** PipeWire restarts successfully but WirePlumber fails
**When** the user selects "Fix Audio"
**Then**

- Execution screen shows:
  - `Restarting PipeWire... SUCCESS`
  - `Restarting WirePlumber... FAILED`
  - stderr output shown for the failed step
- Result screen shows:
  - Overall status: PARTIAL
  - Recommendation text visible

---

## TC-TUI-005: Fix Bluetooth — sudo password prompt

**Related:** US-010, ADR-004

**Given** the TUI is running and `SudoPrivilegeManager` is active
**When** the user selects "Fix Bluetooth"
**Then**

- The terminal prompts for the sudo password (inline in terminal, not a popup)
- After correct password entry, the bluetooth service restart executes
- Execution screen shows the step result

**Manual test note:** Requires a real TTY with sudo configured for the current user.

---

## TC-TUI-006: Fix Network — successful execution

**Related:** US-015, US-016

**Given** NetworkManager is running
**When** the user selects "Fix Network"
**Then**

- Execution screen shows: `Restarting NetworkManager... SUCCESS`
- Result screen shows overall status: SUCCESS
- Network connectivity is maintained after restart

---

## TC-TUI-007: Fix GNOME Shell — from TTY (no graphical session)

**Related:** US-015, US-016, ADR-007

**Given** the TUI is running in a raw TTY (Ctrl+Alt+F2, no GNOME session active)
**When** the user selects "Fix GNOME Shell"
**Then**

- The `killall gnome-shell` command executes
- Result screen shows SUCCESS (GNOME shell process was killed)
- No confirmation dialog is shown (TUI does not use graphical dialogs)
- The TUI continues running normally in the TTY

---

## TC-TUI-008: Fix All — all steps succeed

**Related:** US-015, US-016

**Given** all subsystems are running
**When** the user selects "Fix All"
**Then**

- Execution screen shows all steps in order:
  - Fix Audio step 1 (pipewire)
  - Fix Audio step 2 (wireplumber)
  - Fix Bluetooth
  - Fix Network
  - Fix GNOME Shell
- Each step shows its status as it completes
- Result screen shows overall status: SUCCESS

**Automation hint:** Mock all commands; assert 5 step entries in result order.

---

## TC-TUI-009: Fix All — bluetooth fails, execution continues

**Related:** US-015, US-016, ADR-006

**Given** bluetooth restart fails but all other steps succeed
**When** the user selects "Fix All"
**Then**

- Bluetooth step shows FAILED with stderr
- Network and GNOME Shell steps still execute (CONTINUE policy in FIX_ALL)
- Result screen shows overall status: PARTIAL
- Recommendation text is shown

---

## TC-TUI-010: Check Environment — displays full profile

**Related:** US-017

**Given** the TUI is running on Ubuntu 24 with GNOME and PipeWire
**When** the user selects "Check Environment"
**Then**

- The environment screen displays:
  - Distro: Ubuntu 24.04
  - Desktop: GNOME
  - Audio Stack: PipeWire
  - Graphical Session: Yes (or No if in TTY)
- A "Back to menu" option is available

**Automation hint:** Mock `EnvironmentDetector`; assert screen shows each field.

---

## TC-TUI-011: Exit from main menu

**Related:** US-014

**Given** the main menu is displayed
**When** the user selects "Exit" (option 0)
**Then**

- The TUI closes cleanly
- The terminal is restored to its previous state
- No error messages are printed

**Automation hint:** Assert process terminates with exit code 0.

---

## TC-TUI-012: Back navigation from result screen to menu

**Related:** US-016

**Given** an action has completed and the result screen is shown
**When** the user selects "Back to menu"
**Then**

- The main menu is displayed again
- Previous execution output is no longer shown
- The menu header still shows the environment profile

---

## TC-TUI-013: TTY-specific test — full session in Ctrl+Alt+F2

**Related:** US-014, ADR-001

**Given** the user switches to a raw TTY with Ctrl+Alt+F2
**When** the user runs `./gradlew run --args="--tui"` from the TTY shell
**Then**

- The Lanterna interface renders correctly in the raw terminal
- All actions are accessible and execute correctly
- Privilege escalation uses sudo (inline password prompt)
- No dependency on any graphical environment

**Manual test note:** This is the primary scenario FixKitty was designed for.
Must be tested manually in a real TTY — cannot be simulated with a virtual terminal.

---

## Test Environment Setup (Manual)

Before running manual TUI tests:

1. **Standard terminal:** `./gradlew run --args="--tui"`
   - Verify Lanterna renders correctly (not garbled)
   - Best with a proper terminal emulator (GNOME Terminal, Alacritty, etc.)

2. **Raw TTY:** Press Ctrl+Alt+F2 to switch to TTY2
   - Log in with your credentials
   - Navigate to the project directory
   - Run `./gradlew run --args="--tui"`
   - Verify full functionality without any graphical session

3. **Service availability check before testing:**

   ```bash
   systemctl --user is-active pipewire
   systemctl is-active bluetooth
   systemctl is-active NetworkManager
   ```
