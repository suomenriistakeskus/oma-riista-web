package fi.riista.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometryFactory;
import fi.riista.feature.common.entity.GeoLocation;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

public class GISFinnishEconomicZoneUtil {
    private static final GISUtils.SRID INTERNAL_SRID = GISUtils.SRID.ETRS_TM35FIN;
    private static GISFinnishEconomicZoneUtil INSTANCE;

    public static synchronized GISFinnishEconomicZoneUtil getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new GISFinnishEconomicZoneUtil();
        }

        return INSTANCE;
    }

    private static MultiPolygon loadGeometry() {
        final ObjectMapper objectMapper = new ObjectMapper();
        final LinkedList<Polygon> geometryList = new LinkedList<>();

        try (final InputStream is = new ClassPathResource("finnish-economic-zone.geojson").getInputStream()) {
            final FeatureCollection featureCollection = objectMapper.readValue(is, FeatureCollection.class);

            for (final Feature feature : featureCollection) {
                final Geometry geometry = PolygonConversionUtil.geoJsonToJava(
                        feature.getGeometry(), INTERNAL_SRID);

                if (geometry != null && geometry instanceof Polygon) {
                    geometryList.add((Polygon) geometry);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final GeometryFactory geometryFactory = GISUtils.getGeometryFactory(INTERNAL_SRID);
        return new MultiPolygon(geometryList.toArray(new Polygon[geometryList.size()]), geometryFactory);
    }

    private final PreparedGeometry multiPolygon;

    private GISFinnishEconomicZoneUtil() {
        this.multiPolygon = PreparedGeometryFactory.prepare(loadGeometry());
    }

    public boolean containsLocation(final GeoLocation geoLocation) {
        final GeometryFactory geometryFactory = GISUtils.getGeometryFactory(INTERNAL_SRID);
        return multiPolygon.contains(geometryFactory.createPoint(geoLocation.toCoordinate()));
    }
}
