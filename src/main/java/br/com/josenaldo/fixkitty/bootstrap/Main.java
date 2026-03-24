package br.com.josenaldo.fixkitty.bootstrap;

import br.com.josenaldo.fixkitty.interfaces.gui.GuiApp;
import br.com.josenaldo.fixkitty.interfaces.tui.TuiApp;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application entry point for FixKitty.
 *
 * <p>Creates the Guice injector and delegates to the appropriate interface
 * (GUI or TUI) based on runtime arguments and environment.
 */
public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    /**
     * Application entry point.
     *
     * @param args command-line arguments; {@code --tui} selects terminal mode
     */
    public static void main(String[] args) {
        log.info("FixKitty starting...");
        InterfaceSelector selector = new InterfaceSelector();
        boolean tuiMode = selector.shouldUseTui(args);

        Injector injector = Guice.createInjector(new AppModule(tuiMode)); // used in Task 13+

        if (tuiMode) {
            log.info("Launching TUI");
            TuiApp tuiApp = injector.getInstance(TuiApp.class);
            tuiApp.start();
        } else {
            log.info("Launching GUI");
            GuiApp.injector = injector;
            javafx.application.Application.launch(GuiApp.class, args);
        }
    }
}
