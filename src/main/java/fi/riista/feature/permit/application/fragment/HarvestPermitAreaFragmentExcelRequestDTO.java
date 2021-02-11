package fi.riista.feature.permit.application.fragment;

import javax.validation.constraints.NotNull;
import java.util.List;

public class HarvestPermitAreaFragmentExcelRequestDTO {

    @NotNull
    private Integer fragmentSizeLimitSquareMeters;

    // Geohashes for polygon centroids
    @NotNull
    private List<String> fragmentIds;

    public Integer getFragmentSizeLimitSquareMeters() {
        return fragmentSizeLimitSquareMeters;
    }

    public void setFragmentSizeLimitSquareMeters(final Integer fragmentSizeLimit) {
        this.fragmentSizeLimitSquareMeters = fragmentSizeLimit;
    }

    public List<String> getFragmentIds() {
        return fragmentIds;
    }

    public void setFragmentIds(final List<String> fragmentIds) {
        this.fragmentIds = fragmentIds;
    }
}
