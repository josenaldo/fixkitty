package br.com.josenaldo.fixkitty.infrastructure.privilege;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class PkexecPrivilegeManagerTest {

    PkexecPrivilegeManager manager = new PkexecPrivilegeManager();

    @Test
    void escalate_prependsPkexec() {
        String[] result = manager.escalate(new String[]{"systemctl", "restart", "NetworkManager"});
        assertThat(result).containsExactly("pkexec", "systemctl", "restart", "NetworkManager");
    }

    @Test
    void escalate_doesNotMutateInput() {
        String[] input = {"cmd"};
        manager.escalate(input);
        assertThat(input).containsExactly("cmd");
    }
}
