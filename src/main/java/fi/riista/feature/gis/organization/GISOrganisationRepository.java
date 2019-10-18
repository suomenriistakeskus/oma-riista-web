package fi.riista.feature.gis.organization;

import fi.riista.feature.gis.GISWGS84Point;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.util.GISUtils;
import io.vavr.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static fi.riista.util.Collect.tuplesToMap;

@Repository
public class GISOrganisationRepository {

    private static final Logger LOG = LoggerFactory.getLogger(GISOrganisationRepository.class);

    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(final DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Transactional(readOnly = true)
    public Optional<String> getOfficialCodeForRhyByWGS84Location(final GISWGS84Point point) {


        final String wkt = point.toWellKnownText();
        final int fromSRIDvalue = GISUtils.SRID.WGS84.value;
        final int toSRIDValue = GISUtils.SRID.ETRS_TM35.value;
        final MapSqlParameterSource paramSource = new MapSqlParameterSource()
                .addValue("fromSRID", fromSRIDvalue)
                .addValue("toSRID", toSRIDValue)
                .addValue("wkt", wkt);

        // Geometric features are stored in EPSG:3047 which is identical to EPSG:3067 except for bounds
        // For ST_Intersects, the WGS84 coordinate needs to be transformed into corresponding geometry
        // as the stored geometries.
        final String sql = "SELECT id" +
                " FROM rhy WHERE ST_Intersects(geom, ST_Transform( ST_GeomFromText(:wkt, :fromSRID), :toSRID));\n";

        final List<String> result = jdbcTemplate.query(
                sql,
                paramSource,
                (rs, rowNum) -> rs.getString("id"));

        if (result.isEmpty()) {
            return Optional.empty();
        } else if (result.size() > 1) {
            LOG.warn("Multiple rhys found for one point");
        }

        return Optional.of(result.iterator().next());

    }

    @Transactional(readOnly = true)
    public Map<Long, GISWGS84Point> getRKAAndRHYWGS84Locations() {

        // Get locations for all RKAs and RHYs that have location and transform the lat,lon to WGS84
        final String sql = "SELECT " +
                "wgspoints.organisation_id AS org, " +
                "ST_X(wgspoints.wgs) as longitude, " +
                "ST_Y(wgspoints.wgs) as latitude " +
                "FROM (SELECT " +
                "organisation_id, " +
                "ST_Transform(ST_SetSRID(ST_MakePoint(longitude, latitude), :fromSRID), :toSRID) AS wgs " +
                "FROM organisation " +
                "WHERE " +
                "latitude IS NOT NULL AND " +
                "longitude IS NOT NULL AND " +
                "organisation_type IN (:rka, :rhy)) AS wgspoints;\n";

        final int fromSRIDvalue = GISUtils.SRID.ETRS_TM35FIN.value;
        final int toSRIDValue = GISUtils.SRID.WGS84.value;

        final MapSqlParameterSource paramSource = new MapSqlParameterSource()
                .addValue("fromSRID", fromSRIDvalue)
                .addValue("toSRID", toSRIDValue)
                .addValue("rka", OrganisationType.RKA.name())
                .addValue("rhy", OrganisationType.RHY.name());

        return jdbcTemplate.query(
                sql,
                paramSource,
                (rs, rowNum) -> {
                    final long id = rs.getLong("org");
                    final double latitude = rs.getDouble("latitude");
                    final double longitude = rs.getDouble("longitude");
                    return Tuple.of(id, GISWGS84Point.create(latitude, longitude));
                })
                .stream()
                .collect(tuplesToMap());
    }


}
