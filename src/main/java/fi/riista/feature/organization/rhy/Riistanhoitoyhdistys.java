package fi.riista.feature.organization.rhy;

import com.google.common.collect.ImmutableSet;
import fi.riista.feature.gis.hta.GISHirvitalousalue;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import org.joda.time.LocalDate;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import java.util.HashSet;
import java.util.Set;

@Entity
@Access(value = AccessType.FIELD)
@DiscriminatorValue("RHY")
public class Riistanhoitoyhdistys extends Organisation {

    public static final int ANNUAL_STATISTICS_FIRST_YEAR = 2017;
    public static final int ANNUAL_STATISTICS_DEFAULT_LAST_YEAR = 2023; // TODO Update when new annual statistics opened

    public static final String RHY_OFFICIAL_CODE_HELSINKI = "602";

    private static final Set<String> FREE_HUNTING_MUNICIPALITY = ImmutableSet.copyOf(new String[]{
            "701", "702", "703", "704", "705", "706", "707", "708", "201", "202", "203", "204", "205", "206", "207",
            "208", "209", "210", "211", "212", "213", "214", "215", "216", "217", "218", "219", "220", "262", "271",
            "279", "282"
    });

    private static final String RHY_SUFFIX_FI = "riistanhoitoyhdistys";
    private static final String RHY_SUFFIX_SV = "jaktvårdsförening";

    @Column(name = "is_at_coast")
    private Boolean atCoast;

    @Enumerated(EnumType.STRING)
    @Column(name = "rhy_srva_rotation")
    private SrvaRotation srvaRotation;

    @Column(name="rhy_srva_rotation_start")
    private LocalDate rotationStart;

    @ManyToMany
    @JoinTable(name = "rhy_hta",
            joinColumns = {@JoinColumn(name = ID_COLUMN_NAME, referencedColumnName = Organisation.ID_COLUMN_NAME)},
            inverseJoinColumns = {@JoinColumn(name = GISHirvitalousalue.ID_COLUMN_NAME, referencedColumnName = "gid")})
    private Set<GISHirvitalousalue> relatedHtas = new HashSet<>();

    public static String shortenRhySuffixFi(final String input) {
        return input.endsWith(RHY_SUFFIX_FI)
                ? input.substring(0, input.length() - RHY_SUFFIX_FI.length()) + "RHY"
                : input;
    }

    public static String shortenRhySuffixSv(final String input) {
        return input.endsWith(RHY_SUFFIX_SV)
                ? input.substring(0, input.length() - RHY_SUFFIX_SV.length()) + "JVF"
                : input;
    }

    public Riistanhoitoyhdistys() {
        super(OrganisationType.RHY);
    }

    public Riistanhoitoyhdistys(final RiistakeskuksenAlue alue, final String nimiFI, final String nimiSV, final String officialRhyId) {
        this();
        setNameFinnish(nimiFI);
        setNameSwedish(nimiSV);
        setOfficialCode(officialRhyId);

        this.parentOrganisation = alue;
    }

    public Organisation getRiistakeskuksenAlue() {
        return getClosestAncestorOfType(OrganisationType.RKA).orElse(null);
    }

    public void setRiistakeskuksenAlue(final RiistakeskuksenAlue alue) {
        parentOrganisation = alue;
    }

    public Boolean getAtCoast() {
        return atCoast;
    }

    public void setAtCoast(final Boolean atCoast) {
        this.atCoast = atCoast;
    }

    public boolean isFreeHuntingMunicipality() {
        return FREE_HUNTING_MUNICIPALITY.contains(getOfficialCode());
    }

    public SrvaRotation getSrvaRotation() {
        return srvaRotation;
    }

    public void setSrvaRotation(final SrvaRotation srvaRotation) {
        this.srvaRotation = srvaRotation;
    }

    public LocalDate getRotationStart() {
        return rotationStart;
    }

    public void setRotationStart(final LocalDate rotationStart) {
        this.rotationStart = rotationStart;
    }

    public Set<GISHirvitalousalue> getRelatedHtas() {
        return relatedHtas;
    }
}
