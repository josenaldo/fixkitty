# US-009 — ProcessBuilder Command Runner

**Epic:** Infrastructure
**Phase:** 1
**Status:** Pending

## Story

**As a** system,
**I want** a `ProcessBuilderCommandRunner` that executes commands, captures stdout/stderr, enforces timeouts, and returns a `StepResult`,
**so that** recovery actions are executed in a controlled and observable manner.

## Dependencies

- **US-004** — Domain Entities — `ExecutionStep`, `StepResult`, `StepStatus` must exist
- **US-005** — Port Interfaces — `CommandRunner` must exist

## Acceptance Criteria

- `execute(ExecutionStep step)` never throws for execution failures; it always returns a `StepResult`
- stdout and stderr are captured separately and returned in the `StepResult`
- If the process exceeds `step.timeoutSeconds()`, it is forcibly terminated and `StepStatus.TIMEOUT` is returned
- Exit code 0 maps to `SUCCESS`; any non-zero exit code maps to `FAILED`
- `friendlyMessage` is set appropriately for each outcome
- Integration tests using real system commands pass

## Implementation Notes

### Classes to Create

- `br.com.josenaldo.fixkitty.infrastructure.command.ProcessBuilderCommandRunner` implements `CommandRunner`

### Key Behavior

The `execute(ExecutionStep step)` method must:

1. Build a `ProcessBuilder` from `step.command()` — the command array is passed directly; privilege escalation has already been applied by the use case if required
2. Call `processBuilder.redirectErrorStream(false)` to capture stdout and stderr as separate streams
3. Start the process: `Process process = processBuilder.start()`
4. Launch two threads to drain `process.getInputStream()` (stdout) and `process.getErrorStream()` (stderr) concurrently into `StringBuilder` instances; this prevents deadlock when both streams fill their OS buffers
5. Call `process.waitFor(step.timeoutSeconds(), TimeUnit.SECONDS)`
6. If `waitFor` returns `false` (timeout elapsed):
   - Call `process.destroyForcibly()`
   - Wait for the drain threads to complete (join with a short timeout)
   - Return a `StepResult` with:
     - `status = StepStatus.TIMEOUT`
     - `exitCode = -1`
     - stdout/stderr as captured so far
     - `friendlyMessage = "Command timed out after " + step.timeoutSeconds() + " seconds"`
     - `durationMs = step.timeoutSeconds() * 1000L`
7. If `waitFor` returns `true` (process exited):
   - Wait for the drain threads to complete (join)
   - `exitCode = process.exitValue()`
   - `status = (exitCode == 0) ? StepStatus.SUCCESS : StepStatus.FAILED`
   - `friendlyMessage`:
     - On SUCCESS: `"Completed successfully"`
     - On FAILED: `"Command exited with code " + exitCode`
   - `durationMs` = elapsed wall-clock time in milliseconds (record `startMs = System.currentTimeMillis()` before step 3)
   - Return the `StepResult`
8. If `processBuilder.start()` throws an `IOException`:
   - Return a `StepResult` with:
     - `status = StepStatus.FAILED`
     - `exitCode = -1`
     - `stdout = ""`
     - `stderr = exception.getMessage()`
     - `friendlyMessage = "Failed to start process: " + exception.getMessage()`
     - `durationMs = 0`

### Stream Draining

Use a helper method or inner class to drain a stream:

```java
private String drain(InputStream stream) throws IOException {
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append(System.lineSeparator());
        }
        return sb.toString();
    }
}
```

Each drain must run in a separate thread (e.g., using a virtual thread via `Thread.ofVirtual().start(...)` available in Java 25) to prevent buffer deadlocks.

### Integration Tests (Real Commands, No Mocking)

- `execute_echoCommand_returnsSuccess()` — command: `["echo", "hello"]`; assert `status == SUCCESS`, `exitCode == 0`, `stdout` contains `"hello"`
- `execute_falseCommand_returnsFailed()` — command: `["false"]` (always exits 1 on Linux); assert `status == FAILED`, `exitCode == 1`
- `execute_sleepCommand_timesOut()` — command: `["sleep", "60"]` with `timeoutSeconds = 1`; assert `status == TIMEOUT`, `exitCode == -1`

### Edge Cases

- Empty stdout or stderr must be returned as an empty string `""`, never as `null`
- If a drain thread is interrupted while joining, log the interruption at WARN level and return whatever was captured; do not rethrow
- The process produces large output: the buffered reader approach reads line by line and does not load the entire output into memory before processing, which is sufficient for Phase 1

## Related

- **ADR-006** — Structured execution result; `StepResult` fields and their semantics are defined there
- **TC-GUI-002** — GUI integration test that depends on real command execution
- **TC-TUI-003** — TUI integration test that depends on real command execution
