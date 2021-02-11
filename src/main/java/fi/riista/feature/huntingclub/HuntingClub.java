package fi.riista.feature.huntingclub;

import com.querydsl.core.annotations.QueryDelegate;
import com.querydsl.core.types.dsl.BooleanExpression;
import fi.riista.feature.common.entity.FinnishBusinessIdEntity;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gis.GISQueryService;
import fi.riista.feature.gis.HtaNotResolvableByGeoLocationException;
import fi.riista.feature.gis.RhyNotResolvableByGeoLocationException;
import fi.riista.feature.gis.hta.GISHirvitalousalue;
import fi.riista.feature.huntingclub.permit.endofhunting.basicsummary.BasicClubHuntingSummary;
import fi.riista.feature.huntingclub.permit.endofhunting.moosesummary.MooseHuntingSummary;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Entity
@Access(value = AccessType.FIELD)
@DiscriminatorValue("CLUB")
public class HuntingClub extends Organisation {

    public static final int CREATED_CLUB_MIN_OFFICIAL_CODE = 5_000_000;

    @ManyToOne(fetch = FetchType.LAZY)
    private GISHirvitalousalue mooseArea;

    @Min(0)
    @Max(99999999)
    @Column(precision = 10, scale = 2)
    private Double huntingAreaSize;

    @OneToMany(mappedBy = "club")
    private Set<MooseHuntingSummary> mooseHuntingSummaries = new HashSet<>();

    @OneToMany(mappedBy = "club")
    private Set<BasicClubHuntingSummary> basicHuntingSummaries = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column
    private HuntingClubSubtype subtype;

    @ManyToOne(fetch = FetchType.LAZY)
    private Person clubPerson;

    @Valid
    @Embedded
    private FinnishBusinessIdEntity businessId;

    @Size(max = 7)
    @Column(length = 7)
    private String associationRegistryNumber;

    @AssertTrue
    public boolean isClubTypeValid() {
        return subtype == null && clubPerson == null && businessId == null && associationRegistryNumber == null ||
                subtype == HuntingClubSubtype.PERSON && businessId == null && associationRegistryNumber == null ||
                subtype == HuntingClubSubtype.BUSINESS && clubPerson == null && associationRegistryNumber == null ||
                subtype == HuntingClubSubtype.RY && clubPerson == null;
    }

    public HuntingClub() {
        super(OrganisationType.CLUB);
    }

    public HuntingClub(final Riistanhoitoyhdistys riistanhoitoyhdistys, final String nameFI, final String nameSV, final String officialCode) {
        this();
        setNameFinnish(nameFI);
        setNameSwedish(nameSV);
        setOfficialCode(officialCode);

        parentOrganisation = riistanhoitoyhdistys;
    }

    public GISHirvitalousalue getMooseArea() {
        return mooseArea;
    }

    public void setMooseArea(final GISHirvitalousalue mooseArea) {
        this.mooseArea = mooseArea;
    }

    public Double getHuntingAreaSize() {
        return huntingAreaSize;
    }

    public void setHuntingAreaSize(final Double huntingAreaSize) {
        this.huntingAreaSize = huntingAreaSize;
    }

    Set<MooseHuntingSummary> getMooseHuntingSummaries() {
        return mooseHuntingSummaries;
    }

    Set<BasicClubHuntingSummary> getBasicHuntingSummaries() {
        return basicHuntingSummaries;
    }

    public void updateLocation(GeoLocation geoLocation, GISQueryService gisQueryService) {
        // Check that location is within Finland
        final Riistanhoitoyhdistys rhy = gisQueryService.findRhyByLocation(geoLocation);

        if (rhy == null) {
            throw new RhyNotResolvableByGeoLocationException(geoLocation);
        }

        this.setGeoLocation(geoLocation);
        this.setParentOrganisation(rhy);

        final GISHirvitalousalue hta = gisQueryService.findHirvitalousalue(geoLocation);
        HtaNotResolvableByGeoLocationException.assertNotNull(hta);
        this.setMooseArea(hta);
    }

    public HuntingClubSubtype getSubtype() {
        return subtype;
    }

    public void setSubtype(final HuntingClubSubtype subtype) {
        this.subtype = subtype;
    }

    public Person getClubPerson() {
        return clubPerson;
    }

    public void setClubPerson(final Person clubPerson) {
        this.clubPerson = clubPerson;
    }

    public FinnishBusinessIdEntity getBusinessId() {
        return businessId;
    }

    public void setBusinessId(final FinnishBusinessIdEntity businessId) {
        this.businessId = businessId;
    }

    public String getAssociationRegistryNumber() {
        return associationRegistryNumber;
    }

    public void setAssociationRegistryNumber(final String associationRegistryNumber) {
        this.associationRegistryNumber = associationRegistryNumber;
    }

    // Querydsl delegates -->

    @QueryDelegate(HuntingClub.class)
    public static BooleanExpression userCreated(final QHuntingClub club) {
        return club.officialCode.startsWith("5");
    }
}
