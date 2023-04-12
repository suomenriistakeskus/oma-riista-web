package fi.riista.feature.huntingclub.copy;

import javax.validation.constraints.NotNull;

public class HuntingClubAreaCopyDTO {

    @NotNull
    private Long id;

    @NotNull
    private Integer huntingYear;

    @NotNull
    private Boolean copyGroups;

    @NotNull
    private Boolean copyPOIs;

    public boolean isCopyGroups() {
        return copyGroups != null && copyGroups;
    }

    public HuntingClubAreaCopyDTO() {
    }

    public HuntingClubAreaCopyDTO(final long id, final int huntingYear, final boolean copyGroups, final boolean copyPOIs) {
        this.id = id;
        this.huntingYear = huntingYear;
        this.copyGroups = copyGroups;
        this.copyPOIs = copyPOIs;
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

    public Boolean isCopyPOIs() {
        return copyPOIs != null && copyPOIs;
    }

    public void setCopyPOIs(final Boolean copyPOIs) {
        this.copyPOIs = copyPOIs;
    }
}
