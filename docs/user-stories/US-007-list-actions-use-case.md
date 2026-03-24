# US-007 — List Actions Use Case

**Epic:** Core Application
**Phase:** 1
**Status:** Pending

## Story

**As a** developer,
**I want** `ListActionsUseCase` implemented,
**so that** the GUI and TUI can display available actions for the detected environment.

## Dependencies

- **US-004** — Domain Entities — `RecoveryAction` and `EnvironmentProfile` must exist
- **US-005** — Port Interfaces — `EnvironmentDetector` and `ActionCatalog` must exist

## Acceptance Criteria

- `ListActionsUseCase.execute()` returns the list of `RecoveryAction` values for the detected environment
- The use case delegates detection to `EnvironmentDetector` and catalog lookup to `ActionCatalog`
- No business logic is duplicated here; the use case is a thin orchestration layer
- Unit tests pass

## Implementation Notes

### Classes to Create

- `br.com.josenaldo.fixkitty.application.ListActionsUseCase` — the use case class

### Constructor Injection (Guice)

```java
@Inject
public ListActionsUseCase(
    EnvironmentDetector environmentDetector,
    ActionCatalog catalog
) { ... }
```

### Key Behavior

The `execute()` method:

1. Call `environmentDetector.detect()` → `profile`
2. Return `catalog.actionsFor(profile)`

The returned list is whatever the catalog returns; the use case does not filter, sort, or modify it.

### Unit Tests to Write

- `listActions_returnsActionsForDetectedProfile()` — mock `EnvironmentDetector` to return a fixed profile; mock `ActionCatalog.actionsFor()` to return a known list; assert that `execute()` returns the same list
- `listActions_delegatesToCatalogWithCorrectProfile()` — verify that `catalog.actionsFor()` is called with exactly the profile returned by `environmentDetector.detect()`

### Edge Cases

- If the catalog returns an empty list for a given profile, the use case returns an empty list without error; the caller (GUI or TUI) is responsible for displaying an appropriate message
- The use case does not cache the result; each call to `execute()` triggers a fresh environment detection

## Related

- **ADR-001** — Clean Architecture; use cases live in the `application` layer and must not contain infrastructure concerns
