package fi.riista.feature.gis.rhy;

import fi.riista.config.properties.DataSourceProperties;
import fi.riista.feature.gis.GISBounds;
import fi.riista.feature.gis.GISPoint;
import fi.riista.util.GISUtils;
import fi.riista.util.GISUtils.SRID;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Repository
public class GISRiistanhoitoyhdistysRepository {

    @Resource
    private DataSourceProperties dataSourceProperties;

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    // Return RHY area information by filtering areas containing given Point
    @Transactional(readOnly = true)
    public List<GISRiistanhoitoyhdistys> queryByPoint(final GISPoint gisPoint) {
        Objects.requireNonNull(gisPoint);
        Objects.requireNonNull(gisPoint.getLatitude());
        Objects.requireNonNull(gisPoint.getLongitude());

        if (!dataSourceProperties.isGisQuerySupported()) {
            return Collections.emptyList();
        }

        // Geometric features are stored in EPSG:3047 which is identical to EPSG:3067 except for bounds
        final String sql = "SELECT gid, id, nimi_fi, nimi_sv, maa_ala, vesi_ala, koko_ala" +
                " FROM rhy WHERE ST_Contains(geom, ST_GeomFromText(?, ?));\n";

        return jdbcTemplate.query(sql, new Object[]{gisPoint.toWellKnownText(), SRID.ETRS_TM35.value}, (resultSet, i) -> {
            final GISRiistanhoitoyhdistys row = new GISRiistanhoitoyhdistys();
            row.setGid(resultSet.getInt("gid"));
            row.setOfficialCode(resultSet.getString("id"));
            row.setNimiFi(resultSet.getString("nimi_fi"));
            row.setNimiSv(resultSet.getString("nimi_sv"));
            row.setMaaAla(resultSet.getBigDecimal("maa_ala"));
            row.setVesiAla(resultSet.getBigDecimal("vesi_ala"));
            row.setKokoAla(resultSet.getBigDecimal("koko_ala"));
            return row;
        });
    }

    @Transactional(readOnly = true)
    public List<String> queryRhyGeoJSON(final String officialCode, final SRID srid) {
        final String sql = "SELECT ST_AsGeoJSON(ST_Transform(geom, ?), ?, 0) AS geojson FROM rhy WHERE id=?;\n";
        final Object[] params = {srid.value, srid.getDecimalPrecision(), officialCode};
        return jdbcTemplate.query(sql, params, (rs, rowNum) -> rs.getString("geojson"));
    }


    @Transactional(readOnly = true)
    public Geometry queryInvertedRhyGeom(final String officialCode, final SRID srid) {
        final String FINNISH_BOUNDS = "LINESTRING(50199.4814 6582464.0358, 761274.6247 7799839.8902)";

        // Inversion is done locally inside bounds of Finland using ST_Difference
        final String SQL = "WITH finland AS (" +
                "  SELECT ST_Buffer(ST_Envelope(ST_GeomFromText(:finnishBounds, 3067)), 0) AS geom" +
                "), d AS (" +
                "  SELECT ST_Difference(finland.geom, ST_Transform(ST_Buffer(rhy.geom, 0), :srid)) AS geom" +
                "  FROM finland CROSS JOIN rhy" +
                "  WHERE id = :officialCode" +
                ") SELECT ST_AsBinary(d.geom) AS geom FROM d;";

        final NamedParameterJdbcOperations jdbcOperations = new NamedParameterJdbcTemplate(this.jdbcTemplate);

        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("finnishBounds", FINNISH_BOUNDS)
                .addValue("srid", srid.getValue())
                .addValue("officialCode", officialCode);

        return jdbcOperations.queryForObject(SQL, params, (rs, i) -> {
            final Geometry geometry = GISUtils.readFromPostgisWkb(rs.getBytes("geom"), srid);
            return geometry.isValid() ? geometry : geometry.buffer(0);
        });
    }

    @Transactional(readOnly = true)
    public List<GISBounds> queryRhyBounds(final String officialCode) {
        return queryRhyBoundsInternal(officialCode, SRID.WGS84);
    }

    @Transactional(readOnly = true)
    public List<GISBounds> queryRhyBounds(final String officialCode, final SRID srid) {
        return queryRhyBoundsInternal(officialCode, srid);
    }

    private List<GISBounds> queryRhyBoundsInternal(final String officialCode, final SRID srid) {
        final String sql = "WITH extent AS (SELECT ST_Extent(ST_Transform(geom, ?)) AS e FROM rhy WHERE id=?)\n" +
                "SELECT ST_XMin(e) AS xmin, ST_YMin(e) AS ymin, ST_XMax(e) AS xmax, ST_YMax(e) AS ymax FROM extent;";

        return jdbcTemplate.query(sql, new Object[]{srid.value, officialCode}, (rs, rowNum) -> {
            final GISBounds b = new GISBounds();
            b.setMinLng(rs.getDouble("xmin"));
            b.setMinLat(rs.getDouble("ymin"));
            b.setMaxLng(rs.getDouble("xmax"));
            b.setMaxLat(rs.getDouble("ymax"));
            return b;
        });
    }
}
