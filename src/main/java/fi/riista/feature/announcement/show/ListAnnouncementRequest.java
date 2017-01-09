package fi.riista.feature.announcement.show;

import fi.riista.feature.organization.OrganisationType;

public class ListAnnouncementRequest {
    public enum Direction {
        SENT,
        RECEIVED
    }

    private OrganisationType organisationType;
    private String officialCode;
    private Direction direction;

    public OrganisationType getOrganisationType() {
        return organisationType;
    }

    public void setOrganisationType(final OrganisationType organisationType) {
        this.organisationType = organisationType;
    }

    public String getOfficialCode() {
        return officialCode;
    }

    public void setOfficialCode(final String officialCode) {
        this.officialCode = officialCode;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(final Direction direction) {
        this.direction = direction;
    }
}
