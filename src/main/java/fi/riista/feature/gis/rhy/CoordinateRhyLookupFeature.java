package fi.riista.feature.gis.rhy;

import fi.riista.feature.gis.GISBounds;
import fi.riista.feature.gis.GISPoint;
import fi.riista.feature.gis.GISQueryService;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.util.GISUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;

@Component
public class CoordinateRhyLookupFeature {
    private static final Logger LOG = LoggerFactory.getLogger(CoordinateRhyLookupFeature.class);

    @Resource
    private GISRiistanhoitoyhdistysRepository rhyGisRepository;

    @Resource
    private GISQueryService gisQueryService;

    @Transactional(readOnly = true)
    public OrganisationNameDTO findByGeoLocation(final GISPoint geoLocation) {
        final Riistanhoitoyhdistys rhyByLocation = gisQueryService.findRhyByLocation(geoLocation);

        if (rhyByLocation == null) {
            LOG.warn("RHY not found for coordinates {}, {}", geoLocation.getLatitude(), geoLocation.getLongitude());
            return null;
        }

        LOG.debug("Found GIS rhy: {}", rhyByLocation.getOfficialCode());

        return OrganisationNameDTO.create(rhyByLocation);
    }

    @Transactional(readOnly = true)
    public String getRhyGeoJSON(@Nonnull final String officialCode) {
        return getFirstForOfficialCode(
                rhyGisRepository.queryRhyGeoJSON(officialCode, GISUtils.SRID.WGS84), officialCode);
    }

    @Transactional(readOnly = true)
    public GISBounds getRhyBounds(final String officialCode) {
        return getFirstForOfficialCode(
                rhyGisRepository.queryRhyBounds(officialCode), officialCode);
    }

    private static <T> T getFirstForOfficialCode(final List<T> list, final String officialCode) {
        if (list.isEmpty()) {
            return null;
        } else if (list.size() > 1) {
            LOG.warn("Multiple matches for officialCode={}", officialCode);
        }
        return list.iterator().next();
    }
}
