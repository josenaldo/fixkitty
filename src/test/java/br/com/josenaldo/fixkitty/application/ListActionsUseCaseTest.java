package br.com.josenaldo.fixkitty.application;

import br.com.josenaldo.fixkitty.core.domain.EnvironmentProfile;
import br.com.josenaldo.fixkitty.core.domain.RecoveryAction;
import br.com.josenaldo.fixkitty.core.ports.ActionCatalog;
import br.com.josenaldo.fixkitty.core.ports.EnvironmentDetector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListActionsUseCaseTest {

    @Mock EnvironmentDetector detector;
    @Mock ActionCatalog catalog;

    @InjectMocks
    ListActionsUseCase useCase;

    @Test
    void execute_detectsEnvironmentAndDelegatesToCatalog() {
        EnvironmentProfile profile = new EnvironmentProfile("Ubuntu 24.04", "GNOME", "PipeWire", true);
        List<RecoveryAction> actions = List.of(RecoveryAction.FIX_AUDIO, RecoveryAction.FIX_BLUETOOTH);
        when(detector.detect()).thenReturn(profile);
        when(catalog.actionsFor(profile)).thenReturn(actions);

        List<RecoveryAction> result = useCase.execute();

        assertEquals(actions, result);
        verify(detector).detect();
        verify(catalog).actionsFor(profile);
    }
}
