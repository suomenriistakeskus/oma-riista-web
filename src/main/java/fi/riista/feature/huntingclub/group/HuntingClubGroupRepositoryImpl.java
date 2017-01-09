package fi.riista.feature.huntingclub.group;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.SimpleTemplate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.sql.JPASQLQuery;
import com.querydsl.spatial.GeometryExpression;
import com.querydsl.sql.SQLTemplates;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.GameDiaryEntry;
import fi.riista.feature.huntingclub.area.QHuntingClubArea;
import fi.riista.sql.SQZone;
import fi.riista.util.GISUtils;
import org.geolatte.geom.Geometry;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collections;
import java.util.List;

import static com.querydsl.spatial.GeometryExpressions.setSRID;


@Repository
@Transactional
public class HuntingClubGroupRepositoryImpl implements HuntingClubGroupRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Resource
    private SQLTemplates queryDslSqlTemplates;

    @Override
    @Transactional(readOnly = true)
    public List<HuntingClubGroup> findAllGroupsWithAreaIntersecting(final GameDiaryEntry diaryEntry,
                                                                    final int huntingYear) {
        final GeometryExpression<?> geometryExpression = setSRID(
                makePoint(diaryEntry.getGeoLocation()),
                GISUtils.SRID.ETRS_TM35FIN.getValue());

        final List<Long> zoneIds = new JPASQLQuery<>(entityManager, queryDslSqlTemplates)
                .from(SQZone.zone)
                .select(SQZone.zone.zoneId)
                .where(SQZone.zone.geom.intersects(geometryExpression))
                .fetch();

        if (zoneIds.isEmpty()) {
            return Collections.emptyList();
        }

        final QHuntingClubArea area = QHuntingClubArea.huntingClubArea;
        final QHuntingClubGroup group = QHuntingClubGroup.huntingClubGroup;

        return new JPAQuery<>(entityManager)
                .from(group)
                .join(group.huntingArea, area)
                .select(group)
                .where(area.zone.id.in(zoneIds)
                        .and(area.active.isTrue())
                        .and(area.huntingYear.eq(huntingYear)))
                .fetch();
    }

    private static SimpleTemplate<Geometry> makePoint(final GeoLocation geoLocation) {
        return Expressions.template(Geometry.class, "ST_MakePoint({0},{1})",
                geoLocation.getLongitude(), geoLocation.getLatitude());
    }
}
