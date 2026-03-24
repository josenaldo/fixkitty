package br.com.josenaldo.fixkitty.application;

import br.com.josenaldo.fixkitty.core.domain.RecoveryAction;
import br.com.josenaldo.fixkitty.core.ports.ActionCatalog;
import br.com.josenaldo.fixkitty.core.ports.EnvironmentDetector;
import com.google.inject.Inject;
import java.util.List;

/**
 * Use case that returns the recovery actions available in the current environment.
 *
 * <p>Detects the environment first, then queries the action catalog for
 * the matching set of supported actions.
 */
public class ListActionsUseCase {

    private final EnvironmentDetector detector;
    private final ActionCatalog catalog;

    /**
     * Creates a new use case with the required dependencies.
     *
     * @param detector the detector used to identify the current environment
     * @param catalog  the catalog used to look up available actions
     */
    @Inject
    public ListActionsUseCase(EnvironmentDetector detector, ActionCatalog catalog) {
        this.detector = detector;
        this.catalog = catalog;
    }

    /**
     * Returns the list of recovery actions supported in the current environment.
     *
     * @return ordered list of available actions; never {@code null}
     */
    public List<RecoveryAction> execute() {
        return catalog.actionsFor(detector.detect());
    }
}
