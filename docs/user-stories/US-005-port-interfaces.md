# US-005 — Port Interfaces

**Epic:** Core Domain
**Phase:** 1
**Status:** Pending

## Story

**As a** developer,
**I want** the port interfaces defined with full Javadoc,
**so that** application and infrastructure communicate via abstractions.

## Dependencies

- **US-004** — Domain Entities — all port method signatures reference domain types that must exist first

## Acceptance Criteria

- All four port interfaces are defined in `br.com.josenaldo.fixkitty.core.ports`
- `ActionCatalog` includes the `defaultRecommendationFor(RecoveryAction)` method
- No concrete implementation class is referenced from within `core` or `application`
- Each interface has a Javadoc comment on every method

## Implementation Notes

### Classes to Create

- `CommandRunner` (`br.com.josenaldo.fixkitty.core.ports`) — port for executing system commands
- `PrivilegeManager` (`br.com.josenaldo.fixkitty.core.ports`) — port for privilege escalation
- `EnvironmentDetector` (`br.com.josenaldo.fixkitty.core.ports`) — port for detecting the runtime environment
- `ActionCatalog` (`br.com.josenaldo.fixkitty.core.ports`) — port for querying available actions and their execution plans

### Key Behavior

#### CommandRunner

```java
/**
 * Executes a single {@link ExecutionStep} and returns a structured {@link StepResult}.
 *
 * <p>Implementations must never throw for execution failures (non-zero exit codes,
 * timeouts). Exceptions are only acceptable for programming errors such as a null
 * argument.
 */
public interface CommandRunner {
    StepResult execute(ExecutionStep step);
}
```

#### PrivilegeManager

```java
/**
 * Transforms a command array to include the appropriate privilege-escalation
 * mechanism for the current interface (e.g., {@code sudo} for TUI, {@code pkexec}
 * for GUI).
 */
public interface PrivilegeManager {
    /**
     * Returns a new command array with the escalation prefix prepended.
     * Must not mutate the input array.
     *
     * @param command the original command array
     * @return a new array with privilege escalation prepended
     */
    String[] escalate(String[] command);
}
```

#### EnvironmentDetector

```java
/**
 * Detects the current Linux environment and returns a profile descriptor.
 *
 * <p>Implementations should prefer reading environment variables and
 * system files over executing external processes where possible.
 */
public interface EnvironmentDetector {
    /**
     * Returns a snapshot of the detected environment.
     * This method may be called more than once; each call must return a
     * fresh detection result.
     *
     * @return the detected {@link EnvironmentProfile}
     */
    EnvironmentProfile detect();
}
```

#### ActionCatalog

```java
/**
 * Provides the set of recovery actions available for a given environment profile
 * and the execution plan for each action.
 */
public interface ActionCatalog {

    /**
     * Returns the recovery actions available for the given environment profile.
     * The list is ordered for display purposes.
     *
     * @param profile the detected environment profile
     * @return an ordered, non-null, possibly empty list of available actions
     */
    List<RecoveryAction> actionsFor(EnvironmentProfile profile);

    /**
     * Returns the ordered list of {@link ExecutionStep} objects for the given
     * action and profile.
     *
     * @param action  the recovery action to plan
     * @param profile the detected environment profile
     * @return an ordered, non-null list of steps; empty for {@code CHECK_ENVIRONMENT}
     */
    List<ExecutionStep> planFor(RecoveryAction action, EnvironmentProfile profile);

    /**
     * Returns a human-readable recommendation to display when the result is
     * not fully successful.
     *
     * @param action the recovery action that was executed
     * @return a recommendation string, or {@code null} if not applicable
     */
    String defaultRecommendationFor(RecoveryAction action);
}
```

### Edge Cases

- `CommandRunner.execute()` must never throw a checked or unchecked exception due to execution failures (non-zero exit code, timeout, process not found). It must return a `StepResult` with `StepStatus.FAILED` or `StepStatus.TIMEOUT`. Only programming errors (null arguments, internal state corruption) justify throwing.
- `ActionCatalog.planFor(CHECK_ENVIRONMENT, ...)` must return an empty list; callers must handle an empty step list without error.
- `PrivilegeManager.escalate()` must not mutate the input array; it must always return a new array.

## Related

- **ADR-001** — Ports-and-adapters pattern; this US creates the port side of every adapter pair
- **ADR-006** — Structured execution result; `CommandRunner` returns `StepResult` as defined there
