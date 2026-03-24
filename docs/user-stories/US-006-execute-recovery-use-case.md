# US-006 — Execute Recovery Use Case

**Epic:** Core Application
**Phase:** 1
**Status:** Pending

## Story

**As a** developer,
**I want** `ExecuteRecoveryUseCase` implemented and unit-tested,
**so that** any interface can trigger a recovery action uniformly.

## Dependencies

- **US-004** — Domain Entities — `RecoveryAction`, `ExecutionResult`, `StepResult`, `FailurePolicy`, `ResultStatus` must exist
- **US-005** — Port Interfaces — `EnvironmentDetector`, `ActionCatalog`, `CommandRunner`, `PrivilegeManager` must exist

## Acceptance Criteria

- Accepts a `RecoveryAction` and returns an `ExecutionResult`
- Applies `FailurePolicy` correctly per step (ABORT stops, CONTINUE keeps going, WARN keeps going)
- Sets `recommendation` on the result when `ResultStatus` is not `SUCCESS`
- `recommendation` is `null` when all steps succeed
- Never throws an exception for execution failures
- All unit tests listed below pass

## Implementation Notes

### Classes to Create

- `br.com.josenaldo.fixkitty.application.ExecuteRecoveryUseCase` — the use case class

### Constructor Injection (Guice)

```java
@Inject
public ExecuteRecoveryUseCase(
    EnvironmentDetector environmentDetector,
    ActionCatalog catalog,
    CommandRunner commandRunner,
    PrivilegeManager privilegeManager
) { ... }
```

### Key Behavior

The `execute(RecoveryAction action)` method must follow this algorithm:

1. Call `environmentDetector.detect()` to obtain the current `EnvironmentProfile`
2. Call `catalog.planFor(action, profile)` to obtain the ordered list of `ExecutionStep` objects
3. Record `startedAt = Instant.now()`
4. Initialize an empty `List<StepResult> results`
5. For each `step` in the plan:

   a. If `step.requiresPrivilege()` is `true`:
      - Call `privilegeManager.escalate(step.command())` to get an escalated command array
      - Create a modified step: `new ExecutionStep(step.id(), step.description(), escalatedCommand, false, step.timeoutSeconds(), step.onFailure())`

   b. Call `commandRunner.execute(step)` (or the modified step if privilege was required) to get a `StepResult`

   c. Add `stepResult` to `results`

   d. If `stepResult.status()` is `FAILED` or `TIMEOUT`:
      - If `step.onFailure()` is `ABORT`: create a `StepResult` with `StepStatus.SKIPPED`, `exitCode = -1`, `durationMs = 0`, empty stdout/stderr, and `friendlyMessage = "Skipped due to prior failure"` for every remaining step in the plan; add all to `results`; break the loop
      - If `step.onFailure()` is `CONTINUE` or `WARN`: add to results, continue to the next step

6. Record `finishedAt = Instant.now()`
7. Compute `ResultStatus` using the aggregation rules below
8. Set `recommendation`:
   - If `resultStatus == ResultStatus.SUCCESS` → `recommendation = null`
   - Otherwise → `recommendation = catalog.defaultRecommendationFor(action)`
9. Return `new ExecutionResult(action, resultStatus, List.copyOf(results), startedAt, finishedAt, recommendation)`

### ResultStatus Aggregation Rules

Apply these rules in order after all steps have been collected:

| Condition | ResultStatus |
| --- | --- |
| All step results have status `SUCCESS` | `SUCCESS` |
| All non-SKIPPED results are `FAILED` or `TIMEOUT` | `FAILED` |
| At least one `SUCCESS` and at least one `FAILED`/`TIMEOUT` with policy `CONTINUE` | `PARTIAL` |
| At least one `SUCCESS` and all failures have policy `WARN` | `SUCCESS` |
| All results are `WARN` failures and none are `SUCCESS` | `FAILED` |

### Unit Tests to Write

- `execute_allStepsSucceed_returnsSuccess()` — all steps return SUCCESS; result status is SUCCESS; recommendation is null
- `execute_oneStepFails_withContinue_returnsPartial()` — one step fails with CONTINUE policy; result status is PARTIAL; recommendation is non-null
- `execute_firstStepFails_withAbort_remainingSkipped()` — first step fails with ABORT; all remaining steps appear in results as SKIPPED
- `execute_allStepsFailWithWarn_returnsFailed()` — all steps fail with WARN policy; no successes; result is FAILED
- `execute_someSucceedSomeFailWithWarn_returnsSuccess()` — some steps succeed, some fail with WARN; result is SUCCESS
- `execute_stepTimesOut_withAbort_stopsExecution()` — a step returns TIMEOUT with ABORT; loop stops; remaining steps are SKIPPED
- `execute_privilegedStep_callsPrivilegeManager()` — verify `privilegeManager.escalate()` is called when `step.requiresPrivilege()` is true
- `execute_nonSuccessResult_setsRecommendation()` — when result is not SUCCESS; `result.recommendation()` equals the value returned by `catalog.defaultRecommendationFor()`
- `execute_fullSuccess_recommendationIsNull()` — when result is SUCCESS; `result.recommendation()` is null

### Edge Cases

- An empty step list (as returned by `planFor(CHECK_ENVIRONMENT, ...)`) must produce a valid `ExecutionResult` with an empty steps list and `ResultStatus.SUCCESS`
- `startedAt` and `finishedAt` must be recorded around the loop, not around the use-case method body, to exclude detection and planning time

## Related

- **ADR-001** — Clean Architecture; use cases live in the `application` layer
- **ADR-006** — Structured execution result; `ExecutionResult` aggregation logic is specified here
- **TC-GUI-002** — GUI test for successful execution
- **TC-GUI-003** — GUI test for partial execution
- **TC-GUI-008** — GUI test for FIX_ALL action
- **TC-GUI-009** — GUI test for recommendation display
- **TC-TUI-003** — TUI test for successful execution
- **TC-TUI-004** — TUI test for partial execution
- **TC-TUI-008** — TUI test for FIX_ALL action
- **TC-TUI-009** — TUI test for recommendation display
