package fi.riista.feature.gis;

import com.querydsl.jpa.JPQLQueryFactory;
import com.querydsl.sql.SQLQueryFactory;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.gis.zone.QGISZone;
import fi.riista.sql.SQMhHirvi;
import fi.riista.sql.SQZoneFeature;
import fi.riista.sql.SQZoneMhHirvi;
import fi.riista.sql.SQZonePalsta;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Component
public class OnlyStateAreaService {
    // Are codes for fragment areas are allocated from numbers larger than 100k
    private static final int MIN_FRAGMENT_MH_AREA_CODE = 100_000;

    @Resource
    private JPQLQueryFactory queryFactory;

    @Resource
    private SQLQueryFactory sqlQueryFactory;

    // Area size calculation for state area size is not very precise.
    // Even if the source area only contains geometries for Metsähallitus there is a great chance
    // that the calculated private area size is not zero. Therefore we must manually recognize these situations
    // and use hacks to transfer all private area size to state land area size.
    @Transactional(readOnly = true)
    public boolean shouldContainOnlyStateLand(final List<Long> zoneIds) {
        if (zoneIds.isEmpty()) {
            return false;
        }

        // Check all area constituents to make sure only normal moose areas are selected.
        final long externalAreaCount = countExternalAreas(zoneIds);
        final long palstaCount = countPalstaCount(zoneIds);
        final long otherFeatureCount = countOtherFeatureCount(zoneIds);
        final long hirviNormalAreaCount = countMetsahallitusHirviNormalCount(zoneIds);
        final long hirviFragmentAreaCount = countMetsahallitusHirviFragmentCount(zoneIds);

        return palstaCount == 0
                && otherFeatureCount == 0
                && externalAreaCount == 0
                && hirviFragmentAreaCount == 0
                && hirviNormalAreaCount > 0;
    }

    private long countPalstaCount(final List<Long> zoneIds) {
        final SQZonePalsta ZONE_PALSTA = new SQZonePalsta("zp");

        return sqlQueryFactory
                .select(ZONE_PALSTA.count())
                .from(ZONE_PALSTA)
                .where(ZONE_PALSTA.zoneId.in(zoneIds))
                .fetchCount();
    }

    private long countOtherFeatureCount(final List<Long> zoneIds) {
        final SQZoneFeature ZONE_FEATURE = new SQZoneFeature("zf");

        return sqlQueryFactory
                .select(ZONE_FEATURE.count())
                .from(ZONE_FEATURE)
                .where(ZONE_FEATURE.zoneId.in(zoneIds))
                .fetchCount();
    }

    private long countMetsahallitusHirviNormalCount(final List<Long> zoneIds) {
        final SQZoneMhHirvi ZMH = new SQZoneMhHirvi("zmh");
        final SQMhHirvi MH = new SQMhHirvi("mh");

        return sqlQueryFactory
                .select(ZMH.count())
                .from(ZMH)
                .join(MH).on(ZMH.mhHirviId.intValue().eq(MH.gid))
                .where(ZMH.zoneId.in(zoneIds).and(MH.koodi.lt(MIN_FRAGMENT_MH_AREA_CODE)))
                .fetchCount();
    }

    private long countMetsahallitusHirviFragmentCount(final List<Long> zoneIds) {
        final SQZoneMhHirvi ZMH = new SQZoneMhHirvi("zmh");
        final SQMhHirvi MH = new SQMhHirvi("mh");

        return sqlQueryFactory
                .select(ZMH.count())
                .from(ZMH)
                .join(MH).on(ZMH.mhHirviId.intValue().eq(MH.gid))
                .where(ZMH.zoneId.in(zoneIds).and(MH.koodi.gt(MIN_FRAGMENT_MH_AREA_CODE)))
                .fetchCount();
    }

    private long countExternalAreas(final List<Long> zoneIds) {
        final QGISZone ZONE = QGISZone.gISZone;

        return queryFactory.selectFrom(ZONE)
                .where(ZONE.id.in(zoneIds))
                .where(ZONE.sourceType.eq(GISZone.SourceType.EXTERNAL))
                .fetchCount();
    }
}
