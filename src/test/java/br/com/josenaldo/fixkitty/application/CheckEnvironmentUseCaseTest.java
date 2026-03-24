package br.com.josenaldo.fixkitty.application;

import br.com.josenaldo.fixkitty.core.domain.EnvironmentProfile;
import br.com.josenaldo.fixkitty.core.ports.EnvironmentDetector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckEnvironmentUseCaseTest {

    @Mock
    EnvironmentDetector detector;

    @InjectMocks
    CheckEnvironmentUseCase useCase;

    @Test
    void execute_delegatesToDetector_returnsProfile() {
        EnvironmentProfile profile = new EnvironmentProfile("Ubuntu 24.04", "GNOME", "PipeWire", true);
        when(detector.detect()).thenReturn(profile);

        EnvironmentProfile result = useCase.execute();

        assertSame(profile, result);
        verify(detector).detect();
    }
}
