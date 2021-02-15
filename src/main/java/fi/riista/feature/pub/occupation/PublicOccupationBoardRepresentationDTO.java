package fi.riista.feature.pub.occupation;

import com.google.common.base.MoreObjects;
import fi.riista.feature.organization.occupation.OccupationBoardRepresentationRole;

public class PublicOccupationBoardRepresentationDTO {
    
    private final String name;
    private final OccupationBoardRepresentationRole boardRepresentationRole;

    public PublicOccupationBoardRepresentationDTO(final String name,
                                                  final OccupationBoardRepresentationRole boardRepresentationRole) {
        this.name = name;
        this.boardRepresentationRole = boardRepresentationRole;
    }

    public String getName() {
        return name;
    }

    public OccupationBoardRepresentationRole getBoardRepresentationRole() {
        return boardRepresentationRole;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name)
                .add("boardRepresentationRole", boardRepresentationRole)
                .toString();
    }
}
