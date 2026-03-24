# ADR-006: ExecutionResult as Structured Data, Never Exceptions

**Date:** 2026-03-23
**Status:** Accepted

## Context

Recovery actions can fail for many reasons: a service may not exist, a command may time
out, or the user may lack the required privileges. These are expected operational outcomes,
not application bugs.

If failures are propagated as exceptions, the calling code (use cases, controllers) must
catch and interpret them — losing structured context like exit codes, stderr output, and
human-readable recommendations along the way.

## Decision

All command execution failures are represented as structured data in `ExecutionResult`
and `StepResult`. No exception is thrown for expected execution failures.

- `StepResult.status` captures the outcome (SUCCESS, FAILED, SKIPPED, TIMEOUT)
- `StepResult.stderr` and `StepResult.exitCode` provide diagnostic details
- `ExecutionResult.recommendation` provides a human-readable next step
- `ExecutionResult.status` aggregates the overall outcome (SUCCESS, PARTIAL, FAILED)

Exceptions are reserved for genuine application bugs (e.g., null pointer, missing
required configuration) — not for Linux command failures.

## Consequences

- GUI and TUI can always render a meaningful result, even on failure
- Use cases have no try/catch for system-level failures
- Tests can assert on structured outcomes rather than catching exceptions
- Callers always receive a complete, inspectable result object
