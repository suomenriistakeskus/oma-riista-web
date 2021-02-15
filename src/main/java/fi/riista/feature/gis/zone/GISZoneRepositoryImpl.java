package fi.riista.feature.gis.zone;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.jpa.sql.JPASQLQuery;
import com.querydsl.sql.SQLTemplates;
import fi.riista.config.Constants;
import fi.riista.feature.gis.GISBounds;
import fi.riista.feature.gis.geojson.PalstaFeatureCollectionDifference;
import fi.riista.feature.gis.zone.query.CalculateCombinedGeometryQueries;
import fi.riista.feature.gis.zone.query.CalculateZoneAreaSizeQueries;
import fi.riista.feature.gis.zone.query.CopyZoneGeometryQueries;
import fi.riista.feature.gis.zone.query.GetBoundsQueries;
import fi.riista.feature.gis.zone.query.GetCombinedFeatureCollectionQuery;
import fi.riista.feature.gis.zone.query.GetGeometryQuery;
import fi.riista.feature.gis.zone.query.GetInvertedGeometryQuery;
import fi.riista.feature.gis.zone.query.GetOtherFeatureCollectionQuery;
import fi.riista.feature.gis.zone.query.GetPalstaFeatureCollectionQuery;
import fi.riista.feature.gis.zone.query.GetPolygonFeatureCollectionQuery;
import fi.riista.feature.gis.zone.query.GetStateGeometryQuery;
import fi.riista.feature.gis.zone.query.UpdateExternalFeatureQueries;
import fi.riista.feature.gis.zone.query.UpdateOtherFeatureQueries;
import fi.riista.feature.gis.zone.query.UpdatePalstaFeatureQueries;
import fi.riista.integration.koiratutka.HuntingClubAreaImportFeatureDTO;
import fi.riista.sql.SQZone;
import fi.riista.util.GISUtils;
import fi.riista.util.JdbcTemplateEnhancer;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.joda.time.DateTime;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static fi.riista.feature.gis.zone.GISZoneConstants.AREA_SIZE_CALCULATION_FAILED;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

@Repository
public class GISZoneRepositoryImpl implements GISZoneRepositoryCustom {
    private static final Logger LOG = LoggerFactory.getLogger(GISZoneRepositoryImpl.class);

    private JdbcOperations jdbcTemplate;
    private NamedParameterJdbcOperations namedParameterJdbcTemplate;
    private NamedParameterJdbcOperations enhancedJdbcTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    @Resource
    private SQLTemplates queryDslSqlTemplates;

    private GetBoundsQueries getBoundsQueries;
    private GetPalstaFeatureCollectionQuery getPalstaFeatureCollectionQuery;
    private GetOtherFeatureCollectionQuery getOtherFeatureCollectionQuery;
    private GetCombinedFeatureCollectionQuery getCombinedFeatureCollectionQuery;
    private GetPolygonFeatureCollectionQuery getPolygonFeatureCollectionQuery;
    private GetGeometryQuery getGeometryQuery;
    private GetInvertedGeometryQuery getInvertedGeometryQuery;
    private GetStateGeometryQuery getStateGeometryQuery;
    private UpdatePalstaFeatureQueries updatePalstaFeatureQueries;
    private UpdateOtherFeatureQueries updateOtherFeatureQueries;
    private UpdateExternalFeatureQueries updateExternalFeatureQueries;
    private CalculateZoneAreaSizeQueries calculateZoneAreaSizeQueries;
    private CalculateCombinedGeometryQueries calculateCombinedGeometryQueries;
    private CopyZoneGeometryQueries copyZoneGeometryQueries;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(this.jdbcTemplate);
        this.enhancedJdbcTemplate = JdbcTemplateEnhancer.wrap(this.namedParameterJdbcTemplate);
        this.getBoundsQueries = new GetBoundsQueries(namedParameterJdbcTemplate);
        this.getPalstaFeatureCollectionQuery = new GetPalstaFeatureCollectionQuery(namedParameterJdbcTemplate);
        this.getOtherFeatureCollectionQuery = new GetOtherFeatureCollectionQuery(namedParameterJdbcTemplate);
        this.getCombinedFeatureCollectionQuery = new GetCombinedFeatureCollectionQuery(namedParameterJdbcTemplate);
        this.getPolygonFeatureCollectionQuery = new GetPolygonFeatureCollectionQuery(namedParameterJdbcTemplate);
        this.getGeometryQuery = new GetGeometryQuery(namedParameterJdbcTemplate);
        this.getInvertedGeometryQuery = new GetInvertedGeometryQuery(namedParameterJdbcTemplate);
        this.getStateGeometryQuery = new GetStateGeometryQuery(namedParameterJdbcTemplate);
        this.updatePalstaFeatureQueries = new UpdatePalstaFeatureQueries(jdbcTemplate);
        this.updateOtherFeatureQueries = new UpdateOtherFeatureQueries(jdbcTemplate);
        this.updateExternalFeatureQueries = new UpdateExternalFeatureQueries(jdbcTemplate);
        this.calculateCombinedGeometryQueries = new CalculateCombinedGeometryQueries(this.enhancedJdbcTemplate);
        this.calculateZoneAreaSizeQueries = new CalculateZoneAreaSizeQueries(enhancedJdbcTemplate);
        this.copyZoneGeometryQueries = new CopyZoneGeometryQueries(namedParameterJdbcTemplate);
    }

    @Override
    @Transactional(readOnly = true)
    public GISBounds getBounds(final long zoneId, final GISUtils.SRID srid) {
        return getBoundsQueries.getBounds(zoneId, srid);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, GISBounds> getBounds(final Collection<Long> zoneIds, final GISUtils.SRID srid) {
        return getBoundsQueries.getBounds(zoneIds, srid);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Feature> getPalstaFeatures(final long zoneId, final GISUtils.SRID srid) {
        return getPalstaFeatureCollectionQuery.execute(zoneId, srid);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Feature> getOtherFeatures(final long zoneId, final GISUtils.SRID srid) {
        return getOtherFeatureCollectionQuery.execute(zoneId, srid);
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
    @Transactional(readOnly = true)
    public Geometry getStateGeometry(final long zoneId, final GISUtils.SRID srid) {
        return getStateGeometryQuery.execute(zoneId, srid);
    }

    @Override
    @Transactional(readOnly = true)
    public Geometry getStateGeometry(final Geometry geom, final GISUtils.SRID srid) {
        return getStateGeometryQuery.execute(geom, srid);
    }

    @Override
    @Transactional
    public void calculateCombinedGeometry(final long zoneId) {
        calculateCombinedGeometryQueries.updateGeometry(zoneId);
    }

    @Override
    @Transactional
    public void updatePalstaFeatures(final long zoneId, final FeatureCollection featureCollection) {
        final List<Integer> existingIds = updatePalstaFeatureQueries.list(zoneId);
        final PalstaFeatureCollectionDifference palstaFeatureCollection =
                PalstaFeatureCollectionDifference.create(featureCollection, existingIds);

        updatePalstaFeatureQueries.removeById(zoneId, palstaFeatureCollection.getRemovable());
        updatePalstaFeatureQueries.insert(zoneId, palstaFeatureCollection.getInsertable());
    }

    @Override
    @Transactional
    public void updateOtherFeatures(long zoneId, FeatureCollection featureCollection, final GISUtils.SRID srid) {
        updateOtherFeatureQueries.removeZoneFeatures(zoneId);
        updateOtherFeatureQueries.insertOtherFeatures(zoneId, featureCollection, srid);
    }

    @Override
    @Transactional
    public void updateExternalFeatures(final long zoneId,
                                       final GISUtils.SRID srid,
                                       final List<HuntingClubAreaImportFeatureDTO> features) {
        updatePalstaFeatureQueries.removeAll(zoneId);
        updateExternalFeatureQueries.removeZoneFeatures(zoneId);
        updateExternalFeatureQueries.insertZoneFeatures(zoneId, srid, features);
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
                    final Timestamp modificationTimestamp = tuple.get(ZONE.modificationTime);

                    return new GISZoneWithoutGeometryDTO(zoneId, size, sourceType, new DateTime(modificationTimestamp.getTime(), Constants.DEFAULT_TIMEZONE));
                }));
    }

    @Nullable
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

    private static GISZoneSizeDTO createSizeDTO(final Tuple tuple) {
        final double computedAreaSize = tuple.get(SQZone.zone.computedAreaSize);
        final double waterAreaSize = tuple.get(SQZone.zone.waterAreaSize);

        if (computedAreaSize < 0 || waterAreaSize < 0) {
            if (isCalculationFailed(computedAreaSize)) {
                return GISZoneSizeDTO.createCalculationFailed();
            }
            return GISZoneSizeDTO.createCalculating();
        }

        final Double stateLandAreaSize = tuple.get(SQZone.zone.stateLandAreaSize);
        final Double privateLandAreaSize = tuple.get(SQZone.zone.privateLandAreaSize);
        final TotalLandWaterSizeDTO total = new TotalLandWaterSizeDTO(
                computedAreaSize, computedAreaSize - waterAreaSize, waterAreaSize);

        return GISZoneSizeDTO.create(total,
                stateLandAreaSize != null ? stateLandAreaSize : 0,
                privateLandAreaSize != null ? privateLandAreaSize : 0);
    }

    private static boolean isCalculationFailed(final double computedAreaSize) {
        return Math.abs(computedAreaSize - AREA_SIZE_CALCULATION_FAILED) < 0.01;
    }

    @Nullable
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
                    return totalArea > 0 ? GISZoneSizeDTO.create(total, stateLandArea, privateLandArea) : null;
                });

        return result.size() == 1 ? result.get(0) : null;
    }

    @Override
    @Transactional
    public void calculateAreaSize(final long zoneId, final boolean onlyStateLand) {
        final TotalLandWaterSizeDTO all = calculateLandAndWaterAreaSize(zoneId);

        final GISZoneSizeDTO dto;

        if (!onlyStateLand) {
            // Ensure that negative areas will not be persisted due to rounding errors
            final double stateLandAreaSize = Math.max(0,
                    calculateZoneAreaSizeQueries.getSumOfStateLandAreaSize(zoneId));
            final double privateLandAreaSize = Math.max(0, all.getLand() - stateLandAreaSize);
            dto = GISZoneSizeDTO.create(all, stateLandAreaSize, privateLandAreaSize);

        } else {
            LOG.warn("Setting zone privateLandAreaSize explicitly to zero for zoneId={}", zoneId);
            dto = GISZoneSizeDTO.create(all, all.getLand(), 0);
        }

        calculateZoneAreaSizeQueries.updateAreaSize(zoneId, dto);
    }

    private TotalLandWaterSizeDTO calculateLandAndWaterAreaSize(final long zoneId) {
        final double totalAreaSize = calculateZoneAreaSizeQueries.getAreaSize(zoneId);
        final double waterAreaSize = calculateZoneAreaSizeQueries.getSumOfWaterAreaSize(zoneId);
        final double landAreaSize = totalAreaSize - waterAreaSize;

        return new TotalLandWaterSizeDTO(totalAreaSize, landAreaSize, waterAreaSize);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GISZoneSizeByOfficialCodeDTO> calculateRhyAreaSize(final long zoneId) {
        return calculateZoneAreaSizeQueries.getSumOfAreaSizeByRhy(zoneId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GISZoneSizeByOfficialCodeDTO> calculateVerotusLohkoAreaSize(final int huntingYear, final long zoneId) {
        return calculateZoneAreaSizeQueries.getSumOfAreaSizeByVerotusLohko(huntingYear, zoneId);
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
        to.setWaterAreaSize(from.getWaterAreaSize());
        to.setStateLandAreaSize(from.getStateLandAreaSize());
        to.setPrivateLandAreaSize(from.getPrivateLandAreaSize());
        to.setMetsahallitusHirvi(new HashSet<>(from.getMetsahallitusHirvi()));

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
                "SELECT ST_AsBinary(ST_SubDivide((ST_Dump(geom)).geom, :chunkSize)) AS geom FROM zone WHERE zone_id " +
                        "IN (:zoneIds)",
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
            updatePalstaFeatureQueries.removeAll(zone.getId());
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

    @Override
    @Transactional(readOnly = true)
    public List<GISZoneMmlPropertyIntersectionDTO> findIntersectingPalsta(final long zoneId) {
        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("chunkSize", 16384)
                .addValue("minimumIntersectionArea", 100)
                .addValue("zoneId", zoneId);


        // - Divide zone geometry for performance
        final String querySplices = "SELECT " +
                "    ST_MakeValid(ST_SubDivide((ST_Dump(zone.geom)).geom, :chunkSize)) AS geom " +
                "  FROM zone " +
                "  WHERE zone.zone_id = :zoneId ";

        // - Sum up intersection with zone by palsta
        final String queryPalstaIntersection = "SELECT " +
                "    pa.id AS id, " +
                "    SUM(ST_Area(ST_Intersection(pa.geom, splices.geom))) AS area " +
                "  FROM splices " +
                "  INNER JOIN palstaalue pa ON ST_Intersects(pa.geom, splices.geom) " +
                "  GROUP BY pa.id";


        final String sql = "WITH " +
                "splices AS ( " + querySplices + "), " +
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
                "WHERE ( palstaIntersection.area > :minimumIntersectionArea ) ";

        return ImmutableList.copyOf(enhancedJdbcTemplate.query(
                sql,
                params,
                (resultSet, i) -> new GISZoneMmlPropertyIntersectionDTO(
                        zoneId,
                        resultSet.getLong("tunnus"),
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getDouble("area")
                )
        ));
    }

}

