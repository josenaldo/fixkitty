# ADR-003: Lanterna Isolated in interfaces/tui

**Date:** 2026-03-23
**Status:** Accepted

## Context

The TUI requires a library capable of rendering navigable menus, panels, and real-time
output in a terminal. Lanterna 3.x was selected for this purpose.

However, locking the entire codebase to Lanterna would make it difficult to replace
the library if it is discontinued or if a better alternative emerges.

## Decision

Lanterna classes are **only allowed inside `interfaces/tui`**. No Lanterna import may
appear in `core`, `application`, `infrastructure`, or `bootstrap`.

The TUI layer exposes no Lanterna types outward. It consumes use case outputs (plain Java
objects) and renders them using Lanterna internally.

## Consequences

- Replacing Lanterna requires changes only within `interfaces/tui`
- The architecture validation step must verify no Lanterna imports exist outside the TUI package
- TUI screens receive `ExecutionResult`, `EnvironmentProfile`, etc. as plain DTOs —
  never Lanterna-specific structures
