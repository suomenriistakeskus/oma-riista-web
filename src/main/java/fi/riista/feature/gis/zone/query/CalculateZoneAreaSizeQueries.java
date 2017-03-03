package fi.riista.feature.gis.zone.query;

import fi.riista.util.JdbcTemplateUtils;
import javaslang.Tuple;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class CalculateZoneAreaSizeQueries {
    private final NamedParameterJdbcOperations jdbcOperations;

    public CalculateZoneAreaSizeQueries(final NamedParameterJdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    public void updateAreaSizeFromCombinedGeometry(final long zoneId) {
        final String sql = "UPDATE zone SET computed_area_size = COALESCE(ST_Area(geom),0) WHERE zone_id = :zoneId";
        jdbcOperations.update(sql, zoneParam(zoneId));
    }

    public void updateWaterAreaSize(final long zoneId, final double waterAreaSize) {
        final String updateAreaSizeSql = "UPDATE zone SET water_area_size = :waterSize WHERE zone.zone_id = :zoneId";
        jdbcOperations.update(updateAreaSizeSql, zoneParam(zoneId).addValue("waterSize", Math.max(0.0, waterAreaSize)));
    }

    public double getSumOfWaterAreaSize(final long zoneId) {
        return queryForDoubleOrZero(zoneParam(zoneId), "WITH z AS (" +
                " SELECT ST_SubDivide((ST_Dump(geom)).geom, 4096) AS geom" +
                " FROM zone" +
                " WHERE zone_id = :zoneId" +
                ") SELECT" +
                " SUM(ST_Area(ST_Intersection(va.geom, ST_Buffer(z.geom, 0))))" +
                " FROM z JOIN vesialue va ON ST_Intersects(va.geom, z.geom)");
    }

    public Map<String, Double> getSumOfAreaSizeByRhy(final long zoneId) {
        return jdbcOperations.query("WITH z AS (" +
                " SELECT ST_SubDivide((ST_Dump(ST_SetSRID(geom, 3047))).geom, 4096) AS geom" +
                " FROM zone" +
                " WHERE zone_id = :zoneId" +
                ") SELECT" +
                " rhy.id as rhy_official_code," +
                " SUM(ST_Area(ST_Intersection(rhy.geom, ST_Buffer(z.geom, 0)))) AS area_size" +
                " FROM z JOIN rhy ON ST_Intersects(rhy.geom, z.geom)" +
                " GROUP BY rhy.id", zoneParam(zoneId), (resultSet, i) -> {
            final String rhyOfficialCode = resultSet.getString("rhy_official_code");
            final double areaSize = resultSet.getDouble("area_size");
            return Tuple.of(rhyOfficialCode, areaSize);
        }).stream().collect(Collectors.toMap(t -> t._1, t-> t._2));
    }

    public Map<String, Double> getSumOfAreaSizeByHta(final long zoneId) {
        return jdbcOperations.query("WITH z AS (" +
                " SELECT ST_SubDivide((ST_Dump(geom)).geom, 4096) AS geom" +
                " FROM zone" +
                " WHERE zone_id = :zoneId" +
                ") SELECT" +
                " hta.numero as hta_code," +
                " SUM(ST_Area(ST_Intersection(hta.geom, ST_Buffer(z.geom, 0)))) AS area_size" +
                " FROM z JOIN hta ON ST_Intersects(hta.geom, z.geom)" +
                " GROUP BY hta.numero", zoneParam(zoneId), (resultSet, i) -> {
            final String rhyOfficialCode = resultSet.getString("hta_code");
            final double areaSize = resultSet.getDouble("area_size");
            return Tuple.of(rhyOfficialCode, areaSize);
        }).stream().collect(Collectors.toMap(t -> t._1, t-> t._2));
    }

    private double queryForDoubleOrZero(final MapSqlParameterSource params, final String sql) {
        final double defaultValue = 0.0;
        final Double result = JdbcTemplateUtils.queryForDouble(sql, defaultValue, jdbcOperations, params);
        return Optional.ofNullable(result).orElse(defaultValue);
    }

    private static MapSqlParameterSource zoneParam(final long zoneId) {
        return new MapSqlParameterSource("zoneId", zoneId);
    }
}
