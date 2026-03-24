package br.com.josenaldo.fixkitty.core.domain;

import java.time.Instant;
import java.util.List;

/**
 * The aggregated result of executing a {@link RecoveryAction}.
 *
 * <p>Contains the ordered list of step results and an overall status
 * computed from them. The {@code recommendation} field is {@code null}
 * when all steps succeeded; it contains a human-readable next-step
 * suggestion otherwise.
 *
 * @param action         the action that was executed
 * @param status         overall outcome (see ResultStatus aggregation rules in design spec)
 * @param steps          individual step outcomes in execution order
 * @param startedAt      timestamp when execution began
 * @param finishedAt     timestamp when execution completed
 * @param recommendation suggested next step for the user; {@code null} on full success
 */
public record ExecutionResult(
        RecoveryAction action,
        ResultStatus status,
        List<StepResult> steps,
        Instant startedAt,
        Instant finishedAt,
        String recommendation) {

    /**
     * Returns {@code true} if the overall result is {@link ResultStatus#SUCCESS}.
     *
     * @return {@code true} on full success
     */
    public boolean isSuccess() {
        return status == ResultStatus.SUCCESS;
    }
}
