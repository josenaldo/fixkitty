# FixKitty — Phase 1 Design Spec

**Date:** 2026-03-23
**Status:** Approved
**Scope:** Phase 1 MVP — Ubuntu 24, GUI + TUI, 6 recovery actions

---

## Overview

FixKitty is a local recovery console for Linux desktop environments. It allows users to
restart broken subsystems (audio, Bluetooth, network, GNOME shell) without rebooting,
through both a graphical interface (JavaFX) and a terminal interface (Lanterna TUI).

The system follows Clean Architecture: business logic lives exclusively in `core` and
`application`; `infrastructure` and `interfaces` are replaceable adapters.

---

## Technology Stack

| Layer                | Technology                                  |
| -------------------- | ------------------------------------------- |
| Language             | Java 25                                     |
| Build                | Gradle (Kotlin DSL)                         |
| Dependency injection | Google Guice                                |
| App logging          | SLF4J + Logback                             |
| GUI                  | JavaFX 23 + AtlantaFX (Dracula) + Ikonli    |
| TUI                  | Lanterna 3.x (isolated in `interfaces/tui`) |
| Tests                | JUnit 5 + Mockito                           |

---

## Package Structure

```text
br.com.josenaldo.fixkitty
  ├── core/
  │   ├── domain/          ← RecoveryAction, EnvironmentProfile, ExecutionStep,
  │   │                       StepResult, ExecutionResult, enums
  │   └── ports/           ← CommandRunner, PrivilegeManager,
  │                           EnvironmentDetector, ActionCatalog
  ├── application/         ← ExecuteRecoveryUseCase, ListActionsUseCase,
  │                           CheckEnvironmentUseCase
  ├── infrastructure/
  │   ├── command/         ← ProcessBuilderCommandRunner
  │   ├── privilege/       ← SudoPrivilegeManager, PkexecPrivilegeManager
  │   ├── detectors/       ← Ubuntu24EnvironmentDetector
  │   └── catalog/         ← Ubuntu24ActionCatalog
  ├── interfaces/
  │   ├── gui/             ← JavaFX controllers, AppRestarter
  │   └── tui/             ← Lanterna screens and handlers
  └── bootstrap/           ← Main, AppModule (Guice), InterfaceSelector
```

### Dependency Rule

```text
bootstrap → interfaces → application → core
                      ↑
                infrastructure → core
```

Infrastructure is **never** imported directly by `application` — only via ports.

---

## Domain Model

### Enums

```text
RecoveryAction
  FIX_AUDIO, FIX_BLUETOOTH, FIX_NETWORK,
  FIX_GNOME_SHELL, FIX_ALL, CHECK_ENVIRONMENT

  Note: CHECK_ENVIRONMENT is included in RecoveryAction to unify action routing
  through ListActionsUseCase and the TUI/GUI menu. It produces an ExecutionResult
  with an empty step list. CheckEnvironmentUseCase handles the profile detection
  independently; both paths are valid entry points.

StepStatus
  SUCCESS   — command exited with code 0
  FAILED    — command exited with non-zero code
  SKIPPED   — not executed (due to ABORT policy on a prior step)
  TIMEOUT   — command did not complete within the allowed time

ResultStatus
  SUCCESS   — all steps completed with SUCCESS
  PARTIAL   — at least one step succeeded and at least one failed (with CONTINUE or WARN policy)
  FAILED    — all steps failed, or the first step failed with ABORT policy

FailurePolicy
  ABORT     — if this step fails, mark all remaining steps as SKIPPED and stop execution
  CONTINUE  — if this step fails, continue executing remaining steps (overall status may be PARTIAL or FAILED)
  WARN      — same as CONTINUE, but the failure is treated as non-critical (does not promote result to FAILED)
```

### ResultStatus Aggregation Rule

Given a completed set of `StepResult` entries, the `ExecutionResult.status` is determined as:

1. If all steps are `SUCCESS` → `ResultStatus.SUCCESS`
2. If all non-`SKIPPED` steps are `FAILED` (regardless of policy) → `ResultStatus.FAILED`
3. If at least one step is `SUCCESS` and at least one is `FAILED` (with `CONTINUE` policy) → `ResultStatus.PARTIAL`
4. If at least one step is `SUCCESS` and all failures had `WARN` policy → `ResultStatus.SUCCESS`
   (WARN failures are treated as non-critical and do not lower the overall status)
5. If **all** non-`SKIPPED` steps failed with `WARN` policy and **none** succeeded → `ResultStatus.FAILED`
   (WARN makes individual failures non-critical, but a total failure is still a failure)

`SKIPPED` steps (caused by a prior `ABORT`) do not count as failures in the aggregation.

### Entities

**ExecutionStep**

| Field             | Type          | Description                                                                       |
| ----------------- | ------------- | --------------------------------------------------------------------------------- |
| id                | String        | Unique step identifier                                                            |
| description       | String        | Human-readable description                                                        |
| command           | String[]      | The OS command to execute (e.g. `["systemctl", "--user", "restart", "pipewire"]`) |
| requiresPrivilege | boolean       | Whether privilege escalation is needed                                            |
| timeoutSeconds    | int           | Max execution time before TIMEOUT                                                 |
| onFailure         | FailurePolicy | Behavior if this step fails                                                       |

**StepResult**

| Field           | Type          | Description                              |
| --------------- | ------------- | ---------------------------------------- |
| step            | ExecutionStep | The step that was executed               |
| status          | StepStatus    | Outcome of the step                      |
| exitCode        | int           | Process exit code (-1 if not started)    |
| stdout          | String        | Standard output captured                 |
| stderr          | String        | Standard error captured                  |
| durationMs      | long          | Execution time in milliseconds           |
| friendlyMessage | String        | Human-readable explanation of the result |

**ExecutionResult**

| Field          | Type               | Description                                                                |
| -------------- | ------------------ | -------------------------------------------------------------------------- |
| action         | RecoveryAction     | The action that was executed                                               |
| status         | ResultStatus       | Overall outcome (see aggregation rule)                                     |
| steps          | List\<StepResult\> | Individual step outcomes                                                   |
| startedAt      | Instant            | Execution start timestamp                                                  |
| finishedAt     | Instant            | Execution end timestamp                                                    |
| recommendation | String (nullable)  | Suggested next step; set by the use case based on step results (see below) |

**recommendation field:** Set by `ExecuteRecoveryUseCase` after all steps complete.
The recommendation is non-null when any step has status `FAILED` or `TIMEOUT`.
`Ubuntu24ActionCatalog` provides a default recommendation string per action (e.g.,
`"Try logging out and back in if audio is still broken."`), which the use case applies
when the result is not fully successful.

**EnvironmentProfile**

| Field               | Type    | Description                           |
| ------------------- | ------- | ------------------------------------- |
| distro              | String  | e.g. "Ubuntu 24.04"                   |
| desktop             | String  | e.g. "GNOME"                          |
| audioStack          | String  | e.g. "PipeWire"                       |
| hasGraphicalSession | boolean | Whether a graphical session is active |

### Ports

```java
/** Executes a single step's command and returns the structured result. */
CommandRunner
  StepResult execute(ExecutionStep step);

/** Transforms a raw command array to include privilege escalation (sudo, pkexec). */
PrivilegeManager
  String[] escalate(String[] command);

/** Detects the current Linux environment and returns a profile descriptor. */
EnvironmentDetector
  EnvironmentProfile detect();

/** Provides available actions and their execution plans for a given environment. */
ActionCatalog
  List<RecoveryAction> actionsFor(EnvironmentProfile profile);
  List<ExecutionStep> planFor(RecoveryAction action, EnvironmentProfile profile);
  String defaultRecommendationFor(RecoveryAction action);
```

### Domain Rule

`ExecutionResult` never throws exceptions for execution failures — failure is structured
data. Exceptions are reserved for application bugs only.

---

## Use Cases

### ExecuteRecoveryUseCase

- **Input:** `RecoveryAction`
- **Output:** `ExecutionResult`

**Flow:**

1. Detect environment via `EnvironmentDetector`
2. Retrieve step plan via `ActionCatalog.planFor(action, profile)`
3. For each step:
   - If `requiresPrivilege` is true, apply `PrivilegeManager.escalate(step.command())`
     to produce an escalated command, then execute via `CommandRunner`
   - If `requiresPrivilege` is false, execute via `CommandRunner` directly
   - Apply `FailurePolicy`:
     - `ABORT`: on failure, mark all remaining steps as `SKIPPED` and stop
     - `CONTINUE`: on failure, continue to the next step
     - `WARN`: on failure, continue; treat the step as non-critical for aggregation
4. Aggregate all `StepResult` entries into `ExecutionResult` using the ResultStatus rule
5. Set `recommendation` via `ActionCatalog.defaultRecommendationFor(action)` if the
   result is not fully `SUCCESS`

### ListActionsUseCase

- **Input:** none
- **Output:** `List<RecoveryAction>`
- Detects the current environment via `EnvironmentDetector` and returns actions
  available for that profile from `ActionCatalog`.

### CheckEnvironmentUseCase

- **Input:** none
- **Output:** `EnvironmentProfile`
- Detects and returns the current environment profile via `EnvironmentDetector`.

---

## Infrastructure — Ubuntu 24

### ProcessBuilderCommandRunner

- Executes `step.command()` via `ProcessBuilder` (or the escalated command if privilege
  was applied before this call — `CommandRunner` always receives the final command array)
- Captures `stdout` and `stderr`
- Enforces timeout via `Process.waitFor(timeout, TimeUnit.SECONDS)`
- Returns `StepStatus.TIMEOUT` on timeout, `StepStatus.FAILED` for non-zero exit codes,
  `StepStatus.SUCCESS` for exit code 0

### Privilege Managers

- **SudoPrivilegeManager** — prepends `"sudo"` to the command array (used by TUI)
- **PkexecPrivilegeManager** — wraps with `["pkexec", ...]` (used by GUI)
- Bootstrap injects the correct implementation per interface

### Ubuntu24EnvironmentDetector

Implements `EnvironmentDetector`. Detects environment by reading:

- `/etc/os-release` — distro name and version
- `$XDG_CURRENT_DESKTOP` — desktop environment
- `systemctl --user is-active pipewire` — audio stack detection
- `$DISPLAY` / `$WAYLAND_DISPLAY` — graphical session presence

Returns an `EnvironmentProfile` value object.

### Ubuntu24ActionCatalog

Implements `ActionCatalog`. Provides `ExecutionStep` lists with explicit `command`,
`requiresPrivilege`, and `onFailure` per action:

| Action            | Steps                                                          | requiresPrivilege | onFailure |
| ----------------- | -------------------------------------------------------------- | ----------------- | --------- |
| FIX_AUDIO         | `systemctl --user restart pipewire`                            | false             | CONTINUE  |
| FIX_AUDIO (2)     | `systemctl --user restart wireplumber`                         | false             | CONTINUE  |
| FIX_BLUETOOTH     | `systemctl restart bluetooth`                                  | true              | ABORT     |
| FIX_NETWORK       | `systemctl restart NetworkManager`                             | true              | ABORT     |
| FIX_GNOME_SHELL   | `killall gnome-shell`                                          | false             | ABORT     |
| FIX_ALL           | audio steps → bluetooth step → network step → gnome shell step | (per step above)  | CONTINUE  |
| CHECK_ENVIRONMENT | *(empty — no system steps)*                                    | —                 | —         |

Note: `killall gnome-shell` runs as the current user and does not require root on Ubuntu 24.

**FIX_ALL step policy:** FIX_ALL creates **independent** `ExecutionStep` instances with `onFailure=CONTINUE`
for all steps, overriding the individual action policies (e.g., FIX_BLUETOOTH uses ABORT when run
standalone, but CONTINUE when part of FIX_ALL). This ensures a single failure does not prevent
the remaining subsystems from being recovered.

---

## Interfaces

### TUI (Lanterna)

- **TuiApp** — entry point; initializes Lanterna terminal
- **MainMenuScreen** — navigable menu listing all 6 actions; environment is detected at
  startup and shown in the menu header
- **ExecutionScreen** — real-time step progress display during execution
- **ResultScreen** — full `ExecutionResult` (status, steps, recommendation)
- **EnvironmentScreen** — displays detected `EnvironmentProfile`

Environment is detected automatically on TUI startup and shown in `MainMenuScreen` header.
Selecting CHECK_ENVIRONMENT from the menu refreshes and displays the full profile.

Lanterna classes must **never** be imported outside `interfaces/tui`.

### GUI (JavaFX)

- **MainController** — buttons for each action with Ikonli icons; AtlantaFX Dracula theme
- **LogPanel** — scrollable TextArea for real-time step progress; auto-scrolls to latest entry
- **EnvironmentPanel** — displays detected `EnvironmentProfile`; populated on app startup
  and refreshed when the user triggers CHECK_ENVIRONMENT
- **AppRestarter** — relaunches the process via `ProcessBuilder` then calls
  `Platform.exit()` followed by `System.exit(0)`

#### Fix GNOME Shell — GUI special handling

1. User clicks "Fix GNOME Shell"
2. Confirmation dialog: *"This action will restart the GNOME shell. The graphical
   interface will briefly close and FixKitty will relaunch automatically. Continue?"*
3. **Cancel** → nothing happens
4. **Confirm** → execute action → skip result display → call `AppRestarter` immediately
   (the GUI will close during execution; result display is not attempted)

---

## Bootstrap

- `Main.java` — reads `--tui` argument; delegates to `InterfaceSelector`
- `InterfaceSelector` — decides which interface to launch based on the argument and
  graphical session availability
- `AppModule` — Guice module; binds `SudoPrivilegeManager` for TUI and
  `PkexecPrivilegeManager` for GUI using a named or conditional binding

---

## Testing Strategy

| Layer          | Approach                                                            |
| -------------- | ------------------------------------------------------------------- |
| Domain         | Pure unit tests — no mocks needed                                   |
| Use Cases      | Unit tests with mocked ports (Mockito)                              |
| Infrastructure | Integration tests with real commands (limited scope, safe commands) |
| GUI/TUI        | Manual testing only for Phase 1 (see ADR-010)                       |
