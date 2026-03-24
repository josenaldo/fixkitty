package br.com.josenaldo.fixkitty.core.ports;

import br.com.josenaldo.fixkitty.core.domain.EnvironmentProfile;
import br.com.josenaldo.fixkitty.core.domain.ExecutionStep;
import br.com.josenaldo.fixkitty.core.domain.RecoveryAction;
import java.util.List;

/**
 * Port for retrieving recovery action definitions for a given environment.
 *
 * <p>Decouples the business logic from the specific commands used on each
 * Linux distribution. Implementations supply the concrete command arrays,
 * privilege requirements, and default recommendations.
 */
public interface ActionCatalog {

    /**
     * Returns the recovery actions available for the given environment.
     *
     * @param profile the detected environment profile
     * @return an ordered list of available actions; never {@code null}
     */
    List<RecoveryAction> actionsFor(EnvironmentProfile profile);

    /**
     * Returns the ordered list of execution steps for the given action.
     *
     * <p>For {@link RecoveryAction#CHECK_ENVIRONMENT}, returns an empty list.
     *
     * @param action  the recovery action to look up
     * @param profile the environment profile to tailor steps for
     * @return the execution steps; never {@code null}, may be empty
     */
    List<ExecutionStep> planFor(RecoveryAction action, EnvironmentProfile profile);

    /**
     * Returns a human-readable recommendation to display on partial or failed results.
     *
     * @param action the action that produced a non-success result
     * @return the recommendation text, or {@code null} if not applicable
     */
    String defaultRecommendationFor(RecoveryAction action);
}
