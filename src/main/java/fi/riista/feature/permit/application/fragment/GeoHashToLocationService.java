package fi.riista.feature.permit.application.fragment;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import fi.riista.feature.gis.GISWGS84Point;
import fi.riista.util.GISUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

@Service
public class GeoHashToLocationService {

    private NamedParameterJdbcOperations jdbcOperations;

    @Autowired
    public void setDataSource(final DataSource dataSource) {
        this.jdbcOperations = new NamedParameterJdbcTemplate(dataSource);
    }

    @Transactional(readOnly = true)
    public GISWGS84Point convert(final String hash) {
        final MapSqlParameterSource params = new MapSqlParameterSource("hash", hash);

        return jdbcOperations.queryForObject(
                "SELECT ST_AsBinary(ST_Centroid(ST_GeomFromGeoHash(:hash))) AS geom",
                params, (rs, i) -> {
                    final byte[] wkb = rs.getBytes("geom");
                    final Geometry geometry = GISUtils.readFromPostgisWkb(wkb, GISUtils.SRID.WGS84);
                    final Point point = (Point) geometry;

                    final double longitude = point.getX();
                    final double latitude = point.getY();
                    return new GISWGS84Point(latitude, longitude);
                });
    }
}
