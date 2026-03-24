# US-008 — Check Environment Use Case

**Epic:** Core Application
**Phase:** 1
**Status:** Pending

## Story

**As a** developer,
**I want** `CheckEnvironmentUseCase` implemented,
**so that** the app can display the detected system profile to the user.

## Dependencies

- **US-004** — Domain Entities — `EnvironmentProfile` must exist
- **US-005** — Port Interfaces — `EnvironmentDetector` must exist

## Acceptance Criteria

- `CheckEnvironmentUseCase.execute()` returns the `EnvironmentProfile` from the detector
- The use case does not modify the profile returned by the detector
- Unit test passes

## Implementation Notes

### Classes to Create

- `br.com.josenaldo.fixkitty.application.CheckEnvironmentUseCase` — the use case class

### Constructor Injection (Guice)

```java
@Inject
public CheckEnvironmentUseCase(EnvironmentDetector environmentDetector) { ... }
```

### Key Behavior

The `execute()` method:

1. Return `environmentDetector.detect()`

This is intentionally a thin delegation layer. Its value is in giving the GUI and TUI a stable, named contract rather than calling the detector directly, and in making the interaction testable via standard unit tests.

### Unit Tests to Write

- `checkEnvironment_returnsDelegatedProfile()` — mock `EnvironmentDetector.detect()` to return a fixed `EnvironmentProfile`; assert that `execute()` returns the exact same instance

### Edge Cases

- Each call to `execute()` triggers a fresh `detect()` call; the result is never cached at the use-case level
- If `EnvironmentDetector.detect()` throws an unchecked exception, it propagates to the caller; no swallowing or wrapping is performed at this layer

## Related

- **ADR-001** — Clean Architecture; this use case is the single point of contact between the interface layer and the environment detection infrastructure
- **ADR-005** — Environment detection design; `EnvironmentProfile` fields are defined there
- **TC-GUI-010** — GUI test: environment panel populated on startup
- **TC-GUI-011** — GUI test: environment panel refreshed on Check Environment button click
- **TC-TUI-010** — TUI test: environment screen displays correct profile fields
