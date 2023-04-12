package fi.riista.feature.permit.application;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
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
import fi.riista.feature.common.decision.DecisionStatus;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.QGameSpecies;
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
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.QPermitDecision;
import fi.riista.feature.permit.decision.revision.QPermitDecisionRevision;
import fi.riista.feature.permit.decision.species.QPermitDecisionSpeciesAmount;
import fi.riista.sql.SQKiinteistoNimet;
import fi.riista.sql.SQPalstaalue;
import fi.riista.sql.SQVesialue;
import fi.riista.sql.SQZone;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import fi.riista.util.Locales;
import io.vavr.Tuple3;
import org.apache.commons.lang3.StringUtils;
import org.geolatte.geom.Geometry;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.querydsl.core.group.GroupBy.groupBy;
import static fi.riista.feature.common.decision.GrantStatus.RESTRICTED;
import static fi.riista.feature.common.decision.GrantStatus.UNCHANGED;
import static fi.riista.feature.permit.application.HarvestPermitApplication.Status.ACTIVE;
import static fi.riista.feature.permit.application.HarvestPermitApplication.Status.AMENDING;
import static fi.riista.util.F.mapNullable;
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
    private static final QGameSpecies SPECIES = QGameSpecies.gameSpecies;
    private static final QHarvestPermitApplicationSpeciesAmount SPA =
            QHarvestPermitApplicationSpeciesAmount.harvestPermitApplicationSpeciesAmount;

    private NamedParameterJdbcOperations jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

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
        return DECISION.id.in(decisionIds).and(DECISION.grantStatus.in(UNCHANGED, RESTRICTED));
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

    @Override
    @Transactional(readOnly = true)
    public List<GameSpecies> listSpecies() {
        final QPermitDecisionSpeciesAmount SPECIES_AMOUNT = QPermitDecisionSpeciesAmount.permitDecisionSpeciesAmount;
        final QGameSpecies SPECIES = QGameSpecies.gameSpecies;
        return jpqlQueryFactory.select(SPECIES)
                .from(SPECIES_AMOUNT)
                .join(SPECIES_AMOUNT.gameSpecies, SPECIES)
                .distinct()
                .fetch();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameSpecies> listSpecies(@Nonnull final HarvestPermitCategory permitCategory) {
        requireNonNull(permitCategory);

        final QHarvestPermitApplication APPLICATION = QHarvestPermitApplication.harvestPermitApplication;
        final QPermitDecisionSpeciesAmount SPECIES_AMOUNT = QPermitDecisionSpeciesAmount.permitDecisionSpeciesAmount;
        final QPermitDecision DECISION = QPermitDecision.permitDecision;

        return jpqlQueryFactory.select(SPECIES_AMOUNT.gameSpecies)
                .from(APPLICATION)
                .join(APPLICATION.decision, DECISION)
                .join(SPECIES_AMOUNT).on(SPECIES_AMOUNT.permitDecision.eq(DECISION))
                .where(APPLICATION.harvestPermitCategory.eq(permitCategory))
                .distinct()
                .fetch();
    }

    private HarvestPermitApplicationSearchQueryBuilder baseSearchQueryApplications(final HarvestPermitApplicationSearchDTO dto) {
        // Use map nullable to avoid selecting default language when not specified in dto.
        final Locale decisionLocale = mapNullable(dto.getDecisionLocale(), Locales::getLocaleByLanguageCode);

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
                        .withDecisionLocale(decisionLocale)
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
            final HarvestPermitApplication firstApplication, final HarvestPermitApplication secondApplication, final int chunkSize) {
        final Long firstZoneId = F.getId(firstApplication.getArea().getZone());
        final Long secondZoneId = F.getId(secondApplication.getArea().getZone());

        LOG.info("findIntersectingPalsta firstZoneId={} secondZoneId={} chunkSize={}", firstZoneId, secondZoneId, chunkSize);

        final PathBuilder<Geometry> g1 = new PathBuilder<>(Geometry.class, "g1");
        final PathBuilder<Geometry> g2 = new PathBuilder<>(Geometry.class, "g2");
        final PathBuilder<Geometry> g3 = new PathBuilder<>(Geometry.class, "g3");
        final PathBuilder<Geometry> g4 = new PathBuilder<>(Geometry.class, "g4");
        final PathBuilder<Geometry> g5 = new PathBuilder<>(Geometry.class, "g5");
        final PathBuilder<Geometry> g6 = new PathBuilder<>(Geometry.class, "g6");
        final SQPalstaalue PA1 = new SQPalstaalue("pa1");
        final SQKiinteistoNimet KN = SQKiinteistoNimet.kiinteistoNimet;
        final SQVesialue VA = SQVesialue.vesialue;

        final GeometryExpression<Geometry> g1Geometry = GeometryExpressions.asGeometry(g1.get("geom", Geometry.class));
        final GeometryExpression<Geometry> g2Geometry = GeometryExpressions.asGeometry(g2.get("geom", Geometry.class));
        final GeometryExpression<Geometry> g3Geometry = GeometryExpressions.asGeometry(g3.get("geom", Geometry.class));
        final GeometryExpression<Geometry> g4Geometry = GeometryExpressions.asGeometry(g4.get("geom", Geometry.class));

        final PathBuilder<Integer> g4Id = g4.get("id", Integer.class);
        final PathBuilder<Integer> g5Id = g5.get("id", Integer.class);
        final PathBuilder<Integer> g6Id = g6.get("id", Integer.class);
        final PathBuilder<Long> g4Tunnus = g4.get("tunnus", Long.class);
        final PathBuilder<Long> g5Tunnus = g5.get("tunnus", Long.class);

        final GeometryExpression<Geometry> zoneIntersection =
                GeometryExpressions.geometryOperation(SpatialOps.INTERSECTION, g1Geometry, g2Geometry);

        final NumberPath<Double> g5ConflictAreaSize = g5.getNumber("area_size", Double.class);
        final NumberPath<Double> g6ConflictWaterSize = g6.getNumber("water_area", Double.class);
        return sqlQueryFactory.query()
                .with(g1, loadSplicedZoneGeometryExpression(firstZoneId, chunkSize))
                .with(g2, loadSplicedZoneGeometryExpression(secondZoneId, chunkSize))
                .with(g3, SQLExpressions
                        .select(stDump(zoneIntersection).as("geom"))
                        .from(g1).join(g2).on(g1Geometry.intersects(g2Geometry)))

                .with(g4, SQLExpressions
                        .select(PA1.id, PA1.tunnus, PA1.geom.intersection(g3Geometry).as("geom"))
                        .from(g3)
                        .join(PA1).on(PA1.geom.intersects(g3Geometry))
                        .where(g3Geometry.geometryType().in("ST_Polygon", "ST_MultiPolygon")))

                .with(g5, SQLExpressions
                        .select(g4Id, g4Tunnus, Expressions.numberOperation(Double.class, SpatialOps.AREA, g4Geometry).sum().as("area_size"))
                        .from(g4)
                        .where(g4Geometry.geometryType().in("ST_Polygon", "ST_MultiPolygon"))
                        .groupBy(g4Id, g4Tunnus))

                .with(g6, SQLExpressions
                        .select(g4Id, Expressions.numberOperation(Double.class, SpatialOps.AREA, VA.geom.intersection(g4Geometry)).sum().as("water_area"))
                        .from(g4)
                        .join(VA).on(VA.geom.intersects(g4Geometry))
                        .where(g4Geometry.geometryType().in("ST_Polygon", "ST_MultiPolygon"))
                        .groupBy(g4Id))

                .select(Projections.tuple(
                        g5Id,
                        g5Tunnus,
                        KN.nimi,
                        g5ConflictAreaSize,
                        g6ConflictWaterSize.coalesce(0.0).as("water_area")
                ))
                .from(g5)
                .leftJoin(KN).on(KN.tunnus.eq(g5Tunnus))
                .leftJoin(g6).on(g6Id.eq(g5Id))
                .where(g5ConflictAreaSize.gt(MIN_INTERSECTION_SIZE))
                .orderBy(new OrderSpecifier<>(Order.ASC, g5Tunnus),
                         new OrderSpecifier<>(Order.ASC, g5Id))
                .fetch()
                .stream()
                .map(tuple -> {
                    final HarvestPermitApplicationConflictPalsta conflictPalsta =
                            new HarvestPermitApplicationConflictPalsta();
                    conflictPalsta.setFirstApplication(firstApplication);
                    conflictPalsta.setSecondApplication(secondApplication);
                    conflictPalsta.setPalstaId(tuple.get(g5Id));
                    conflictPalsta.setPalstaTunnus(tuple.get(g5Tunnus));
                    conflictPalsta.setPalstaNimi(tuple.get(KN.nimi));
                    conflictPalsta.setConflictAreaSize(tuple.get(g5ConflictAreaSize));
                    conflictPalsta.setConflictAreaWaterSize(tuple.get(Expressions.numberPath(Double.class, "water_area")));
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

    private static SQLQuery<Geometry> loadSplicedZoneGeometryExpression(final Long firstZoneId, final int chunkSize) {
        return SQLExpressions
                .select(stSubDivide(stDump(SQZone.zone.geom), Expressions.asNumber(chunkSize)).as("geom"))
                .from(SQZone.zone).where(SQZone.zone.zoneId.eq(firstZoneId));
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<HarvestPermitApplication> findByPermitDecisionIn(final Collection<PermitDecision> decisions) {
        if (decisions.isEmpty()) {
            return emptyList();
        }

        return jpqlQueryFactory.select(APPLICATION)
                .from(DECISION)
                .join(DECISION.application, APPLICATION)
                .where(DECISION.in(decisions))
                .fetch();
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<HarvestPermitApplication> findNotHandledByHuntingYearAndSpeciesAndCategory(final int huntingYear,
                                                                                           final GameSpecies species,
                                                                                           final HarvestPermitCategory category) {
        final LocalDate huntingYearStart = DateUtil.huntingYearBeginDate(huntingYear);
        final LocalDate huntingYearEnd = DateUtil.huntingYearEndDate(huntingYear);

        return jpqlQueryFactory
                .select(APPLICATION)
                .from(SPA)
                .innerJoin(SPA.harvestPermitApplication, APPLICATION)
                .innerJoin(SPA.gameSpecies, SPECIES)
                .leftJoin(APPLICATION.decision, DECISION)
                .where(SPA.beginDate.between(huntingYearStart, huntingYearEnd)
                        .and(SPECIES.eq(species))
                        .and(APPLICATION.harvestPermitCategory.eq(category))
                        .and(DECISION.isNull())
                        .and(APPLICATION.status.eq(ACTIVE).or(APPLICATION.status.eq(AMENDING))))
                .orderBy(APPLICATION.applicationNumber.asc())
                .fetch();
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<Long> findApplicationIdsInReindeerArea(final Collection<HarvestPermitApplication> applications,
                                                       final HarvestPermitCategory category) {
        if (applications.isEmpty()) {
            return emptyList();
        }

        final Set<Long> appIds = F.getUniqueIds(applications);

        String parentApp = "";
        switch (category) {
            case MAMMAL:
                parentApp = "mammal_permit_application";
                break;
            case LARGE_CARNIVORE_BEAR:
            case LARGE_CARNIVORE_LYNX:
            case LARGE_CARNIVORE_LYNX_PORONHOITO:
            case LARGE_CARNIVORE_WOLF:
            case LARGE_CARNIVORE_WOLF_PORONHOITO:
                parentApp = "carnivore_permit_application";
                break;
            case DEPORTATION:
                parentApp = "deportation_permit_application";
                break;
            case RESEARCH:
                parentApp = "research_permit_application";
                break;
            default:
                throw new IllegalArgumentException("Category not supported");
        }

        final String sql = "SELECT app.harvest_permit_application_id " +
                "FROM " + parentApp + " parentApp " +
                "JOIN harvest_permit_application app " +
                "ON parentApp.harvest_permit_application_id = app.harvest_permit_application_id " +
                "JOIN harvest_area area " +
                "ON ST_Contains(area.geom, ST_SetSRID(ST_MakePoint(parentApp.longitude, parentApp.latitude), 3067)) " +
                "WHERE app.harvest_permit_application_id IN (:appIds) " +
                "AND area.type = 'PORONHOITOALUE'";

        return jdbcTemplate.query(sql,
                new MapSqlParameterSource("appIds", appIds),
                (rs, rowNum) -> rs.getLong("harvest_permit_application_id"));
    }

}
