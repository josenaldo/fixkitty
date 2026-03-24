package br.com.josenaldo.fixkitty.core.domain;

/**
 * The structured result of executing a single {@link ExecutionStep}.
 *
 * <p>Captures all observable output from the OS process. Never throws
 * exceptions for execution failures — all outcomes are represented as data.
 *
 * @param step           the step that was executed
 * @param status         outcome classification
 * @param exitCode       process exit code; {@code -1} if the process never started
 * @param stdout         standard output captured from the process
 * @param stderr         standard error captured from the process
 * @param durationMs     elapsed execution time in milliseconds
 * @param friendlyMessage human-readable explanation of the outcome
 */
public record StepResult(
        ExecutionStep step,
        StepStatus status,
        int exitCode,
        String stdout,
        String stderr,
        long durationMs,
        String friendlyMessage) {
}
