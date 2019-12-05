package fi.riista.feature.huntingclub.hunting.overview;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.querydsl.core.types.Expression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesService;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.gamediary.harvest.QHarvest;
import fi.riista.feature.gis.geojson.GeoJSONConstants;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.gis.zone.QGISZone;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.QHarvestPermit;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.QHuntingClub;
import fi.riista.feature.huntingclub.area.QHuntingClubArea;
import fi.riista.feature.huntingclub.group.QHuntingClubGroup;
import fi.riista.feature.huntingclub.hunting.day.QGroupHuntingDay;
import fi.riista.feature.permit.application.QHarvestPermitApplication;
import fi.riista.feature.permit.area.QHarvestPermitArea;
import fi.riista.feature.permit.area.partner.QHarvestPermitAreaPartner;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.QPermitDecision;
import fi.riista.security.EntityPermission;
import fi.riista.util.GISUtils;
import fi.riista.util.LocalisedString;
import org.geojson.FeatureCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Component
public class SharedPermitMapFeature {

    private static class CacheKey {
        private static CacheKey cacheKey(final long harvestPermitId,
                                         final int huntingYear,
                                         final int gameSpeciesCode) {
            return new CacheKey(harvestPermitId, huntingYear, gameSpeciesCode);
        }

        private final long permitId;
        private final int huntingYear;
        private final int speciesCode;

        public CacheKey(final long permitId, final int huntingYear, final int speciesCode) {
            this.permitId = permitId;
            this.huntingYear = huntingYear;
            this.speciesCode = speciesCode;
        }

        public long getPermitId() {
            return permitId;
        }

        public int getHuntingYear() {
            return huntingYear;
        }

        public int getSpeciesCode() {
            return speciesCode;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final CacheKey cacheKey = (CacheKey) o;
            return permitId == cacheKey.permitId &&
                    huntingYear == cacheKey.huntingYear &&
                    speciesCode == cacheKey.speciesCode;
        }

        @Override
        public int hashCode() {
            return Objects.hash(permitId, huntingYear, speciesCode);
        }
    }

    @Resource
    private JPQLQueryFactory queryFactory;

    @Resource
    private GameSpeciesService gameSpeciesService;

    @Resource
    private RequireEntityService requireEntityService;

    private GISZoneRepository zoneRepository;

    @Resource
    private SharedPermitHarvestDTOTransformer sharedPermitHarvestDTOTransformer;

    private final LoadingCache<CacheKey, FeatureCollection> featureCollectionCache =
            CacheBuilder.newBuilder()
                    .expireAfterAccess(1, TimeUnit.MINUTES)
                    .build(new CacheLoader<CacheKey, FeatureCollection>() {
                        @Override
                        public FeatureCollection load(final CacheKey key) {
                            return SharedPermitMapFeature.this.doLoad(key.getPermitId(), key.getHuntingYear(),
                                                                      key.getSpeciesCode());
                        }
                    });

    @Autowired
    public void setZoneRepository(final GISZoneRepository gisZoneRepository) {
        this.zoneRepository = gisZoneRepository;
    }

    @Transactional(readOnly = true, timeout = 10 * 60)
    public FeatureCollection findPermitArea(final long harvestPermitId,
                                            final int huntingYear,
                                            final int gameSpeciesCode) {
        final CacheKey key = CacheKey.cacheKey(harvestPermitId, huntingYear, gameSpeciesCode);
        try {
            return featureCollectionCache.get(key);
        } catch (ExecutionException ee) {
            throw new RuntimeException(ee);
        }
    }

    @Transactional(readOnly = true)
    public List<HarvestDTO> listHarvest(final long harvestPermitId,
                                        final int huntingYear,
                                        final int gameSpeciesCode) {
        final GameSpecies species = gameSpeciesService.requireByOfficialCode(gameSpeciesCode);
        final HarvestPermit harvestPermit =
                requireEntityService.requireHarvestPermit(harvestPermitId, EntityPermission.READ);

        return harvestPermit.isMooselikePermitType()
                ? sharedPermitHarvestDTOTransformer.apply(getHarvestForPermit(harvestPermit, huntingYear, species))
                : Collections.emptyList();
    }

    Map<Long, Map<String, Object>> getApplicationAreas(final PermitDecision permitDecision) {
        final QPermitDecision DECISION = QPermitDecision.permitDecision;
        final QHarvestPermitApplication APPLICATION = QHarvestPermitApplication.harvestPermitApplication;
        final QHarvestPermitArea PERMIT_AREA = QHarvestPermitArea.harvestPermitArea;
        final QHarvestPermitAreaPartner AREA_PARTNER = QHarvestPermitAreaPartner.harvestPermitAreaPartner;
        final QHuntingClubArea CLUB_AREA = QHuntingClubArea.huntingClubArea;
        final QHuntingClub CLUB = QHuntingClub.huntingClub;
        final QGISZone ZONE = QGISZone.gISZone;

        final Expression<LocalisedString> clubName = CLUB.nameLocalisation();

        return queryFactory.from(DECISION)
                .join(DECISION.application, APPLICATION)
                .join(APPLICATION.area, PERMIT_AREA)
                .join(PERMIT_AREA.partners, AREA_PARTNER)
                .join(AREA_PARTNER.sourceArea, CLUB_AREA)
                .join(CLUB_AREA.club, CLUB)
                .join(CLUB_AREA.zone, ZONE)
                .where(DECISION.eq(permitDecision))
                .select(ZONE.id,
                        CLUB.id,
                        clubName,
                        ZONE.computedAreaSize)
                .distinct()
                .fetch()
                .stream()
                .collect(toMap(tuple -> tuple.get(ZONE.id), tuple -> ImmutableMap.of(
                        GeoJSONConstants.PROPERTY_CLUB_NAME, tuple.get(clubName).asMap(),
                        GeoJSONConstants.PROPERTY_HUNTING_CLUB_ID, tuple.get(CLUB.id),
                        GeoJSONConstants.PROPERTY_AREA_SIZE, tuple.get(ZONE.computedAreaSize))));
    }

    Map<Long, Map<String, Object>> getGroupAreas(final HarvestPermit permit,
                                                 final int huntingYear,
                                                 final GameSpecies species) {
        final QHarvestPermit harvestPermit = QHarvestPermit.harvestPermit;
        final QHuntingClub huntingClub = QHuntingClub.huntingClub;
        final QHuntingClubGroup huntingClubGroup = QHuntingClubGroup.huntingClubGroup;
        final QHuntingClubArea huntingClubArea = QHuntingClubArea.huntingClubArea;
        final QGISZone zone = QGISZone.gISZone;

        final Expression<LocalisedString> huntingClubName = huntingClub.nameLocalisation();

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
                        huntingClubName,
                        zone.computedAreaSize)
                .distinct()
                .fetch()
                .stream()
                .collect(toMap(tuple -> tuple.get(zone.id), tuple -> ImmutableMap.of(
                        GeoJSONConstants.PROPERTY_CLUB_NAME, tuple.get(huntingClubName).asMap(),
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


    private FeatureCollection doLoad(final long harvestPermitId,
                                     final int huntingYear,
                                     final int gameSpeciesCode) {
        final GameSpecies species = gameSpeciesService.requireByOfficialCode(gameSpeciesCode);
        final HarvestPermit harvestPermit =
                requireEntityService.requireHarvestPermit(harvestPermitId, EntityPermission.READ);

        if (!harvestPermit.isMooselikePermitType()) {
            return new FeatureCollection();
        }

        final Map<Long, Map<String, Object>> permitZones = Optional.ofNullable(harvestPermit.getPermitDecision())
                .map(this::getApplicationAreas)
                .orElseGet(() -> getGroupAreas(harvestPermit, huntingYear, species));

        final FeatureCollection combinedFeatures = zoneRepository.getCombinedFeatures(
                permitZones.keySet(), GISUtils.SRID.WGS84);

        combinedFeatures.forEach(feature -> {
            final long zoneId = Long.parseLong(feature.getId());

            Optional.ofNullable(permitZones.get(zoneId))
                    .ifPresent(props -> feature.getProperties().putAll(props));
        });
        return combinedFeatures;
    }
}
