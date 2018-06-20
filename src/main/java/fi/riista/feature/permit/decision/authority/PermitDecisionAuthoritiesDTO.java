package fi.riista.feature.permit.decision.authority;

import javax.validation.Valid;

public class PermitDecisionAuthoritiesDTO {
    private long id;

    @Valid
    private PermitDecisionAuthorityDTO presenter;

    @Valid
    private PermitDecisionAuthorityDTO presenter2;

    @Valid
    private PermitDecisionAuthorityDTO decisionMaker;

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public PermitDecisionAuthorityDTO getPresenter() {
        return presenter;
    }

    public void setPresenter(final PermitDecisionAuthorityDTO presenter) {
        this.presenter = presenter;
    }

    public PermitDecisionAuthorityDTO getPresenter2() {
        return presenter2;
    }

    public void setPresenter2(final PermitDecisionAuthorityDTO presenter2) {
        this.presenter2 = presenter2;
    }

    public PermitDecisionAuthorityDTO getDecisionMaker() {
        return decisionMaker;
    }

    public void setDecisionMaker(final PermitDecisionAuthorityDTO decisionMaker) {
        this.decisionMaker = decisionMaker;
    }
}
