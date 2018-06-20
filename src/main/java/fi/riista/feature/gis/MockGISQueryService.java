package fi.riista.feature.gis;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import fi.riista.config.profile.MockGisDatabase;
import fi.riista.feature.RuntimeEnvironmentUtil;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.Municipality;
import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.common.repository.MunicipalityRepository;
import fi.riista.feature.gis.hta.GISHirvitalousalue;
import fi.riista.feature.gis.hta.GISHirvitalousalueRepository;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import fi.riista.integration.mml.dto.MMLRekisteriyksikonTietoja;
import fi.riista.util.GISFinnishEconomicZoneUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@MockGisDatabase
public class MockGISQueryService implements GISQueryService {

    private static final Logger LOG = LoggerFactory.getLogger(MockGISQueryService.class);

    public static final GeoLocation RHY_GEOLOCATION_NOT_FOUND = new GeoLocation(Integer.MIN_VALUE, Integer.MIN_VALUE);
    public static final GeoLocation HTA_GEOLOCATION_NOT_FOUND = new GeoLocation(Integer.MAX_VALUE, Integer.MAX_VALUE);
    public static final GeoLocation PROPERTY_NOT_FOUND = new GeoLocation(Integer.MIN_VALUE, Integer.MIN_VALUE);
    public static final MMLRekisteriyksikonTietoja PROPERTY_QUERY_RESULT =
            new MMLRekisteriyksikonTietoja("10010010001000", "999");

    @Resource
    private RuntimeEnvironmentUtil runtimeEnvironmentUtil;

    @Resource
    private RiistanhoitoyhdistysRepository riistanhoitoyhdistysRepository;

    @Resource
    private GISHirvitalousalueRepository hirvitalousalueRepository;

    @Resource
    private MunicipalityRepository municipalityRepository;

    private AtomicInteger sequenceGenerator = new AtomicInteger(0);

    @PostConstruct
    public void init() {
        Preconditions.checkState(runtimeEnvironmentUtil.isDevelopmentEnvironment() ||
                        runtimeEnvironmentUtil.isIntegrationTestEnvironment(),
                "Mock implementation cannot be used in this environment!");
    }

    @Override
    public Riistanhoitoyhdistys findRhyByLocation(@Nonnull GeoLocation geoLocation) {
        return RHY_GEOLOCATION_NOT_FOUND.equals(geoLocation) ? null : randomRhy();
    }

    @Override
    public Riistanhoitoyhdistys findRhyByLocation(@Nonnull GISPoint gisPoint) {
        return randomRhy();
    }

    @Override
    public Riistanhoitoyhdistys findRhyForEconomicZone(@Nonnull GeoLocation geoLocation) {
        return GISFinnishEconomicZoneUtil.getInstance().containsLocation(geoLocation) ? randomRhy() : null;
    }

    @Override
    public Optional<MMLRekisteriyksikonTietoja> findPropertyByLocation(final GISPoint gisPoint) {
        LOG.warn("Using mock implementation to find property!");
        return PROPERTY_NOT_FOUND.equals(new GeoLocation(gisPoint.getLatitude(), gisPoint.getLongitude()))
                ? Optional.empty() : Optional.of(PROPERTY_QUERY_RESULT);
    }

    @Override
    public Optional<MMLRekisteriyksikonTietoja> findPropertyByLocation(final GeoLocation geoLocation) {
        LOG.warn("Using mock implementation to find property!");
        return PROPERTY_NOT_FOUND.equals(geoLocation) ? Optional.empty() : Optional.of(PROPERTY_QUERY_RESULT);
    }

    @Override
    public Integer findMetsahallitusHirviAlueId(@Nonnull GeoLocation geoLocation, int year) {
        return null;
    }

    @Override
    public Integer findMetsahallitusPienriistaAlueId(@Nonnull GeoLocation geoLocation, int year) {
        return null;
    }

    @Override
    public Municipality findMunicipality(@Nonnull GeoLocation geoLocation) {
        return randomMunicipality();
    }

    @Override
    public GISHirvitalousalue findHirvitalousalue(@Nonnull GeoLocation geoLocation) {
        return HTA_GEOLOCATION_NOT_FOUND.equals(geoLocation) ? null : randomHta();
    }

    private Municipality randomMunicipality() {
        LOG.warn("Using mock implementation to find municipality!");
        return random(municipalityRepository);
    }

    private Riistanhoitoyhdistys randomRhy() {
        LOG.warn("Using mock implementation to find RHY!");
        return random(riistanhoitoyhdistysRepository);
    }

    private GISHirvitalousalue randomHta() {
        LOG.warn("Using mock implementation to find HTA!");
        return random(hirvitalousalueRepository);
    }

    private <T> T random(BaseRepository<T, ?> repo) {
        final Iterator<T> iterator = Iterators.cycle(repo.findAll());
        Iterators.advance(iterator, sequenceGenerator.incrementAndGet());
        return iterator.hasNext() ? iterator.next() : null;
    }

    @Override
    public List<Long> findZonesWithChanges() {
        return Collections.emptyList();
    }

    @Override
    public OptionalInt findInhabitedBuildingDistance(final GISPoint position, final int maxDistanceToSeek) {
        // Return distances only when an even number is picked from sequence.
        final int i = sequenceGenerator.incrementAndGet();
        return i % 2 == 0 ? OptionalInt.of(i % 100) : OptionalInt.empty();
    }
}
