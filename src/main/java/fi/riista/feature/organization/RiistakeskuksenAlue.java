package fi.riista.feature.organization;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@Access(value = AccessType.FIELD)
@DiscriminatorValue("RKA")
public class RiistakeskuksenAlue extends Organisation {

    protected RiistakeskuksenAlue() {
        super(OrganisationType.RKA);
    }

    public RiistakeskuksenAlue(Riistakeskus riistakeskus, String nimiFI, String nimiSV, String areaCode) {
        this();
        setNameFinnish(nimiFI);
        setNameSwedish(nimiSV);
        setOfficialCode(areaCode);

        this.parentOrganisation = riistakeskus;
        this.organisationType = OrganisationType.RKA;
    }

}
