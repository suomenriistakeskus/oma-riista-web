package fi.riista.feature.pub.occupation;

import com.google.common.base.MoreObjects;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.OrganisationType;

public class PublicOccupationTypeDTO {

    private final String name;
    private final OccupationType occupationType;
    private final OrganisationType organisationType;

    public PublicOccupationTypeDTO(String name, OccupationType occupationType, OrganisationType organisationType) {
        this.name = name;
        this.occupationType = occupationType;
        this.organisationType = organisationType;
    }

    public String getName() {
        return name;
    }

    public OccupationType getOccupationType() {
        return occupationType;
    }

    public OrganisationType getOrganisationType() {
        return organisationType;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name)
                .add("occupationType", occupationType)
                .add("organisationType", organisationType)
                .toString();
    }
}
