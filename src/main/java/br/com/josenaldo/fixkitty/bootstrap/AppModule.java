package br.com.josenaldo.fixkitty.bootstrap;

import com.google.inject.AbstractModule;

/**
 * Guice dependency injection module for FixKitty.
 *
 * <p>Binds all interface implementations to their port abstractions.
 * This is the single wiring point for the entire application.
 */
public class AppModule extends AbstractModule {

    /** Whether the application is running in TUI mode. */
    private final boolean tuiMode;

    /**
     * Creates an AppModule for the specified interface mode.
     *
     * @param tuiMode {@code true} to configure TUI bindings; {@code false} for GUI
     */
    public AppModule(boolean tuiMode) {
        this.tuiMode = tuiMode;
    }

    @Override
    protected void configure() {
        // Bindings will be added as infrastructure classes are implemented
    }
}
