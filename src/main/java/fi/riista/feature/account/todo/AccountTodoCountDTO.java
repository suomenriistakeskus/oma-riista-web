package fi.riista.feature.account.todo;

import java.util.Set;

public class AccountTodoCountDTO {
    private Set<Long> permitIds;
    private long invitations;

    public AccountTodoCountDTO(Set<Long> permitIds, long invitations) {
        this.permitIds = permitIds;
        this.invitations = invitations;
    }

    public Set<Long> getPermitIds() {
        return permitIds;
    }

    public long getInvitations() {
        return invitations;
    }
}

