package fi.riista.feature.huntingclub.hunting.overview;

import com.google.common.collect.ImmutableMap;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.gamediary.GameDiaryService;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.QHarvest;
import fi.riista.feature.gis.geojson.GeoJSONConstants;
import fi.riista.feature.gis.zone.QGISZone;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.QHarvestPermit;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.QHuntingClub;
import fi.riista.feature.huntingclub.area.QHuntingClubArea;
import fi.riista.feature.huntingclub.group.QHuntingClubGroup;
import fi.riista.feature.huntingclub.hunting.day.QGroupHuntingDay;
import fi.riista.security.EntityPermission;
import fi.riista.util.GISUtils;
import fi.riista.util.LocalisedString;
import org.geojson.FeatureCollection;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Component
public class SharedPermitMapFeature {

    private static final int SIMPLIFY_AMOUNT = 10;

    @Resource
    private JPQLQueryFactory queryFactory;

    @Resource
    private GameDiaryService gameDiaryService;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private GISZoneRepository zoneRepository;

    @Resource
    private SharedPermitHarvestDTOTransformer sharedPermitHarvestDTOTransformer;

    @Transactional(readOnly = true)
    public FeatureCollection findPermitArea(final long harvestPermitId,
                                            final int huntingYear,
                                            final int gameSpeciesCode) {
        final GameSpecies species = gameDiaryService.getGameSpeciesByOfficialCode(gameSpeciesCode);
        final HarvestPermit harvestPermit =
                requireEntityService.requireHarvestPermit(harvestPermitId, EntityPermission.READ);

        if (!harvestPermit.isMooselikePermitType()) {
            return new FeatureCollection();
        }

        final Map<Long, Map<String, Object>> permitZones = getPermitZones(harvestPermit, huntingYear, species);
        final FeatureCollection combinedFeatures = zoneRepository.getCombinedFeatures(
                permitZones.keySet(), GISUtils.SRID.WGS84, SIMPLIFY_AMOUNT);

        combinedFeatures.forEach(feature -> {
            final long zoneId = Long.parseLong(feature.getId());

            Optional.ofNullable(permitZones.get(zoneId))
                    .ifPresent(props -> feature.getProperties().putAll(props));
        });

        return combinedFeatures;
    }

    @Transactional(readOnly = true)
    public List<HarvestDTO> listHarvest(final long harvestPermitId,
                                        final int huntingYear,
                                        final int gameSpeciesCode) {
        final GameSpecies species = gameDiaryService.getGameSpeciesByOfficialCode(gameSpeciesCode);
        final HarvestPermit harvestPermit =
                requireEntityService.requireHarvestPermit(harvestPermitId, EntityPermission.READ);

        return harvestPermit.isMooselikePermitType()
                ? sharedPermitHarvestDTOTransformer.apply(getHarvestForPermit(harvestPermit, huntingYear, species))
                : Collections.emptyList();
    }

    Map<Long, Map<String, Object>> getPermitZones(final HarvestPermit permit,
                                                  final int huntingYear,
                                                  final GameSpecies species) {
        final QHarvestPermit harvestPermit = QHarvestPermit.harvestPermit;
        final QHuntingClub huntingClub = QHuntingClub.huntingClub;
        final QHuntingClubGroup huntingClubGroup = QHuntingClubGroup.huntingClubGroup;
        final QHuntingClubArea huntingClubArea = QHuntingClubArea.huntingClubArea;
        final QGISZone zone = QGISZone.gISZone;

        final JPQLQuery<HuntingClub> permitPartnerClubs = JPAExpressions.selectFrom(harvestPermit)
                .join(harvestPermit.permitPartners, huntingClub)
                .where(harvestPermit.eq(permit))
                .select(huntingClub);

        return queryFactory
                .from(huntingClubGroup)
                .join(huntingClubGroup.parentOrganisation, huntingClub._super)
                .join(huntingClubGroup.huntingArea, huntingClubArea)
                .join(huntingClubArea.zone, zone)
                .where(huntingClubGroup.harvestPermit.eq(permit),
                        huntingClub.in(permitPartnerClubs),
                        huntingClubGroup.species.eq(species),
                        huntingClubGroup.huntingYear.eq(huntingYear),
                        huntingClubArea.huntingYear.eq(huntingYear),
                        huntingClubArea.active.isTrue())
                .select(zone.id,
                        huntingClub.id,
                        huntingClub.nameFinnish,
                        huntingClub.nameSwedish,
                        zone.computedAreaSize)
                .distinct()
                .fetch()
                .stream()
                .collect(toMap(tuple -> tuple.get(zone.id), tuple -> ImmutableMap.of(
                        GeoJSONConstants.PROPERTY_CLUB_NAME, LocalisedString.of(
                                tuple.get(huntingClub.nameFinnish), tuple.get(huntingClub.nameSwedish)).asMap(),
                        GeoJSONConstants.PROPERTY_HUNTING_CLUB_ID, tuple.get(huntingClub.id),
                        GeoJSONConstants.PROPERTY_AREA_SIZE, tuple.get(zone.computedAreaSize))));
    }

    private List<Harvest> getHarvestForPermit(final HarvestPermit permit,
                                              final int huntingYear,
                                              final GameSpecies species) {
        final QHarvestPermit harvestPermit = QHarvestPermit.harvestPermit;
        final QHuntingClub huntingClub = QHuntingClub.huntingClub;
        final QHuntingClubGroup huntingClubGroup = QHuntingClubGroup.huntingClubGroup;
        final QGroupHuntingDay groupHuntingDay = QGroupHuntingDay.groupHuntingDay;
        final QHarvest harvest = QHarvest.harvest;

        final JPQLQuery<HuntingClub> permitPartnerClubs = JPAExpressions.selectFrom(harvestPermit)
                .join(harvestPermit.permitPartners, huntingClub)
                .where(harvestPermit.eq(permit))
                .select(huntingClub);

        // NOTE: Result should not contain duplicate Harvest,
        // because Harvest can only be linked to single hunting group day
        return queryFactory
                .from(huntingClubGroup)
                .join(huntingClubGroup.parentOrganisation, huntingClub._super)
                .join(huntingClubGroup.huntingDays, groupHuntingDay)
                .join(groupHuntingDay.harvests, harvest)
                .where(huntingClubGroup.harvestPermit.eq(permit),
                        huntingClub.in(permitPartnerClubs),
                        huntingClubGroup.huntingYear.eq(huntingYear),
                        huntingClubGroup.species.eq(species))
                .orderBy(harvest.pointOfTime.desc(), harvest.id.desc())
                .select(huntingClub, harvest)
                .fetch()
                .stream()
                .map(tuple -> {
                    final HuntingClub resultClub = Objects.requireNonNull(tuple.get(0, HuntingClub.class));
                    final Harvest resultHarvest = Objects.requireNonNull(tuple.get(1, Harvest.class));
                    resultHarvest.setHuntingClub(resultClub);

                    return resultHarvest;
                })
                .collect(toList());
    }
}
