package fi.riista.feature.gis.kiinteisto;

import fi.riista.config.properties.DataSourceProperties;
import fi.riista.feature.gis.GISPoint;
import fi.riista.integration.mml.dto.MMLRekisteriyksikonTietoja;
import fi.riista.integration.mml.service.MMLRekisteriyksikonTietojaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

@Component
public class CoordinatePropertyLookupFeature {
    private static final Logger LOG = LoggerFactory.getLogger(CoordinatePropertyLookupFeature.class);

    @Resource
    private GISPropertyIdentifierRepository propertyIdentifierRepository;

    @Resource
    private MMLRekisteriyksikonTietojaService mmlRekisteriyksikonTietojaService;

    @Resource
    private DataSourceProperties dataSourceProperties;

    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public List<MMLRekisteriyksikonTietoja> findByPosition(final GISPoint gisPoint) {
        if (dataSourceProperties.isGisQuerySupported()) {
            final List<MMLRekisteriyksikonTietoja> localProperties =
                    propertyIdentifierRepository.findIntersectingWithPoint(gisPoint);

            if (!localProperties.isEmpty()) {
                return localProperties;
            }
            LOG.warn("Could not lookup propertyIdentifier from local database for {}", gisPoint);

        } else {
            LOG.warn("Local GIS query is not supported");
        }

        // Fallback to WFS
        try {
            return mmlRekisteriyksikonTietojaService.findByPosition(gisPoint);
        } catch (RuntimeException ex) {
            LOG.error("MML WFS request failed", ex);
            return Collections.emptyList();
        }
    }
}
