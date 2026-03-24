package br.com.josenaldo.fixkitty.core.domain;

/**
 * Describes the detected Linux desktop environment.
 *
 * <p>Used by {@code ActionCatalog} to select the correct set of commands
 * for the current system. Immutable value object.
 *
 * @param distro              the Linux distribution name and version (e.g. "Ubuntu 24.04")
 * @param desktop             the desktop environment name (e.g. "GNOME")
 * @param audioStack          the detected audio subsystem (e.g. "PipeWire")
 * @param hasGraphicalSession {@code true} if a graphical display session is active
 */
public record EnvironmentProfile(
        String distro,
        String desktop,
        String audioStack,
        boolean hasGraphicalSession) {
}
