package fi.riista.feature.organization.rhy;

import com.google.common.collect.ImmutableSet;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.RiistakeskuksenAlue;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.validation.constraints.Size;
import java.util.Set;

@Entity
@Access(value = AccessType.FIELD)
@DiscriminatorValue("RHY")
public class Riistanhoitoyhdistys extends Organisation {

    public static final String RHY_OFFICIAL_CODE_HELSINKI = "602";

    private static final Set<String> FREE_HUNTING_MUNICIPALITY = ImmutableSet.copyOf(new String[]{
            "701", "702", "703", "704", "705", "706", "707", "708", "201", "202", "203", "204", "205", "206", "207",
            "208", "209", "210", "211", "212", "213", "214", "215", "216", "217", "218", "219", "220", "262", "271",
            "279", "282"
    });

    private static final String RHY_SUFFIX_FI = "riistanhoitoyhdistys";
    private static final String RHY_SUFFIX_SV = "jaktvårdsförening";

    @Size(max = 255)
    @Column
    private String poronhoitoalueId;

    @Size(max = 255)
    @Column
    private String hallialueId;

    @Column(name = "is_at_coast")
    private Boolean atCoast;

    public static String shortenRhySuffixFi(final String input) {
        return input.endsWith(RHY_SUFFIX_FI) ? input.substring(0, input.length() - RHY_SUFFIX_FI.length()) + "RHY" : input;
    }

    public static String shortenRhySuffixSv(final String input) {
        return input.endsWith(RHY_SUFFIX_SV) ? input.substring(0, input.length() - RHY_SUFFIX_SV.length()) + "JVF" : input;
    }

    public Riistanhoitoyhdistys() {
        super(OrganisationType.RHY);
    }

    public Riistanhoitoyhdistys(RiistakeskuksenAlue alue, String nimiFI, String nimiSV, String officialRhyId) {
        this();
        setNameFinnish(nimiFI);
        setNameSwedish(nimiSV);
        setOfficialCode(officialRhyId);

        this.parentOrganisation = alue;
    }

    public Organisation getRiistakeskuksenAlue() {
        return getClosestAncestorOfType(OrganisationType.RKA).orElse(null);
    }

    public void setRiistakeskuksenAlue(RiistakeskuksenAlue alue) {
        parentOrganisation = alue;
    }

    public String getPoronhoitoalueId() {
        return poronhoitoalueId;
    }

    public void setPoronhoitoalueId(String poronhoitoalueId) {
        this.poronhoitoalueId = poronhoitoalueId;
    }

    public String getHallialueId() {
        return hallialueId;
    }

    public void setHallialueId(String hallialueId) {
        this.hallialueId = hallialueId;
    }

    public Boolean getAtCoast() {
        return atCoast;
    }

    public void setAtCoast(Boolean atCoast) {
        this.atCoast = atCoast;
    }

    public boolean isFreeHuntingMunicipality() {
        return FREE_HUNTING_MUNICIPALITY.contains(getOfficialCode());
    }
}
