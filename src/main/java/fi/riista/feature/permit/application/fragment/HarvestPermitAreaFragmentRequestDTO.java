package fi.riista.feature.permit.application.fragment;

import fi.riista.feature.gis.GISWGS84Point;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class HarvestPermitAreaFragmentRequestDTO {

    @NotNull
    private Long applicationId;

    @NotNull
    private Integer fragmentSizeLimit;

    @NotNull
    @Valid
    private GISWGS84Point location;

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(final Long applicationId) {
        this.applicationId = applicationId;
    }

    public Integer getFragmentSizeLimit() {
        return fragmentSizeLimit;
    }

    public void setFragmentSizeLimit(final Integer fragmentSizeLimit) {
        this.fragmentSizeLimit = fragmentSizeLimit;
    }

    public GISWGS84Point getLocation() {
        return location;
    }

    public void setLocation(final GISWGS84Point location) {
        this.location = location;
    }
}
