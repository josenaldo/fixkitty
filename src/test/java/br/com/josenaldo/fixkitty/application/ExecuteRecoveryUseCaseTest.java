package br.com.josenaldo.fixkitty.application;

import br.com.josenaldo.fixkitty.core.domain.*;
import br.com.josenaldo.fixkitty.core.ports.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExecuteRecoveryUseCaseTest {

    @Mock EnvironmentDetector detector;
    @Mock ActionCatalog catalog;
    @Mock CommandRunner runner;
    @Mock PrivilegeManager privilege;

    ExecuteRecoveryUseCase useCase;

    EnvironmentProfile profile = new EnvironmentProfile("Ubuntu 24.04", "GNOME", "PipeWire", true);

    ExecutionStep stepNoPrivilege = new ExecutionStep("s1", "Echo", new String[]{"echo","hi"}, false, 5, FailurePolicy.CONTINUE);
    ExecutionStep stepWithPrivilege = new ExecutionStep("s2", "Sudo cmd", new String[]{"cmd"}, true, 5, FailurePolicy.ABORT);
    ExecutionStep stepAbort = new ExecutionStep("s3", "Abort step", new String[]{"fail"}, false, 5, FailurePolicy.ABORT);
    ExecutionStep stepContinue = new ExecutionStep("s4", "Continue step", new String[]{"ok"}, false, 5, FailurePolicy.CONTINUE);

    StepResult success(ExecutionStep s) {
        return new StepResult(s, StepStatus.SUCCESS, 0, "ok", "", 10, "Completed successfully");
    }

    StepResult failed(ExecutionStep s) {
        return new StepResult(s, StepStatus.FAILED, 1, "", "error", 10, "Command exited with code 1");
    }

    @BeforeEach
    void setup() {
        useCase = new ExecuteRecoveryUseCase(detector, catalog, runner, privilege);
        when(detector.detect()).thenReturn(profile);
    }

    @Test
    void execute_allStepsSucceed_returnsSuccess() {
        when(catalog.planFor(RecoveryAction.FIX_AUDIO, profile))
            .thenReturn(List.of(stepNoPrivilege));
        when(runner.execute(stepNoPrivilege)).thenReturn(success(stepNoPrivilege));

        ExecutionResult result = useCase.execute(RecoveryAction.FIX_AUDIO);

        assertEquals(ResultStatus.SUCCESS, result.status());
        assertNull(result.recommendation());
        assertTrue(result.isSuccess());
    }

    @Test
    void execute_oneStepFails_withContinue_returnsPartial() {
        ExecutionStep step2 = new ExecutionStep("s2b", "Step 2", new String[]{"ok"}, false, 5, FailurePolicy.CONTINUE);
        when(catalog.planFor(RecoveryAction.FIX_AUDIO, profile))
            .thenReturn(List.of(stepContinue, step2));
        when(runner.execute(stepContinue)).thenReturn(failed(stepContinue));
        when(runner.execute(step2)).thenReturn(success(step2));
        when(catalog.defaultRecommendationFor(RecoveryAction.FIX_AUDIO)).thenReturn("Try again.");

        ExecutionResult result = useCase.execute(RecoveryAction.FIX_AUDIO);

        assertEquals(ResultStatus.PARTIAL, result.status());
        assertEquals(2, result.steps().size());
        assertEquals("Try again.", result.recommendation());
    }

    @Test
    void execute_firstStepFails_withAbort_remainingAreSkipped() {
        ExecutionStep step2 = new ExecutionStep("s2c", "After abort", new String[]{"ok"}, false, 5, FailurePolicy.CONTINUE);
        when(catalog.planFor(RecoveryAction.FIX_BLUETOOTH, profile))
            .thenReturn(List.of(stepAbort, step2));
        when(runner.execute(stepAbort)).thenReturn(failed(stepAbort));
        when(catalog.defaultRecommendationFor(RecoveryAction.FIX_BLUETOOTH)).thenReturn("Check service.");

        ExecutionResult result = useCase.execute(RecoveryAction.FIX_BLUETOOTH);

        assertEquals(ResultStatus.FAILED, result.status());
        assertEquals(2, result.steps().size());
        assertEquals(StepStatus.FAILED, result.steps().get(0).status());
        assertEquals(StepStatus.SKIPPED, result.steps().get(1).status());
        verify(runner, never()).execute(step2);
    }

    @Test
    void execute_privilegedStep_callsPrivilegeManager() {
        String[] escalated = {"sudo", "cmd"};
        when(privilege.escalate(stepWithPrivilege.command())).thenReturn(escalated);
        when(catalog.planFor(RecoveryAction.FIX_BLUETOOTH, profile))
            .thenReturn(List.of(stepWithPrivilege));
        // The runner receives a step with the escalated command
        when(runner.execute(any(ExecutionStep.class))).thenReturn(
            new StepResult(stepWithPrivilege, StepStatus.SUCCESS, 0, "", "", 10, "OK"));

        useCase.execute(RecoveryAction.FIX_BLUETOOTH);

        verify(privilege).escalate(stepWithPrivilege.command());
    }

    @Test
    void execute_fullSuccess_recommendationIsNull() {
        when(catalog.planFor(RecoveryAction.FIX_NETWORK, profile))
            .thenReturn(List.of(stepNoPrivilege));
        when(runner.execute(stepNoPrivilege)).thenReturn(success(stepNoPrivilege));

        ExecutionResult result = useCase.execute(RecoveryAction.FIX_NETWORK);

        assertNull(result.recommendation());
        verify(catalog, never()).defaultRecommendationFor(any());
    }
}
