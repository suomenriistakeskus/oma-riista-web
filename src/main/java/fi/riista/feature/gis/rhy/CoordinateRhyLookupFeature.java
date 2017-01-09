package fi.riista.feature.gis.rhy;

import fi.riista.feature.gis.WGS84Bounds;
import fi.riista.feature.gis.GISPoint;
import fi.riista.feature.gis.GISQueryService;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;

@Component
public class CoordinateRhyLookupFeature {
    private static final Logger LOG = LoggerFactory.getLogger(CoordinateRhyLookupFeature.class);

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
    public String getRhyGeoJSON(@Nonnull String officialCode) {
        return gisQueryService.getRhyGeoJSON(officialCode);
    }

    @Transactional(readOnly = true)
    public WGS84Bounds getRhyBounds(String officialCode) {
        return gisQueryService.getRhyBounds(officialCode);
    }
}
