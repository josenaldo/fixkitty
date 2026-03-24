package br.com.josenaldo.fixkitty.core.ports;

import br.com.josenaldo.fixkitty.core.domain.ExecutionStep;
import br.com.josenaldo.fixkitty.core.domain.StepResult;

/**
 * Port for executing OS commands.
 *
 * <p>Implementations must capture stdout and stderr, enforce the step's
 * timeout, and return a structured {@link StepResult}. Must never throw
 * exceptions for execution failures — all failure states are represented
 * in the returned {@code StepResult}.
 */
public interface CommandRunner {

    /**
     * Executes the command defined in the given step.
     *
     * <p>The command array in {@code step} is already escalated if privilege
     * was required; this method always executes exactly what is in the array.
     *
     * @param step the step to execute
     * @return a fully populated result; never {@code null}
     */
    StepResult execute(ExecutionStep step);
}
