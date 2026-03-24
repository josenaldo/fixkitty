# ADR-004: Dual PrivilegeManager Implementations (Sudo + Pkexec)

**Date:** 2026-03-23
**Status:** Accepted

## Context

Several recovery actions require elevated privileges (e.g., restarting system services
via `systemctl`). The mechanism for requesting those privileges differs depending on the
active interface:

- In a TTY terminal, prompting for a password inline (sudo) is natural and expected.
- In a graphical session, a native authentication dialog (polkit/pkexec) is the correct
  desktop UX pattern.

Running the entire GUI as root is not acceptable — it creates a security risk and is
architecturally unsound.

## Decision

Define a `PrivilegeManager` port in `core/ports`. Provide two implementations:

- **SudoPrivilegeManager** — prepends `sudo` to the command; used by TUI
- **PkexecPrivilegeManager** — wraps the command with `pkexec`; used by GUI

`AppModule` (Guice) binds the correct implementation based on which interface is launched.
Use cases have no knowledge of which escalation strategy is in use.

## Consequences

- Password prompts are context-appropriate for each interface
- A third strategy (e.g., a privileged daemon) can be added without touching use cases
- Integration tests for privileged commands require a real system and cannot run in CI
  without a configured sudoers entry — this is documented as a known limitation
