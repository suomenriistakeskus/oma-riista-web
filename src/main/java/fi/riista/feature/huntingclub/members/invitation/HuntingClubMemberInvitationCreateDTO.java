package fi.riista.feature.huntingclub.members.invitation;

import java.util.Set;

public class HuntingClubMemberInvitationCreateDTO {

    private Long groupId;
    private Set<String> hunterNumbers;

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Set<String> getHunterNumbers() {
        return hunterNumbers;
    }

    public void setHunterNumbers(Set<String> hunterNumbers) {
        this.hunterNumbers = hunterNumbers;
    }
}
