package fi.riista.feature.pub.occupation;

import com.google.common.base.MoreObjects;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.occupation.OccupationGroupType;

public class PublicOccupationGroupTypeDTO {

    private final String name;
    private final OccupationGroupType occupationType;
    private final OrganisationType organisationType;

    public PublicOccupationGroupTypeDTO(String name, OccupationGroupType occupationType, OrganisationType organisationType) {
        this.name = name;
        this.occupationType = occupationType;
        this.organisationType = organisationType;
    }

    public String getName() {
        return name;
    }

    public OccupationGroupType getOccupationType() {
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
