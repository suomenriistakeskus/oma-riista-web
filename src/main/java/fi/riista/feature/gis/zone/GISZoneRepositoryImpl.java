package fi.riista.feature.gis.zone;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.sql.JPASQLQuery;
import com.querydsl.sql.SQLTemplates;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import fi.riista.config.jackson.CustomJacksonObjectMapper;
import fi.riista.feature.gis.geojson.PalstaFeatureCollection;
import fi.riista.feature.gis.zone.query.CalculateCombinedGeometryQueries;
import fi.riista.feature.gis.zone.query.CalculateZoneAreaSizeQueries;
import fi.riista.feature.gis.zone.query.CopyZoneGeometryQueries;
import fi.riista.feature.gis.zone.query.GetCombinedFeatureCollectionQuery;
import fi.riista.feature.gis.zone.query.GetExternalFeatureCollectionQuery;
import fi.riista.feature.gis.zone.query.GetPalstaFeatureCollectionQuery;
import fi.riista.feature.gis.zone.query.UpdateExternalFeatureQueries;
import fi.riista.feature.gis.zone.query.UpdatePalstaFeatureQueries;
import fi.riista.feature.huntingclub.area.zone.HuntingClubAreaFeatureDTO;
import fi.riista.sql.SQZone;
import fi.riista.util.GISUtils;
import fi.riista.util.JdbcTemplateEnhancer;
import org.geojson.FeatureCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyList;

@Repository
public class GISZoneRepositoryImpl implements GISZoneRepositoryCustom {

    private JdbcOperations jdbcTemplate;
    private NamedParameterJdbcOperations namedParameterJdbcTemplate;
    private NamedParameterJdbcOperations enchancedJdbcTemplate;

    @Resource
    private CustomJacksonObjectMapper objectMapper;

    @PersistenceContext
    private EntityManager entityManager;

    @Resource
    private SQLTemplates queryDslSqlTemplates;

    private GetPalstaFeatureCollectionQuery getPalstaFeatureCollectionQuery;
    private GetExternalFeatureCollectionQuery getExternalFeatureCollectionQuery;
    private GetCombinedFeatureCollectionQuery getCombinedFeatureCollectionQuery;
    private UpdatePalstaFeatureQueries updatePalstaFeatureQueries;
    private UpdateExternalFeatureQueries updateExternalFeatureQueries;
    private CalculateZoneAreaSizeQueries calculateZoneAreaSizeQueries;
    private CalculateCombinedGeometryQueries calculateCombinedGeometryQueries;
    private CopyZoneGeometryQueries copyZoneGeometryQueries;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(this.jdbcTemplate);
        this.enchancedJdbcTemplate = JdbcTemplateEnhancer.wrap(this.namedParameterJdbcTemplate);
        this.getPalstaFeatureCollectionQuery = new GetPalstaFeatureCollectionQuery(namedParameterJdbcTemplate, objectMapper);
        this.getExternalFeatureCollectionQuery = new GetExternalFeatureCollectionQuery(namedParameterJdbcTemplate, objectMapper);
        this.getCombinedFeatureCollectionQuery = new GetCombinedFeatureCollectionQuery(namedParameterJdbcTemplate, objectMapper);
        this.updatePalstaFeatureQueries = new UpdatePalstaFeatureQueries(jdbcTemplate);
        this.updateExternalFeatureQueries = new UpdateExternalFeatureQueries(jdbcTemplate);
        this.calculateCombinedGeometryQueries = new CalculateCombinedGeometryQueries(this.enchancedJdbcTemplate);
        this.calculateZoneAreaSizeQueries = new CalculateZoneAreaSizeQueries(enchancedJdbcTemplate);
        this.copyZoneGeometryQueries = new CopyZoneGeometryQueries(namedParameterJdbcTemplate);
    }

    @Override
    @Transactional(readOnly = true)
    public double[] getBounds(final long zoneId, final GISUtils.SRID srid) {
        final String sql = "WITH extent AS " +
                "(SELECT ST_Extent(ST_Transform(geom, :srid)) AS e FROM zone WHERE zone_id = :zoneId)" +
                " SELECT ST_XMin(e) AS xmin, ST_YMin(e) AS ymin, ST_XMax(e) AS xmax, ST_YMax(e) AS ymax FROM extent";

        return namedParameterJdbcTemplate.queryForObject(sql, new MapSqlParameterSource()
                .addValue("srid", srid.getValue())
                .addValue("zoneId", zoneId), (rs, rowNum) -> new double[]{
                rs.getDouble("xmin"),
                rs.getDouble("ymin"),
                rs.getDouble("xmax"),
                rs.getDouble("ymax")
        });
    }

    @Override
    @Transactional(readOnly = true)
    public FeatureCollection getPalstaFeatures(final Long zoneId, final GISUtils.SRID srid) {
        return getPalstaFeatureCollectionQuery.execute(zoneId, srid);
    }

    @Override
    @Transactional(readOnly = true)
    public FeatureCollection getFeatures(final long zoneId, final GISUtils.SRID srid) {
        return getExternalFeatureCollectionQuery.execute(zoneId, srid);
    }

    @Override
    @Transactional(readOnly = true)
    public FeatureCollection getCombinedFeatures(final Set<Long> zoneIds,
                                                 final GISUtils.SRID srid,
                                                 final double simplifyAmount) {
        return getCombinedFeatureCollectionQuery.execute(zoneIds, srid, simplifyAmount);
    }

    @Override
    @Transactional
    public void updatePalstaFeatures(final long zoneId, final FeatureCollection featureCollection) {
        final List<Integer> currentPalstaIds = jdbcTemplate.queryForList(
                "SELECT palsta_id FROM zone_palsta WHERE zone_id = ?", Integer.class, zoneId);

        final PalstaFeatureCollection palstaFeatureCollection =
                new PalstaFeatureCollection(featureCollection, currentPalstaIds);

        updateExternalFeatureQueries.removeZoneFeatures(zoneId);
        updatePalstaFeatureQueries.removeZonePalsta(zoneId, palstaFeatureCollection.getToRemove());
        updatePalstaFeatureQueries.updateZonePalstaList(zoneId, palstaFeatureCollection.getToAdd());
        calculateCombinedGeometryQueries.updateGeometryFromPalsta(zoneId);
    }

    @Override
    @Transactional
    public void updateFeatures(final long zoneId,
                               final GISUtils.SRID srid,
                               final List<HuntingClubAreaFeatureDTO> features) {
        updatePalstaFeatureQueries.removeZonePalsta(zoneId);
        updateExternalFeatureQueries.removeZoneFeatures(zoneId);
        updateExternalFeatureQueries.insertZoneFeatures(zoneId, srid, features);
        calculateCombinedGeometryQueries.updateGeometryFromFeatures(zoneId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GISZoneWithoutGeometryDTO> fetchWithoutGeometry(final Collection<Long> zoneIds) {
        if (zoneIds.isEmpty()) {
            return emptyList();
        }

        return new JPASQLQuery<>(entityManager, queryDslSqlTemplates)
                .from(SQZone.zone)
                .select(Projections.constructor(GISZoneWithoutGeometryDTO.class,
                        SQZone.zone.zoneId, SQZone.zone.sourceType, SQZone.zone.computedAreaSize,
                        SQZone.zone.waterAreaSize, SQZone.zone.modificationTime))
                .where(SQZone.zone.zoneId.in(zoneIds))
                .fetch();
    }

    @Override
    @Transactional
    public void calculateAreaSize(final long zoneId) {
        calculateZoneAreaSizeQueries.updateAreaSizeFromCombinedGeometry(zoneId);
        calculateZoneAreaSizeQueries.updateWaterAreaSize(zoneId,
                calculateZoneAreaSizeQueries.getSumOfWaterAreaSize(zoneId));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Double> calculateRhyAreaSize(final long zoneId) {
        return calculateZoneAreaSizeQueries.getSumOfAreaSizeByRhy(zoneId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Double> calculateHtaAreaSize(final long zoneId) {
        return calculateZoneAreaSizeQueries.getSumOfAreaSizeByHta(zoneId);
    }

    @Override
    @Transactional
    public GISZone copyZone(final GISZone from, final GISZone to) {
        removeZonePalstaAndFeatures(to);

        to.setSourceType(from.getSourceType());
        to.setExcludedGeom(from.getExcludedGeom());
        to.setComputedAreaSize(from.getComputedAreaSize());
        to.setMetsahallitusHirvi(new HashSet<>(from.getMetsahallitusHirvi()));
        to.setWaterAreaSize(from.getWaterAreaSize());
        entityManager.persist(to);
        entityManager.flush();

        copyZoneGeometryQueries.copyZoneCombinedGeometry(from, to);
        copyZoneGeometryQueries.copyZonePalsta(from, to);
        copyZoneGeometryQueries.copyZoneFeatures(from, to);

        return to;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Geometry> loadSplicedGeometries(final Collection<Long> zoneIds) {
        if (zoneIds.isEmpty()) {
            return emptyList();
        }

        final GeometryFactory geometryFactory = GISUtils.getGeometryFactory(GISUtils.SRID.ETRS_TM35FIN);
        final WKBReader wkbReader = new WKBReader(geometryFactory);
        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("chunkSize", 2048)
                .addValue("zoneIds", zoneIds);

        return namedParameterJdbcTemplate.query(
                "SELECT ST_AsBinary(ST_SubDivide(geom, :chunkSize)) AS geom FROM zone WHERE zone_id IN (:zoneIds)",
                params, (resultSet, i) -> {
                    final byte[] wkb = resultSet.getBytes("geom");

                    try {
                        return wkbReader.read(wkb);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Override
    @Transactional
    public void removeZonePalstaAndFeatures(final GISZone zone) {
        if (zone.getId() != null) {
            updatePalstaFeatureQueries.removeZonePalsta(zone.getId());
            updateExternalFeatureQueries.removeZoneFeatures(zone.getId());
        }
    }
}
