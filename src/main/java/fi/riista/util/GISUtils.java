package fi.riista.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.spatial.GeometryExpression;
import com.querydsl.spatial.GeometryExpressions;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gis.GISBounds;
import io.vavr.Tuple2;
import org.geojson.Crs;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.GeoJsonObject;
import org.geojson.jackson.CrsType;
import org.locationtech.jts.algorithm.Area;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.Polygonal;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.util.GeometryEditor;
import org.locationtech.jts.index.strtree.STRtree;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static com.querydsl.core.types.dsl.Expressions.constant;
import static com.querydsl.spatial.GeometryExpressions.fromText;
import static java.util.stream.Collectors.toList;

public final class GISUtils {

    private static final String GEOJSON_EPSG_CRS_PREFIX = "urn:ogc:def:crs:EPSG::";

    public enum SRID {
        ETRS_TM35(3047),
        ETRS_TM35FIN(3067),
        WGS84(4326);

        public final int value;

        SRID(final int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        // https://en.wikipedia.org/wiki/Decimal_degrees
        public int getDecimalPrecision() {
            // WGS84 + 7 decimal precision = 0.5 cm accuracy
            // TM35 + 3 decimal precision = 0.1 cm accuracy
            return this == SRID.WGS84 ? 7 : 3;
        }

        public Crs getGeoJsonCrs() {
            final Crs crs = new Crs();
            crs.setType(CrsType.name);
            crs.setProperties(Collections.singletonMap("name", GEOJSON_EPSG_CRS_PREFIX + value));
            return crs;
        }

        public static SRID fromGeoJsonCrs(final @Nonnull Crs crs) {
            Objects.requireNonNull(crs);

            final String crsName = Objects.toString(crs.getProperties().get("name"));

            if (crsName.startsWith(GEOJSON_EPSG_CRS_PREFIX)) {
                final String sridCode = crsName.substring(GEOJSON_EPSG_CRS_PREFIX.length());

                return GISUtils.SRID.ofCode(Integer.parseInt(sridCode));
            }

            throw new IllegalArgumentException("Could not parse GeoJSON CRS value " + crsName);
        }

        public static SRID ofCode(int code) {
            for (final SRID srid : SRID.values()) {
                if (code == srid.value) {
                    return srid;
                }
            }
            return null;
        }
    }

    private static final LoadingCache<SRID, GeometryFactory> GEOMETRY_FACTORIES = CacheBuilder
            .newBuilder()
            .build(new CacheLoader<SRID, GeometryFactory>() {
                @Override
                public GeometryFactory load(@Nullable final SRID srid) {
                    final PrecisionModel precisionModel = srid != null
                            ? new PrecisionModel(Math.pow(10.0, srid.getDecimalPrecision()))
                            : new PrecisionModel();
                    return new GeometryFactory(precisionModel, srid == null ? 0 : srid.value);
                }
            });

    @Nonnull
    public static GeometryFactory getGeometryFactory(final SRID srid) {
        try {
            return GEOMETRY_FACTORIES.get(srid);
        } catch (final ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static Geometry readFromPostgisWkb(final byte[] wkb, final GISUtils.SRID srid) {
        final GeometryFactory geometryFactory = getGeometryFactory(srid);
        final WKBReader wkbReader = new WKBReader(geometryFactory);

        try {
            return wkbReader.read(wkb);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] writeToPostgisWkb(final Geometry geometry) {
        final WKBWriter wkbReader = new WKBWriter();
        return wkbReader.write(geometry);
    }

    public static double[] getGeoJsonBBox(final Geometry geometry) {
        return Optional.ofNullable(geometry)
                .filter(g -> g instanceof Polygonal && !g.isEmpty())
                .map(Geometry::getEnvelopeInternal)
                .filter(envelope -> !envelope.isNull())
                .map(e -> new double[]{e.getMinX(), e.getMinY(), e.getMaxX(), e.getMaxY()})
                .orElse(null);
    }

    public static Geometry polygonUnion(final FeatureCollection features, final GISUtils.SRID srid) {
        final Geometry[] geometries = features.getFeatures().stream()
                .map(Feature::getGeometry)
                .filter(Objects::nonNull)
                .map(geom -> PolygonConversionUtil.geoJsonToJava(geom, srid))
                .filter(Objects::nonNull)
                .toArray(Geometry[]::new);

        return geometries.length > 0 ? getGeometryFactory(srid).createGeometryCollection(geometries).union() : null;
    }

    public static Geometry computeUnionFaster(final List<Geometry> geomList) {
        if (geomList.isEmpty()) {
            return null;
        }
        return unionTree(createSTRTreeIndex(geomList));
    }

    private static List createSTRTreeIndex(final List<Geometry> geomList) {
        final STRtree index = new STRtree(16);

        for (final Geometry item : geomList) {
            index.insert(item.getEnvelopeInternal(), item);
        }

        return index.itemsTree();
    }

    private static Geometry unionTree(final List<?> tree) {
        final ArrayList<Geometry> geomList = new ArrayList<>(16);

        // Do not use Stream to conserve memory!
        for (final Object o : tree) {
            if (o instanceof List) {
                geomList.add(unionTree((List) o));
            } else if (o instanceof Geometry) {
                geomList.add((Geometry) o);
            }

            if (Thread.currentThread().isInterrupted()) {
                throw new RuntimeException("Processing was interrupted");
            }
        }

        final GeometryFactory factory = geomList.get(0).getFactory();
        return factory.buildGeometry(geomList).buffer(0);
    }

    public static Geometry filterPolygonsByMinimumAreaSize(final Geometry union,
                                                           final double minimumExteriorSize,
                                                           final double minimumInteriorSize) {
        return new GeometryEditor().edit(union, (geometry, factory) -> {
            if (!(geometry instanceof Polygon)) {
                return geometry;
            }

            final Polygon polygon = (Polygon) geometry;
            final LinearRing shell = (LinearRing) polygon.getExteriorRing();
            final double exteriorAreaSize = Math.abs(Area.ofRingSigned(shell.getCoordinateSequence()));

            if (exteriorAreaSize < minimumExteriorSize) {
                return null;
            }

            final ArrayList<LinearRing> holes = new ArrayList<>(polygon.getNumInteriorRing());

            for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                final LinearRing hole = (LinearRing) polygon.getInteriorRingN(i);

                final double holeAreaSize = Math.abs(Area.ofRingSigned(hole.getCoordinateSequence()));

                if (holeAreaSize >= minimumInteriorSize) {
                    holes.add(hole);
                }
            }

            return factory.createPolygon(shell, holes.toArray(new LinearRing[]{}));
        });
    }

    @Nonnull
    public static Polygon createPolygon(@Nonnull final GeoLocation location,
                                        @Nonnull final Iterable<Tuple2<Integer, Integer>> offsets) {

        return createPolygon(location, offsets, SRID.ETRS_TM35FIN);
    }

    @Nonnull
    public static Polygon createPolygon(@Nonnull final GeoLocation location,
                                        @Nonnull final Iterable<Tuple2<Integer, Integer>> offsets,
                                        @Nonnull final SRID srid) {

        Objects.requireNonNull(location, "location must not be null");
        Objects.requireNonNull(offsets, "offsets must not be null");
        Objects.requireNonNull(srid, "srid must not be null");

        final List<Coordinate> coordinateList = F.stream(offsets)
                .filter(Objects::nonNull)
                .map(pair -> pair.apply(location::move))
                .map(GeoLocation.TO_COORDINATE)
                .collect(toList());

        final int listSize = coordinateList.size();
        final boolean lastCoordinateNotEqualToFirst =
                listSize > 1 && !coordinateList.get(0).equals(coordinateList.get(listSize - 1));

        final Coordinate[] coordinates = new Coordinate[lastCoordinateNotEqualToFirst ? listSize + 1 : listSize];
        coordinateList.toArray(coordinates);

        // Add last coordinate to close the ring because LinearRing/Polygon
        // requires that the last coordinate is same as the first one.
        if (lastCoordinateNotEqualToFirst) {
            coordinates[listSize] = coordinates[0];
        }

        return getGeometryFactory(srid).createPolygon(coordinates);
    }

    @Nonnull
    public static Polygon createPolygon(final GISBounds bounds, final SRID srid) {
        final double[] bbox = bounds.toBBox();

        final double west = bbox[0];
        final double south = bbox[1];
        final double east = bbox[2];
        final double north = bbox[3];

        final Coordinate lowLeft = new Coordinate(west, south);
        final Coordinate topLeft = new Coordinate(west, north);
        final Coordinate topRight = new Coordinate(east, north);
        final Coordinate lowRight = new Coordinate(east, south);

        final GeometryFactory geometryFactory = getGeometryFactory(srid);

        return geometryFactory.createPolygon(geometryFactory.createLinearRing(new Coordinate[]{
                lowLeft, lowRight, topRight, topLeft, lowLeft
        }));
    }

    public static GeoJsonObject parseGeoJSONGeometry(@Nonnull final ObjectMapper objectMapper,
                                                     @Nonnull final String geometry) {

        if (StringUtils.hasText(geometry)) {
            try {
                return objectMapper.readValue(geometry, GeoJsonObject.class);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public static Point createPoint(final @Nonnull GeoLocation geoLocation){
        return getGeometryFactory(SRID.ETRS_TM35FIN)
                .createPoint(new Coordinate(geoLocation.getLongitude(), geoLocation.getLatitude()));
    }

    public static GeometryExpression<?> createPointWithDefaultSRID(@Nonnull final NumberPath<Integer> longitude,
                                                                   @Nonnull final NumberPath<Integer> latitude) {

        Objects.requireNonNull(longitude, "longitude must not be null");
        Objects.requireNonNull(latitude, "latitude must not be null");

        final StringExpression point = longitude.stringValue()
                .prepend(constant("POINT("))
                .concat(constant(" "))
                .concat(latitude.stringValue())
                .concat(constant(")"));

        return GeometryExpressions.setSRID(fromText(point), SRID.ETRS_TM35FIN.getValue());
    }

    public static GeometryExpression<?> createPointWithDefaultSRID(@Nonnull final GeoLocation location) {
        Objects.requireNonNull(location, "location must not be null");
        final String point = String.format("POINT(%d %d)", location.getLongitude(), location.getLatitude());
        return GeometryExpressions.setSRID(fromText(point), SRID.ETRS_TM35FIN.getValue());
    }

    private GISUtils() {
        throw new AssertionError();
    }
}
