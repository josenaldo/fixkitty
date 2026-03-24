package br.com.josenaldo.fixkitty.application;

import br.com.josenaldo.fixkitty.core.domain.*;
import br.com.josenaldo.fixkitty.core.ports.*;
import com.google.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Use case that executes a recovery action and returns a structured result.
 *
 * <p>Orchestrates environment detection, step planning, command execution,
 * privilege escalation, and result aggregation. Never throws exceptions for
 * execution failures — all failure states are captured in {@link ExecutionResult}.
 */
public class ExecuteRecoveryUseCase {

    private final EnvironmentDetector detector;
    private final ActionCatalog catalog;
    private final CommandRunner runner;
    private final PrivilegeManager privilege;

    /**
     * Creates the use case with all required ports.
     *
     * @param detector  detects the current Linux environment
     * @param catalog   supplies the execution plan for each action
     * @param runner    executes OS commands and captures output
     * @param privilege escalates commands that require elevated privileges
     */
    @Inject
    public ExecuteRecoveryUseCase(EnvironmentDetector detector,
                                   ActionCatalog catalog,
                                   CommandRunner runner,
                                   PrivilegeManager privilege) {
        this.detector = detector;
        this.catalog = catalog;
        this.runner = runner;
        this.privilege = privilege;
    }

    /**
     * Executes the given recovery action and returns the aggregated result.
     *
     * @param action the action to execute
     * @return the fully populated execution result; never {@code null}
     */
    public ExecutionResult execute(RecoveryAction action) {
        Instant startedAt = Instant.now();
        EnvironmentProfile profile = detector.detect();
        List<ExecutionStep> plan = catalog.planFor(action, profile);
        List<StepResult> results = new ArrayList<>();
        boolean aborted = false;

        for (ExecutionStep step : plan) {
            if (aborted) {
                results.add(skipped(step));
                continue;
            }
            ExecutionStep toRun = step.requiresPrivilege()
                    ? escalated(step)
                    : step;
            StepResult stepResult = runner.execute(toRun);
            results.add(stepResult);

            boolean failed = stepResult.status() == StepStatus.FAILED
                    || stepResult.status() == StepStatus.TIMEOUT;
            if (failed && step.onFailure() == FailurePolicy.ABORT) {
                aborted = true;
            }
        }

        ResultStatus overallStatus = aggregate(results);
        String recommendation = overallStatus == ResultStatus.SUCCESS
                ? null
                : catalog.defaultRecommendationFor(action);

        return new ExecutionResult(action, overallStatus, results, startedAt, Instant.now(), recommendation);
    }

    private ExecutionStep escalated(ExecutionStep step) {
        String[] escalatedCommand = privilege.escalate(step.command());
        return new ExecutionStep(step.id(), step.description(), escalatedCommand,
                false, step.timeoutSeconds(), step.onFailure());
    }

    private StepResult skipped(ExecutionStep step) {
        return new StepResult(step, StepStatus.SKIPPED, -1, "", "", 0, "Skipped due to prior failure");
    }

    private ResultStatus aggregate(List<StepResult> results) {
        long successCount = results.stream()
                .filter(r -> r.status() == StepStatus.SUCCESS).count();
        long failedCount = results.stream()
                .filter(r -> r.status() == StepStatus.FAILED || r.status() == StepStatus.TIMEOUT).count();
        long warnFailedCount = results.stream()
                .filter(r -> (r.status() == StepStatus.FAILED || r.status() == StepStatus.TIMEOUT)
                        && r.step().onFailure() == FailurePolicy.WARN).count();

        if (failedCount == 0) return ResultStatus.SUCCESS;
        if (successCount == 0) return ResultStatus.FAILED;
        if (warnFailedCount == failedCount) return ResultStatus.SUCCESS; // all failures were non-critical WARN
        return ResultStatus.PARTIAL;
    }
}
