package fi.riista.feature.gis.kiinteisto;

import fi.riista.feature.gis.GISBounds;
import fi.riista.util.GISUtils;
import org.geojson.FeatureCollection;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Component
public class PropertyGeometryLookupFeature {

    @Resource
    private GISPropertyGeometryRepository propertyGeometryRepository;

    @Transactional(readOnly = true)
    public FeatureCollection findByPoint(final double lat, final double lng) {
        return propertyGeometryRepository.findIntersectingWithPoint(lat, lng, GISUtils.SRID.WGS84);
    }

    @Transactional(readOnly = true)
    public FeatureCollection findByPropertyIdentifier(String propertyIdentifier) {
        return propertyGeometryRepository.findByPropertyIdentifier(propertyIdentifier, GISUtils.SRID.WGS84);
    }

    @Transactional(readOnly = true)
    public FeatureCollection findById(Long id) {
        return propertyGeometryRepository.findOne(id, GISUtils.SRID.WGS84);
    }

    @Transactional(readOnly = true)
    public FeatureCollection findByBounds(GISBounds bounds) {
        return propertyGeometryRepository.findByBounds(bounds, 500, GISUtils.SRID.WGS84);
    }
}
