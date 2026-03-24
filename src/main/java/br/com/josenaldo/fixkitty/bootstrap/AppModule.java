package br.com.josenaldo.fixkitty.bootstrap;

import br.com.josenaldo.fixkitty.core.ports.*;
import br.com.josenaldo.fixkitty.infrastructure.catalog.Ubuntu24ActionCatalog;
import br.com.josenaldo.fixkitty.infrastructure.command.ProcessBuilderCommandRunner;
import br.com.josenaldo.fixkitty.infrastructure.detectors.Ubuntu24EnvironmentDetector;
import br.com.josenaldo.fixkitty.infrastructure.privilege.*;
import com.google.inject.AbstractModule;

/**
 * Guice dependency injection module for FixKitty.
 *
 * <p>Binds all port interfaces to their Ubuntu 24 implementations.
 * Selects the appropriate {@link PrivilegeManager} based on the active interface.
 */
public class AppModule extends AbstractModule {

    private final boolean tuiMode;

    /**
     * Creates an AppModule for the specified interface mode.
     *
     * @param tuiMode {@code true} to configure TUI bindings (sudo); {@code false} for GUI (pkexec)
     */
    public AppModule(boolean tuiMode) {
        this.tuiMode = tuiMode;
    }

    @Override
    protected void configure() {
        bind(CommandRunner.class).to(ProcessBuilderCommandRunner.class);
        bind(EnvironmentDetector.class).to(Ubuntu24EnvironmentDetector.class);
        bind(ActionCatalog.class).to(Ubuntu24ActionCatalog.class);

        if (tuiMode) {
            bind(PrivilegeManager.class).to(SudoPrivilegeManager.class);
        } else {
            bind(PrivilegeManager.class).to(PkexecPrivilegeManager.class);
        }
    }
}
