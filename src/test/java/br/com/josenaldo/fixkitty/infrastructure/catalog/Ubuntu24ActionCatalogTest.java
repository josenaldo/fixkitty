package br.com.josenaldo.fixkitty.infrastructure.catalog;

import br.com.josenaldo.fixkitty.core.domain.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class Ubuntu24ActionCatalogTest {

    Ubuntu24ActionCatalog catalog = new Ubuntu24ActionCatalog();
    EnvironmentProfile profile = new EnvironmentProfile("Ubuntu 24.04", "GNOME", "PipeWire", true);

    @Test
    void actionsFor_returnsAllSixActions() {
        List<RecoveryAction> actions = catalog.actionsFor(profile);
        assertEquals(6, actions.size());
    }

    @Test
    void planFor_fixAudio_returnsTwoSteps() {
        List<ExecutionStep> steps = catalog.planFor(RecoveryAction.FIX_AUDIO, profile);
        assertEquals(2, steps.size());
        assertFalse(steps.get(0).requiresPrivilege());
        assertFalse(steps.get(1).requiresPrivilege());
    }

    @Test
    void planFor_fixBluetooth_requiresPrivilege() {
        List<ExecutionStep> steps = catalog.planFor(RecoveryAction.FIX_BLUETOOTH, profile);
        assertEquals(1, steps.size());
        assertTrue(steps.get(0).requiresPrivilege());
        assertEquals(FailurePolicy.ABORT, steps.get(0).onFailure());
    }

    @Test
    void planFor_fixGnomeShell_noPrivilegeRequired() {
        List<ExecutionStep> steps = catalog.planFor(RecoveryAction.FIX_GNOME_SHELL, profile);
        assertEquals(1, steps.size());
        assertFalse(steps.get(0).requiresPrivilege());
    }

    @Test
    void planFor_fixAll_allStepsHaveContinuePolicy() {
        List<ExecutionStep> steps = catalog.planFor(RecoveryAction.FIX_ALL, profile);
        assertFalse(steps.isEmpty());
        steps.forEach(s -> assertEquals(FailurePolicy.CONTINUE, s.onFailure(),
            "FIX_ALL step " + s.id() + " must use CONTINUE policy"));
    }

    @Test
    void planFor_checkEnvironment_returnsEmptyList() {
        List<ExecutionStep> steps = catalog.planFor(RecoveryAction.CHECK_ENVIRONMENT, profile);
        assertTrue(steps.isEmpty());
    }

    @Test
    void defaultRecommendationFor_allActionsExceptCheckEnvironment_nonNull() {
        for (RecoveryAction action : RecoveryAction.values()) {
            if (action == RecoveryAction.CHECK_ENVIRONMENT) continue;
            assertNotNull(catalog.defaultRecommendationFor(action),
                "Recommendation must not be null for " + action);
        }
    }
}
