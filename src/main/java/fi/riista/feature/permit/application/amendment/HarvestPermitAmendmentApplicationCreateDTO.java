package fi.riista.feature.permit.application.amendment;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

public class HarvestPermitAmendmentApplicationCreateDTO {

    @NotNull
    private Long originalPermitId;

    @Nullable
    private Long nonEdibleHarvestId;

    @Nullable
    private Integer gameSpeciesCode;

    @NotNull
    public Long getOriginalPermitId() {
        return originalPermitId;
    }

    public void setOriginalPermitId(@NotNull final Long originalPermitId) {
        this.originalPermitId = originalPermitId;
    }

    @Nullable
    public Long getNonEdibleHarvestId() {
        return nonEdibleHarvestId;
    }

    public void setNonEdibleHarvestId(@Nullable final Long nonEdibleHarvestId) {
        this.nonEdibleHarvestId = nonEdibleHarvestId;
    }

    @Nullable
    public Integer getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    public void setGameSpeciesCode(@Nullable final Integer gameSpeciesCode) {
        this.gameSpeciesCode = gameSpeciesCode;
    }
}
