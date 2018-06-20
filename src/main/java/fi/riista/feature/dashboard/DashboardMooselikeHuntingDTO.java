package fi.riista.feature.dashboard;

public class DashboardMooselikeHuntingDTO {

    private Long speciesId;
    private String speciesName;
    private int harvestCount;
    private int observationCount;
    private int openPermitCount;
    private int closedPermitCount;
    private int moderatorClosedPermitCount;
    private Integer mooseDataCardGroupCount;

    public Long getSpeciesId() {
        return speciesId;
    }

    public void setSpeciesId(final Long speciesId) {
        this.speciesId = speciesId;
    }

    public String getSpeciesName() {
        return speciesName;
    }

    public void setSpeciesName(final String speciesName) {
        this.speciesName = speciesName;
    }

    public int getHarvestCount() {
        return harvestCount;
    }

    public void setHarvestCount(final int harvestCount) {
        this.harvestCount = harvestCount;
    }

    public int getObservationCount() {
        return observationCount;
    }

    public void setObservationCount(final int observationCount) {
        this.observationCount = observationCount;
    }

    public int getOpenPermitCount() {
        return openPermitCount;
    }

    public void setOpenPermitCount(final int openPermitCount) {
        this.openPermitCount = openPermitCount;
    }

    public int getClosedPermitCount() {
        return closedPermitCount;
    }

    public void setClosedPermitCount(final int closedPermitCount) {
        this.closedPermitCount = closedPermitCount;
    }

    public int getModeratorClosedPermitCount() {
        return moderatorClosedPermitCount;
    }

    public void setModeratorClosedPermitCount(final int moderatorClosedPermitCount) {
        this.moderatorClosedPermitCount = moderatorClosedPermitCount;
    }

    public Integer getMooseDataCardGroupCount() {
        return mooseDataCardGroupCount;
    }

    public void setMooseDataCardGroupCount(final Integer mooseDataCardGroupCount) {
        this.mooseDataCardGroupCount = mooseDataCardGroupCount;
    }

}
