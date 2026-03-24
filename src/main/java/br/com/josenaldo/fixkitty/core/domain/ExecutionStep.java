package br.com.josenaldo.fixkitty.core.domain;

/**
 * Represents a single command step within a recovery action's execution plan.
 *
 * <p>Steps are immutable. The {@code command} array contains the OS command
 * to execute. If {@code requiresPrivilege} is {@code true}, the command array
 * must be escalated by the {@code PrivilegeManager} before execution.
 *
 * @param id                unique step identifier within its action
 * @param description       human-readable description shown to the user
 * @param command           the OS command to execute as an array of arguments
 * @param requiresPrivilege whether privilege escalation (sudo/pkexec) is needed
 * @param timeoutSeconds    maximum allowed execution time in seconds
 * @param onFailure         behaviour policy when this step fails
 */
public record ExecutionStep(
        String id,
        String description,
        String[] command,
        boolean requiresPrivilege,
        int timeoutSeconds,
        FailurePolicy onFailure) {
}
