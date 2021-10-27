package fi.riista.feature.gis.zone.query;

import fi.riista.feature.gis.geojson.GeoJSONConstants;
import fi.riista.util.GISUtils;
import fi.riista.util.PolygonConversionUtil;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKBWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcOperations;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;

public class UpdateOtherFeatureQueries {
    private static final Logger LOG = LoggerFactory.getLogger(UpdateOtherFeatureQueries.class);

    private static final String INSERT_SQL = "INSERT INTO zone_feature(zone_id, geom) VALUES (?, ST_Transform(?, 3067))";
    private static final String DELETE_ALL_SQL = "DELETE FROM zone_feature WHERE zone_id = ?";

    private static boolean isOtherFeature(final Feature f) {
        return f.getId() != null && f.getId().startsWith(GeoJSONConstants.ID_PREFIX_OTHER);
    }

    private final JdbcOperations jdbcOperations;

    public UpdateOtherFeatureQueries(final JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    public void insertOtherFeatures(final long zoneId,
                                    final FeatureCollection featureCollection,
                                    final GISUtils.SRID srid) {
        final List<byte[]> wkbList = serializeToWkb(featureCollection, srid);

        jdbcOperations.batchUpdate(INSERT_SQL, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(final PreparedStatement ps, final int i) throws SQLException {
                ps.setLong(1, zoneId);
                ps.setBytes(2, wkbList.get(i));
            }

            @Override
            public int getBatchSize() {
                return wkbList.size();
            }
        });
    }

    private static List<byte[]> serializeToWkb(final FeatureCollection featureCollection, final GISUtils.SRID srid) {
        final WKBWriter wkbWriter = new WKBWriter(2, true);

        return featureCollection.getFeatures().stream()
                .filter(UpdateOtherFeatureQueries::isOtherFeature)
                .map(f -> PolygonConversionUtil.geoJsonToJava(f.getGeometry(), srid))
                .map(geometry -> {
                    if (geometry instanceof org.locationtech.jts.geom.MultiPolygon && geometry.getNumGeometries() > 1) {
                        LOG.warn("Converting multi-polygon to single polygon geometries");
                        final List<Geometry> geometries = new ArrayList<>();
                        for (int i = 0; i < geometry.getNumGeometries(); i++) {
                            geometries.add(geometry.getGeometryN(i));
                        }
                        return geometries;
                    }
                    return singletonList(geometry);
                })
                .flatMap(Collection::stream)
                .map(wkbWriter::write)
                .collect(Collectors.toList());
    }

    public void removeZoneFeatures(final long zoneId) {
        jdbcOperations.update(DELETE_ALL_SQL, zoneId);
    }
}
