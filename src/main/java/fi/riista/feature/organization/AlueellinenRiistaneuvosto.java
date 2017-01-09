package fi.riista.feature.organization;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@Access(value = AccessType.FIELD)
@DiscriminatorValue("ARN")
public class AlueellinenRiistaneuvosto extends Organisation {

    protected AlueellinenRiistaneuvosto() {
        super(OrganisationType.ARN);
    }

    public AlueellinenRiistaneuvosto(RiistakeskuksenAlue riistakeskuksenAlue, String nimiFI, String nimiSV) {
        this();
        setNameFinnish(nimiFI);
        setNameSwedish(nimiSV);
        setOfficialCode(riistakeskuksenAlue.getOfficialCode());

        parentOrganisation = riistakeskuksenAlue;
    }
}
