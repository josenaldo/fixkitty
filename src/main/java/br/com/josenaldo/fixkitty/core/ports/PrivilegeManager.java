package br.com.josenaldo.fixkitty.core.ports;

/**
 * Port for privilege escalation of OS commands.
 *
 * <p>Transforms a plain command array into one that includes the
 * escalation mechanism (e.g. sudo, pkexec). The exact mechanism
 * depends on the active interface (TUI vs GUI).
 */
public interface PrivilegeManager {

    /**
     * Wraps the given command array with privilege escalation.
     *
     * @param command the original command array to escalate
     * @return a new array with the escalation prefix prepended
     */
    String[] escalate(String[] command);
}
