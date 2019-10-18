package fi.riista.feature.account.todo;

import java.util.Set;

public class AccountPermitTodoCountDTO {

    private final Set<Long> permitIds;

    public AccountPermitTodoCountDTO(final Set<Long> permitIds) {
        this.permitIds = permitIds;
    }

    public Set<Long> getPermitIds() {
        return permitIds;
    }
}
