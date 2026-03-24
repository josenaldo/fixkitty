package br.com.josenaldo.fixkitty.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Determines which interface to launch based on runtime context.
 *
 * <p>Selects TUI when the {@code --tui} argument is present or when no
 * graphical session is detected. Falls back to TUI if GUI launch fails.
 */
public class InterfaceSelector {

    private static final Logger log = LoggerFactory.getLogger(InterfaceSelector.class);

    /**
     * Returns {@code true} if the application should run in TUI mode.
     *
     * @param args command-line arguments passed to the application
     * @return {@code true} for TUI, {@code false} for GUI
     */
    public boolean shouldUseTui(String[] args) {
        if (args == null) {
            args = new String[0];
        }
        for (String arg : args) {
            if ("--tui".equals(arg)) {
                log.info("TUI mode selected via --tui argument");
                return true;
            }
        }
        boolean hasDisplay = System.getenv("DISPLAY") != null
                || System.getenv("WAYLAND_DISPLAY") != null;
        if (!hasDisplay) {
            log.info("No graphical session detected, falling back to TUI");
            return true;
        }
        return false;
    }
}
