package br.com.josenaldo.fixkitty.core.ports;

import br.com.josenaldo.fixkitty.core.domain.EnvironmentProfile;

/**
 * Port for detecting the current Linux desktop environment.
 *
 * <p>Implementations read OS-specific signals (environment variables,
 * config files, running services) to populate an {@link EnvironmentProfile}.
 */
public interface EnvironmentDetector {

    /**
     * Detects and returns a description of the current environment.
     *
     * @return the detected profile; never {@code null}
     */
    EnvironmentProfile detect();
}
