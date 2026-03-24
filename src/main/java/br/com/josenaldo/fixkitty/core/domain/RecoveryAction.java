package br.com.josenaldo.fixkitty.core.domain;

/**
 * Enumerates all recovery actions supported by FixKitty.
 *
 * <p>Each constant represents a subsystem recovery operation. The
 * {@link #CHECK_ENVIRONMENT} action is included for unified menu routing
 * and produces an empty execution plan.
 */
public enum RecoveryAction {

    /** Restarts the PipeWire audio server and WirePlumber session manager. */
    FIX_AUDIO("Fix Audio"),

    /** Restarts the system Bluetooth service. */
    FIX_BLUETOOTH("Fix Bluetooth"),

    /** Restarts the NetworkManager service. */
    FIX_NETWORK("Fix Network"),

    /** Restarts the GNOME shell process. */
    FIX_GNOME_SHELL("Fix GNOME Shell"),

    /** Executes all fix actions in sequence. */
    FIX_ALL("Fix All"),

    /** Detects and displays the current environment profile. */
    CHECK_ENVIRONMENT("Check Environment");

    private final String displayName;

    RecoveryAction(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the human-readable label for this action.
     *
     * @return the display name, never {@code null} or blank
     */
    public String displayName() {
        return displayName;
    }
}
