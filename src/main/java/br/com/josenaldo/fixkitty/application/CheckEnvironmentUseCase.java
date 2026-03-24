package br.com.josenaldo.fixkitty.application;

import br.com.josenaldo.fixkitty.core.domain.EnvironmentProfile;
import br.com.josenaldo.fixkitty.core.ports.EnvironmentDetector;
import com.google.inject.Inject;

/**
 * Use case that detects and returns the current Linux environment profile.
 *
 * <p>Delegates entirely to the injected {@link EnvironmentDetector}.
 * Contains no business logic beyond delegation.
 */
public class CheckEnvironmentUseCase {

    private final EnvironmentDetector detector;

    /**
     * Creates a new use case with the given environment detector.
     *
     * @param detector the detector to use for environment discovery
     */
    @Inject
    public CheckEnvironmentUseCase(EnvironmentDetector detector) {
        this.detector = detector;
    }

    /**
     * Detects and returns the current environment profile.
     *
     * @return the detected profile; never {@code null}
     */
    public EnvironmentProfile execute() {
        return detector.detect();
    }
}
