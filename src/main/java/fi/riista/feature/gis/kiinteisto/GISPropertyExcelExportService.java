package fi.riista.feature.gis.kiinteisto;

import com.google.common.collect.ImmutableList;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.SQLQueryFactory;
import fi.riista.feature.common.entity.PropertyIdentifier;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.gis.zone.GISZoneMmlPropertyIntersectionDTO;
import fi.riista.sql.SQKiinteistoNimet;
import fi.riista.sql.SQZonePalsta;
import fi.riista.util.JdbcTemplateEnhancer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.List;

@Component
public class GISPropertyExcelExportService {

    @Resource
    private SQLQueryFactory sqlQueryFactory;

    private NamedParameterJdbcOperations namedParameterJdbcTemplate;
    private NamedParameterJdbcOperations enhancedJdbcTemplate;

    @Autowired
    public void setDataSource(final DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.enhancedJdbcTemplate = JdbcTemplateEnhancer.wrap(this.namedParameterJdbcTemplate);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<GISPropertyExcelRow> fetchAll(final @Nonnull GISZone zoneEntity) {
        final String sql = "WITH e AS (" +
                " SELECT (ST_Dump(ST_Transform(excluded_geom, 3067))).geom as geom" +
                " FROM zone WHERE zone_id = :zoneId" +
                "), ex AS (SELECT " +
                "  z.palsta_id," +
                "  SUM(ST_Area(ST_Intersection(e.geom, z.geom))) AS excluded_size" +
                "  FROM zone_palsta z JOIN e ON z.geom && e.geom" +
                "  WHERE z.zone_id = :zoneId" +
                "  GROUP BY z.palsta_id" +
                ") SELECT" +
                " zp.palsta_id AS id," +
                " zp.palsta_tunnus AS tunnus," +
                " kn.nimi AS nimi," +
                " ST_Area(zp.geom) AS area_size," +
                " COALESCE(ex.excluded_size, 0) AS excluded_size," +
                " zp.is_changed" +
                " FROM zone_palsta zp" +
                " LEFT JOIN ex USING (palsta_id)" +
                " LEFT JOIN kiinteisto_nimet kn ON (kn.tunnus = zp.palsta_tunnus)" +
                " WHERE zp.zone_id = :zoneId" +
                " ORDER BY zp.palsta_tunnus, zp.palsta_id;";

        final MapSqlParameterSource params = new MapSqlParameterSource("zoneId", zoneEntity.getId());

        return namedParameterJdbcTemplate.query(sql, params, (rs, i) -> new GISPropertyExcelRow(
                rs.getInt("id"),
                PropertyIdentifier.create(rs.getLong("tunnus")),
                rs.getString("nimi"),
                rs.getDouble("area_size"),
                rs.getDouble("excluded_size"),
                rs.getBoolean("is_changed")
        ));
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<GISPropertyExcelChangedView.ExcelRow> fetchChanged(final @Nonnull GISZone zoneEntity) {
        final SQZonePalsta ZONE_PALSTA = new SQZonePalsta("zp");
        final SQKiinteistoNimet NIMI_A = new SQKiinteistoNimet("n1"); // Current
        final SQKiinteistoNimet NIMI_B = new SQKiinteistoNimet("n2"); // New

        final NumberPath<Integer> pathPalstaId = ZONE_PALSTA.palstaId;

        final NumberPath<Long> pathPalstaTunnus = ZONE_PALSTA.palstaTunnus;
        final NumberPath<Long> pathPalstaTunnusNew = ZONE_PALSTA.newPalstaTunnus;

        final StringPath pathPalstaName = NIMI_A.nimi;
        final StringPath pathPalstaNameNew = NIMI_B.nimi;

        final NumberPath<Double> pathAreaDiff = ZONE_PALSTA.diffArea;
        final NumberExpression<Double> pathAreaSize = ZONE_PALSTA.geom.asMultiPolygon().area();

        return sqlQueryFactory
                .from(ZONE_PALSTA)
                .leftJoin(NIMI_A).on(NIMI_A.tunnus.eq(pathPalstaTunnus))
                .leftJoin(NIMI_B).on(NIMI_B.tunnus.eq(pathPalstaTunnusNew))
                .where(ZONE_PALSTA.zoneId.eq(zoneEntity.getId())
                        .and(ZONE_PALSTA.isChanged.isTrue()))
                .select(Projections.constructor(GISPropertyExcelChangedView.ExcelRow.class,
                        pathPalstaTunnus, pathPalstaTunnusNew,
                        pathPalstaName, pathPalstaNameNew,
                        pathAreaSize, pathAreaDiff))
                .orderBy(pathPalstaTunnus.asc(), pathPalstaId.asc())
                .fetch();
    }


    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<GISZoneMmlPropertyIntersectionDTO> findIntersectingPalsta(final @Nonnull GISZone zoneEntity) {
        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("chunkSize", 16384)
                .addValue("minimumIntersectionArea", 100)
                .addValue("zoneId", zoneEntity.getId());

        final String queryGeometry;

        switch (zoneEntity.getSourceType()) {
            case LOCAL:
                // Select union from free form geometries as they may overlap
                queryGeometry = "SELECT ST_Union(geom) AS geom FROM zone_feature WHERE zone_id = :zoneId ";
                break;
            case EXTERNAL:
                // Calculate palsta information for imported geometry
                queryGeometry = "SELECT ST_MakeValid(ST_SubDivide(geom, :chunkSize)) AS geom FROM zone WHERE zone_id = :zoneId ";
                break;
            default:
                throw new IllegalArgumentException();
        }

        // - Sum up intersection with zone by palsta
        final String queryPalstaIntersection = "SELECT " +
                "    pa.id AS id, " +
                "    SUM(ST_Area(ST_Intersection(pa.geom, geometry.geom))) AS area " +
                "  FROM geometry " +
                "  INNER JOIN palstaalue pa ON ST_Intersects(pa.geom, geometry.geom) " +
                "  GROUP BY pa.id ";

        final String sql = "WITH " +
                "geometry AS ( " + queryGeometry + "), " +
                "palstaIntersection AS ( " + queryPalstaIntersection + " ) " +
                // - Select palstas with intersection at least on 'minimumIntersectionArea' and join property name
                "SELECT " +
                "  pa2.tunnus AS tunnus, " +
                "  pa2.id AS id, " +
                "  k.nimi AS name, " +
                "  palstaIntersection.area AS area " +
                "FROM palstaIntersection " +
                "INNER JOIN palstaalue pa2 ON pa2.id = palstaIntersection.id " +
                "LEFT JOIN kiinteisto_nimet k ON pa2.tunnus = k.tunnus " +
                "WHERE ( palstaIntersection.area > :minimumIntersectionArea ) " +
                "ORDER BY tunnus, id";

        return ImmutableList.copyOf(enhancedJdbcTemplate.query(
                sql,
                params,
                (resultSet, i) -> new GISZoneMmlPropertyIntersectionDTO(
                        zoneEntity.getId(),
                        resultSet.getLong("tunnus"),
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getDouble("area"))));
    }

}
