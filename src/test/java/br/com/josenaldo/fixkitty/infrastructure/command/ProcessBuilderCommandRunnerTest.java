package br.com.josenaldo.fixkitty.infrastructure.command;

import br.com.josenaldo.fixkitty.core.domain.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProcessBuilderCommandRunnerTest {

    ProcessBuilderCommandRunner runner = new ProcessBuilderCommandRunner();

    ExecutionStep echoStep = new ExecutionStep(
        "echo", "Echo hello", new String[]{"echo", "hello"},
        false, 5, FailurePolicy.CONTINUE);

    ExecutionStep falseStep = new ExecutionStep(
        "false", "Always fails", new String[]{"false"},
        false, 5, FailurePolicy.CONTINUE);

    ExecutionStep sleepStep = new ExecutionStep(
        "sleep", "Sleep forever", new String[]{"sleep", "60"},
        false, 1, FailurePolicy.CONTINUE);

    @Test
    void execute_successfulCommand_returnsSuccess() {
        StepResult result = runner.execute(echoStep);

        assertEquals(StepStatus.SUCCESS, result.status());
        assertEquals(0, result.exitCode());
        assertTrue(result.stdout().contains("hello"));
        assertTrue(result.durationMs() >= 0);
    }

    @Test
    void execute_failingCommand_returnsFailed() {
        StepResult result = runner.execute(falseStep);

        assertEquals(StepStatus.FAILED, result.status());
        assertNotEquals(0, result.exitCode());
    }

    @Test
    void execute_timeoutCommand_returnsTimeout() {
        StepResult result = runner.execute(sleepStep);

        assertEquals(StepStatus.TIMEOUT, result.status());
        assertEquals(-1, result.exitCode());
    }

    @Test
    void execute_capturesStderr() {
        ExecutionStep step = new ExecutionStep(
            "ls-missing", "List missing dir", new String[]{"ls", "/nonexistent_dir_xyz"},
            false, 5, FailurePolicy.CONTINUE);

        StepResult result = runner.execute(step);

        assertEquals(StepStatus.FAILED, result.status());
        assertFalse(result.stderr().isBlank());
    }
}
