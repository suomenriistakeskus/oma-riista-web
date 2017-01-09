package fi.riista.feature.gis.rhy;

import fi.riista.config.properties.DataSourceProperties;
import fi.riista.feature.gis.WGS84Bounds;
import fi.riista.feature.gis.GISPoint;
import fi.riista.util.GISUtils.SRID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

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

    public List<String> queryRhyGeoJSON(final String officialCode, final SRID srid) {
        final String sql = "SELECT ST_AsGeoJSON(ST_Transform(geom, ?)) AS geojson FROM rhy WHERE id=?;\n";
        return jdbcTemplate.query(
                sql, new Object[]{srid.value, officialCode}, (rs, rowNum) -> rs.getString("geojson"));
    }

    public List<WGS84Bounds> queryRhyBounds(final String officialCode) {
        final String sql = "WITH extent AS (SELECT ST_Extent(ST_Transform(geom, ?)) AS e FROM rhy WHERE id=?)\n" +
                "SELECT ST_XMin(e) AS xmin, ST_YMin(e) AS ymin, ST_XMax(e) AS xmax, ST_YMax(e) AS ymax FROM extent;";

        return jdbcTemplate.query(sql, new Object[]{SRID.WGS84.value, officialCode}, (rs, rowNum) -> {
            final WGS84Bounds b = new WGS84Bounds();
            b.setMinLng(rs.getDouble("xmin"));
            b.setMinLat(rs.getDouble("ymin"));
            b.setMaxLng(rs.getDouble("xmax"));
            b.setMaxLat(rs.getDouble("ymax"));
            return b;
        });
    }

}
