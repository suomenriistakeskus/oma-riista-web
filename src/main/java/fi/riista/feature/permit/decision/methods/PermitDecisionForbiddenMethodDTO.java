package fi.riista.feature.permit.decision.methods;

import java.util.HashSet;
import java.util.Set;

public class PermitDecisionForbiddenMethodDTO {

    private Set<ForbiddenMethodType> forbiddenMethodTypes = new HashSet<>();

    public PermitDecisionForbiddenMethodDTO() {
    }

    public PermitDecisionForbiddenMethodDTO(final Set<ForbiddenMethodType> forbiddenMethodTypes) {
        this.forbiddenMethodTypes = forbiddenMethodTypes;
    }

    public Set<ForbiddenMethodType> getForbiddenMethodTypes() {
        return forbiddenMethodTypes;
    }

    public void setForbiddenMethodTypes(final Set<ForbiddenMethodType> forbiddenMethodTypes) {
        this.forbiddenMethodTypes = forbiddenMethodTypes;
    }
}
