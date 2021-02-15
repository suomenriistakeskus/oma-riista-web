package fi.riista.feature.common.decision.authority;

import javax.validation.Valid;

public class DecisionAuthoritiesDTO {
    private long id;

    @Valid
    private DecisionAuthorityDTO presenter;

    @Valid
    private DecisionAuthorityDTO presenter2;

    @Valid
    private DecisionAuthorityDTO decisionMaker;

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public DecisionAuthorityDTO getPresenter() {
        return presenter;
    }

    public void setPresenter(final DecisionAuthorityDTO presenter) {
        this.presenter = presenter;
    }

    public DecisionAuthorityDTO getPresenter2() {
        return presenter2;
    }

    public void setPresenter2(final DecisionAuthorityDTO presenter2) {
        this.presenter2 = presenter2;
    }

    public DecisionAuthorityDTO getDecisionMaker() {
        return decisionMaker;
    }

    public void setDecisionMaker(final DecisionAuthorityDTO decisionMaker) {
        this.decisionMaker = decisionMaker;
    }
}
