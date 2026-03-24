package br.com.josenaldo.fixkitty.core.domain;

/**
 * Overall outcome status for a completed recovery action.
 *
 * <p>Aggregated from the individual {@link StepStatus} results according
 * to the rules defined in the Phase 1 design spec.
 */
public enum ResultStatus {
    /** All steps completed successfully. */
    SUCCESS,
    /** At least one step succeeded and at least one failed (with CONTINUE policy). */
    PARTIAL,
    /** All non-skipped steps failed, or the first step failed with ABORT policy. */
    FAILED
}
