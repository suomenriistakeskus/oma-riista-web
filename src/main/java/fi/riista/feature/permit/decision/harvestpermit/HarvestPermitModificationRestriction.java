package fi.riista.feature.permit.decision.harvestpermit;

import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.QHarvest;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.QHarvestPermit;
import fi.riista.feature.harvestpermit.QHarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.allocation.QHarvestPermitAllocation;
import fi.riista.feature.harvestpermit.endofhunting.QMooseHarvestReport;
import fi.riista.feature.huntingclub.group.QHuntingClubGroup;
import fi.riista.feature.huntingclub.permit.basicsummary.QBasicClubHuntingSummary;
import fi.riista.feature.huntingclub.permit.summary.QMooseHuntingSummary;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmount;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.function.Function;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

@Service
public class HarvestPermitModificationRestriction {

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean canModifyHarvestPermit(final PermitDecision permitDecision,
                                          final HarvestPermit harvestPermit) {
        return hasSingleAmountForDecisionSpecies(permitDecision.getSpeciesAmounts()) &&
                hasSingleAmountForHarvestSpecies(harvestPermit.getSpeciesAmounts()) &&
                !hasReportedData(harvestPermit) &&
                !hasAmendmentPermits(harvestPermit);
    }

    private boolean hasReportedData(final HarvestPermit harvestPermit) {
        return hasReportedHarvests(harvestPermit) ||
                hasAllocations(harvestPermit) ||
                hasHuntingClubGroups(harvestPermit) ||
                hasMooseHarvestReports(harvestPermit) ||
                hasMooseHuntingSummaries(harvestPermit) ||
                hasBasicClubHuntingSummaries(harvestPermit);
    }

    static boolean hasSingleAmountForHarvestSpecies(final Collection<HarvestPermitSpeciesAmount> speciesAmounts) {
        return hasSingleAmount(speciesAmounts, HarvestPermitSpeciesAmount::getGameSpecies);
    }

    static boolean hasSingleAmountForDecisionSpecies(final Collection<PermitDecisionSpeciesAmount> speciesAmounts) {
        return hasSingleAmount(speciesAmounts, PermitDecisionSpeciesAmount::getGameSpecies);
    }

    private static <T> boolean hasSingleAmount(final Collection<T> speciesAmounts,
                                               final Function<T, GameSpecies> propertyGetter) {
        return speciesAmounts.stream()
                // Count by species
                .collect(groupingBy(propertyGetter, counting()))
                // Any species has more than 1 amount?
                .values().stream().noneMatch(a -> a > 1);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean hasAmendmentPermits(final HarvestPermit harvestPermit) {
        return 0 < harvestPermitRepository.count(QHarvestPermit.harvestPermit.originalPermit.eq(harvestPermit));
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean hasReportedHarvests(final HarvestPermit harvestPermit) {
        final QHarvest HARVEST = QHarvest.harvest;

        return 0 < jpqlQueryFactory.query()
                .select(HARVEST).from(HARVEST)
                .where(HARVEST.harvestPermit.eq(harvestPermit))
                .fetchCount();
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean hasAllocations(final HarvestPermit harvestPermit) {
        final QHarvestPermitAllocation ALLOCATION = QHarvestPermitAllocation.harvestPermitAllocation;

        return 0 < jpqlQueryFactory.query()
                .select(ALLOCATION).from(ALLOCATION)
                .where(ALLOCATION.harvestPermit.eq(harvestPermit))
                .fetchCount();
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean hasHuntingClubGroups(final HarvestPermit harvestPermit) {
        final QHuntingClubGroup GROUP = QHuntingClubGroup.huntingClubGroup;

        return 0 < jpqlQueryFactory.query()
                .select(GROUP).from(GROUP)
                .where(GROUP.harvestPermit.eq(harvestPermit))
                .fetchCount();
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean hasMooseHarvestReports(final HarvestPermit harvestPermit) {
        final QMooseHarvestReport MHR = QMooseHarvestReport.mooseHarvestReport;
        final QHarvestPermitSpeciesAmount SPA = QHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;

        return 0 < jpqlQueryFactory.query()
                .select(MHR).from(MHR)
                .join(MHR.speciesAmount, SPA)
                .where(SPA.harvestPermit.eq(harvestPermit))
                .fetchCount();
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean hasMooseHuntingSummaries(final HarvestPermit harvestPermit) {
        final QMooseHuntingSummary MHS = QMooseHuntingSummary.mooseHuntingSummary;

        return 0 < jpqlQueryFactory.query()
                .select(MHS).from(MHS)
                .where(MHS.harvestPermit.eq(harvestPermit))
                .fetchCount();
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean hasBasicClubHuntingSummaries(final HarvestPermit harvestPermit) {
        final QBasicClubHuntingSummary BCH = QBasicClubHuntingSummary.basicClubHuntingSummary;
        final QHarvestPermitSpeciesAmount SPA = QHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;

        return 0 < jpqlQueryFactory.query()
                .select(BCH).from(BCH)
                .join(BCH.speciesAmount, SPA)
                .where(SPA.harvestPermit.eq(harvestPermit))
                .fetchCount();
    }

}
