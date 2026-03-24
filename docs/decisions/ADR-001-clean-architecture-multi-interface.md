# ADR-001: Clean Architecture with Multiple Interfaces

**Date:** 2026-03-23
**Status:** Accepted

## Context

FixKitty must operate in two distinct environments: a graphical desktop session (JavaFX GUI)
and a raw TTY terminal (Lanterna TUI). This is a hard requirement because the GUI itself
may be unavailable when the user needs to fix a broken GNOME shell or graphical session.

Both interfaces must execute the same recovery logic with the same reliability.

## Decision

Apply Clean Architecture with strict layer separation:

- **core/domain** — entities and value objects with no external dependencies
- **core/ports** — interfaces (abstractions) that infrastructure implements
- **application** — use cases that orchestrate domain objects via ports
- **infrastructure** — concrete implementations (ProcessBuilder, systemctl, profiles)
- **interfaces/gui** and **interfaces/tui** — thin adapters that call use cases and render results
- **bootstrap** — wires all layers together via Guice

GUI and TUI are **never** the source of business logic. They receive input, call a use case,
and display the result.

## Consequences

- Business logic is testable without any UI framework
- Adding a new interface (e.g., CLI, REST API) requires no changes to core or application
- Controllers that accidentally contain logic will be flagged during architecture validation
- Dependency rule: `interfaces → application → core ← infrastructure`
