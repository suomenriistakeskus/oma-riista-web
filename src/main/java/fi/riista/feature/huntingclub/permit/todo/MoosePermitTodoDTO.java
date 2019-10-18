package fi.riista.feature.huntingclub.permit.todo;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class MoosePermitTodoDTO {
    @JsonIgnore
    private final long clubId;
    private final boolean areaMissing;
    private final boolean groupMissing;
    private final boolean groupPermitMissing;
    private final boolean groupLeaderMissing;

    public MoosePermitTodoDTO(final long clubId,
                              final boolean areaMissing,
                              final boolean groupMissing,
                              final boolean groupPermitMissing,
                              final boolean groupLeaderMissing) {

        this.clubId = clubId;
        this.areaMissing = areaMissing;
        this.groupMissing = groupMissing;
        this.groupPermitMissing = groupPermitMissing;
        this.groupLeaderMissing = groupLeaderMissing;
    }

    public long getClubId() {
        return clubId;
    }

    public boolean isAreaMissing() {
        return areaMissing;
    }

    public boolean isGroupMissing() {
        return groupMissing;
    }

    public boolean isGroupPermitMissing() {
        return groupPermitMissing;
    }

    public boolean isGroupLeaderMissing() {
        return groupLeaderMissing;
    }

    @JsonGetter
    public boolean isTodo() {
        return areaMissing || groupMissing || groupPermitMissing || groupLeaderMissing;
    }

    public static MoosePermitTodoDTO noTodo(final long clubId) {
        return new MoosePermitTodoDTO(clubId, false, false, false, false);
    }
}
