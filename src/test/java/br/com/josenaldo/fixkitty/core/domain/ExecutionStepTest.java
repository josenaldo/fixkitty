package br.com.josenaldo.fixkitty.core.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ExecutionStepTest {

    @Test
    void constructor_setsAllFields() {
        String[] cmd = {"echo", "hello"};
        ExecutionStep step = new ExecutionStep("id1", "Echo hello", cmd, false, 5, FailurePolicy.CONTINUE);

        assertEquals("id1", step.id());
        assertEquals("Echo hello", step.description());
        assertArrayEquals(cmd, step.command());
        assertFalse(step.requiresPrivilege());
        assertEquals(5, step.timeoutSeconds());
        assertEquals(FailurePolicy.CONTINUE, step.onFailure());
    }

    @Test
    void constructor_withPrivilegeRequired() {
        ExecutionStep step = new ExecutionStep("s1", "desc", new String[]{"sudo", "cmd"}, true, 10, FailurePolicy.ABORT);
        assertTrue(step.requiresPrivilege());
    }
}
