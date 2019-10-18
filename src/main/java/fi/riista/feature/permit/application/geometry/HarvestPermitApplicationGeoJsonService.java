package fi.riista.feature.permit.application.geometry;

import com.google.common.collect.ImmutableSet;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.gis.geojson.GeoJSONConstants;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.huntingclub.QHuntingClub;
import fi.riista.feature.huntingclub.area.QHuntingClubArea;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.permit.area.partner.HarvestPermitAreaPartnerRepository;
import fi.riista.feature.permit.area.partner.QHarvestPermitAreaPartner;
import fi.riista.util.GISUtils;
import fi.riista.util.LocalisedString;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import static com.querydsl.core.group.GroupBy.groupBy;

@Service
public class HarvestPermitApplicationGeoJsonService {

    @Resource
    private GISZoneRepository gisZoneRepository;

    @Resource
    private HarvestPermitAreaPartnerRepository harvestPermitAreaPartnerRepository;

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public FeatureCollection getPermitAreaCombined(final HarvestPermitArea harvestPermitArea) {
        return gisZoneRepository.getCombinedPolygonFeatures(harvestPermitArea.getZone().getId(), GISUtils.SRID.WGS84);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public FeatureCollection getPermitAreaForEachPartner(final HarvestPermitArea harvestPermitArea) {
        final Map<Long, LocalisedString> clubNames = createZoneToClubNameMapping(harvestPermitArea);
        final Map<Long, LocalisedString> areaNames = createZoneToAreaNameMapping(harvestPermitArea);

        final List<Long> zoneIds = harvestPermitAreaPartnerRepository.findAreaPartnerZoneIds(harvestPermitArea.getId());
        final FeatureCollection featureCollection =
                gisZoneRepository.getCombinedFeatures(ImmutableSet.copyOf(zoneIds), GISUtils.SRID.WGS84);

        for (final Feature feature : featureCollection) {
            final long zoneId = Long.parseLong(feature.getId());
            feature.setProperty(GeoJSONConstants.PROPERTY_CLUB_NAME, clubNames.get(zoneId).asMap());
            feature.setProperty(GeoJSONConstants.PROPERTY_AREA_NAME, areaNames.get(zoneId).asMap());
        }

        return featureCollection;
    }

    private Map<Long, LocalisedString> createZoneToClubNameMapping(final HarvestPermitArea harvestPermitArea) {
        final QHarvestPermitAreaPartner PARTNER = QHarvestPermitAreaPartner.harvestPermitAreaPartner;
        final QHuntingClub CLUB = QHuntingClub.huntingClub;
        final QHuntingClubArea CLUB_AREA = QHuntingClubArea.huntingClubArea;

        return jpqlQueryFactory
                .from(PARTNER)
                .join(PARTNER.sourceArea, CLUB_AREA)
                .join(CLUB_AREA.club, CLUB)
                .where(PARTNER.harvestPermitArea.eq(harvestPermitArea))
                .transform(groupBy(PARTNER.zone.id).as(CLUB.nameLocalisation()));
    }

    private Map<Long, LocalisedString> createZoneToAreaNameMapping(final HarvestPermitArea harvestPermitArea) {
        final QHarvestPermitAreaPartner PARTNER = QHarvestPermitAreaPartner.harvestPermitAreaPartner;
        final QHuntingClubArea CLUB_AREA = QHuntingClubArea.huntingClubArea;

        return jpqlQueryFactory
                .from(PARTNER)
                .join(PARTNER.sourceArea, CLUB_AREA)
                .where(PARTNER.harvestPermitArea.eq(harvestPermitArea))
                .transform(groupBy(PARTNER.zone.id).as(CLUB_AREA.nameLocalisation()));
    }
}
