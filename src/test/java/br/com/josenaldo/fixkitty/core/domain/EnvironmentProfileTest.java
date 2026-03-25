package br.com.josenaldo.fixkitty.core.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class EnvironmentProfileTest {

    @Test
    void constructor_storesAllFields() {
        EnvironmentProfile profile = new EnvironmentProfile("Ubuntu 24.04", "GNOME", "PipeWire", true);

        assertThat(profile.distro()).isEqualTo("Ubuntu 24.04");
        assertThat(profile.desktop()).isEqualTo("GNOME");
        assertThat(profile.audioStack()).isEqualTo("PipeWire");
        assertThat(profile.hasGraphicalSession()).isTrue();
    }

    @Test
    void hasGraphicalSession_false_whenNoGraphicalSession() {
        EnvironmentProfile profile = new EnvironmentProfile("Ubuntu 24.04", "None", "PulseAudio", false);

        assertThat(profile.hasGraphicalSession()).isFalse();
    }
}
