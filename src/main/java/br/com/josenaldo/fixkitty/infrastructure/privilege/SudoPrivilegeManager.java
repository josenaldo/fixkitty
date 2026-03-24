package br.com.josenaldo.fixkitty.infrastructure.privilege;

import br.com.josenaldo.fixkitty.core.ports.PrivilegeManager;

/**
 * Privilege manager that prepends {@code sudo} to the command array.
 *
 * <p>Used in the TUI, where the password is entered inline in the terminal.
 */
public class SudoPrivilegeManager implements PrivilegeManager {

    /**
     * Prepends {@code "sudo"} to the given command array.
     *
     * @param command the original command array; not modified
     * @return a new array with {@code "sudo"} as the first element
     */
    @Override
    public String[] escalate(String[] command) {
        String[] escalated = new String[command.length + 1];
        escalated[0] = "sudo";
        System.arraycopy(command, 0, escalated, 1, command.length);
        return escalated;
    }
}
