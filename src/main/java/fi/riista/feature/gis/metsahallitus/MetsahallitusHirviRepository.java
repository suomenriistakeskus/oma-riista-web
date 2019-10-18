package fi.riista.feature.gis.metsahallitus;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import com.vividsolutions.jts.geom.Geometry;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gis.geojson.GeoJSONConstants;
import fi.riista.sql.SQMhHirvi;
import fi.riista.sql.SQZoneMhHirvi;
import fi.riista.util.F;
import fi.riista.util.GISUtils;
import fi.riista.util.PolygonConversionUtil;
import org.geojson.Feature;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.List;

@Repository
public class MetsahallitusHirviRepository {

    @Resource
    private SQLQueryFactory sqlQueryFactory;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<MetsahallitusHirviDTO> findAll(final int year) {
        final SQMhHirvi MH_HIRVI = SQMhHirvi.mhHirvi;

        return sqlQueryFactory
                .select(Projections.constructor(MetsahallitusHirviDTO.class,
                        MH_HIRVI.gid,
                        Expressions.constant(year),
                        MH_HIRVI.koodi,
                        MH_HIRVI.nimi,
                        MH_HIRVI.pintaAla.multiply(10_000)))
                .from(MH_HIRVI)
                .where(MH_HIRVI.vuosi.eq(year))
                .fetch();
    }

    @Nullable
    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Integer findGid(final GeoLocation geoLocation, final int year) {
        final SQMhHirvi MH_HIRVI = SQMhHirvi.mhHirvi;

        return sqlQueryFactory
                .select(MH_HIRVI.gid)
                .from(MH_HIRVI)
                .where(MH_HIRVI.vuosi.eq(year), MH_HIRVI.geom.intersects(GISUtils.createPointWithDefaultSRID(geoLocation)))
                .fetchFirst();
    }

    @Nullable
    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Feature findFeature(final int id, final GISUtils.SRID srid) {
        final SQMhHirvi MH_HIRVI = SQMhHirvi.mhHirvi;
        final List<Feature> features = loadFeatures(srid, sqlQueryFactory.from(MH_HIRVI).where(MH_HIRVI.gid.eq(id)));
        return features.isEmpty() ? null : features.get(0);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<Feature> findByZoneAsFeatures(final long zoneId, final GISUtils.SRID srid) {
        final SQZoneMhHirvi ZONE_MH_HIRVI = SQZoneMhHirvi.zoneMhHirvi;
        final SQMhHirvi MH_HIRVI = SQMhHirvi.mhHirvi;

        return loadFeatures(srid, sqlQueryFactory
                .from(ZONE_MH_HIRVI)
                .join(MH_HIRVI).on(MH_HIRVI.gid.eq(ZONE_MH_HIRVI.mhHirviId.intValue()))
                .where(ZONE_MH_HIRVI.zoneId.eq(zoneId)));
    }

    private static List<Feature> loadFeatures(final GISUtils.SRID srid, final SQLQuery<?> baseQuery) {
        final SQMhHirvi MH_HIRVI = SQMhHirvi.mhHirvi;
        final SimpleExpression<byte[]> WKB_GEOM = MH_HIRVI.geom.transform(srid.getValue()).asBinary();

        final List<Tuple> results = baseQuery
                .select(MH_HIRVI.gid, MH_HIRVI.vuosi, MH_HIRVI.koodi, MH_HIRVI.nimi, MH_HIRVI.pintaAla, WKB_GEOM)
                .fetch();

        return F.mapNonNullsToList(results, tuple -> {
            final Feature feature = new Feature();

            feature.setId(GeoJSONConstants.ID_PREFIX_MH_HIRVI + tuple.get(MH_HIRVI.gid));
            feature.setProperty(GeoJSONConstants.PROPERTY_YEAR, tuple.get(MH_HIRVI.vuosi));
            feature.setProperty(GeoJSONConstants.PROPERTY_NUMBER, tuple.get(MH_HIRVI.koodi));
            feature.setProperty(GeoJSONConstants.PROPERTY_NAME, tuple.get(MH_HIRVI.nimi));
            feature.setProperty(GeoJSONConstants.PROPERTY_SIZE, tuple.get(MH_HIRVI.pintaAla) * 10_000);

            final Geometry geometry = GISUtils.readFromPostgisWkb(tuple.get(WKB_GEOM), srid);
            feature.setBbox(GISUtils.getGeoJsonBBox(geometry));
            feature.setGeometry(PolygonConversionUtil.javaToGeoJSON(geometry));

            return feature;
        });
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Integer findLatestYear() {
        final SQMhHirvi MH_HIRVI = SQMhHirvi.mhHirvi;

        return sqlQueryFactory
                .select(MH_HIRVI.vuosi.max())
                .from(MH_HIRVI)
                .fetchOne();
    }
}
