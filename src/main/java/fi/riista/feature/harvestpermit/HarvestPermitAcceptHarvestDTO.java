package fi.riista.feature.harvestpermit;

import fi.riista.feature.gamediary.harvest.Harvest;

import javax.validation.constraints.NotNull;

public class HarvestPermitAcceptHarvestDTO {
    @NotNull
    private Long harvestId;

    @NotNull
    private Integer harvestRev;

    @NotNull
    private Harvest.StateAcceptedToHarvestPermit toState;

    public Long getHarvestId() {
        return harvestId;
    }

    public void setHarvestId(final Long harvestId) {
        this.harvestId = harvestId;
    }

    public Integer getHarvestRev() {
        return harvestRev;
    }

    public void setHarvestRev(final Integer harvestRev) {
        this.harvestRev = harvestRev;
    }

    public Harvest.StateAcceptedToHarvestPermit getToState() {
        return toState;
    }

    public void setToState(final Harvest.StateAcceptedToHarvestPermit toState) {
        this.toState = toState;
    }
}
