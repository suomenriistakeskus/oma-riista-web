package fi.riista.feature.huntingclub;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gis.GISQueryService;
import fi.riista.feature.gis.HtaNotResolvableByGeoLocationException;
import fi.riista.feature.gis.RhyNotResolvableByGeoLocationException;
import fi.riista.feature.gis.hta.GISHirvitalousalue;
import fi.riista.feature.huntingclub.permit.basicsummary.BasicClubHuntingSummary;
import fi.riista.feature.huntingclub.permit.summary.MooseHuntingSummary;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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

    public HuntingClub() {
        super(OrganisationType.CLUB);
    }

    public HuntingClub(final Riistanhoitoyhdistys riistanhoitoyhdistys, final String nameFI, final String nameSV) {
        this();
        setNameFinnish(nameFI);
        setNameSwedish(nameSV);
        setOfficialCode(UUID.randomUUID().toString());

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
        RhyNotResolvableByGeoLocationException.assertNotNull(rhy);

        this.setGeoLocation(geoLocation);
        this.setParentOrganisation(rhy);

        final GISHirvitalousalue hta = gisQueryService.findHirvitalousalue(geoLocation);
        HtaNotResolvableByGeoLocationException.assertNotNull(hta);
        this.setMooseArea(hta);
    }
}
