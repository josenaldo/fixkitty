package br.com.josenaldo.fixkitty.infrastructure.privilege;

import br.com.josenaldo.fixkitty.core.ports.PrivilegeManager;

/**
 * Privilege manager that wraps commands with {@code pkexec} (polkit).
 *
 * <p>Used in the GUI to display a native graphical authentication dialog.
 */
public class PkexecPrivilegeManager implements PrivilegeManager {

    /**
     * Prepends {@code "pkexec"} to the given command array.
     *
     * @param command the original command array; not modified
     * @return a new array with {@code "pkexec"} as the first element
     */
    @Override
    public String[] escalate(String[] command) {
        String[] escalated = new String[command.length + 1];
        escalated[0] = "pkexec";
        System.arraycopy(command, 0, escalated, 1, command.length);
        return escalated;
    }
}
