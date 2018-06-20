package fi.riista.feature.gis.kiinteisto;

import fi.riista.feature.gis.GISBounds;
import fi.riista.feature.gis.GISPoint;
import org.geojson.FeatureCollection;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Component
public class PropertyGeometryLookupFeature {

    @Resource
    private GISPropertyGeometryRepository propertyGeometryRepository;

    @Transactional(readOnly = true)
    public FeatureCollection findByPoint(GISPoint gisPoint) {
        return propertyGeometryRepository.findIntersectingWithPoint(gisPoint);
    }

    @Transactional(readOnly = true)
    public FeatureCollection findByPropertyIdentifier(String propertyIdentifier) {
        return propertyGeometryRepository.findByPropertyIdentifier(propertyIdentifier);
    }

    @Transactional(readOnly = true)
    public FeatureCollection findById(Long id) {
        return propertyGeometryRepository.findOne(id);
    }

    @Transactional(readOnly = true)
    public FeatureCollection findByBounds(GISBounds bounds) {
        return propertyGeometryRepository.findByBounds(bounds, 500);
    }
}
