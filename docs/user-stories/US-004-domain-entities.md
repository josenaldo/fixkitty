# US-004 — Domain Entities

**Epic:** Core Domain
**Phase:** 1
**Status:** Pending

## Story

**As a** developer,
**I want** the domain entities implemented with full Javadoc,
**so that** they serve as the shared foundation for all layers.

## Dependencies

- **US-001** — Gradle Project Setup — Java 25 toolchain must be configured for record syntax
- **US-002** — Package Structure — `br.com.josenaldo.fixkitty.core.domain` package must exist

## Acceptance Criteria

- All entities are immutable (Java records or final classes with no setters)
- `ExecutionStep` has a `command` field of type `String[]`
- All enums are defined with Javadoc on every constant
- Unit tests cover construction, field access, and equality for all records
- `ExecutionResult.isSuccess()` returns `true` only when `status == ResultStatus.SUCCESS`
- `RecoveryAction` exposes a `displayName()` method returning a human-readable label

## Implementation Notes

### Classes to Create

All classes go in `br.com.josenaldo.fixkitty.core.domain`.

**Enums:**

- `RecoveryAction` — enumeration of all supported actions
- `StepStatus` — status of a single execution step
- `ResultStatus` — overall status of a recovery execution
- `FailurePolicy` — policy applied when a step fails

**Records:**

- `ExecutionStep` — a single command to be executed as part of a recovery action
- `StepResult` — the outcome of executing a single `ExecutionStep`
- `ExecutionResult` — the complete outcome of a recovery action
- `EnvironmentProfile` — a descriptor of the detected Linux environment

### Key Behavior

#### RecoveryAction

```java
public enum RecoveryAction {
    FIX_AUDIO("Fix Audio"),
    FIX_BLUETOOTH("Fix Bluetooth"),
    FIX_NETWORK("Fix Network"),
    FIX_GNOME_SHELL("Fix GNOME Shell"),
    FIX_ALL("Fix All"),
    CHECK_ENVIRONMENT("Check Environment");

    private final String displayName;

    RecoveryAction(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
```

#### StepStatus

```java
public enum StepStatus {
    /** The step completed and the command exited with code 0. */
    SUCCESS,
    /** The step completed but the command exited with a non-zero code. */
    FAILED,
    /** The step was not executed because a prior step failed with ABORT policy. */
    SKIPPED,
    /** The step exceeded its configured timeout and was forcibly terminated. */
    TIMEOUT
}
```

#### ResultStatus

```java
public enum ResultStatus {
    /** All executed steps succeeded. */
    SUCCESS,
    /** Some steps succeeded and at least one failed with CONTINUE policy. */
    PARTIAL,
    /** All executed steps failed or the first step failed with ABORT policy. */
    FAILED
}
```

#### FailurePolicy

```java
public enum FailurePolicy {
    /** Stop execution immediately if this step fails. */
    ABORT,
    /** Continue executing subsequent steps if this step fails. */
    CONTINUE,
    /** Continue executing subsequent steps; failure is treated as non-critical. */
    WARN
}
```

#### ExecutionStep (record)

```java
public record ExecutionStep(
    String id,
    String description,
    String[] command,
    boolean requiresPrivilege,
    int timeoutSeconds,
    FailurePolicy onFailure
) {}
```

Note: because `String[]` does not implement value-based equality, the auto-generated `equals` and `hashCode` from the record may use array reference equality for `command`. This is acceptable for Phase 1; document it in the Javadoc.

#### StepResult (record)

```java
public record StepResult(
    ExecutionStep step,
    StepStatus status,
    int exitCode,
    String stdout,
    String stderr,
    long durationMs,
    String friendlyMessage
) {}
```

The field `exitCode` is `-1` when the process never started (e.g., IOException during process creation) or when the step was SKIPPED.

#### ExecutionResult (record)

```java
public record ExecutionResult(
    RecoveryAction action,
    ResultStatus status,
    List<StepResult> steps,
    Instant startedAt,
    Instant finishedAt,
    String recommendation
) {
    /** Returns true if and only if the overall status is SUCCESS. */
    public boolean isSuccess() {
        return status == ResultStatus.SUCCESS;
    }
}
```

The `recommendation` field is nullable; it is `null` when `status == ResultStatus.SUCCESS` and non-null otherwise.

#### EnvironmentProfile (record)

```java
public record EnvironmentProfile(
    String distro,
    String desktop,
    String audioStack,
    boolean hasGraphicalSession
) {}
```

### Edge Cases

- `StepResult.exitCode` is `-1` when the process never started or when the step was SKIPPED; implementations must never use `process.exitValue()` before confirming the process terminated
- `ExecutionResult.recommendation` is `null` when all steps succeeded; callers must null-check before displaying it
- `List<StepResult>` in `ExecutionResult` should be an unmodifiable list; wrap with `Collections.unmodifiableList()` or use `List.copyOf()` in the catalog/use-case before constructing the record

## Related

- **ADR-006** — Structured execution result design; `ExecutionResult`, `StepResult`, and `ResultStatus` are defined here
- **TC-GUI-002** — Tests that verify `ExecutionResult` fields are rendered correctly in the log panel
- **TC-TUI-003** — Tests that verify TUI renders step results accurately
