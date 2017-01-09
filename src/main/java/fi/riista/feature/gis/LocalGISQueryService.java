package fi.riista.feature.gis;

import fi.riista.config.profile.PostGisDatabase;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.Municipality;
import fi.riista.feature.common.repository.MunicipalityRepository;
import fi.riista.feature.gis.hta.GISHirvitalousalue;
import fi.riista.feature.gis.hta.GISHirvitalousalueRepository;
import fi.riista.feature.gis.rhy.GISRiistanhoitoyhdistys;
import fi.riista.feature.gis.metsahallitus.GISMetsahallitusRepository;
import fi.riista.feature.gis.rhy.GISRiistanhoitoyhdistysRepository;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import fi.riista.util.GISUtils.SRID;
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
import java.util.List;

@Service
@PostGisDatabase
public class LocalGISQueryService implements GISQueryService {
    private static final Logger LOG = LoggerFactory.getLogger(LocalGISQueryService.class);

    @Resource
    private GISRiistanhoitoyhdistysRepository rhyGisRepository;

    @Resource
    private GISMetsahallitusRepository metsahallitusRepository;

    @Resource
    private RiistanhoitoyhdistysRepository rhyRepository;

    @Resource
    private MunicipalityRepository municipalityRepository;

    @Resource
    private GISHirvitalousalueRepository hirvitalousalueRepository;

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
    public Integer findMetsahallitusHirviAlueId(@Nonnull GeoLocation geoLocation, int year) {
        return metsahallitusRepository.findHirviAlueId(geoLocation, year);
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Integer findMetsahallitusPienriistaAlueId(@Nonnull GeoLocation geoLocation, int year) {
        return metsahallitusRepository.findPienriistaAlueId(geoLocation, year);
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
    public String getRhyGeoJSON(@Nonnull String officialCode) {
        List<String> res = rhyGisRepository.queryRhyGeoJSON(officialCode, SRID.WGS84);
        if (res.isEmpty()) {
            return null;
        } else if (res.size() > 1) {
            LOG.warn("Multiple matches for officialCode={}", officialCode);
        }
        return res.iterator().next();
    }

    @Override
    public WGS84Bounds getRhyBounds(@Nonnull String officialCode) {
        List<WGS84Bounds> res = rhyGisRepository.queryRhyBounds(officialCode);
        if (res.isEmpty()) {
            return null;
        } else if (res.size() > 1) {
            LOG.warn("Multiple matches for officialCode={}", officialCode);
        }
        return res.iterator().next();
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Municipality findMunicipality(@Nonnull GeoLocation geoLocation) {
        return municipalityRepository.findMunicipality(geoLocation.getLatitude(), geoLocation.getLongitude());
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<Long> findZonesWithChanges() {
        final String sql = "SELECT DISTINCT zone_id FROM zone_palsta WHERE is_changed IS TRUE";

        return jdbcTemplate.queryForList(sql, Long.class);
    }

    @Override
    public GISHirvitalousalue findHirvitalousalue(@Nonnull GeoLocation geoLocation) {
        return hirvitalousalueRepository.findByPoint(geoLocation);
    }
}
