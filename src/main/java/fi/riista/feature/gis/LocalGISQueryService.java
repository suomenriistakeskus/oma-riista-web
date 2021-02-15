package fi.riista.feature.gis;

import fi.riista.config.profile.PostGisDatabase;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.Municipality;
import fi.riista.feature.common.repository.MunicipalityRepository;
import fi.riista.feature.gis.hta.GISHirvitalousalue;
import fi.riista.feature.gis.hta.GISHirvitalousalueRepository;
import fi.riista.feature.gis.kiinteisto.GISPropertyIdentifierRepository;
import fi.riista.feature.gis.metsahallitus.MetsahallitusAreaLookupResult;
import fi.riista.feature.gis.metsahallitus.MetsahallitusHirviRepository;
import fi.riista.feature.gis.metsahallitus.MetsahallitusMaterialYear;
import fi.riista.feature.gis.metsahallitus.MetsahallitusPienriistaRepository;
import fi.riista.feature.gis.rhy.GISRiistanhoitoyhdistys;
import fi.riista.feature.gis.rhy.GISRiistanhoitoyhdistysRepository;
import fi.riista.feature.harvestpermit.season.GISHarvestAreaRepository;
import fi.riista.feature.harvestpermit.season.HarvestArea;
import fi.riista.feature.harvestpermit.season.HarvestAreaRepository;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import fi.riista.integration.mml.dto.MMLRekisteriyksikonTietoja;
import fi.riista.integration.mml.service.MMLBuildingUnitService;
import fi.riista.integration.mml.service.MMLRekisteriyksikonTietojaService;
import fi.riista.util.F;
import fi.riista.util.GISFinnishEconomicZoneUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Set;

import static fi.riista.util.Collect.entriesToMap;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;

@Service
@PostGisDatabase
public class LocalGISQueryService implements GISQueryService {
    private static final Logger LOG = LoggerFactory.getLogger(LocalGISQueryService.class);

    @Resource
    private GISRiistanhoitoyhdistysRepository rhyGisRepository;

    @Resource
    private GISHirvitalousalueRepository hirvitalousalueRepository;

    @Resource
    private GISPropertyIdentifierRepository propertyIdentifierRepository;

    @Resource
    private MetsahallitusHirviRepository metsahallitusHirviRepository;

    @Resource
    private MetsahallitusPienriistaRepository metsahallitusPienriistaRepository;

    @Resource
    private RiistanhoitoyhdistysRepository rhyRepository;

    @Resource
    private MunicipalityRepository municipalityRepository;

    @Resource
    private MMLRekisteriyksikonTietojaService mmlRekisteriyksikonTietojaService;

    @Resource
    private MMLBuildingUnitService buildingUnitService;

    @Resource
    private MetsahallitusMaterialYear metsahallitusMaterialYear;

    @Resource
    private HarvestAreaRepository harvestAreaRepository;

    @Resource
    private GISHarvestAreaRepository gisHarvestAreaRepository;

    private NamedParameterJdbcOperations namedParameterJdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(new JdbcTemplate(dataSource));
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Riistanhoitoyhdistys findRhyByLocation(@Nonnull GeoLocation geoLocation) {
        return findRhyByLocation(GISPoint.create(geoLocation));
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Riistanhoitoyhdistys findRhyForEconomicZone(@Nonnull GeoLocation geoLocation) {
        final boolean insideEconomicZone = GISFinnishEconomicZoneUtil.getInstance().containsLocation(geoLocation);

        // OR-479 Map grey seal on economic zone to Helsinki RHY
        return insideEconomicZone ?
                rhyRepository.findByOfficialCode(Riistanhoitoyhdistys.RHY_OFFICIAL_CODE_HELSINKI) : null;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public MetsahallitusAreaLookupResult findMetsahallitusAreas(final @Nonnull GeoLocation geoLocation) {
        final int latestHirviYear = metsahallitusMaterialYear.getLatestHirviYear();
        final int latestPienriistaYear = metsahallitusMaterialYear.getLatestPienriistaYear();

        final Integer hirviAlueId = metsahallitusHirviRepository.findGid(geoLocation, latestHirviYear);
        final Integer pienriistaAlueId =
                metsahallitusPienriistaRepository.findPienriistaAlueId(geoLocation, latestPienriistaYear);

        return new MetsahallitusAreaLookupResult(hirviAlueId, pienriistaAlueId);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Riistanhoitoyhdistys findRhyByLocation(final @Nonnull GISPoint gisPoint) {

        return singleRhyByLocation(gisPoint)
                .map(this::getSameOrganisation)
                .orElse(null);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Optional<HarvestArea> findHarvestAreaByLocation(
            final @Nonnull HarvestArea.HarvestAreaType areaType, final @Nonnull GeoLocation geoLocation) {

        final GISPoint gisPoint = GISPoint.create(requireNonNull(geoLocation));
        final List<Long> ids = gisHarvestAreaRepository.queryAreaIdByPoint(areaType, gisPoint);

        return singleResultFrom(ids, gisPoint).flatMap(harvestAreaRepository::findById);
    }

    private Riistanhoitoyhdistys getSameOrganisation(final GISRiistanhoitoyhdistys gisResult) {
        final Riistanhoitoyhdistys rhy = rhyRepository.findByOfficialCode(gisResult.getOfficialCode());

        if (rhy == null) {
            LOG.warn("No such officialCode={}", gisResult.getOfficialCode());
        }

        return rhy;
    }

    private Optional<GISRiistanhoitoyhdistys> singleRhyByLocation(final GISPoint geoLocation) {
        final List<GISRiistanhoitoyhdistys> intersectingRhy = rhyGisRepository.queryByPoint(geoLocation);

        return singleResultFrom(intersectingRhy, geoLocation);
    }

    private <T> Optional<T> singleResultFrom(final Collection<T> collection, final GISPoint geoLocation) {
        if (collection.isEmpty()) {
            LOG.warn("No matches for geoLocation{}", geoLocation);
            return empty();

        } else if (collection.size() > 1) {
            LOG.warn("Multiple matches for geoLocation={}", geoLocation);
            return empty();

        } else {
            return collection.stream().findFirst();
        }
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW, noRollbackFor = RuntimeException.class)
    public Optional<MMLRekisteriyksikonTietoja> findPropertyByLocation(@Nonnull final GISPoint gisPoint) {
        final List<MMLRekisteriyksikonTietoja> allPropertiesByLocation = findAllPropertiesByLocation(gisPoint);

        if (allPropertiesByLocation.isEmpty()) {
            return Optional.empty();
        }

        final MMLRekisteriyksikonTietoja firstProperty = allPropertiesByLocation.get(0);

        if (allPropertiesByLocation.size() > 1) {
            LOG.warn("Found multiple zones for point {}, extracting property identifier {} from the first one",
                    gisPoint, firstProperty.getPropertyIdentifier());
        }

        return Optional.of(firstProperty);
    }

    @Override
    public Optional<MMLRekisteriyksikonTietoja> findPropertyByLocation(@Nonnull final GeoLocation geoLocation) {
        return findPropertyByLocation(GISPoint.create(geoLocation));
    }

    private List<MMLRekisteriyksikonTietoja> findAllPropertiesByLocation(final GISPoint gisPoint) {
        final List<MMLRekisteriyksikonTietoja> localProperties =
                propertyIdentifierRepository.findIntersectingWithPoint(gisPoint);

        if (!localProperties.isEmpty()) {
            return localProperties;
        }

        LOG.warn("Could not lookup propertyIdentifier from local database for {}", gisPoint);

        // Fallback to WFS
        try {
            return mmlRekisteriyksikonTietojaService.findByPosition(gisPoint);
        } catch (RuntimeException ex) {
            LOG.error("MML WFS request failed", ex);
            return Collections.emptyList();
        }
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Municipality findMunicipality(@Nonnull GeoLocation geoLocation) {
        return municipalityRepository.findMunicipality(geoLocation.getLatitude(), geoLocation.getLongitude());
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Map<Long, Boolean> findZonesWithChanges(final @Nonnull Set<Long> zoneIds) {
        requireNonNull(zoneIds);

        if (zoneIds.isEmpty()) {
            return Collections.emptyMap();
        }

        final MapSqlParameterSource params = new MapSqlParameterSource("zoneIds", zoneIds);

        // Changed flag is only updated manually during source material update
        final List<Long> palstaChanges = namedParameterJdbcTemplate.queryForList(
                "SELECT DISTINCT zone_id\n" +
                        "FROM zone_palsta\n" +
                        "WHERE zone_id IN (:zoneIds)\n" +
                        "AND is_changed IS TRUE",
                params, Long.class);

        // MH hunting year can get out of sync only when material for next hunting year is not yet available
        // at the beginning of the season.
        final List<Long> metsahallitusChanges = namedParameterJdbcTemplate.queryForList(
                "SELECT DISTINCT zone.zone_id\n" +
                        "FROM hunting_club_area area\n" +
                        "JOIN zone ON (area.zone_id = zone.zone_id)\n" +
                        "JOIN zone_mh_hirvi zmh ON (zmh.zone_id = zone.zone_id)\n" +
                        "JOIN mh_hirvi mh ON (zmh.mh_hirvi_id = mh.gid)\n" +
                        "WHERE area.zone_id IN (:zoneIds)\n" +
                        "AND area.metsahallitus_year <> mh.vuosi",
                params, Long.class);

        return zoneIds.stream()
                .map(zoneId ->
                        F.entry(zoneId, palstaChanges.contains(zoneId) || metsahallitusChanges.contains(zoneId)))
                .collect(entriesToMap());
    }


    @Override
    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public GISHirvitalousalue findHirvitalousalue(@Nonnull GeoLocation geoLocation) {
        return hirvitalousalueRepository.findByPoint(geoLocation);
    }

    @Override
    public OptionalInt findInhabitedBuildingDistance(final GISPoint position, final int maxDistanceToSeek) {
        final OptionalDouble distanceToResidence =
                buildingUnitService.findMinimumDistanceToGeometryDWithin(position, maxDistanceToSeek);

        if (!distanceToResidence.isPresent()) {
            return OptionalInt.empty();
        }

        final int integerDistance = Double.valueOf(distanceToResidence.getAsDouble()).intValue();

        return integerDistance > maxDistanceToSeek ? OptionalInt.empty() : OptionalInt.of(integerDistance);
    }
}
