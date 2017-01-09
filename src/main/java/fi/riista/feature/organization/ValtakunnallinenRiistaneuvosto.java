package fi.riista.feature.organization;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@Access(value = AccessType.FIELD)
@DiscriminatorValue("VRN")
public class ValtakunnallinenRiistaneuvosto extends Organisation {

    protected ValtakunnallinenRiistaneuvosto() {
        super(OrganisationType.VRN);
    }

    public ValtakunnallinenRiistaneuvosto(Riistakeskus riistakeskus, String nimiFI, String nimiSV) {
        this();
        setNameFinnish(nimiFI);
        setNameSwedish(nimiSV);
        parentOrganisation = riistakeskus;
    }
}
