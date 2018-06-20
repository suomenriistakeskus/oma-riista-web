package fi.riista.feature.gis.vector;

import com.google.common.base.Stopwatch;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.util.AffineTransformation;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.precision.GeometryPrecisionReducer;
import com.wdtinc.mapbox_vector_tile.VectorTile;
import com.wdtinc.mapbox_vector_tile.adapt.jts.IUserDataConverter;
import com.wdtinc.mapbox_vector_tile.adapt.jts.JtsAdapter;
import com.wdtinc.mapbox_vector_tile.adapt.jts.UserDataIgnoreConverter;
import com.wdtinc.mapbox_vector_tile.build.MvtLayerBuild;
import com.wdtinc.mapbox_vector_tile.build.MvtLayerProps;
import fi.riista.util.GISUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.sql.DataSource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class VectorTileService {
    private static final Logger LOG = LoggerFactory.getLogger(VectorTileService.class);

    private static final IUserDataConverter USER_DATA_CONVERTER = new UserDataIgnoreConverter();
    private static final GeometryPrecisionReducer GEOMETRY_PRECISION_REDUCER;
    private static final GeometryFactory GEOMETRY_FACTORY;

    private static final String SQL_ZONE_GEOMETRY = "SELECT ST_ASBinary(ST_ClipByBox2D(simple_geom, ST_MakeEnvelope(:xmin, :ymin, :xmax, :ymax, 4326))) AS geom FROM zone WHERE zone_id = :zoneId";

    static {
        GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), GISUtils.SRID.WGS84.getValue());
        GEOMETRY_PRECISION_REDUCER = new GeometryPrecisionReducer(new PrecisionModel(PrecisionModel.FIXED));
        GEOMETRY_PRECISION_REDUCER.setPointwise(true);
    }

    private static final int MVT_EXTENT = 4096;

    private static Function<Geometry, Geometry> createTileTransformer(final Envelope tileEnvelope) {
        final AffineTransformation affineTransform = new AffineTransformation();

        // Transform Setup: Shift to 0 as minimum value
        affineTransform.translate(-tileEnvelope.getMinX(), -tileEnvelope.getMinY());

        // Transform Setup: Scale X and Y to tile extent values, flip Y values
        affineTransform.scale(
                1d / (tileEnvelope.getWidth() / (double) MVT_EXTENT),
                -1d / (tileEnvelope.getHeight() / (double) MVT_EXTENT));

        // Transform Setup: Bump Y values to positive quadrant
        affineTransform.translate(0d, MVT_EXTENT);

        return (clippedSourceGeometry) -> {
            final Geometry affineResult = affineTransform.transform(clippedSourceGeometry);
            final Geometry reduceResult = GEOMETRY_PRECISION_REDUCER.reduce(affineResult);
            reduceResult.setUserData(clippedSourceGeometry.getUserData());
            return reduceResult;
        };
    }

    private NamedParameterJdbcOperations jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Transactional(readOnly = true)
    public byte[] getZoneVectorTile(final Long zoneId, final int z, final int x, final int y) {
        final Stopwatch s = Stopwatch.createStarted();

        final Envelope envelope = VectorTileUtil.tileEnvelope(z, x, y);
        final Function<Geometry, Geometry> tileTransformer = createTileTransformer(envelope);

        // Create zone geometry
        final List<Geometry> clippedZoneGeometry = loadZoneGeometry(zoneId, z, envelope);

        final VectorTile.Tile.Builder tileBuilder = VectorTile.Tile.newBuilder();

        if (!clippedZoneGeometry.isEmpty()) {
            tileBuilder.addLayers(createVectorLayer(tileTransformer, clippedZoneGeometry, "all"));
        }

        // MVT Bytes
        final VectorTile.Tile vectorTile = tileBuilder.build();

        if (s.elapsed(TimeUnit.MILLISECONDS) > 100) {
            LOG.warn("tile created {}", s);
        }

        return vectorTile.toByteArray();
    }

    @Nonnull
    private static VectorTile.Tile.Layer createVectorLayer(final Function<Geometry, Geometry> tileTransformer,
                                                           final List<Geometry> clippedExcludedGeometry,
                                                           final String layerName) {
        final List<Geometry> transformedGeometry = clippedExcludedGeometry.stream()
                .map(tileTransformer)
                .filter(g -> g != null && (g instanceof Polygon || g instanceof MultiPolygon))
                .collect(Collectors.toList());

        final MvtLayerProps layerProperties = new MvtLayerProps();
        final VectorTile.Tile.Layer.Builder secondLayer = VectorTile.Tile.Layer.newBuilder()
                .setVersion(2)
                .setExtent(MVT_EXTENT)
                .setName(layerName);

        secondLayer.addAllFeatures(JtsAdapter.toFeatures(transformedGeometry, layerProperties, USER_DATA_CONVERTER));
        MvtLayerBuild.writeProps(secondLayer, layerProperties);

        return secondLayer.build();
    }

    private List<Geometry> loadZoneGeometry(final Long zoneId, final int zoom, final Envelope envelope) {
        final MapSqlParameterSource params = envelopeToSqlParams(envelope, zoom).addValue("zoneId", zoneId);

        return loadGeometry(SQL_ZONE_GEOMETRY, params);
    }

    private static MapSqlParameterSource envelopeToSqlParams(final Envelope envelope, final int zoom) {
        // 1 minute of latitude degree is one mile (1852m)
        final double res = VectorTileUtil.tileResolution(zoom);
        final double bufferLat = res / (60.0 * 1852.0);
        // Adjust longitude buffer for 60 degrees latitude
        final double bufferLng = bufferLat * 2;

        return new MapSqlParameterSource()
                .addValue("xmin", envelope.getMinX() - bufferLat)
                .addValue("ymin", envelope.getMinY() - bufferLng)
                .addValue("xmax", envelope.getMaxX() + bufferLat)
                .addValue("ymax", envelope.getMaxY() + bufferLng);
    }

    private List<Geometry> loadGeometry(final String sql, final MapSqlParameterSource params) {
        final Stopwatch s = Stopwatch.createStarted();

        try {
            return jdbcTemplate.query(sql, params, rowMapper).stream()
                    .filter(g -> g != null && (g instanceof Polygon || g instanceof MultiPolygon))
                    .collect(Collectors.toList());
        } finally {
            if (s.elapsed(TimeUnit.MILLISECONDS) > 50) {
                LOG.warn("JDBC load took {}", s);
            }
        }
    }

    private static final RowMapper<Geometry> rowMapper = (resultSet, i) -> {
        final WKBReader wkbReader = new WKBReader(GEOMETRY_FACTORY);
        final byte[] wkb = resultSet.getBytes(1);

        try {
            return wkb != null ? wkbReader.read(wkb) : null;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    };

}
