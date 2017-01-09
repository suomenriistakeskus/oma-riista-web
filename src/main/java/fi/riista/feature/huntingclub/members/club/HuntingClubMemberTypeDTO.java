package fi.riista.feature.huntingclub.members.club;

import fi.riista.feature.organization.occupation.OccupationType;

import javax.validation.constraints.NotNull;

public class HuntingClubMemberTypeDTO {
    @NotNull
    private Long id;

    private Long clubId;

    @NotNull
    private OccupationType occupationType;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getClubId() {
        return clubId;
    }

    public void setClubId(final Long clubId) {
        this.clubId = clubId;
    }

    public OccupationType getOccupationType() {
        return occupationType;
    }

    public void setOccupationType(final OccupationType occupationType) {
        this.occupationType = occupationType;
    }
}
