package br.com.josenaldo.fixkitty.infrastructure.privilege;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class SudoPrivilegeManagerTest {

    SudoPrivilegeManager manager = new SudoPrivilegeManager();

    @Test
    void escalate_prependsSudo() {
        String[] result = manager.escalate(new String[]{"systemctl", "restart", "bluetooth"});
        assertThat(result).containsExactly("sudo", "systemctl", "restart", "bluetooth");
    }

    @Test
    void escalate_doesNotMutateInput() {
        String[] input = {"cmd"};
        manager.escalate(input);
        assertThat(input).containsExactly("cmd");
    }
}
