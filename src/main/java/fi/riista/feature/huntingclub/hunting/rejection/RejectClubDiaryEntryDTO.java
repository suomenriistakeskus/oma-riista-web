package fi.riista.feature.huntingclub.hunting.rejection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import fi.riista.feature.gamediary.GameDiaryEntryType;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class RejectClubDiaryEntryDTO implements Serializable {
    public static RejectClubDiaryEntryDTO createForHarvest(long entryId, long groupId) {
        final RejectClubDiaryEntryDTO dto = new RejectClubDiaryEntryDTO();
        dto.setType(GameDiaryEntryType.HARVEST);
        dto.setEntryId(entryId);
        dto.setGroupId(groupId);
        return dto;
    }

    public static RejectClubDiaryEntryDTO createForObservation(long entryId, long groupId) {
        final RejectClubDiaryEntryDTO dto = new RejectClubDiaryEntryDTO();
        dto.setType(GameDiaryEntryType.OBSERVATION);
        dto.setEntryId(entryId);
        dto.setGroupId(groupId);
        return dto;
    }

    @JsonIgnore
    private Long clubId;

    @JsonProperty("id")
    private Long groupId;

    @NotNull
    private GameDiaryEntryType type;

    @NotNull
    private Long entryId;

    public Long getEntryId() {
        return entryId;
    }

    public void setEntryId(final Long entryId) {
        this.entryId = entryId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(final Long groupId) {
        this.groupId = groupId;
    }

    public GameDiaryEntryType getType() {
        return type;
    }

    public void setType(final GameDiaryEntryType type) {
        this.type = type;
    }
}
