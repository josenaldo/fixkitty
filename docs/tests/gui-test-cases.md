# FixKitty — GUI Test Cases

**Date:** 2026-03-23
**Interface:** JavaFX GUI
**Phase:** 1 MVP
**Status:** Manual (Phase 1) → Automatable with TestFX + Monocle (Phase 2+)

Each test case is structured to map directly to a TestFX test method when automation is added.
Fields: **Given** (precondition), **When** (action), **Then** (expected result).

---

## TC-GUI-001: Application launches in GUI mode

**Related:** US-003, US-018

**Given** the application is started without `--tui` argument
**When** the JVM initializes
**Then**

- The main window opens
- AtlantaFX Dracula theme is applied (dark background)
- All 6 action buttons are visible: Fix Audio, Fix Bluetooth, Fix Network, Fix GNOME Shell, Fix All, Check Environment
- Each button has an Ikonli icon
- The environment panel is populated with the detected system profile
- The log panel is empty

**Automation hint:** `@Start` method launches the app; assert node visibility with `verifyThat`.

---

## TC-GUI-002: Fix Audio — successful execution

**Related:** US-019, US-020

**Given** PipeWire and WirePlumber services are running on the system
**When** the user clicks "Fix Audio"
**Then**

- The log panel shows step progress as each command runs
- Each step entry shows: step description and status (SUCCESS)
- After completion, the overall status is displayed as SUCCESS
- No recommendation text is shown (all steps succeeded)

**Automation hint:** Mock `CommandRunner` to return SUCCESS results; assert log panel text.

---

## TC-GUI-003: Fix Audio — partial failure

**Related:** US-019, US-020, ADR-006

**Given** PipeWire restarts successfully but WirePlumber fails
**When** the user clicks "Fix Audio"
**Then**

- Log panel shows pipewire step as SUCCESS
- Log panel shows wireplumber step as FAILED with stderr content
- Overall status is displayed as PARTIAL
- Recommendation text is shown (non-empty)

**Automation hint:** Mock `CommandRunner`: first call returns SUCCESS, second returns FAILED.

---

## TC-GUI-004: Fix Bluetooth — pkexec authentication dialog appears

**Related:** US-011, ADR-004

**Given** the GUI is running and `PkexecPrivilegeManager` is active
**When** the user clicks "Fix Bluetooth"
**Then**

- A native polkit authentication dialog is shown (or a mocked equivalent in tests)
- After authentication, the bluetooth service restart executes
- Log panel updates with the step result

**Automation hint:** In automated tests, inject a mock `PrivilegeManager` that bypasses pkexec.

---

## TC-GUI-005: Fix Network — successful execution

**Related:** US-019, US-020

**Given** NetworkManager service exists on the system
**When** the user clicks "Fix Network"
**Then**

- Log panel shows NetworkManager restart step
- Overall status is SUCCESS
- Network connectivity is not interrupted (service restarts cleanly)

---

## TC-GUI-006: Fix GNOME Shell — cancel confirmation dialog

**Related:** US-021, ADR-007

**Given** the GUI is running
**When** the user clicks "Fix GNOME Shell"
**Then**

- A confirmation dialog appears with warning text:
  *"This action will restart the GNOME shell. The graphical interface will briefly close and FixKitty will relaunch automatically. Continue?"*
- The dialog has "Confirm" and "Cancel" buttons

**When** the user clicks "Cancel"
**Then**

- The dialog closes
- No command is executed
- The log panel remains unchanged

**Automation hint:** `clickOn("Cancel")` after dialog appears; assert no log entries added.

---

## TC-GUI-007: Fix GNOME Shell — confirm and relaunch

**Related:** US-021, US-022, ADR-007

**Given** the GUI is running
**When** the user clicks "Fix GNOME Shell" and then clicks "Confirm"
**Then**

- The action executes
- `gnome-shell` is killed and restarts automatically
- FixKitty relaunches as a new process
- The new instance opens in GUI mode with the main window

**Manual test note:** This test must be performed in a real desktop session.
Cannot be fully automated — process relaunch crosses JVM boundaries.

---

## TC-GUI-008: Fix All — all steps succeed

**Related:** US-018, US-019, US-020

**Given** all subsystem services are running
**When** the user clicks "Fix All"
**Then**

- Log panel shows steps in order: audio (×2) → bluetooth → network → gnome shell
- Each step shows its result progressively
- Overall status is SUCCESS

**Automation hint:** Mock all `CommandRunner` calls to return SUCCESS; assert step order in log.

---

## TC-GUI-009: Fix All — one step fails with CONTINUE policy

**Related:** US-020, ADR-006

**Given** bluetooth restart fails but all other steps succeed
**When** the user clicks "Fix All"
**Then**

- Log panel shows bluetooth step as FAILED
- Remaining steps (network, gnome shell) still execute
- Overall status is PARTIAL
- Recommendation text is shown

**Automation hint:** Mock `CommandRunner` to fail on the bluetooth command only.

---

## TC-GUI-010: Check Environment — displays detected profile

**Related:** US-023

**Given** the app is running on Ubuntu 24 with GNOME and PipeWire
**When** the user clicks "Check Environment"
**Then**

- The environment panel refreshes
- Displays: distro ("Ubuntu 24.04"), desktop ("GNOME"), audio stack ("PipeWire"),
  graphical session (true)

**Automation hint:** Mock `EnvironmentDetector`; assert environment panel labels.

---

## TC-GUI-011: Environment panel populated on startup

**Related:** US-023, ADR-005

**Given** the application is launched
**When** the main window appears (before any user interaction)
**Then**

- The environment panel already shows the detected profile
- No user action is required to see the environment info

**Automation hint:** Assert panel content in `@Start` setup, before any clicks.

---

## TC-GUI-012: Buttons disabled during execution

**Related:** US-018, US-019

**Given** a recovery action is in progress
**When** steps are executing (any action)
**Then**

- All 6 action buttons are disabled
- Buttons re-enable after the action completes

**Manual test note:** Click Fix Audio and immediately verify buttons are disabled.
**Automation hint:** Assert `isDisabled()` on buttons while a slow mock command is running.

---

## Test Environment Setup (Manual)

Before running manual tests on Ubuntu 24:

1. `./gradlew run` — confirm GUI launches
2. Verify AtlantaFX Dracula theme is visible (dark background, colored accents)
3. Verify all 6 buttons are present with icons
4. Confirm PipeWire is running: `systemctl --user is-active pipewire`
5. Confirm NetworkManager is running: `systemctl is-active NetworkManager`
6. Confirm Bluetooth is running: `systemctl is-active bluetooth`
