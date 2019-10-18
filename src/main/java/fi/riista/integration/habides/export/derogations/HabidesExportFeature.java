package fi.riista.integration.habides.export.derogations;

import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.JPQLQueryFactory;
import com.querydsl.sql.SQLQueryFactory;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesService;
import fi.riista.feature.gamediary.QGameSpecies;
import fi.riista.feature.gamediary.harvest.QHarvest;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.QHarvestPermit;
import fi.riista.feature.harvestpermit.QHarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.organization.QRiistakeskuksenAlue;
import fi.riista.feature.organization.rhy.QRiistanhoitoyhdistys;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.feature.permit.application.bird.QBirdPermitApplication;
import fi.riista.feature.permit.decision.PermitDecision;
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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.list;
import static com.querydsl.core.group.GroupBy.sum;
import static fi.riista.util.Collect.idSet;
import static java.util.Collections.emptyMap;
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

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasPrivilege('EXPORT_HABIDES_REPORTS')")
    @Transactional(readOnly = true)
    public String exportReportForBirdsAsXml(final LocalDate startDate, final LocalDate endDate, final int speciesCode) {

        final GameSpecies species = gameSpeciesService.requireByOfficialCode(speciesCode);
        final List<HarvestPermitSpeciesAmount> amounts = queryDerogations(startDate, endDate, speciesCode);
        final List<HarvestPermit> permits = amounts.stream().map(HarvestPermitSpeciesAmount::getHarvestPermit).collect(toList());
        final List<PermitDecision> decisions = permits.stream().map(HarvestPermit::getPermitDecision).collect(toList());
        final Set<Long> rkaIds = decisions.stream().map(item -> item.getRhy().getParentOrganisation().getId()).collect(toSet());
        final Map<Long, Integer> harvestAmounts = queryHarvestAmounts(permits, speciesCode);
        final Map<Long, List<PermitDecisionDerogationReason>> reasons = queryReasons(decisions);
        final Map<Long, List<PermitDecisionForbiddenMethod>> methods = queryMethods(decisions);
        final Map<Long, String> locations = queryBirdPermitLocations(decisions);
        final Map<String, String> nutsAreas = queryRhyNutsAreas();
        final Map<Long, String> authorities = queryAuthorities(rkaIds);

        return JaxbUtils.marshalToString(
                HabidesXmlGenerator.generateBirdsXml(
                        species, amounts, harvestAmounts, reasons, methods, locations, nutsAreas, authorities),
                jaxbMarshaller);
    }

    private List<HarvestPermitSpeciesAmount> queryDerogations(final LocalDate startDate, final LocalDate endDate, final Integer speciesCode) {
        final QHarvestPermitSpeciesAmount AMOUNT = QHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;
        final QGameSpecies SPECIES = QGameSpecies.gameSpecies;
        final QHarvestPermit PERMIT = QHarvestPermit.harvestPermit;
        final QPermitDecision DECISION = QPermitDecision.permitDecision;
        final QRiistanhoitoyhdistys RHY = QRiistanhoitoyhdistys.riistanhoitoyhdistys;

        return queryFactory
                .selectFrom(AMOUNT)
                .join(AMOUNT.gameSpecies, SPECIES)
                .join(AMOUNT.harvestPermit, PERMIT).fetchJoin()
                .join(PERMIT.permitDecision, DECISION).fetchJoin()
                .join(PERMIT.rhy, RHY).fetchJoin()
                .where(
                        AMOUNT.beginDate.between(startDate, endDate),
                        SPECIES.officialCode.eq(speciesCode),
                        DECISION.status.eq(PermitDecision.Status.PUBLISHED),
                        PERMIT.permitTypeCode.in(PermitTypeCode.DEROGATION_PERMIT_CODES))
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

    private Map<Long, List<PermitDecisionForbiddenMethod>> queryMethods(final Collection<PermitDecision> decisions) {
        final QPermitDecisionForbiddenMethod METHOD = QPermitDecisionForbiddenMethod.permitDecisionForbiddenMethod;
        final QPermitDecision DECISION = QPermitDecision.permitDecision;

        if (decisions.isEmpty()) {
            return emptyMap();
        }

        return queryFactory.from(METHOD)
                .join(METHOD.permitDecision, DECISION)
                .where(DECISION.in(decisions))
                .transform(groupBy(DECISION.id).as(list(METHOD)));
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
