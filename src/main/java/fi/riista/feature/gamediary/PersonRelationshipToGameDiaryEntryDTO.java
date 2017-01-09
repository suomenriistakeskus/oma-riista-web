package fi.riista.feature.gamediary;

import java.io.Serializable;

public class PersonRelationshipToGameDiaryEntryDTO implements Serializable {

    private final boolean isAuthor;
    private final boolean isActor;

    public PersonRelationshipToGameDiaryEntryDTO() {
        this.isAuthor = false;
        this.isActor = false;
    }

    public PersonRelationshipToGameDiaryEntryDTO(
            final boolean isAuthor,
            final boolean isActor) {
        this.isAuthor = isAuthor;
        this.isActor = isActor;
    }

    public boolean isAuthor() {
        return isAuthor;
    }

    public boolean isActor() {
        return isActor;
    }
}
