package fi.riista.feature.huntingclub.copy;

import javax.validation.constraints.NotNull;

public class HuntingClubGroupCopyDTO {

    @NotNull
    private Long huntingAreaId;

    @NotNull
    private Integer huntingYear;

    public Long getHuntingAreaId() {
        return huntingAreaId;
    }

    public void setHuntingAreaId(Long huntingAreaId) {
        this.huntingAreaId = huntingAreaId;
    }

    public Integer getHuntingYear() {
        return huntingYear;
    }

    public void setHuntingYear(Integer huntingYear) {
        this.huntingYear = huntingYear;
    }
}
