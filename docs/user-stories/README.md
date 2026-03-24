# FixKitty Phase 1 — User Stories Index

**Date:** 2026-03-23
**Scope:** Phase 1 MVP — Ubuntu 24, GUI + TUI, 6 recovery actions

Each user story is an independent file with full implementation detail, acceptance criteria,
dependencies, and test case references. Stories can be implemented in any order as long as
their declared dependencies are satisfied.

---

## Dependency Graph

```text
US-001 (Gradle Setup)
  └── US-002 (Package Structure)
        └── US-003 (Bootstrap)

US-004 (Domain Entities)          ← requires US-001, US-002
  └── US-005 (Port Interfaces)
        ├── US-006 (ExecuteRecoveryUseCase)
        ├── US-007 (ListActionsUseCase)
        ├── US-008 (CheckEnvironmentUseCase)
        ├── US-009 (ProcessBuilderCommandRunner)
        ├── US-010 (SudoPrivilegeManager)
        ├── US-011 (PkexecPrivilegeManager)
        └── US-012 (Ubuntu24EnvironmentDetector)
              └── US-013 (Ubuntu24ActionCatalog)  ← also requires US-004, US-005

US-014 (TUI Main Menu)            ← requires US-003, US-006–US-013
  ├── US-015 (TUI Execution Progress)
  │     └── US-016 (TUI Result Screen)
  └── US-017 (TUI Environment Screen)

US-018 (GUI Main Window)          ← requires US-003, US-006–US-013
  ├── US-019 (GUI Log Panel)
  │     └── US-020 (GUI Result Display)
  ├── US-021 (GUI GNOME Shell Confirmation)
  │     └── US-022 (GUI Auto-Relaunch)
  └── US-023 (GUI Environment Panel)
```

---

## Stories by Epic

### Epic 1: Project Setup

| ID | Title | File |
|----|-------|------|
| US-001 | Gradle Project with Full Dependency Stack | [US-001-gradle-project-setup.md](US-001-gradle-project-setup.md) |
| US-002 | Clean Architecture Package Structure | [US-002-package-structure.md](US-002-package-structure.md) |
| US-003 | Bootstrap with GUI/TUI Selection | [US-003-bootstrap.md](US-003-bootstrap.md) |

### Epic 2: Core Domain and Ports

| ID | Title | File |
|----|-------|------|
| US-004 | Domain Entities | [US-004-domain-entities.md](US-004-domain-entities.md) |
| US-005 | Port Interfaces | [US-005-port-interfaces.md](US-005-port-interfaces.md) |
| US-006 | ExecuteRecoveryUseCase | [US-006-execute-recovery-use-case.md](US-006-execute-recovery-use-case.md) |
| US-007 | ListActionsUseCase | [US-007-list-actions-use-case.md](US-007-list-actions-use-case.md) |
| US-008 | CheckEnvironmentUseCase | [US-008-check-environment-use-case.md](US-008-check-environment-use-case.md) |

### Epic 3: Infrastructure — Ubuntu 24

| ID | Title | File |
|----|-------|------|
| US-009 | ProcessBuilderCommandRunner | [US-009-process-builder-command-runner.md](US-009-process-builder-command-runner.md) |
| US-010 | SudoPrivilegeManager | [US-010-sudo-privilege-manager.md](US-010-sudo-privilege-manager.md) |
| US-011 | PkexecPrivilegeManager | [US-011-pkexec-privilege-manager.md](US-011-pkexec-privilege-manager.md) |
| US-012 | Ubuntu24EnvironmentDetector | [US-012-ubuntu24-environment-detector.md](US-012-ubuntu24-environment-detector.md) |
| US-013 | Ubuntu24ActionCatalog | [US-013-ubuntu24-action-catalog.md](US-013-ubuntu24-action-catalog.md) |

### Epic 4: TUI (Lanterna)

| ID | Title | File |
|----|-------|------|
| US-014 | TUI Main Menu | [US-014-tui-main-menu.md](US-014-tui-main-menu.md) |
| US-015 | TUI Execution Progress | [US-015-tui-execution-progress.md](US-015-tui-execution-progress.md) |
| US-016 | TUI Result Screen | [US-016-tui-result-screen.md](US-016-tui-result-screen.md) |
| US-017 | TUI Environment Screen | [US-017-tui-environment-screen.md](US-017-tui-environment-screen.md) |

### Epic 5: GUI (JavaFX)

| ID | Title | File |
|----|-------|------|
| US-018 | GUI Main Window with Action Buttons | [US-018-gui-main-window.md](US-018-gui-main-window.md) |
| US-019 | GUI Real-Time Log Panel | [US-019-gui-log-panel.md](US-019-gui-log-panel.md) |
| US-020 | GUI Result Display | [US-020-gui-result-display.md](US-020-gui-result-display.md) |
| US-021 | GUI Fix GNOME Shell Confirmation | [US-021-gui-gnome-shell-confirmation.md](US-021-gui-gnome-shell-confirmation.md) |
| US-022 | GUI Auto-Relaunch After Fix GNOME Shell | [US-022-gui-auto-relaunch.md](US-022-gui-auto-relaunch.md) |
| US-023 | GUI Environment Panel | [US-023-gui-environment-panel.md](US-023-gui-environment-panel.md) |
