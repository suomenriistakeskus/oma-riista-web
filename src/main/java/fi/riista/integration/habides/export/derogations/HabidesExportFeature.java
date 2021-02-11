package fi.riista.integration.habides.export.derogations;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.JPQLQueryFactory;
import com.querydsl.sql.SQLQueryFactory;
import fi.riista.feature.common.decision.DecisionStatus;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesService;
import fi.riista.feature.gamediary.QGameSpecies;
import fi.riista.feature.gamediary.harvest.QHarvest;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.QHarvestPermit;
import fi.riista.feature.harvestpermit.QHarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.nestremoval.QHarvestPermitNestRemovalUsage;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.organization.QRiistakeskuksenAlue;
import fi.riista.feature.organization.rhy.QRiistanhoitoyhdistys;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.feature.permit.application.bird.QBirdPermitApplication;
import fi.riista.feature.permit.application.mammal.QMammalPermitApplication;
import fi.riista.feature.permit.application.nestremoval.QNestRemovalPermitApplication;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.PermitDecisionRepository;
import fi.riista.feature.permit.decision.QPermitDecision;
import fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReason;
import fi.riista.feature.permit.decision.derogation.QPermitDecisionDerogationReason;
import fi.riista.feature.permit.decision.methods.PermitDecisionForbiddenMethod;
import fi.riista.feature.permit.decision.methods.QPermitDecisionForbiddenMethod;
import fi.riista.sql.SQRhy;
import fi.riista.util.JaxbUtils;
import org.joda.time.LocalDate;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.list;
import static com.querydsl.core.group.GroupBy.map;
import static com.querydsl.core.group.GroupBy.sum;
import static fi.riista.feature.common.decision.DecisionStatus.DRAFT;
import static fi.riista.feature.common.decision.DecisionStatus.LOCKED;
import static fi.riista.feature.common.decision.DecisionStatus.PUBLISHED;
import static fi.riista.util.Collect.idSet;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Component
public class HabidesExportFeature {

    @Resource
    private SQLQueryFactory sqlQueryFactory;

    @Resource
    private JPQLQueryFactory queryFactory;

    @Resource
    private GameSpeciesService gameSpeciesService;

    @Resource(name = "habidesReportExportJaxbMarshaller")
    private Jaxb2Marshaller jaxbMarshaller;

    @Resource
    private PermitDecisionRepository permitDecisionRepository;

    private void assertNoDraftDecisionsExist(final int year, final int speciesCode) throws DraftDecisionsExistException {
        final List<HarvestPermitSpeciesAmount> amounts = queryDerogations(year, speciesCode, singletonList(DRAFT));
        final List<String> draftDecisions = amounts.stream()
                .map(amount -> amount.getHarvestPermit().getPermitNumber())
                .collect(toList());
        if (!draftDecisions.isEmpty()) {
            throw new DraftDecisionsExistException(draftDecisions);
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasPrivilege('EXPORT_HABIDES_REPORTS')")
    @Transactional(readOnly = true, rollbackFor = { DraftDecisionsExistException.class, DerogationNotFoundException.class })
    public String exportReportAsXml(final int year, final int speciesCode) throws DraftDecisionsExistException, DerogationNotFoundException {
        assertNoDraftDecisionsExist(year, speciesCode);

        final boolean isBirdPermitSpecies = GameSpecies.isBirdPermitSpecies(speciesCode);

        final GameSpecies species = gameSpeciesService.requireByOfficialCode(speciesCode);
        final List<HarvestPermitSpeciesAmount> amounts = queryDerogations(year, speciesCode, Arrays.asList(LOCKED, PUBLISHED));
        if (amounts.isEmpty()) {
            throw new DerogationNotFoundException();
        }
        final List<HarvestPermit> permits = amounts.stream().map(HarvestPermitSpeciesAmount::getHarvestPermit).collect(toList());
        final List<PermitDecision> decisions = permits.isEmpty() ? emptyList() : permitDecisionRepository.findByHarvestPermitIn(permits);
        final Set<Long> rkaIds = decisions.stream().map(item -> item.getRhy().getParentOrganisation().getId()).collect(toSet());
        final Map<Long, Integer> harvestAmounts = queryHarvestAmounts(permits, speciesCode);
        final Map<Long, HabidesNestRemovalAmount> nestEggConstructionAmounts = queryNestEggConstructionAmount(permits, speciesCode);
        final Map<Long, List<PermitDecisionDerogationReason>> reasons = queryReasons(decisions);
        final Map<Long, Map<Integer, List<PermitDecisionForbiddenMethod>>> methods = queryMethods(decisions);
        final Map<Long, String> locations = isBirdPermitSpecies ? queryBirdPermitLocations(decisions) : queryMammalPermitLocations(decisions);
        final Map<Long, String> nestLocations = queryNestRemovalPermitLocations(decisions);
        final Map<String, String> nutsAreas = queryRhyNutsAreas();
        final Map<Long, String> authorities = queryAuthorities(rkaIds);

        return JaxbUtils.marshalToString(
                HabidesXmlGenerator.generateXml(
                        species, amounts, harvestAmounts, nestEggConstructionAmounts,
                        reasons, methods, locations, nestLocations, nutsAreas, authorities),
                jaxbMarshaller);
    }

    private List<HarvestPermitSpeciesAmount> queryDerogations(final int year,
                                     final Integer speciesCode,
                                     final List<DecisionStatus> statuses) {
        if (statuses.isEmpty()) {
            return emptyList();
        }

        final QHarvestPermitSpeciesAmount AMOUNT = QHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;
        final QGameSpecies SPECIES = QGameSpecies.gameSpecies;
        final QHarvestPermit PERMIT = QHarvestPermit.harvestPermit;
        final QPermitDecision DECISION = QPermitDecision.permitDecision;
        final QRiistanhoitoyhdistys RHY = QRiistanhoitoyhdistys.riistanhoitoyhdistys;

        final boolean isHabitatsAnnexIVSpecies = GameSpecies.isHabitatsAnnexIVSpecies(speciesCode);
        final boolean isBirdPermitSpecies = GameSpecies.isBirdPermitSpecies(speciesCode);
        // Nest removal permits reported only for birds and annex IV species of habitats directive
        // (currently in OR only wolf, bear, lynx and otter)
        final BooleanExpression nestRemovalForBirdsAndAnnexIVSpecies = isBirdPermitSpecies || isHabitatsAnnexIVSpecies ?
                null :
                PERMIT.permitTypeCode.ne(PermitTypeCode.NEST_REMOVAL_BASED);

        final LocalDate startDate = isBirdPermitSpecies ? new LocalDate(year, 1, 1) : new LocalDate(year, 8, 1);
        final LocalDate endDate = isBirdPermitSpecies ? new LocalDate(year, 12, 31) : new LocalDate(year + 1, 7, 31);

        final BooleanExpression approvedPermitsOnly =
                AMOUNT.specimenAmount.gt(0)
                .or(AMOUNT.nestAmount.gt(0))
                .or(AMOUNT.eggAmount.gt(0))
                .or(AMOUNT.constructionAmount.gt(0));

        return queryFactory
                .selectFrom(AMOUNT)
                .join(AMOUNT.gameSpecies, SPECIES)
                .join(AMOUNT.harvestPermit, PERMIT).fetchJoin()
                .join(PERMIT.permitDecision, DECISION).fetchJoin()
                .join(PERMIT.rhy, RHY).fetchJoin()
                .where(
                        AMOUNT.beginDate.between(startDate, endDate),
                        SPECIES.officialCode.eq(speciesCode),
                        DECISION.status.in(statuses),
                        PERMIT.permitTypeCode.in(PermitTypeCode.DEROGATION_PERMIT_CODES),
                        nestRemovalForBirdsAndAnnexIVSpecies,
                        approvedPermitsOnly)
                .fetch();
    }

    private Map<Long, List<PermitDecisionDerogationReason>> queryReasons(final Collection<PermitDecision> decisions) {
        final QPermitDecisionDerogationReason REASON = QPermitDecisionDerogationReason.permitDecisionDerogationReason;
        final QPermitDecision DECISION = QPermitDecision.permitDecision;

        if (decisions.isEmpty()) {
            return emptyMap();
        }

        return queryFactory.from(REASON)
                .join(REASON.permitDecision, DECISION)
                .where(DECISION.in(decisions))
                .transform(groupBy(DECISION.id).as(list(REASON)));
    }

    private Map<Long, Map<Integer, List<PermitDecisionForbiddenMethod>>> queryMethods(final Collection<PermitDecision> decisions) {
        final QPermitDecisionForbiddenMethod METHOD = QPermitDecisionForbiddenMethod.permitDecisionForbiddenMethod;
        final QPermitDecision DECISION = QPermitDecision.permitDecision;

        if (decisions.isEmpty()) {
            return emptyMap();
        }

        return queryFactory.from(METHOD)
                .join(METHOD.permitDecision, DECISION)
                .where(DECISION.in(decisions))
                .transform(groupBy(DECISION.id).as(map(METHOD.gameSpecies.officialCode, list(METHOD))));
    }

    private Map<Long, Integer> queryHarvestAmounts(final Collection<HarvestPermit> permits, final int speciesCode) {
        final QHarvest HARVEST = QHarvest.harvest;
        final QHarvestPermit PERMIT = QHarvestPermit.harvestPermit;
        final QGameSpecies SPECIES = QGameSpecies.gameSpecies;

        if (permits.isEmpty()) {
            return emptyMap();
        }

        return queryFactory.from(HARVEST)
                .join(HARVEST.harvestPermit, PERMIT)
                .join(HARVEST.species, SPECIES)
                .where(
                        PERMIT.in(permits),
                        SPECIES.officialCode.eq(speciesCode),
                        HARVEST.harvestReportState.eq(HarvestReportState.APPROVED))
                .transform(groupBy(PERMIT.id).as(sum(HARVEST.amount)));
    }

    private Map<Long, HabidesNestRemovalAmount> queryNestEggConstructionAmount(final Collection<HarvestPermit> permits, final int speciesCode) {
        final QHarvestPermitNestRemovalUsage USAGE = QHarvestPermitNestRemovalUsage.harvestPermitNestRemovalUsage;
        final QHarvestPermitSpeciesAmount SPECIES_AMOUNT = QHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;
        final QHarvestPermit PERMIT = QHarvestPermit.harvestPermit;
        final QGameSpecies SPECIES = QGameSpecies.gameSpecies;

        if (permits.isEmpty()) {
            return emptyMap();
        }

        return queryFactory.select(PERMIT.id, USAGE.nestAmount, USAGE.eggAmount, USAGE.constructionAmount)
                .from(USAGE)
                .join(USAGE.harvestPermitSpeciesAmount, SPECIES_AMOUNT)
                .join(SPECIES_AMOUNT.harvestPermit, PERMIT)
                .join(SPECIES_AMOUNT.gameSpecies, SPECIES)
                .where(PERMIT.in(permits),
                        SPECIES.officialCode.eq(speciesCode))
                .fetch()
                .stream()
                .collect(Collectors.toMap(t -> t.get(PERMIT.id),
                        t -> new HabidesNestRemovalAmount(t.get(USAGE.nestAmount), t.get(USAGE.eggAmount), t.get(USAGE.constructionAmount))));
    }

    private Map<Long, String> queryBirdPermitLocations(final Collection<PermitDecision> decisions) {
        if (decisions.isEmpty()) {
            return emptyMap();
        }

        final QBirdPermitApplication BIRD_APP = QBirdPermitApplication.birdPermitApplication;
        final Set<Long> applicationIds = decisions.stream().map(PermitDecision::getApplication).collect(idSet());
        final NumberPath<Long> appId = BIRD_APP.harvestPermitApplication.id;

        return queryFactory.from(BIRD_APP)
                .where(appId.in(applicationIds))
                .transform(groupBy(appId).as(BIRD_APP.protectedArea.name));
    }

    private Map<Long, String> queryNestRemovalPermitLocations(final Collection<PermitDecision> decisions) {
        if (decisions.isEmpty()) {
            return emptyMap();
        }

        final QNestRemovalPermitApplication NEST_REMOVAL_APP = QNestRemovalPermitApplication.nestRemovalPermitApplication;
        final Set<Long> applicationIds = decisions.stream().map(PermitDecision::getApplication).collect(idSet());
        final NumberPath<Long> appId = NEST_REMOVAL_APP.harvestPermitApplication.id;

        return queryFactory.from(NEST_REMOVAL_APP)
                .where(appId.in(applicationIds))
                .transform(groupBy(appId).as(NEST_REMOVAL_APP.areaDescription));
    }

    private Map<Long, String> queryMammalPermitLocations(final Collection<PermitDecision> decisions) {
        if (decisions.isEmpty()) {
            return emptyMap();
        }

        final QMammalPermitApplication MAMMAL_PERMIT_APP = QMammalPermitApplication.mammalPermitApplication;
        final Set<Long> applicationIds = decisions.stream().map(PermitDecision::getApplication).collect(idSet());
        final NumberPath<Long> appId = MAMMAL_PERMIT_APP.harvestPermitApplication.id;

        return queryFactory.from(MAMMAL_PERMIT_APP)
                .where(appId.in(applicationIds))
                .transform(groupBy(appId).as(MAMMAL_PERMIT_APP.areaDescription));
    }

    private Map<String, String> queryRhyNutsAreas() {
        final SQRhy RHY = SQRhy.rhy;

        return sqlQueryFactory
                .from(RHY)
                .transform(groupBy(RHY.id).as(RHY.nuts2Id));
    }

    private Map<Long, String> queryAuthorities(final Set<Long> rkaIds) {
        final QRiistakeskuksenAlue RKA = QRiistakeskuksenAlue.riistakeskuksenAlue;

        if (rkaIds.isEmpty()) {
            return emptyMap();
        }

        return queryFactory
                .from(RKA)
                .where(RKA.id.in(rkaIds))
                .transform(groupBy(RKA.id).as(RKA.nameFinnish));
    }

}
