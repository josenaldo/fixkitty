package br.com.josenaldo.fixkitty.infrastructure.detectors;

import br.com.josenaldo.fixkitty.core.domain.EnvironmentProfile;
import br.com.josenaldo.fixkitty.core.ports.EnvironmentDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.nio.file.*;

/**
 * Detects the Linux environment for Ubuntu 24 systems.
 *
 * <p>Reads {@code /etc/os-release} for distro info, uses environment
 * variables for desktop and session detection, and queries systemd for
 * the audio stack.
 */
public class Ubuntu24EnvironmentDetector implements EnvironmentDetector {

    private static final Logger log = LoggerFactory.getLogger(Ubuntu24EnvironmentDetector.class);
    private static final String OS_RELEASE_PATH = "/etc/os-release";

    /**
     * Detects and returns the current environment profile.
     *
     * @return the detected profile with best-effort values; never {@code null}
     */
    @Override
    public EnvironmentProfile detect() {
        String distro = detectDistro();
        String desktop = detectDesktop();
        String audioStack = detectAudioStack();
        boolean hasGraphicalSession = detectGraphicalSession();

        log.info("Environment detected: distro={}, desktop={}, audio={}, graphical={}",
                distro, desktop, audioStack, hasGraphicalSession);

        return new EnvironmentProfile(distro, desktop, audioStack, hasGraphicalSession);
    }

    private String detectDistro() {
        try {
            for (String line : Files.readAllLines(Path.of(OS_RELEASE_PATH))) {
                if (line.startsWith("PRETTY_NAME=")) {
                    return line.substring("PRETTY_NAME=".length()).replace("\"", "").strip();
                }
            }
        } catch (IOException e) {
            log.warn("Could not read {}: {}", OS_RELEASE_PATH, e.getMessage());
        }
        return "Unknown";
    }

    private String detectDesktop() {
        String desktop = System.getenv("XDG_CURRENT_DESKTOP");
        return desktop != null ? desktop : "Unknown";
    }

    private String detectAudioStack() {
        try {
            Process p = new ProcessBuilder("systemctl", "--user", "is-active", "pipewire").start();
            boolean finished = p.waitFor(3, java.util.concurrent.TimeUnit.SECONDS);
            if (finished && p.exitValue() == 0) return "PipeWire";
        } catch (IOException | InterruptedException e) {
            log.warn("Could not detect audio stack: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
        return "Unknown";
    }

    private boolean detectGraphicalSession() {
        return System.getenv("DISPLAY") != null || System.getenv("WAYLAND_DISPLAY") != null;
    }
}
