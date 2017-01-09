package fi.riista.feature.huntingclub.members.group;

import fi.riista.feature.organization.occupation.OccupationType;

import javax.validation.constraints.NotNull;

public class GroupMemberTypeDTO {
    @NotNull
    private Long id;

    @NotNull
    private Long groupId;

    @NotNull
    private OccupationType occupationType;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(final Long groupId) {
        this.groupId = groupId;
    }

    public OccupationType getOccupationType() {
        return occupationType;
    }

    public void setOccupationType(final OccupationType occupationType) {
        this.occupationType = occupationType;
    }
}
