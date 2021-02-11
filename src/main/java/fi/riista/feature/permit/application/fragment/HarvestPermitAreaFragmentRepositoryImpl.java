package fi.riista.feature.permit.application.fragment;

import fi.riista.util.Collect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

/**
 * Repository implementation for harvest permit area fragments. A fragment is a small separate
 * area in the harvest permit area. For moose, separate area with area less than 1000 hectares
 * is considered as a fragment. For deer the limit is 500 hectares.
 */
@Repository
public class HarvestPermitAreaFragmentRepositoryImpl implements HarvestPermitAreaFragmentRepository {

    private static final String QUERY_FRAGMENTS = "" +
            " SELECT" +
            "   polygons.geom AS geom," +
            "   ST_Area(polygons.geom) AS area_size," +
            "   ST_Geohash(ST_Transform(ST_PointOnSurface(polygons.geom), 4326), 8) AS hash" +
            " FROM (SELECT (ST_Dump(zone.geom)).geom AS geom FROM zone WHERE zone.zone_id = :zoneId) polygons" +
            " WHERE ST_Area(polygons.geom) BETWEEN :minFragmentSize AND :maxFragmentSize";

    private static final String QUERY_FRAGMENTS_BY_HASHES =
            QUERY_FRAGMENTS +
                    " AND ST_Geohash(ST_Transform(ST_PointOnSurface(polygons.geom), 4326), 8) IN (:hashes) ";

    private static final String QUERY_FRAGMENTS_WITH_LOCATION = QUERY_FRAGMENTS +
            " AND ST_Contains(polygons.geom, ST_Transform(ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326), " +
            "3067))";

    private static MapSqlParameterSource toJdbcParams(final HarvestPermitAreaFragmentQueryParams queryParams,
                                                      final List<String> fragmentHashes) {
        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("zoneId", queryParams.getZoneId())
                .addValue("metsahallitusYear", queryParams.getMetsahallitusYear())
                .addValue("intersectionBuffer", -0.01)
                .addValue("minFragmentSize", 100)
                .addValue("maxFragmentSize", queryParams.getFragmentSizeLimit())
                .addValue("chunkSize", 8192);

        if (queryParams.hasLocation()) {
            params.addValue("latitude", queryParams.getLocation().getLatitude());
            params.addValue("longitude", queryParams.getLocation().getLongitude());
        }

        if (fragmentHashes != null) {
            params.addValue("hashes", fragmentHashes);
        }

        return params;
    }

    private NamedParameterJdbcOperations jdbcOperations;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcOperations = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HarvestPermitAreaFragmentSizeDTO> getFragmentSize(final HarvestPermitAreaFragmentQueryParams params) {
        final String query = params.hasLocation() ? QUERY_FRAGMENTS_WITH_LOCATION : QUERY_FRAGMENTS;
        return queryFragmentSize(query, params, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HarvestPermitAreaFragmentSizeDTO> getFragmentSize(final HarvestPermitAreaFragmentQueryParams params,
                                                                  final List<String> fragmentHashes) {
        return queryFragmentSize(QUERY_FRAGMENTS_BY_HASHES, params, fragmentHashes);
    }

    private List<HarvestPermitAreaFragmentSizeDTO> queryFragmentSize(final String query,
                                                                     final HarvestPermitAreaFragmentQueryParams params,
                                                                     final List<String> fragmentHashes) {
        final String sql = "WITH fragments AS (" +
                query +
                "), water AS (" +
                "    SELECT f.hash, ST_Intersection(va.geom, f.geom) AS geom" +
                "    FROM fragments f JOIN vesialue va ON ST_Intersects(va.geom, f.geom)" +
                "), water_area AS ( " +
                "    SELECT water.hash, SUM(ST_Area(water.geom)) AS area_size" +
                "    FROM water" +
                "    GROUP BY water.hash" +
                "), state AS (" +
                "    SELECT f.hash, ST_Buffer(ST_Intersection(vm.geom, f.geom), 0) AS geom" +
                "    FROM fragments f JOIN valtionmaa vm ON ST_Intersects(vm.geom, f.geom)" +
                "), state_area AS (" +
                "    SELECT state.hash, SUM(ST_Area(state.geom)) AS area_size" +
                "    FROM state" +
                "    GROUP BY state.hash" +
                "), state_water AS (" +
                "    SELECT state.hash, ST_Intersection(state.geom, vesialue.geom) AS geom" +
                "    FROM state JOIN vesialue ON ST_Intersects(state.geom, vesialue.geom)" +
                "    WHERE GeometryType(state.geom) IN ('POLYGON', 'MULTIPOLYGON')" +
                "), state_water_area AS (" +
                "    SELECT state_water.hash, SUM(ST_Area(state_water.geom)) AS area_size" +
                "    FROM state_water" +
                "    GROUP BY state_water.hash" +
                ") SELECT" +
                "   fragments.hash             AS hash," +
                "   fragments.area_size        AS area_size," +
                "   water_area.area_size       AS water_area_size," +
                "   state_area.area_size       AS state_area_size," +
                "   state_water_area.area_size AS state_water_size" +
                " FROM fragments" +
                " LEFT JOIN water_area ON (water_area.hash = fragments.hash)" +
                " LEFT JOIN state_area ON (state_area.hash = fragments.hash)" +
                " LEFT JOIN state_water_area ON (state_water_area.hash = fragments.hash)" +
                " ORDER BY fragments.area_size DESC";

        return jdbcOperations.query(sql, toJdbcParams(params, fragmentHashes),
                (rs, i) -> new HarvestPermitAreaFragmentSizeDTO(
                        rs.getString("hash"),
                        rs.getDouble("area_size"),
                        rs.getDouble("water_area_size"),
                        rs.getDouble("state_area_size"),
                        rs.getDouble("state_water_size")));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, List<HarvestPermitAreaFragmentPropertyDTO>> getFragmentProperty(
            final HarvestPermitAreaFragmentQueryParams params) {

        final String sql = "WITH fragments AS (" +
                (params.hasLocation() ? QUERY_FRAGMENTS_WITH_LOCATION : QUERY_FRAGMENTS) +
                // 1. Break fragment geometry in smaller parts for intersection calculation
                "), spliced_fragments AS (" +
                "         SELECT hash AS hash, " +
                "                ST_Buffer(ST_SubDivide(geom, :chunkSize), 0) AS geom " +
                "         FROM fragments " +
                // 2. Calculate fragment area size by MML palsta
                " ), palsta_area AS (" +
                "    SELECT f.hash AS hash," +
                "           pa.id AS palsta_id, pa.tunnus," +
                "           ST_Intersection(pa.geom, f.geom) AS geom" +
                "    FROM spliced_fragments f JOIN palstaalue pa" +
                "    ON (pa.geom && f.geom AND ST_Intersects(pa.geom,f.geom))" +
                // 3. Calculate total area size group by MML kiinteistö grouping by fragment hash and palstatunnus
                "), property_size AS (" +
                "    SELECT pa.hash, pa.tunnus, SUM(ST_Area(pa.geom)) AS area_size" +
                "    FROM palsta_area pa" +
                "    GROUP BY pa.hash, pa.tunnus" +
                // 4. Calculate count of intersecting valtionmaa geometries grouped by MML kiinteistö
                "), property_metsahallitus AS (" +
                "    SELECT pa.hash, pa.tunnus, COUNT(valtionmaa.gid) AS amount" +
                "    FROM palsta_area pa JOIN valtionmaa " +
                "    ON (valtionmaa.geom && pa.geom AND ST_Intersects(valtionmaa.geom, ST_Buffer(pa.geom, " +
                ":intersectionBuffer)))" +
                "    GROUP BY pa.hash, pa.tunnus" +
                ") SELECT" +
                "  ps.hash AS hash," +
                "  ps.tunnus AS property_identifier," +
                "  kn.nimi AS property_name," +
                "  ps.area_size AS property_area_size," +
                "  COALESCE(pm.amount, 0) > 0 AS metsahallitus" +
                " FROM property_size ps" +
                " LEFT JOIN property_metsahallitus pm ON (ps.hash = pm.hash AND ps.tunnus = pm.tunnus)" +
                " LEFT JOIN kiinteisto_nimet kn ON (ps.tunnus = kn.tunnus)" +
                " WHERE ps.area_size > :minFragmentSize " +
                " ORDER BY ps.area_size DESC";

        final List<HarvestPermitAreaFragmentPropertyDTO> rows = jdbcOperations.query(sql, toJdbcParams(params, null),
                (rs, i) -> new HarvestPermitAreaFragmentPropertyDTO(
                        rs.getString("hash"),
                        rs.getLong("property_identifier"),
                        rs.getString("property_name"),
                        rs.getDouble("property_area_size"),
                        rs.getBoolean("metsahallitus")));

        return rows.stream().collect(Collect.nullSafeGroupingBy(HarvestPermitAreaFragmentPropertyDTO::getHash));
    }

}
