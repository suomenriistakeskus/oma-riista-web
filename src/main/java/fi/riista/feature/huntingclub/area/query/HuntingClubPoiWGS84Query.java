package fi.riista.feature.huntingclub.area.query;

import fi.riista.feature.gis.GISWGS84Point;
import fi.riista.util.Collect;
import io.vavr.Tuple;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Map;

import static fi.riista.util.GISUtils.SRID.ETRS_TM35FIN;
import static fi.riista.util.GISUtils.SRID.WGS84;

public class HuntingClubPoiWGS84Query {
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public HuntingClubPoiWGS84Query(final DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public Map<Long, GISWGS84Point> getLocations(final Collection<Long> poiGroupIds) {
        final String selectSql =
                "WITH points AS (" +
                        " SELECT " +
                        "   poi_location_id AS id," +
                        "   ST_Transform( ST_SetSRID(ST_MakePoint( longitude, latitude), :fromSRID), :toSRID) AS point" +
                        " FROM poi_location local" +
                        " WHERE poi_id IN (:poiGroupIds))" +
                        " SELECT " +
                        "   id AS id, " +
                        "   ST_X(point) as lng," +
                        "   ST_Y(point) as lat" +
                        " FROM points";

        final MapSqlParameterSource queryParams = new MapSqlParameterSource();
        queryParams.addValue("poiGroupIds", poiGroupIds);
        queryParams.addValue("fromSRID", ETRS_TM35FIN.value);
        queryParams.addValue("toSRID", WGS84.value);

        return namedParameterJdbcTemplate.query(selectSql, queryParams, (rs, i) -> {
            final long id = rs.getLong("id");
            final double lat = rs.getDouble("lat");
            final double lng = rs.getDouble("lng");
            return Tuple.of(id, GISWGS84Point.create(lat, lng));
        }).stream().collect(Collect.tuplesToMap());

    }


}
