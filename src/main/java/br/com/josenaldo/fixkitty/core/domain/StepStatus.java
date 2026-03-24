package br.com.josenaldo.fixkitty.core.domain;

/**
 * Outcome status for a single execution step.
 */
public enum StepStatus {
    /** Command completed with exit code 0. */
    SUCCESS,
    /** Command completed with a non-zero exit code. */
    FAILED,
    /** Step was not executed due to a prior ABORT failure policy. */
    SKIPPED,
    /** Command did not complete within the allowed timeout. */
    TIMEOUT
}
