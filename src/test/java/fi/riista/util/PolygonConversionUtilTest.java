package fi.riista.util;

import com.vividsolutions.jts.geom.Geometry;
import org.geojson.LngLatAlt;
import org.geojson.MultiPolygon;
import org.geojson.Polygon;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PolygonConversionUtilTest {

    @Test
    public void testJsonMultiPolygon_SingleRing_WithThreePointsNotConnected() {
        final Polygon originalPolygon = new Polygon(new LngLatAlt(1,1), new LngLatAlt(1,2), new LngLatAlt(1,3));
        final MultiPolygon originalMultiPolygon = new MultiPolygon(originalPolygon);
        final Geometry geometry = PolygonConversionUtil.geoJsonToJavaPossiblyInvalid(originalMultiPolygon, GISUtils.SRID.WGS84);

        final Polygon expectedPolygon = new Polygon(new LngLatAlt(1,1), new LngLatAlt(1,2), new LngLatAlt(1,3), new LngLatAlt(1,1));
        final MultiPolygon expectedMultiPolygon = new MultiPolygon(expectedPolygon);

        assertEquals(expectedMultiPolygon, PolygonConversionUtil.javaToGeoJSON(geometry));
    }

    @Test
    public void testJsonMultiPolygon_SingleRing_WithThreePointsConnected() {
        final Polygon originalPolygon = new Polygon(new LngLatAlt(1,1), new LngLatAlt(1,2), new LngLatAlt(1,3), new LngLatAlt(1,1));
        final MultiPolygon originalMultiPolygon = new MultiPolygon(originalPolygon);
        final Geometry geometry = PolygonConversionUtil.geoJsonToJavaPossiblyInvalid(originalMultiPolygon, GISUtils.SRID.WGS84);

        assertEquals(originalMultiPolygon, PolygonConversionUtil.javaToGeoJSON(geometry));
    }

    @Test
    public void testJsonMultiPolygon_MultipleRing_WithThreePointsNotConnected() {
        final Polygon originalPolygon1 = new Polygon(new LngLatAlt(1,1), new LngLatAlt(1,2), new LngLatAlt(1,3));
        final Polygon originalPolygon2 = new Polygon(new LngLatAlt(2,1), new LngLatAlt(2,2), new LngLatAlt(2,3));
        final MultiPolygon originalMultiPolygon = new MultiPolygon(originalPolygon1);
        originalMultiPolygon.add(originalPolygon2);
        final Geometry geometry = PolygonConversionUtil.geoJsonToJavaPossiblyInvalid(originalMultiPolygon, GISUtils.SRID.WGS84);

        final Polygon expectedPolygon1 = new Polygon(new LngLatAlt(1,1), new LngLatAlt(1,2), new LngLatAlt(1,3), new LngLatAlt(1,1));
        final Polygon expectedPolygon2 = new Polygon(new LngLatAlt(2,1), new LngLatAlt(2,2), new LngLatAlt(2,3), new LngLatAlt(2,1));
        final MultiPolygon expectedMultiPolygon = new MultiPolygon(expectedPolygon1);
        expectedMultiPolygon.add(expectedPolygon2);

        assertEquals(expectedMultiPolygon, PolygonConversionUtil.javaToGeoJSON(geometry));
    }

    @Test
    public void testJsonMultiPolygon_MultipleRing_WithThreePointsConnected() {
        final Polygon originalPolygon1 = new Polygon(new LngLatAlt(1,1), new LngLatAlt(1,2), new LngLatAlt(1,3), new LngLatAlt(1,1));
        final Polygon originalPolygon2 = new Polygon(new LngLatAlt(2,1), new LngLatAlt(2,2), new LngLatAlt(2,3), new LngLatAlt(2,1));
        final MultiPolygon originalMultiPolygon = new MultiPolygon(originalPolygon1);
        originalMultiPolygon.add(originalPolygon2);
        final Geometry geometry = PolygonConversionUtil.geoJsonToJavaPossiblyInvalid(originalMultiPolygon, GISUtils.SRID.WGS84);

        assertEquals(originalMultiPolygon, PolygonConversionUtil.javaToGeoJSON(geometry));
    }
}
