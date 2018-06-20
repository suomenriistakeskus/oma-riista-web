package fi.riista.feature.gis.zone.query;

import fi.riista.feature.gis.zone.GISZoneSizeDTO;
import fi.riista.feature.gis.zone.GISZoneSizeRhyDTO;
import fi.riista.feature.gis.zone.TotalLandWaterSizeDTO;
import fi.riista.util.JdbcTemplateUtils;
import io.vavr.Tuple;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class CalculateZoneAreaSizeQueries {
    private final NamedParameterJdbcOperations jdbcOperations;

    public CalculateZoneAreaSizeQueries(final NamedParameterJdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    public void updateAreaSize(final long zoneId, final GISZoneSizeDTO dto) {
        final String updateAreaSizeSql = "UPDATE zone SET computed_area_size = :areaSize," +
                " water_area_size = :waterSize," +
                " state_land_area_size = :stateLandAreaSize," +
                " private_land_area_size = :privateLandAreaSize" +
                " WHERE ZONE.zone_id = :zoneId";
        jdbcOperations.update(updateAreaSizeSql, zoneParam(zoneId)
                .addValue("areaSize", dto.getAll().getTotal())
                .addValue("waterSize", dto.getAll().getWater())
                .addValue("stateLandAreaSize", dto.getStateLandAreaSize())
                .addValue("privateLandAreaSize", dto.getPrivateLandAreaSize()));
    }

    public double getAreaSize(final long zoneId) {
        return queryForDoubleOrZero(zoneParam(zoneId), "SELECT ST_Area(geom) FROM zone WHERE zone.zone_id = :zoneId");
    }

    public double getSumOfWaterAreaSize(final long zoneId) {
        return queryForDoubleOrZero(zoneParam(zoneId), "WITH z AS (" +
                " SELECT ST_SubDivide((ST_Dump(geom)).geom, :chunkSize) AS geom" +
                " FROM zone" +
                " WHERE zone_id = :zoneId" +
                ") SELECT" +
                " SUM(ST_Area(ST_Intersection(va.geom, ST_Buffer(z.geom, 0))))" +
                " FROM z JOIN vesialue va ON ST_Intersects(va.geom, z.geom)");
    }

    public double getSumOfStateLandAreaSize(final long zoneId) {
        return queryForDoubleOrZero(zoneParam(zoneId), "WITH z AS (" +
                // 1. Split application permit area to smaller polygons
                " SELECT ST_SubDivide((ST_Dump(geom)).geom, :chunkSize) AS geom" +
                " FROM zone" +
                " WHERE zone_id = :zoneId" +
                // 2. Compute list of intersection geometries for valtionmaa
                "), z_state AS (" +
                " SELECT ST_Buffer((ST_Dump(ST_Intersection(vm.geom, ST_Buffer(z.geom, 0)))).geom, 0) as geom" +
                " FROM z JOIN valtionmaa vm ON ST_Intersects(vm.geom, z.geom)" +
                " WHERE GeometryType(z.geom) IN ('POLYGON', 'MULTIPOLYGON')" +
                // 3. Use previous result to compute intersection geometries with vesialue
                //    Skip non-polygonal intersection artifacts.
                "), z_state_water AS (" +
                " SELECT ST_Intersection(va.geom, z_state.geom) as geom" +
                " FROM z_state JOIN vesialue va ON ST_Intersects(va.geom, z_state.geom)" +
                " WHERE GeometryType(z_state.geom) IN ('POLYGON', 'MULTIPOLYGON')" +
                // 4. Sum valtionmaa intersections for total area size
                "), total_area AS (" +
                " SELECT COALESCE(SUM(ST_Area(z_state.geom)), 0) AS size FROM z_state" +
                // 5. Sum vesialue intersections for total water area size
                "), water_area AS (" +
                " SELECT COALESCE(SUM(ST_Area(z_state_water.geom)), 0) AS size FROM z_state_water" +
                // 6. Subtract water from total for final result
                ") SELECT" +
                " COALESCE(total_area.size, 0) - COALESCE(water_area.size, 0) as land_area_size" +
                " FROM total_area, water_area");
    }

    public List<GISZoneSizeRhyDTO> getSumOfAreaSizeByRhy(final long zoneId) {
        return jdbcOperations.query("WITH z AS (" +
                // 1. Split application permit area to smaller polygons
                " SELECT ST_MakeValid(ST_SubDivide(ST_MakeValid((ST_Dump(geom)).geom), :chunkSize)) AS geom" +
                " FROM zone" +
                " WHERE zone_id = :zoneId" +
                // 2. Calculate intersection geometries for Zone and RHY
                "), z_rhy AS (" +
                " SELECT rhy.id AS rhy_code, ST_MakeValid((ST_Dump(ST_Intersection(ST_SetSRID(rhy.geom, 3067), z.geom))).geom) AS geom" +
                " FROM z JOIN rhy ON ST_Intersects(rhy.geom, ST_SetSRID(z.geom, 3047))" +
                " WHERE GeometryType(z.geom) IN ('POLYGON', 'MULTIPOLYGON')" +
                // 3. Calculate intersection geometries for Zone and Water grouped by RHY
                "),  z_rhy_water AS (" +
                " SELECT rhy_code, ST_Intersection(va.geom, z_rhy.geom) AS geom" +
                " FROM z_rhy JOIN vesialue va ON ST_Intersects(va.geom, z_rhy.geom)" +
                " WHERE GeometryType(z_rhy.geom) IN ('POLYGON', 'MULTIPOLYGON')" +
                // 4. Calculate intersection geometries for Zone and Valtionmaa
                "), z_state AS (" +
                " SELECT ST_MakeValid((ST_Dump(ST_Intersection(vm.geom, z.geom))).geom) AS geom" +
                " FROM z JOIN valtionmaa vm ON ST_Intersects(vm.geom, z.geom)" +
                " WHERE GeometryType(z.geom) IN ('POLYGON', 'MULTIPOLYGON')" +
                // 5. Calculate intersection geometries for Zone and Rhy inside Valtionmaa
                "), z_state_rhy AS (" +
                " SELECT rhy_code, ST_Intersection(z_rhy.geom, z_state.geom) AS geom" +
                " FROM z_state JOIN z_rhy ON ST_Intersects(z_rhy.geom, z_state.geom)" +
                " WHERE GeometryType(z_state.geom) IN ('POLYGON', 'MULTIPOLYGON')" +
                " AND GeometryType(z_rhy.geom) IN ('POLYGON', 'MULTIPOLYGON')" +
                // 6. Calculate intersection geometries for Zone and Water inside Valtionmaa
                "), z_state_water AS (" +
                " SELECT ST_MakeValid((ST_Dump(ST_Intersection(va.geom, z_state.geom))).geom) AS geom" +
                " FROM z_state JOIN vesialue va ON ST_Intersects(va.geom, z_state.geom)" +
                " WHERE GeometryType(z_state.geom) IN ('POLYGON', 'MULTIPOLYGON')" +
                // 7. Calculate intersection geometries for Zone And Water inside Valtionmaa grouped by RHY
                "), z_state_water_rhy AS (" +
                " SELECT rhy_code, ST_Intersection(z_rhy.geom, z_state_water.geom) AS geom" +
                " FROM z_state_water JOIN z_rhy ON ST_Intersects(z_rhy.geom, z_state_water.geom)" +
                " WHERE GeometryType(z_state_water.geom) IN ('POLYGON', 'MULTIPOLYGON')" +
                " AND GeometryType(z_rhy.geom) IN ('POLYGON', 'MULTIPOLYGON')" +
                // 8. Calculate combined geometry area size for total, land and water area size
                "), total_area AS (" +
                " SELECT rhy_code, COALESCE(SUM(ST_Area(z_rhy.geom)), 0) AS size FROM z_rhy GROUP BY rhy_code" +
                "), water_area AS (" +
                " SELECT rhy_code, COALESCE(SUM(ST_Area(z_rhy_water.geom)), 0) AS size FROM z_rhy_water GROUP BY rhy_code" +
                "), land_area AS (" +
                " SELECT rhy_code, COALESCE(total_area.size, 0) - COALESCE(water_area.size, 0) AS size FROM total_area LEFT JOIN water_area USING (rhy_code)" +
                "), state_area AS (" +
                " SELECT rhy_code, COALESCE(SUM(ST_Area(z_state_rhy.geom)), 0) AS size FROM z_state_rhy GROUP BY rhy_code" +
                "), state_water_area AS (" +
                " SELECT rhy_code, COALESCE(SUM(ST_Area(z_state_water_rhy.geom)), 0) AS size FROM z_state_water_rhy GROUP BY rhy_code" +
                "), state_land_area AS (" +
                " SELECT rhy_code, COALESCE(state_area.size, 0) - COALESCE(state_water_area.size, 0) AS size FROM state_area LEFT JOIN state_water_area USING (rhy_code)" +
                "), private_area AS (" +
                "  SELECT rhy_code, COALESCE(total_area.size, 0) - COALESCE(state_area.size, 0) AS size" +
                "  FROM total_area LEFT JOIN state_area USING (rhy_code)" +
                "), private_land_area AS (" +
                " SELECT rhy_code, COALESCE(land_area.size, 0) - COALESCE(state_land_area.size, 0) AS size" +
                " FROM land_area " +
                " LEFT JOIN state_land_area USING (rhy_code)" +
                "), private_water_area AS (" +
                " SELECT rhy_code, COALESCE(private_area.size, 0) - COALESCE(private_land_area.size, 0) AS size" +
                " FROM private_area " +
                " LEFT JOIN private_land_area USING (rhy_code)" +
                ")" +
                "SELECT" +
                " rhy_code," +
                " COALESCE(total_area.size, 0) AS total, " +
                " COALESCE(water_area.size, 0) AS water," +
                " COALESCE(land_area.size, 0) AS land," +
                " COALESCE(state_area.size, 0) AS state, " +
                " COALESCE(state_land_area.size, 0) AS state_land," +
                " COALESCE(state_water_area.size, 0) AS state_water, " +
                " COALESCE(private_area.size, 0) AS private," +
                " COALESCE(private_land_area.size, 0) AS private_land," +
                " COALESCE(private_water_area.size, 0) AS private_water" +
                " FROM total_area " +
                " LEFT JOIN water_area USING (rhy_code)" +
                " LEFT JOIN land_area USING (rhy_code)" +
                " LEFT JOIN state_area USING (rhy_code)" +
                " LEFT JOIN state_water_area USING (rhy_code)" +
                " LEFT JOIN state_land_area USING (rhy_code)" +
                " LEFT JOIN private_area USING (rhy_code)" +
                " LEFT JOIN private_land_area USING (rhy_code)" +
                " LEFT JOIN private_water_area USING (rhy_code);", zoneParam(zoneId), (rs, i) -> {

            final TotalLandWaterSizeDTO bothSize = new TotalLandWaterSizeDTO(
                    rs.getDouble("total"),
                    rs.getDouble("land"),
                    rs.getDouble("water"));
            final TotalLandWaterSizeDTO stateSize = new TotalLandWaterSizeDTO(
                    rs.getDouble("state"),
                    rs.getDouble("state_land"),
                    rs.getDouble("state_water"));
            final TotalLandWaterSizeDTO privateSize = new TotalLandWaterSizeDTO(
                    rs.getDouble("private"),
                    rs.getDouble("private_land"),
                    rs.getDouble("private_water"));

            final String rhyOfficialCode = rs.getString("rhy_code");

            return new GISZoneSizeRhyDTO(rhyOfficialCode, bothSize, stateSize, privateSize);
        });
    }

    public Map<String, Double> getSumOfAreaSizeByHta(final long zoneId) {
        return jdbcOperations.query("WITH z AS (" +
                " SELECT ST_SubDivide((ST_Dump(geom)).geom, :chunkSize) AS geom" +
                " FROM zone" +
                " WHERE zone_id = :zoneId" +
                ") SELECT" +
                " hta.numero AS hta_code," +
                " SUM(ST_Area(ST_Intersection(hta.geom, ST_Buffer(z.geom, 0)))) AS area_size" +
                " FROM z JOIN hta ON ST_Intersects(hta.geom, z.geom)" +
                " GROUP BY hta.numero", zoneParam(zoneId), (resultSet, i) -> {
            final String rhyOfficialCode = resultSet.getString("hta_code");
            final double areaSize = resultSet.getDouble("area_size");
            return Tuple.of(rhyOfficialCode, areaSize);
        }).stream().collect(Collectors.toMap(t -> t._1, t -> t._2));
    }

    private double queryForDoubleOrZero(final MapSqlParameterSource params, final String sql) {
        final double defaultValue = 0.0;
        final Double result = JdbcTemplateUtils.queryForDouble(sql, defaultValue, jdbcOperations, params);
        return Optional.ofNullable(result).orElse(defaultValue);
    }

    private static MapSqlParameterSource zoneParam(final long zoneId) {
        return new MapSqlParameterSource("zoneId", zoneId)
                .addValue("chunkSize", 8192);
    }
}
