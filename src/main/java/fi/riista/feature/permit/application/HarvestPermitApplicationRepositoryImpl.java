package fi.riista.feature.permit.application;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.jpa.JPQLQueryFactory;
import com.querydsl.spatial.GeometryExpression;
import com.querydsl.spatial.GeometryExpressions;
import com.querydsl.spatial.SpatialOps;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import fi.riista.feature.account.user.QSystemUser;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.permit.application.conflict.HarvestPermitApplicationConflictPalsta;
import fi.riista.feature.permit.application.search.HarvestPermitApplicationSearchDTO;
import fi.riista.feature.permit.application.search.HarvestPermitApplicationSearchQueryBuilder;
import fi.riista.feature.permit.decision.QPermitDecision;
import fi.riista.feature.permit.decision.revision.QPermitDecisionRevision;
import fi.riista.sql.SQKiinteistoNimet;
import fi.riista.sql.SQPalstaalue;
import fi.riista.sql.SQZone;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.apache.commons.lang3.StringUtils;
import org.geolatte.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class HarvestPermitApplicationRepositoryImpl implements HarvestPermitApplicationRepositoryCustom {
    private static final Logger LOG = LoggerFactory.getLogger(HarvestPermitApplicationRepositoryImpl.class);
    private static final int MIN_INTERSECTION_SIZE = 1000;

    @Resource
    private SQLQueryFactory sqlQueryFactory;

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Override
    @Transactional(readOnly = true)
    public List<HarvestPermitApplication> listByRevisionCreator(final Long userId) {
        final QHarvestPermitApplication APPLICATION = QHarvestPermitApplication.harvestPermitApplication;
        final QPermitDecisionRevision REV = QPermitDecisionRevision.permitDecisionRevision;
        final QPermitDecision DECISION = QPermitDecision.permitDecision;

        final List<Long> decisionIds = jpqlQueryFactory.select(REV.permitDecision.id)
                .from(REV)
                .where(REV.auditFields.createdByUserId.eq(userId))
                .distinct()
                .fetch();

        return jpqlQueryFactory.select(APPLICATION)
                .from(DECISION)
                .join(DECISION.application, APPLICATION)
                .where(DECISION.id.in(decisionIds))
                .orderBy(APPLICATION.huntingYear.desc(), APPLICATION.applicationNumber.desc())
                .fetch();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HarvestPermitApplication> listPostalQueue() {
        final QHarvestPermitApplication APPLICATION = QHarvestPermitApplication.harvestPermitApplication;
        final QPermitDecisionRevision REV = QPermitDecisionRevision.permitDecisionRevision;
        final QPermitDecision DECISION = QPermitDecision.permitDecision;

        final List<Long> decisionIds = jpqlQueryFactory.select(REV.permitDecision.id)
                .from(REV)
                .where(REV.cancelled.isFalse())
                .where(REV.postalByMail.isTrue())
                .where(REV.postedByMailDate.isNull())
                .where(REV.publishDate.lt(DateUtil.now()))
                .distinct()
                .fetch();

        return jpqlQueryFactory.select(APPLICATION)
                .from(DECISION)
                .join(DECISION.application, APPLICATION)
                .where(DECISION.id.in(decisionIds))
                .orderBy(APPLICATION.huntingYear.desc(), APPLICATION.applicationNumber.desc())
                .fetch();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HarvestPermitApplication> search(final HarvestPermitApplicationSearchDTO dto) {
        if (dto.getApplicationNumber() != null) {
            final QHarvestPermitApplication APPLICATION = QHarvestPermitApplication.harvestPermitApplication;
            return jpqlQueryFactory
                    .selectFrom(APPLICATION)
                    .where(APPLICATION.applicationNumber.eq(dto.getApplicationNumber()))
                    .fetch();
        }
        return baseSearchQueryApplications(dto)
                .withMaxQueryResults(StringUtils.isBlank(dto.getRhyOfficialCode())
                        && StringUtils.isBlank(dto.getRkaOfficialCode())
                        ? 1000 : -1)
                .list();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Integer> searchYears(final HarvestPermitApplicationSearchDTO dto) {
        return baseSearchQueryApplications(dto).listYears();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SystemUser> listHandlers() {
        final QPermitDecision DECISION = QPermitDecision.permitDecision;
        final QSystemUser HANDLER = QSystemUser.systemUser;
        return jpqlQueryFactory.select(HANDLER)
                .from(DECISION)
                .join(DECISION.handler, HANDLER)
                .where(DECISION.handler.isNotNull())
                .distinct()
                .fetch();
    }

    private HarvestPermitApplicationSearchQueryBuilder baseSearchQueryApplications(final HarvestPermitApplicationSearchDTO dto) {
        final HarvestPermitApplicationSearchQueryBuilder builder = new HarvestPermitApplicationSearchQueryBuilder(jpqlQueryFactory)
                .withStatus(dto.getStatus())
                .withGameSpeciesCode(dto.getGameSpeciesCode())
                .withHuntingYear(dto.getHuntingYear())
                .withHandler(dto.getHandlerId());

        if (StringUtils.isNotBlank(dto.getRhyOfficialCode())) {
            builder.withRhy(dto.getRhyOfficialCode());
        } else if (StringUtils.isNotBlank(dto.getRkaOfficialCode())) {
            builder.withRka(dto.getRkaOfficialCode());
        }

        return builder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<HarvestPermitApplicationConflictPalsta> findIntersectingPalsta(
            final HarvestPermitApplication firstApplication, final HarvestPermitApplication secondApplication) {
        final Long firstZoneId = F.getId(firstApplication.getArea().getZone());
        final Long secondZoneId = F.getId(secondApplication.getArea().getZone());

        LOG.info("findIntersectingPalsta firstZoneId={} secondZoneId={}", firstZoneId, secondZoneId);

        final PathBuilder<Geometry> g1 = new PathBuilder<>(Geometry.class, "g1");
        final PathBuilder<Geometry> g2 = new PathBuilder<>(Geometry.class, "g2");
        final PathBuilder<Geometry> g3 = new PathBuilder<>(Geometry.class, "g3");
        final PathBuilder<Geometry> g4 = new PathBuilder<>(Geometry.class, "g4");
        final SQPalstaalue PA1 = new SQPalstaalue("pa1");
        final SQPalstaalue PA2 = new SQPalstaalue("pa2");
        final SQKiinteistoNimet KN = SQKiinteistoNimet.kiinteistoNimet;

        final GeometryExpression<Geometry> g1Geometry = GeometryExpressions.asGeometry(g1.get("geom", Geometry.class));
        final GeometryExpression<Geometry> g2Geometry = GeometryExpressions.asGeometry(g2.get("geom", Geometry.class));
        final GeometryExpression<Geometry> g3Geometry = GeometryExpressions.asGeometry(g3.get("geom", Geometry.class));

        final GeometryExpression<Geometry> zoneIntersection =
                GeometryExpressions.geometryOperation(SpatialOps.INTERSECTION, g1Geometry, g2Geometry);

        final NumberPath<Double> g4ConflictAreaSize = g4.getNumber("area_size", Double.class);
        return sqlQueryFactory.query()
                .with(g1, loadSplicedZoneGeometryExpression(firstZoneId))
                .with(g2, loadSplicedZoneGeometryExpression(secondZoneId))
                .with(g3, SQLExpressions
                        .select(stDump(zoneIntersection).as("geom"))
                        .from(g1).join(g2).on(g1Geometry.intersects(g2Geometry)))
                .with(g4, SQLExpressions
                        .select(PA1.id, Expressions.numberOperation(Double.class, SpatialOps.AREA,
                                PA1.geom.intersection(g3Geometry)).sum().as("area_size"))
                        .from(g3).join(PA1).on(PA1.geom.intersects(g3Geometry))
                        .where(g3Geometry.geometryType().in("ST_Polygon", "ST_MultiPolygon"))
                        .groupBy(PA1.id))
                .select(Projections.tuple(PA2.id, PA2.tunnus, KN.nimi, g4ConflictAreaSize))
                .from(g4)
                .join(PA2).on(PA2.id.eq(g4.get("id", Integer.class)))
                .leftJoin(KN).on(KN.tunnus.eq(PA2.tunnus))
                .where(g4ConflictAreaSize.gt(MIN_INTERSECTION_SIZE))
                .orderBy(PA2.tunnus.asc(), PA2.id.asc())
                .fetch()
                .stream()
                .map(tuple -> {
                    final HarvestPermitApplicationConflictPalsta conflictPalsta = new HarvestPermitApplicationConflictPalsta();
                    conflictPalsta.setFirstApplication(firstApplication);
                    conflictPalsta.setSecondApplication(secondApplication);
                    conflictPalsta.setPalstaId(tuple.get(PA2.id));
                    conflictPalsta.setPalstaTunnus(tuple.get(PA2.tunnus));
                    conflictPalsta.setPalstaNimi(tuple.get(KN.nimi));
                    conflictPalsta.setConflictAreaSize(tuple.get(g4ConflictAreaSize));

                    return conflictPalsta;
                })
                .collect(Collectors.toList());
    }

    private static GeometryExpression<Geometry> stSubDivide(final GeometryExpression geom,
                                                            final SimpleExpression count) {
        return GeometryExpressions.asGeometry(Expressions.template(Geometry.class, "ST_SubDivide({0},{1})", geom, count));
    }

    private static GeometryExpression<Geometry> stDump(final Expression geom) {
        return GeometryExpressions.asGeometry(Expressions.template(Geometry.class, "(ST_Dump({0})).geom", geom));
    }

    private static SQLQuery<Geometry> loadSplicedZoneGeometryExpression(final Long firstZoneId) {
        return SQLExpressions
                .select(stSubDivide(stDump(SQZone.zone.geom), Expressions.asNumber(16384)).as("geom"))
                .from(SQZone.zone).where(SQZone.zone.zoneId.eq(firstZoneId));
    }
}
