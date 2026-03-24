package br.com.josenaldo.fixkitty.interfaces.gui;

import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Relaunches the FixKitty process and terminates the current JVM.
 *
 * <p>Used after Fix GNOME Shell to bring the application back once
 * the GNOME shell has restarted. Waits 3 seconds before relaunching
 * to allow the shell to fully restart.
 */
public class AppRestarter {

    private static final Logger log = LoggerFactory.getLogger(AppRestarter.class);

    /**
     * Relaunches FixKitty in a background process and exits the current JVM.
     *
     * <p>Uses the same Java executable detected via {@link ProcessHandle}.
     * If detection fails, logs a warning and exits without relaunching.
     */
    public void relaunch() {
        ProcessHandle current = ProcessHandle.current();
        String javaCmd = current.info().command().orElse(null);

        if (javaCmd == null) {
            log.warn("Could not determine java executable path — skipping relaunch");
            Platform.exit();
            System.exit(0);
            return;
        }

        // Build relaunch command: wait 3s for GNOME shell to settle, then relaunch
        String relaunchScript = "sleep 3 && nohup " + javaCmd
            + " -jar " + getJarPath() + " &";

        try {
            new ProcessBuilder("bash", "-c", relaunchScript)
                .inheritIO()
                .start();
            log.info("Relaunch scheduled: {}", relaunchScript);
        } catch (Exception e) {
            log.error("Failed to schedule relaunch: {}", e.getMessage());
        }

        Platform.exit();
        System.exit(0);
    }

    private String getJarPath() {
        // For Phase 1 (Gradle run), the jar path is approximated
        // This will be replaced with proper jpackage path in Phase 5
        return System.getProperty("user.dir") + "/build/libs/fixkitty.jar";
    }
}
