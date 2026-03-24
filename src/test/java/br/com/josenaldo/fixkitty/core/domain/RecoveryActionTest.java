package br.com.josenaldo.fixkitty.core.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RecoveryActionTest {

    @Test
    void allActionsHaveDisplayName() {
        for (RecoveryAction action : RecoveryAction.values()) {
            assertNotNull(action.displayName(), "displayName must not be null for " + action);
            assertFalse(action.displayName().isBlank(), "displayName must not be blank for " + action);
        }
    }

    @Test
    void allSixActionsExist() {
        assertEquals(6, RecoveryAction.values().length);
    }

    @Test
    void checkEnvironment_isNotExecutable() {
        assertFalse(RecoveryAction.CHECK_ENVIRONMENT.isExecutable());
    }

    @Test
    void allNonCheckActions_areExecutable() {
        for (RecoveryAction action : RecoveryAction.values()) {
            if (action != RecoveryAction.CHECK_ENVIRONMENT) {
                assertTrue(action.isExecutable(),
                    action.name() + " should be executable");
            }
        }
    }
}
