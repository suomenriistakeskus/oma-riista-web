package fi.riista.feature.gis.zone.query;

import com.google.common.base.Joiner;
import fi.riista.feature.gis.zone.GISZone;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

public class CopyZoneGeometryQueries {
    private static final String[] ZONE_PALSTA_FIELDS = new String[]{
            "palsta_id", "geom", "palsta_tunnus", "is_changed", "new_palsta_id", "new_palsta_tunnus", "diff_area"
    };

    private static final String[] ZONE_FEATURE_FIELDS = new String[]{
            "property_identifier", "geom", "included_species"
    };

    private final NamedParameterJdbcOperations jdbcOperations;
    private final String copyZonePalstaSql;
    private final String copyZoneFeatureSql;

    public CopyZoneGeometryQueries(final NamedParameterJdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;

        final String palstaFields = Joiner.on(", ").join(ZONE_PALSTA_FIELDS);
        final String featureFields = Joiner.on(", ").join(ZONE_FEATURE_FIELDS);

        this.copyZonePalstaSql = "INSERT INTO zone_palsta (zone_id, " + palstaFields + ")" +
                " SELECT :to_zone_id, " + palstaFields +
                " FROM zone_palsta" +
                " WHERE zone_id = :from_zone_id";

        this.copyZoneFeatureSql = "INSERT INTO zone_feature (zone_id, " + featureFields + ")" +
                " SELECT :to_zone_id, " + featureFields +
                " FROM zone_feature" +
                " WHERE zone_id = :from_zone_id";
    }

    public void copyZonePalsta(final GISZone from, final GISZone to) {
        jdbcOperations.update(copyZonePalstaSql, copyParameters(from, to));
    }

    public void copyZoneFeatures(final GISZone from, final GISZone to) {
        jdbcOperations.update(copyZoneFeatureSql, copyParameters(from, to));
    }

    public void copyZoneCombinedGeometry(final GISZone from, final GISZone to) {
        jdbcOperations.update("UPDATE zone " +
                " SET geom = ( " +
                "   SELECT z2.geom " +
                "   FROM zone AS z2 " +
                "   WHERE z2.zone_id = :from_zone_id) " +
                " WHERE zone_id = :to_zone_id", copyParameters(from, to));
    }

    public void addZoneGeomToFeatures(final GISZone from, final GISZone to) {
        jdbcOperations.update("INSERT INTO zone_feature (zone_id, geom) " +
                " SELECT :to_zone_id," +
                " (ST_Dump(geom)).geom" +
                " FROM zone" +
                " WHERE zone_id = :from_zone_id", copyParameters(from, to));
    }

    private static MapSqlParameterSource copyParameters(final GISZone from, final GISZone to) {
        return new MapSqlParameterSource()
                .addValue("from_zone_id", from.getId())
                .addValue("to_zone_id", to.getId());
    }
}
