package fi.riista.api.gis;

import com.vividsolutions.jts.geom.Geometry;
import fi.riista.feature.gis.GISBounds;
import fi.riista.feature.gis.GISWGS84Point;
import fi.riista.feature.gis.kiinteisto.PropertyGeometryLookupFeature;
import fi.riista.feature.gis.metsahallitus.MetsahallitusGeometryLookupFeature;
import fi.riista.feature.gis.metsahallitus.MetsahallitusHirviDTO;
import fi.riista.feature.gis.rhy.CoordinateRhyLookupFeature;
import fi.riista.util.GISUtils;
import fi.riista.util.MediaTypeExtras;
import fi.riista.util.PolygonConversionUtil;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.GeoJsonObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/gis")
public class GISGeometryApiResource {
    @Resource
    private CoordinateRhyLookupFeature coordinateRhyLookupFeature;

    @Resource
    private PropertyGeometryLookupFeature propertyGeometryLookupFeature;

    @Resource
    private MetsahallitusGeometryLookupFeature metsahallitusGeometryLookupFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/rhy/bounds", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> getRhyBounds(@RequestParam String officialCode) {
        return toResponseEntity(coordinateRhyLookupFeature.getRhyBounds(officialCode));
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/rhy/geom", method = RequestMethod.GET, produces = MediaTypeExtras.APPLICATION_GEOJSON_VALUE)
    public ResponseEntity<?> getRhyGeom(@RequestParam String officialCode) {
        return toResponseEntity(coordinateRhyLookupFeature.getRhyGeoJSON(officialCode));
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/property/identifier", method = RequestMethod.GET, produces = MediaTypeExtras.APPLICATION_GEOJSON_VALUE)
    public FeatureCollection propertyByIdentifier(@RequestParam String propertyIdentifier) {
        return propertyGeometryLookupFeature.findByPropertyIdentifier(propertyIdentifier);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/property/id", method = RequestMethod.GET, produces = MediaTypeExtras.APPLICATION_GEOJSON_VALUE)
    public FeatureCollection propertyById(@RequestParam Long id) {
        return propertyGeometryLookupFeature.findById(id);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/property/point", method = RequestMethod.GET, produces = MediaTypeExtras.APPLICATION_GEOJSON_VALUE)
    public FeatureCollection propertyByPoint(@RequestParam double lat, @RequestParam double lng) {
        return propertyGeometryLookupFeature.findByPoint(lat, lng);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/property/bounds", method = RequestMethod.GET, produces = MediaTypeExtras.APPLICATION_GEOJSON_VALUE)
    public FeatureCollection propertyBounds(@RequestParam double minLat, @RequestParam double minLng,
                                            @RequestParam double maxLat, @RequestParam double maxLng) {
        return propertyGeometryLookupFeature.findByBounds(new GISBounds(minLng, minLat, maxLng, maxLat));
    }

    private static ResponseEntity<?> toResponseEntity(final Object data) {
        return data != null ? ResponseEntity.ok(data) : ResponseEntity.notFound().build();
    }

    @RequestMapping(value = "/polygonUnion", method = RequestMethod.POST, produces = MediaTypeExtras.APPLICATION_GEOJSON_VALUE)
    public GeoJsonObject polygonUnion(@RequestBody FeatureCollection features) {
        final Geometry union = GISUtils.polygonUnion(features, GISUtils.SRID.WGS84);
        return union != null ? PolygonConversionUtil.javaToGeoJSON(union) : null;
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/mh/hirvi", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<MetsahallitusHirviDTO> listMetsahallitusHirvi(@RequestParam int year) {
        return metsahallitusGeometryLookupFeature.listHirvi(year);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/mh/hirvi/id", method = RequestMethod.GET, produces = MediaTypeExtras.APPLICATION_GEOJSON_VALUE)
    public Feature getHirviFeature(@RequestParam int id) {
        return metsahallitusGeometryLookupFeature.getHirviFeature(id);
    }
}
