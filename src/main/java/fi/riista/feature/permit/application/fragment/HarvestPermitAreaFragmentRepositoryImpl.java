package fi.riista.feature.permit.application.fragment;

import fi.riista.feature.common.entity.PropertyIdentifier;
import fi.riista.feature.gis.GISPoint;
import fi.riista.feature.gis.metsahallitus.MetsahallitusHirviRepository;
import fi.riista.feature.gis.metsahallitus.MetsahallitusProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Repository
public class HarvestPermitAreaFragmentRepositoryImpl implements HarvestPermitAreaFragmentRepository {

    @Resource
    private MetsahallitusProperties metsahallitusProperties;

    @Resource
    private MetsahallitusHirviRepository metsahallitusHirviRepository;

    private NamedParameterJdbcOperations jdbcOperations;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcOperations = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HarvestPermitAreaFragmentInfoDTO> getFragmentInfo(final Long applicationId) {
        return getFragments(applicationId, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HarvestPermitAreaFragmentInfoDTO> getFragmentInfoInLocation(final Long applicationId,
                                                                            final GISPoint gisPoint) {
        return getFragments(applicationId, gisPoint.toWellKnownText());
    }

    private List<HarvestPermitAreaFragmentInfoDTO> getFragments(final Long applicationId, final String point) {
        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("applicationId", applicationId)
                .addValue("point", point);

        final List<HarvestPermitAreaFragmentInfoDTO> fragments = jdbcOperations.query(
                getFragmentsSql(point != null),
                params, (rs, i) -> new HarvestPermitAreaFragmentInfoDTO(
                        rs.getString("hash"),
                        rs.getDouble("area_size"),
                        rs.getDouble("water_area_size"),
                        rs.getDouble("valtionmaa_area_size"),
                        rs.getDouble("valtionmaa_water_area_size"),
                        PropertyIdentifier.create(rs.getLong("property_identifier")).getDelimitedValue(),
                        rs.getDouble("property_area_size"),
                        rs.getInt("property_id")
                )).stream()
                // Do not list any 0.00 ha areas
                .filter(f -> f.getAreaSize() >= 100)
                .filter(f -> f.getPropertyArea() >= 100)
                .collect(toList());

        final List<Integer> palstaIds = fragments.stream().map(HarvestPermitAreaFragmentInfoDTO::getPropertyId).collect(Collectors.toList());
        final Set<Integer> metsahallitusPalstaIds =
                metsahallitusHirviRepository.filterPalstaIntersectingHirvi(palstaIds, metsahallitusProperties.getLatestMetsahallitusYear());
        fragments.forEach(f ->
                f.setMetsahallitus(metsahallitusPalstaIds.contains(f.getPropertyId())));

        return fragments;
    }

    private static String getFragmentsSql(final boolean usePointFilter) {
        return "WITH geoms AS (\n" +
                "    SELECT (ST_Dump(geom)).geom AS geom\n" +
                "    FROM harvest_permit_application application\n" +
                "      JOIN harvest_permit_area area on area.harvest_permit_area_id = application.area_id\n" +
                "      JOIN zone z on z.zone_id = area.zone_id\n" +
                "    WHERE application.harvest_permit_application_id = :applicationId\n" +
                "), sirpaleet AS (\n" +
                "    SELECT\n" +
                "      geoms.geom AS geom,\n" +
                "      ST_Area(geoms.geom) AS area_size,\n" +
                "      ST_Geohash(ST_Transform(ST_Centroid(geoms.geom), 4326), 8) AS hash\n" +
                "    FROM geoms\n" +
                "    WHERE ST_Area(geoms.geom) < 1000 * 10000\n" +

                (usePointFilter ? "AND ST_Contains(geoms.geom, ST_GeomFromText(:point, 3067))" : "") +

                "), water_areas AS (\n" +
                "    SELECT\n" +
                "      SUM(ST_Area(ST_Intersection(va.geom, s.geom))) AS water_area_size,\n" +
                "      s.hash\n" +
                "    FROM sirpaleet s\n" +
                "      JOIN vesialue va ON ST_Intersects(va.geom, s.geom)\n" +
                "    GROUP BY s.hash\n" +
                "), valtionmaa_area AS (\n" +
                "    SELECT\n" +
                "      SUM(ST_Area(ST_Intersection(vm.geom, s.geom))) AS valtionmaa_area_size,\n" +
                "      s.hash\n" +
                "    FROM sirpaleet s\n" +
                "      JOIN valtionmaa vm ON ST_Intersects(vm.geom, s.geom)\n" +
                "    GROUP BY s.hash\n" +
                "), valtionmaa_waterarea AS (\n" +
                "    WITH vageom AS (\n" +
                "        SELECT\n" +
                "          ST_Buffer(ST_Collect(ST_Intersection(vm.geom, s.geom)), 0) AS geom,\n" +
                "          s.hash\n" +
                "        FROM sirpaleet s\n" +
                "          JOIN valtionmaa vm ON ST_Intersects(vm.geom, s.geom)\n" +
                "        GROUP BY s.hash\n" +
                "    )\n" +
                "    SELECT\n" +
                "      SUM(ST_Area(ST_Intersection(va.geom, vageom.geom))) AS valtionmaa_water_area_size,\n" +
                "      vageom.hash\n" +
                "    FROM vageom\n" +
                "      JOIN vesialue va ON ST_Intersects(va.geom, vageom.geom)\n" +
                "    GROUP BY vageom.hash\n" +
                "), properties AS (\n" +
                "    SELECT\n" +
                "      pa.id,\n" +
                "      pa.tunnus,\n" +
                "      SUM(ST_Area(ST_Intersection(pa.geom, s.geom))) AS palsta_area_size,\n" +
                "      s.hash\n" +
                "    FROM sirpaleet s\n" +
                "      LEFT JOIN palstaalue pa ON ST_Intersects(pa.geom, s.geom)\n" +
                "    GROUP BY pa.id, pa.tunnus, s.hash\n" +
                ")\n" +
                "SELECT s.hash,\n" +
                "  s.area_size as area_size,\n" +
                "  COALESCE(wa.water_area_size, 0) AS water_area_size,\n" +
                "  COALESCE(va.valtionmaa_area_size, 0) AS valtionmaa_area_size,\n" +
                "  COALESCE(vawa.valtionmaa_water_area_size, 0) AS valtionmaa_water_area_size,\n" +
                "  p.tunnus AS property_identifier,\n" +
                "  p.palsta_area_size AS property_area_size,\n" +
                "  p.id as property_id\n" +
                "FROM sirpaleet s\n" +
                "LEFT JOIN water_areas wa ON wa.hash = s.hash\n" +
                "LEFT JOIN valtionmaa_area va ON va.hash = s.hash\n" +
                "LEFT JOIN valtionmaa_waterarea vawa ON vawa.hash = s.hash\n" +
                "JOIN properties p ON p.hash = s.hash\n" +
                "ORDER BY area_size DESC, palsta_area_size DESC";
    }
}
