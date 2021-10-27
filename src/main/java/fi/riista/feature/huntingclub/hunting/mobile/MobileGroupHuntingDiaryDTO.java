package fi.riista.feature.huntingclub.hunting.mobile;

import io.swagger.annotations.ApiModelProperty;

import javax.annotation.Nonnull;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class MobileGroupHuntingDiaryDTO {

    @ApiModelProperty(required = true)
    private final List<MobileGroupHarvestDTO> harvests;
    @ApiModelProperty(required = true)
    private final List<MobileGroupObservationDTO> observations;
    @ApiModelProperty(required = true)
    private final List<MobileGroupHarvestDTO>  rejectedHarvests;
    @ApiModelProperty(required = true)
    private final List<MobileGroupObservationDTO>  rejectedObservations;

    public MobileGroupHuntingDiaryDTO(@Nonnull final List<MobileGroupHarvestDTO> harvests,
                                      @Nonnull final List<MobileGroupObservationDTO> observations,
                                      @Nonnull final List<MobileGroupHarvestDTO> rejectedHarvests,
                                      @Nonnull final List<MobileGroupObservationDTO> rejectedObservations) {
        this.harvests = requireNonNull(harvests);
        this.observations = requireNonNull(observations);
        this.rejectedHarvests = requireNonNull(rejectedHarvests);
        this.rejectedObservations = requireNonNull(rejectedObservations);
    }

    public List<MobileGroupHarvestDTO> getHarvests() {
        return harvests;
    }

    public List<MobileGroupObservationDTO> getObservations() {
        return observations;
    }

    public List<MobileGroupHarvestDTO> getRejectedHarvests() {
        return rejectedHarvests;
    }

    public List<MobileGroupObservationDTO> getRejectedObservations() {
        return rejectedObservations;
    }
}
