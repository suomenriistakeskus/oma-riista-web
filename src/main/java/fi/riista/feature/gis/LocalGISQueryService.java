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
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import fi.riista.integration.mml.dto.MMLRekisteriyksikonTietoja;
import fi.riista.integration.mml.service.MMLBuildingUnitService;
import fi.riista.integration.mml.service.MMLRekisteriyksikonTietojaService;
import fi.riista.util.GISFinnishEconomicZoneUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Riistanhoitoyhdistys findRhyByLocation(@Nonnull GeoLocation geoLocation) {
        return findRhyByLocation(GISPoint.create(geoLocation));
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Riistanhoitoyhdistys findRhyForEconomicZone(@Nonnull GeoLocation geoLocation) {
        final boolean insideEconomicZone = GISFinnishEconomicZoneUtil.getInstance().containsLocation(geoLocation);

        // OR-479 Map grey seal on economic zone to Helsinki RHY
        return insideEconomicZone ? rhyRepository.findByOfficialCode(Riistanhoitoyhdistys.RHY_OFFICIAL_CODE_HELSINKI) : null;
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public MetsahallitusAreaLookupResult findMetsahallitusAreas(final @Nonnull GeoLocation geoLocation) {
        final int latestHirviYear = metsahallitusMaterialYear.getLatestHirviYear();
        final int latestPienriistaYear = metsahallitusMaterialYear.getLatestPienriistaYear();

        final Integer hirviAlueId = metsahallitusHirviRepository.findGid(geoLocation, latestHirviYear);
        final Integer pienriistaAlueId = metsahallitusPienriistaRepository.findPienriistaAlueId(geoLocation, latestPienriistaYear);

        return new MetsahallitusAreaLookupResult(hirviAlueId, pienriistaAlueId);
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Riistanhoitoyhdistys findRhyByLocation(@Nonnull GISPoint gisPoint) {
        final GISRiistanhoitoyhdistys gisResult = singleResultOrNull(gisPoint);

        if (gisResult != null) {
            return getSameOrganisation(gisResult);
        }

        return null;
    }

    private Riistanhoitoyhdistys getSameOrganisation(GISRiistanhoitoyhdistys gisResult) {
        final Riistanhoitoyhdistys rhy = rhyRepository.findByOfficialCode(gisResult.getOfficialCode());

        if (rhy == null) {
            LOG.warn("No such officialCode={}", gisResult.getOfficialCode());
        }

        return rhy;
    }

    private GISRiistanhoitoyhdistys singleResultOrNull(GISPoint geoLocation) {
        final List<GISRiistanhoitoyhdistys> intersectingRhy = rhyGisRepository.queryByPoint(geoLocation);

        if (intersectingRhy.isEmpty()) {
            LOG.warn("No matches for geoLocation{}", geoLocation);
            return null;

        } else if (intersectingRhy.size() > 1) {
            LOG.warn("Multiple matches for geoLocation={}", geoLocation);
            return null;

        } else {
            return intersectingRhy.iterator().next();
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
    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Municipality findMunicipality(@Nonnull GeoLocation geoLocation) {
        return municipalityRepository.findMunicipality(geoLocation.getLatitude(), geoLocation.getLongitude());
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<Long> findZonesWithChanges() {
        // Changed flag is only updated manually during source material update
        final List<Long> palstaChanges = jdbcTemplate.queryForList(
                "SELECT DISTINCT zone_id FROM zone_palsta WHERE is_changed IS TRUE", Long.class);

        // MH hunting year can get out of sync only when material for next hunting year is not yet available at the
        // begin of the season.
        final List<Long> metsahallitusChanges = jdbcTemplate.queryForList("SELECT DISTINCT zone.zone_id\n" +
                "FROM hunting_club_area area\n" +
                "JOIN zone ON (area.zone_id = zone.zone_id)\n" +
                "JOIN zone_mh_hirvi zmh ON (zmh.zone_id = zone.zone_id)\n" +
                "JOIN mh_hirvi mh ON (zmh.mh_hirvi_id = mh.gid)\n" +
                "WHERE area.metsahallitus_year <> mh.vuosi", Long.class);

        return Stream.concat(palstaChanges.stream(), metsahallitusChanges.stream())
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
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
