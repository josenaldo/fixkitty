package br.com.josenaldo.fixkitty.infrastructure.catalog;

import br.com.josenaldo.fixkitty.core.domain.*;
import br.com.josenaldo.fixkitty.core.ports.ActionCatalog;
import java.util.Arrays;
import java.util.List;

/**
 * Action catalog for Ubuntu 24 with GNOME, PipeWire, and NetworkManager.
 *
 * <p>Provides concrete {@link ExecutionStep} definitions for all six
 * recovery actions. {@link RecoveryAction#FIX_ALL} creates independent
 * steps with {@link FailurePolicy#CONTINUE}, overriding the individual
 * action policies to ensure all subsystems are attempted.
 */
public class Ubuntu24ActionCatalog implements ActionCatalog {

    /**
     * Returns all available {@link RecoveryAction} values for the given profile.
     *
     * @param profile the current environment profile (ignored; all actions are supported)
     * @return list of all recovery actions
     */
    @Override
    public List<RecoveryAction> actionsFor(EnvironmentProfile profile) {
        return Arrays.asList(RecoveryAction.values());
    }

    /**
     * Returns the execution plan (list of {@link ExecutionStep}) for the given action and profile.
     *
     * @param action  the recovery action to plan
     * @param profile the current environment profile
     * @return ordered list of steps to execute
     */
    @Override
    public List<ExecutionStep> planFor(RecoveryAction action, EnvironmentProfile profile) {
        return switch (action) {
            case FIX_AUDIO -> List.of(
                step("audio-pipewire", "Restart PipeWire",
                    new String[]{"systemctl", "--user", "restart", "pipewire"},
                    false, 10, FailurePolicy.CONTINUE),
                step("audio-wireplumber", "Restart WirePlumber",
                    new String[]{"systemctl", "--user", "restart", "wireplumber"},
                    false, 10, FailurePolicy.CONTINUE)
            );
            case FIX_BLUETOOTH -> List.of(
                step("bt-restart", "Restart Bluetooth service",
                    new String[]{"systemctl", "restart", "bluetooth"},
                    true, 15, FailurePolicy.ABORT)
            );
            case FIX_NETWORK -> List.of(
                step("net-restart", "Restart NetworkManager",
                    new String[]{"systemctl", "restart", "NetworkManager"},
                    true, 20, FailurePolicy.ABORT)
            );
            case FIX_GNOME_SHELL -> List.of(
                step("gnome-killall", "Restart GNOME Shell",
                    new String[]{"killall", "gnome-shell"},
                    false, 10, FailurePolicy.ABORT)
            );
            case FIX_ALL -> List.of(
                // All steps use CONTINUE — FIX_ALL should attempt all subsystems
                step("all-audio-pipewire", "Restart PipeWire",
                    new String[]{"systemctl", "--user", "restart", "pipewire"},
                    false, 10, FailurePolicy.CONTINUE),
                step("all-audio-wireplumber", "Restart WirePlumber",
                    new String[]{"systemctl", "--user", "restart", "wireplumber"},
                    false, 10, FailurePolicy.CONTINUE),
                step("all-bt-restart", "Restart Bluetooth service",
                    new String[]{"systemctl", "restart", "bluetooth"},
                    true, 15, FailurePolicy.CONTINUE),
                step("all-net-restart", "Restart NetworkManager",
                    new String[]{"systemctl", "restart", "NetworkManager"},
                    true, 20, FailurePolicy.CONTINUE),
                step("all-gnome-killall", "Restart GNOME Shell",
                    new String[]{"killall", "gnome-shell"},
                    false, 10, FailurePolicy.CONTINUE)
            );
            case CHECK_ENVIRONMENT -> List.of();
        };
    }

    /**
     * Returns a default post-action recommendation message for the given action,
     * or {@code null} for {@link RecoveryAction#CHECK_ENVIRONMENT}.
     *
     * @param action the recovery action
     * @return human-readable recommendation string, or {@code null}
     */
    @Override
    public String defaultRecommendationFor(RecoveryAction action) {
        return switch (action) {
            case FIX_AUDIO -> "Try logging out and logging back in if audio is still broken.";
            case FIX_BLUETOOTH -> "Try toggling Bluetooth off and on in system settings.";
            case FIX_NETWORK -> "Try disconnecting and reconnecting to your network.";
            case FIX_GNOME_SHELL -> "If the issue persists, try a full logout and login.";
            case FIX_ALL -> "If issues persist, consider a full logout or system restart.";
            case CHECK_ENVIRONMENT -> null;
        };
    }

    private ExecutionStep step(String id, String desc, String[] cmd,
                               boolean priv, int timeout, FailurePolicy policy) {
        return new ExecutionStep(id, desc, cmd, priv, timeout, policy);
    }
}
