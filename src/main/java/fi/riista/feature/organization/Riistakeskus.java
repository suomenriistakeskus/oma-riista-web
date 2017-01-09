package fi.riista.feature.organization;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@Access(value = AccessType.FIELD)
@DiscriminatorValue("RK")
public class Riistakeskus extends Organisation {

    public static final String OFFICIAL_CODE = "850";

    protected Riistakeskus() {
        super(OrganisationType.RK);
    }

    public Riistakeskus(String nimiFI, String nimiSV) {
        this();
        setNameFinnish(nimiFI);
        setNameSwedish(nimiSV);
        setOfficialCode(OFFICIAL_CODE);
    }
}
