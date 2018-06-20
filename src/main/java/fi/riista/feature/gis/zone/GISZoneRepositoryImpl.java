package fi.riista.feature.gis.zone;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.jpa.sql.JPASQLQuery;
import com.querydsl.sql.SQLTemplates;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import fi.riista.config.jackson.CustomJacksonObjectMapper;
import fi.riista.feature.gis.GISBounds;
import fi.riista.feature.gis.geojson.PalstaFeatureCollection;
import fi.riista.feature.gis.zone.query.CalculateCombinedGeometryQueries;
import fi.riista.feature.gis.zone.query.CalculateZoneAreaSizeQueries;
import fi.riista.feature.gis.zone.query.CopyZoneGeometryQueries;
import fi.riista.feature.gis.zone.query.GetCombinedFeatureCollectionQuery;
import fi.riista.feature.gis.zone.query.GetGeometryQuery;
import fi.riista.feature.gis.zone.query.GetInvertedGeometryQuery;
import fi.riista.feature.gis.zone.query.GetPalstaFeatureCollectionQuery;
import fi.riista.feature.gis.zone.query.GetPolygonFeatureCollectionQuery;
import fi.riista.feature.gis.zone.query.UpdateExternalFeatureQueries;
import fi.riista.feature.gis.zone.query.UpdatePalstaFeatureQueries;
import fi.riista.feature.huntingclub.area.zone.HuntingClubAreaFeatureDTO;
import fi.riista.sql.SQZone;
import fi.riista.util.F;
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

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static fi.riista.util.Collect.idSet;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

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
    private GetCombinedFeatureCollectionQuery getCombinedFeatureCollectionQuery;
    private GetPolygonFeatureCollectionQuery getPolygonFeatureCollectionQuery;
    private GetGeometryQuery getGeometryQuery;
    private GetInvertedGeometryQuery getInvertedGeometryQuery;
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
        this.getCombinedFeatureCollectionQuery = new GetCombinedFeatureCollectionQuery(namedParameterJdbcTemplate);
        this.getPolygonFeatureCollectionQuery = new GetPolygonFeatureCollectionQuery(namedParameterJdbcTemplate);
        this.getGeometryQuery = new GetGeometryQuery(namedParameterJdbcTemplate);
        this.getInvertedGeometryQuery = new GetInvertedGeometryQuery(namedParameterJdbcTemplate);
        this.updatePalstaFeatureQueries = new UpdatePalstaFeatureQueries(jdbcTemplate);
        this.updateExternalFeatureQueries = new UpdateExternalFeatureQueries(jdbcTemplate);
        this.calculateCombinedGeometryQueries = new CalculateCombinedGeometryQueries(this.enchancedJdbcTemplate);
        this.calculateZoneAreaSizeQueries = new CalculateZoneAreaSizeQueries(enchancedJdbcTemplate);
        this.copyZoneGeometryQueries = new CopyZoneGeometryQueries(namedParameterJdbcTemplate);
    }

    @Override
    @Transactional(readOnly = true)
    public GISBounds getBounds(final long zoneId, final GISUtils.SRID srid) {
        final String sql = "WITH extent AS " +
                "(SELECT ST_Extent(ST_Transform(geom, :srid)) AS e FROM zone WHERE zone_id = :zoneId)" +
                " SELECT ST_XMin(e) AS xmin, ST_YMin(e) AS ymin, ST_XMax(e) AS xmax, ST_YMax(e) AS ymax FROM extent";

        return namedParameterJdbcTemplate.queryForObject(sql, new MapSqlParameterSource()
                .addValue("srid", srid.getValue())
                .addValue("zoneId", zoneId), (rs, rowNum) -> {
            final double xmin = rs.getDouble("xmin");
            boolean wasNull = rs.wasNull();
            final double ymin = rs.getDouble("ymin");
            wasNull = wasNull || rs.wasNull();
            final double xmax = rs.getDouble("xmax");
            wasNull = wasNull || rs.wasNull();
            final double ymax = rs.getDouble("ymax");
            wasNull = wasNull || rs.wasNull();

            return wasNull ? null : new GISBounds(xmin, ymin, xmax, ymax);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public FeatureCollection getPalstaFeatures(final long zoneId, final GISUtils.SRID srid) {
        return getPalstaFeatureCollectionQuery.execute(zoneId, srid);
    }

    @Override
    @Transactional(readOnly = true)
    public FeatureCollection getCombinedFeatures(final Set<Long> zoneIds, final GISUtils.SRID srid) {
        return getCombinedFeatureCollectionQuery.execute(zoneIds, srid);
    }

    @Override
    @Transactional(readOnly = true)
    public FeatureCollection getCombinedPolygonFeatures(final long zoneId, final GISUtils.SRID srid) {
        return getPolygonFeatureCollectionQuery.execute(zoneId, srid);
    }

    @Override
    @Transactional(readOnly = true)
    public Geometry getSimplifiedGeometry(final long zoneId, final GISUtils.SRID srid) {
        return getGeometryQuery.execute(zoneId, srid);
    }

    @Override
    @Transactional(readOnly = true)
    public Geometry getInvertedSimplifiedGeometry(final long zoneId, final GISUtils.SRID srid) {
        return getInvertedGeometryQuery.execute(zoneId, srid);
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
        calculateCombinedGeometryQueries.updateGeometry(zoneId);
    }

    @Override
    @Transactional
    public void updateFeatures(final long zoneId,
                               final GISUtils.SRID srid,
                               final List<HuntingClubAreaFeatureDTO> features) {
        updatePalstaFeatureQueries.removeZonePalsta(zoneId);
        updateExternalFeatureQueries.removeZoneFeatures(zoneId);
        updateExternalFeatureQueries.insertZoneFeatures(zoneId, srid, features);
        calculateCombinedGeometryQueries.updateGeometry(zoneId);
    }

    @Override
    @Transactional(readOnly = true)
    public <E extends AreaEntity<Long>> Function<E, GISZoneWithoutGeometryDTO> getAreaMapping(final Iterable<E> iterable) {
        final Set<Long> zoneIds = F.stream(iterable).map(AreaEntity::getZone).collect(idSet());
        final Map<Long, GISZoneWithoutGeometryDTO> mapping = fetchWithoutGeometry(zoneIds);
        return a -> a.getZone() != null ? mapping.get(F.getId(a.getZone())) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, GISZoneWithoutGeometryDTO> fetchWithoutGeometry(final Collection<Long> zoneIds) {
        if (zoneIds.isEmpty()) {
            return emptyMap();
        }

        final SQZone ZONE = SQZone.zone;
        final Expression<?>[] fields = new Expression[]{ZONE.zoneId, ZONE.sourceType, ZONE.modificationTime,
                ZONE.computedAreaSize, ZONE.waterAreaSize, ZONE.stateLandAreaSize, ZONE.privateLandAreaSize
        };

        return new JPASQLQuery<>(entityManager, queryDslSqlTemplates)
                .select(fields)
                .from(ZONE)
                .where(ZONE.zoneId.in(zoneIds))
                .fetch().stream()
                .collect(Collectors.toMap(tuple -> tuple.get(ZONE.zoneId), tuple -> {
                    final Long zoneId = tuple.get(ZONE.zoneId);
                    final GISZoneSizeDTO size = createSizeDTO(tuple);
                    final GISZone.SourceType sourceType = GISZone.SourceType.valueOf(tuple.get(ZONE.sourceType));

                    return new GISZoneWithoutGeometryDTO(zoneId, size, sourceType, tuple.get(ZONE.modificationTime));
                }));
    }

    @Override
    @Transactional(readOnly = true)
    public GISZoneSizeDTO getAreaSize(final long zoneId) {
        final Tuple tuple = new JPASQLQuery<>(entityManager, queryDslSqlTemplates)
                .from(SQZone.zone)
                .select(SQZone.zone.computedAreaSize,
                        SQZone.zone.waterAreaSize,
                        SQZone.zone.stateLandAreaSize,
                        SQZone.zone.privateLandAreaSize)
                .where(SQZone.zone.zoneId.eq(zoneId))
                .fetchOne();

        return createSizeDTO(tuple);
    }

    @Nonnull
    private GISZoneSizeDTO createSizeDTO(final Tuple tuple) {
        final double computedAreaSize = tuple.get(SQZone.zone.computedAreaSize);
        final double waterAreaSize = tuple.get(SQZone.zone.waterAreaSize);
        final Double stateLandAreaSize = tuple.get(SQZone.zone.stateLandAreaSize);
        final Double privateLandAreaSize = tuple.get(SQZone.zone.privateLandAreaSize);
        final TotalLandWaterSizeDTO total = new TotalLandWaterSizeDTO(
                computedAreaSize, computedAreaSize - waterAreaSize, waterAreaSize);

        return new GISZoneSizeDTO(total,
                stateLandAreaSize != null ? stateLandAreaSize : 0,
                privateLandAreaSize != null ? privateLandAreaSize : 0);
    }

    @Override
    @Transactional(readOnly = true)
    public GISZoneSizeDTO getAdjustedAreaSize(final long zoneId) {
        final String sql = "SELECT total_area, water_area, land_area, state_land_area, private_land_area" +
                " FROM zone_area_size WHERE zone_id = :zoneId";

        final MapSqlParameterSource params = new MapSqlParameterSource("zoneId", zoneId);

        final List<GISZoneSizeDTO> result = namedParameterJdbcTemplate.query(sql, params,
                (rs, rowNum) -> {
                    final double totalArea = rs.getDouble("total_area");
                    final double waterArea = rs.getDouble("water_area");
                    final double landArea = rs.getDouble("land_area");
                    final double stateLandArea = rs.getDouble("state_land_area");
                    final double privateLandArea = rs.getDouble("private_land_area");

                    final TotalLandWaterSizeDTO total = new TotalLandWaterSizeDTO(totalArea, landArea, waterArea);
                    return totalArea > 0 ? new GISZoneSizeDTO(total, stateLandArea, privateLandArea) : null;
                });

        return result.size() == 1 ? result.get(0) : null;
    }

    @Override
    @Transactional
    public void calculateAreaSize(final long zoneId) {
        final TotalLandWaterSizeDTO all = calculateLandAndWaterAreaSize(zoneId);
        final double stateLandAreaSize = calculateZoneAreaSizeQueries.getSumOfStateLandAreaSize(zoneId);
        final double privateLandAreaSize = all.getLand() - stateLandAreaSize;

        calculateZoneAreaSizeQueries.updateAreaSize(zoneId,
                new GISZoneSizeDTO(all, stateLandAreaSize, privateLandAreaSize));
    }

    private TotalLandWaterSizeDTO calculateLandAndWaterAreaSize(final long zoneId) {
        final double totalAreaSize = calculateZoneAreaSizeQueries.getAreaSize(zoneId);
        final double waterAreaSize = calculateZoneAreaSizeQueries.getSumOfWaterAreaSize(zoneId);
        final double landAreaSize = totalAreaSize - waterAreaSize;

        return new TotalLandWaterSizeDTO(totalAreaSize, landAreaSize, waterAreaSize);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GISZoneSizeRhyDTO> calculateRhyAreaSize(final long zoneId) {
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
                .addValue("chunkSize", 16384)
                .addValue("zoneIds", zoneIds);

        return namedParameterJdbcTemplate.query(
                "SELECT ST_AsBinary(ST_SubDivide((ST_Dump(geom)).geom, :chunkSize)) AS geom FROM zone WHERE zone_id IN (:zoneIds)",
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

    @Override
    @Transactional(readOnly = true)
    public Set<Integer> getUniqueMetsahallitusYears(final long zoneId) {
        return ImmutableSet.copyOf(namedParameterJdbcTemplate.queryForList("SELECT DISTINCT mh.vuosi" +
                " FROM zone_mh_hirvi zmh" +
                " JOIN mh_hirvi mh ON (zmh.mh_hirvi_id = mh.gid)" +
                " WHERE zmh.zone_id = :zoneId;", new MapSqlParameterSource("zoneId", zoneId), Integer.class));
    }
}
