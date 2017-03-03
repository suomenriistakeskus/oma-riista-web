package fi.riista.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.spatial.GeometryExpression;
import com.querydsl.spatial.GeometryExpressions;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.index.strtree.STRtree;
import com.vividsolutions.jts.precision.GeometryPrecisionReducer;
import fi.riista.feature.common.entity.GeoLocation;
import javaslang.Tuple2;
import org.geojson.Crs;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.GeoJsonObject;
import org.geojson.jackson.CrsType;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
                    return new GeometryFactory(new PrecisionModel(), srid == null ? 0 : srid.value);
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
        return unionTree(createSTRTreeIndex(geomList));
    }

    private static List createSTRTreeIndex(final List<Geometry> geomList) {
        final STRtree index = new STRtree(16);

        final PrecisionModel pm = new PrecisionModel(1000000);
        for (final Geometry item : geomList) {
            final Geometry g = GeometryPrecisionReducer.reduce(item, pm);
            index.insert(g.getEnvelopeInternal(), g);
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

    @Nonnull
    public static Polygon createPolygon(
            @Nonnull final GeoLocation location, @Nonnull final Iterable<Tuple2<Integer, Integer>> offsets) {

        return createPolygon(location, offsets, SRID.ETRS_TM35FIN);
    }

    @Nonnull
    public static Polygon createPolygon(
            @Nonnull final GeoLocation location,
            @Nonnull final Iterable<Tuple2<Integer, Integer>> offsets,
            @Nonnull final SRID srid) {

        Objects.requireNonNull(location, "location must not be null");
        Objects.requireNonNull(offsets, "offsets must not be null");
        Objects.requireNonNull(srid, "srid must not be null");

        final List<Coordinate> coordinateList = F.stream(offsets)
                .filter(Objects::nonNull)
                .map(pair -> pair.transform(location::move))
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

    public static GeoJsonObject parseGeoJSONGeometry(
            @Nonnull final ObjectMapper objectMapper, @Nonnull final String geometry) {

        if (StringUtils.hasText(geometry)) {
            try {
                return objectMapper.readValue(geometry, GeoJsonObject.class);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
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
