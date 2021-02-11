package fi.riista.feature.huntingclub.hunting.rejection;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class AcceptClubDiaryObservationDTO implements Serializable {

    @JsonProperty("id")
    private long groupId;

    private long observationId;

    public long getObservationId() {
        return observationId;
    }

    public void setObservationId(final long observationId) {
        this.observationId = observationId;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(final long groupId) {
        this.groupId = groupId;
    }

}
