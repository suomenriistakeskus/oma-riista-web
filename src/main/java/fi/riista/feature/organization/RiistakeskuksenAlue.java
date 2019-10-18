package fi.riista.feature.organization;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@Access(value = AccessType.FIELD)
@DiscriminatorValue("RKA")
public class RiistakeskuksenAlue extends Organisation {

    private static final String RKA_PREFIX_FI = "Suomen riistakeskus, ";
    private static final String RKA_PREFIX_SV = "Finlands viltcentral, ";

    public static String shortenRkaPrefixFi(final String input) {
        return input.startsWith(RKA_PREFIX_FI) ? input.substring(RKA_PREFIX_FI.length()) : input;
    }

    public static String shortenRkaPrefixSv(final String input) {
        return input.startsWith(RKA_PREFIX_SV) ? input.substring(RKA_PREFIX_SV.length()) : input;
    }

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
