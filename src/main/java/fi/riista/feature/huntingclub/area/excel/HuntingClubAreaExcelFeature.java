package fi.riista.feature.huntingclub.area.excel;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.SQLQueryFactory;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.common.entity.PropertyIdentifier;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.security.EntityPermission;
import fi.riista.sql.SQPalstaalue;
import fi.riista.sql.SQZonePalsta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.List;
import java.util.Locale;

import static java.util.Collections.emptyList;

@Component
public class HuntingClubAreaExcelFeature {
    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private SQLQueryFactory sqlQueryFactory;

    @Resource
    private MessageSource messageSource;

    private NamedParameterJdbcOperations namedParameterJdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Transactional(readOnly = true)
    public HuntingClubAreaExcelView exportAll(final long clubAreaId) {
        final HuntingClubArea huntingClubArea = requireEntityService
                .requireHuntingClubArea(clubAreaId, EntityPermission.READ);

        final Locale locale = LocaleContextHolder.getLocale();
        final EnumLocaliser enumLocaliser = new EnumLocaliser(messageSource, locale);

        return new HuntingClubAreaExcelView(locale, enumLocaliser,
                huntingClubArea.getClub().getNameLocalisation(),
                huntingClubArea.getNameLocalisation(),
                fetchAll(huntingClubArea));
    }

    @Transactional(readOnly = true)
    public HuntingClubAreaChangedExcelView exportChanged(final long clubAreaId) {
        final HuntingClubArea huntingClubArea = requireEntityService
                .requireHuntingClubArea(clubAreaId, EntityPermission.READ);

        final Locale locale = LocaleContextHolder.getLocale();
        final EnumLocaliser enumLocaliser = new EnumLocaliser(messageSource, locale);

        return new HuntingClubAreaChangedExcelView(locale, enumLocaliser,
                huntingClubArea.getClub().getNameLocalisation(),
                huntingClubArea.getNameLocalisation(),
                fetchChanged(huntingClubArea));
    }

    private List<HuntingClubAreaExcelView.ExcelRow> fetchAll(final HuntingClubArea huntingClubArea) {
        return huntingClubArea.getZone() == null ? emptyList() : fetchAll(huntingClubArea.getZone());
    }

    private List<HuntingClubAreaChangedExcelView.ExcelRow> fetchChanged(final HuntingClubArea huntingClubArea) {
        return huntingClubArea.getZone() == null ? emptyList() : fetchChanged(huntingClubArea.getZone());
    }

    private List<HuntingClubAreaExcelView.ExcelRow> fetchAll(final @Nonnull GISZone zoneEntity) {
        // TODO: Convert to QueryDSL when it supports calculating ST_Area for arbitrary geometry expression
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
                " pa.nimi AS nimi," +
                " ST_Area(zp.geom) AS area_size," +
                " COALESCE(ex.excluded_size, 0) AS excluded_size," +
                " zp.is_changed" +
                " FROM zone_palsta zp" +
                " LEFT JOIN ex USING (palsta_id)" +
                " LEFT JOIN palstaalue pa ON (pa.id = zp.palsta_id)" +
                " WHERE zp.zone_id = :zoneId" +
                " ORDER BY zp.palsta_tunnus, zp.palsta_id;";

        final MapSqlParameterSource params = new MapSqlParameterSource("zoneId", zoneEntity.getId());

        return namedParameterJdbcTemplate.query(sql, params, (rs, i) -> new HuntingClubAreaExcelView.ExcelRow(
                rs.getInt("id"),
                PropertyIdentifier.create(rs.getLong("tunnus")),
                rs.getString("nimi"),
                rs.getDouble("area_size"),
                rs.getDouble("excluded_size"),
                rs.getBoolean("is_changed")
        ));
    }

    private List<HuntingClubAreaChangedExcelView.ExcelRow> fetchChanged(final @Nonnull GISZone zoneEntity) {
        final SQZonePalsta zonePalsta = new SQZonePalsta("zp");
        final SQPalstaalue pa1 = new SQPalstaalue("pa1"); // Current
        final SQPalstaalue pa2 = new SQPalstaalue("pa2"); // New

        final NumberPath<Integer> pathPalstaId = zonePalsta.palstaId;
        final NumberPath<Integer> pathPalstaIdNew = zonePalsta.newPalstaId;

        final NumberPath<Long> pathPalstaTunnus = zonePalsta.palstaTunnus;
        final NumberPath<Long> pathPalstaTunnusNew = zonePalsta.newPalstaTunnus;

        final StringPath pathPalstaName = pa1.nimi;
        final StringPath pathPalstaNameNew = pa2.nimi;

        final NumberPath<Double> pathAreaDiff = zonePalsta.diffArea;
        final NumberExpression<Double> pathAreaSize = zonePalsta.geom.asMultiPolygon().area();

        return sqlQueryFactory
                .from(zonePalsta)
                .leftJoin(pa1).on(pa1.id.eq(pathPalstaId))
                .leftJoin(pa2).on(pa2.id.eq(pathPalstaIdNew))
                .where(zonePalsta.zoneId.eq(zoneEntity.getId())
                        .and(zonePalsta.isChanged.isTrue()))
                .select(Projections.constructor(HuntingClubAreaChangedExcelView.ExcelRow.class,
                        pathPalstaTunnus, pathPalstaTunnusNew,
                        pathPalstaName, pathPalstaNameNew,
                        pathAreaSize, pathAreaDiff))
                .orderBy(pathPalstaTunnus.asc(), pathPalstaId.asc())
                .fetch();
    }
}
