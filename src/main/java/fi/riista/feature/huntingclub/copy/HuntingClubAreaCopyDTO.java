package fi.riista.feature.huntingclub.copy;

import javax.validation.constraints.NotNull;

public class HuntingClubAreaCopyDTO {

    @NotNull
    private Long id;

    @NotNull
    private Integer huntingYear;

    @NotNull
    private Boolean copyGroups;

    public boolean isCopyGroups() {
        return copyGroups != null && copyGroups;
    }

    public HuntingClubAreaCopyDTO() {
    }

    public HuntingClubAreaCopyDTO(long id, int huntingYear, boolean copyGroups) {
        this.id = id;
        this.huntingYear = huntingYear;
        this.copyGroups = copyGroups;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getHuntingYear() {
        return huntingYear;
    }

    public void setHuntingYear(Integer huntingYear) {
        this.huntingYear = huntingYear;
    }

    public Boolean getCopyGroups() {
        return copyGroups;
    }

    public void setCopyGroups(Boolean copyGroups) {
        this.copyGroups = copyGroups;
    }
}
