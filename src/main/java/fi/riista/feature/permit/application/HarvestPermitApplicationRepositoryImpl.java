package fi.riista.feature.permit.application;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLQueryFactory;
import com.querydsl.spatial.GeometryExpression;
import com.querydsl.spatial.GeometryExpressions;
import com.querydsl.spatial.SpatialOps;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import fi.riista.feature.account.user.QSystemUser;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.QHarvest;
import fi.riista.feature.gamediary.harvest.specimen.QHarvestSpecimen;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.harvestpermit.QHarvestPermit;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.huntingclub.group.QHuntingClubGroup;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.huntingclub.hunting.day.QGroupHuntingDay;
import fi.riista.feature.organization.rhy.QRiistanhoitoyhdistys;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.feature.permit.application.amendment.QAmendmentApplicationData;
import fi.riista.feature.permit.application.conflict.HarvestPermitApplicationConflictPalsta;
import fi.riista.feature.permit.application.search.HarvestPermitApplicationSearchDTO;
import fi.riista.feature.permit.application.search.HarvestPermitApplicationSearchQueryBuilder;
import fi.riista.feature.common.decision.DecisionStatus;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.QPermitDecision;
import fi.riista.feature.permit.decision.revision.QPermitDecisionRevision;
import fi.riista.sql.SQKiinteistoNimet;
import fi.riista.sql.SQPalstaalue;
import fi.riista.sql.SQZone;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import io.vavr.Tuple3;
import org.apache.commons.lang3.StringUtils;
import org.geolatte.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.querydsl.core.group.GroupBy.groupBy;
import static fi.riista.feature.common.decision.GrantStatus.RESTRICTED;
import static fi.riista.feature.common.decision.GrantStatus.UNCHANGED;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;

@Repository
public class HarvestPermitApplicationRepositoryImpl implements HarvestPermitApplicationRepositoryCustom {
    private static final Logger LOG = LoggerFactory.getLogger(HarvestPermitApplicationRepositoryImpl.class);
    private static final int MIN_INTERSECTION_SIZE = 1000;

    private static final QHarvestPermitApplication APPLICATION = QHarvestPermitApplication.harvestPermitApplication;
    private static final QPermitDecisionRevision REV = QPermitDecisionRevision.permitDecisionRevision;
    private static final QPermitDecision DECISION = QPermitDecision.permitDecision;
    private static final QHarvestPermit PERMIT = QHarvestPermit.harvestPermit;

    @Resource
    private SQLQueryFactory sqlQueryFactory;

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

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

        if (decisionIds.isEmpty()) {
            return emptyList();
        }

        return jpqlQueryFactory.select(APPLICATION)
                .from(DECISION)
                .join(DECISION.application, APPLICATION)
                .where(DECISION.id.in(decisionIds))
                .orderBy(APPLICATION.applicationYear.desc(), APPLICATION.applicationNumber.desc())
                .fetch();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HarvestPermitApplication> listByAnnualPermitsToRenew(final Long handlerId) {

        final Set<Long> decisionIds = fetchDecisionIdsToRenew();

        if (decisionIds.isEmpty()) {
            return emptyList();
        }

        BooleanExpression predicate =
                buildPredicateForApprovedAndPublishedDecisionsIn(decisionIds);
        if (handlerId != null) {
            predicate = predicate.and(DECISION.handler.id.eq(handlerId));
        }
        return jpqlQueryFactory.select(APPLICATION)
                .from(DECISION)
                .innerJoin(DECISION.application, APPLICATION)
                .where(predicate)
                .orderBy(APPLICATION.applicationYear.desc(), APPLICATION.applicationNumber.desc())
                .fetch();
    }

    private static BooleanExpression buildPredicateForApprovedAndPublishedDecisionsIn(final Set<Long> decisionIds) {
        return DECISION.id.in(decisionIds)
                .and(DECISION.status.eq(DecisionStatus.PUBLISHED)
                        .and(DECISION.grantStatus.in(UNCHANGED, RESTRICTED)));
    }

    private Set<Long> fetchDecisionIdsToRenew() {
        // Fetch all annual permits from last year onwards, where renewal is not cancelled
        final List<Tuple> tuples = jpqlQueryFactory.select(PERMIT.permitDecision.id, PERMIT.permitYear,
                PERMIT.harvestReportState)
                .from(PERMIT)
                .join(PERMIT.permitDecision, DECISION)
                .where(PERMIT.permitYear.goe(DateUtil.currentYear() - 1))
                .where(PERMIT.permitTypeCode.eq(PermitTypeCode.ANNUAL_UNPROTECTED_BIRD))
                .where(DECISION.decisionType.eq(PermitDecision.DecisionType.HARVEST_PERMIT))
                .fetch();


        // Group newest permit by decision
        final Map<Long, Tuple3<Long, Integer, HarvestReportState>> newestPermitByDecision = tuples.stream()
                .map(tuple -> new Tuple3<>(tuple.get(PERMIT.permitDecision.id),
                        tuple.get(PERMIT.permitYear),
                        tuple.get(PERMIT.harvestReportState)))
                .collect(Collectors.toMap(
                        tuple -> tuple._1(),
                        identity(),
                        (first, second) -> first._2() > second._2() ? first : second));

        // Select decisions with newest permit having approved harvest report
        return newestPermitByDecision.entrySet().stream()
                .filter(e -> e.getValue()._3() == HarvestReportState.APPROVED)
                .map(e -> e.getKey())
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Integer> getAnnualPermitsToRenewByHandlerId() {
        final Set<Long> decisionIds = fetchDecisionIdsToRenew();
        if (decisionIds.isEmpty()) {
            return emptyMap();
        }


        return jpqlQueryFactory
                .from(DECISION)
                .where(buildPredicateForApprovedAndPublishedDecisionsIn(decisionIds))
                .groupBy(DECISION.handler.id)
                .transform(groupBy(DECISION.handler.id).as(DECISION.count().intValue()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Harvest> findNonEdibleHarvestsByPermit(final HarvestPermit original) {
        final QHarvest HARVEST = QHarvest.harvest;
        final QHarvestSpecimen SPECIMEN = QHarvestSpecimen.harvestSpecimen;
        final QGroupHuntingDay GROUP_HUNTING_DAY = QGroupHuntingDay.groupHuntingDay;
        final QHuntingClubGroup GROUP = QHuntingClubGroup.huntingClubGroup;

        final JPQLQuery<GroupHuntingDay> days = jpqlQueryFactory.selectFrom(GROUP_HUNTING_DAY)
                .join(GROUP_HUNTING_DAY.group, GROUP)
                .where(GROUP.harvestPermit.eq(original));

        return jpqlQueryFactory.select(HARVEST).from(SPECIMEN)
                .join(SPECIMEN.harvest, HARVEST)
                .join(HARVEST.huntingDayOfGroup, GROUP_HUNTING_DAY)
                .where(GROUP_HUNTING_DAY.in(days))
                .where(SPECIMEN.notEdible.isTrue())
                .fetch();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HarvestPermitApplication> findByOriginalPermit(final HarvestPermit originalPermit) {
        QAmendmentApplicationData DATA = QAmendmentApplicationData.amendmentApplicationData;
        QHarvestPermitApplication APPLICATION = QHarvestPermitApplication.harvestPermitApplication;
        return jpqlQueryFactory.select(APPLICATION)
                .from(DATA)
                .join(DATA.application, APPLICATION)
                .where(DATA.originalPermit.eq(originalPermit))
                .fetch();
    }

    @Override
    @Transactional(readOnly = true)
    public Slice<HarvestPermitApplication> search(final HarvestPermitApplicationSearchDTO dto,
                                                  final Pageable pageRequest) {
        if (dto.getApplicationNumber() != null) {
            return new SliceImpl<>(findByApplicationNumber(dto), pageRequest, false);
        }

        return baseSearchQueryApplications(dto).slice(pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HarvestPermitApplication> search(final HarvestPermitApplicationSearchDTO dto) {
        return baseSearchQueryApplications(dto).list();
    }

    private List<HarvestPermitApplication> findByApplicationNumber(final HarvestPermitApplicationSearchDTO dto) {
        final QHarvestPermitApplication APPLICATION = QHarvestPermitApplication.harvestPermitApplication;
        return jpqlQueryFactory
                .selectFrom(APPLICATION)
                .where(APPLICATION.applicationNumber.eq(dto.getApplicationNumber()))
                .fetch();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HarvestPermitApplication> searchForRhy(@Nonnull final String officialCode,
                                                       final int year,
                                                       @Nullable final Integer gameSpeciesCode) {
        requireNonNull(officialCode);

        final QHarvestPermitApplication APPLICATION = QHarvestPermitApplication.harvestPermitApplication;
        final QRiistanhoitoyhdistys RHY = QRiistanhoitoyhdistys.riistanhoitoyhdistys;

        return jpqlQueryFactory.selectFrom(APPLICATION)
                .join(APPLICATION.rhy, RHY)
                .where(APPLICATION.rhy.officialCode.eq(officialCode))
                .where(APPLICATION.applicationYear.eq(year))
                .where(APPLICATION.harvestPermitCategory.eq(HarvestPermitCategory.MOOSELIKE))
                .where(APPLICATION.status.eq(HarvestPermitApplication.Status.ACTIVE))
                .where(gameSpeciesCode != null
                        ? APPLICATION.speciesAmounts.any().gameSpecies.officialCode.eq(gameSpeciesCode)
                        : null).fetch();
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
        final HarvestPermitApplicationSearchQueryBuilder builder =
                new HarvestPermitApplicationSearchQueryBuilder(jpqlQueryFactory)
                        .withStatus(dto.getStatus())
                        .withDecisionType(dto.getDecisionType())
                        .withAppealStatus(dto.getAppealStatus())
                        .withGrantStatus(dto.getGrantStatus())
                        .withProtectedArea(dto.getProtectedArea())
                        .withDerogationReason(dto.getDerogationReason())
                        .withForbiddenMethod(dto.getForbiddenMethod())
                        .withHarvestPermitCategory(dto.getHarvestPermitCategory())
                        .withGameSpeciesCode(dto.getGameSpeciesCode())
                        .withHuntingYear(dto.getHuntingYear())
                        .withValidityYears(dto.getValidityYears())
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
                                PA1.geom.intersection(g3Geometry)).sum().as(
                                "area_size"))
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
                    final HarvestPermitApplicationConflictPalsta conflictPalsta =
                            new HarvestPermitApplicationConflictPalsta();
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
        return GeometryExpressions.asGeometry(Expressions.template(Geometry.class, "ST_SubDivide({0},{1})", geom,
                count));
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
