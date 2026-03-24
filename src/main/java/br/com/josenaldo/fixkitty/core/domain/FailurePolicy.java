package br.com.josenaldo.fixkitty.core.domain;

/**
 * Defines how execution should proceed when a step fails.
 */
public enum FailurePolicy {
    /**
     * Stop execution immediately. Mark all remaining steps as
     * {@link StepStatus#SKIPPED}.
     */
    ABORT,

    /**
     * Continue executing remaining steps regardless of this step's failure.
     * The overall result may be {@link ResultStatus#PARTIAL} or
     * {@link ResultStatus#FAILED}.
     */
    CONTINUE,

    /**
     * Continue executing remaining steps. Treat this step's failure as
     * non-critical: it does not lower the overall result to
     * {@link ResultStatus#FAILED} if at least one step succeeded.
     */
    WARN
}
