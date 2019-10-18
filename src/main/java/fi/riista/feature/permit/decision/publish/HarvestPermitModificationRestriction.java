package fi.riista.feature.permit.decision.publish;

import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.QHarvest;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountRepository;
import fi.riista.feature.harvestpermit.QHarvestPermit;
import fi.riista.feature.harvestpermit.QHarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.group.QHuntingClubGroup;
import fi.riista.feature.huntingclub.hunting.day.QGroupHuntingDay;
import fi.riista.feature.huntingclub.permit.endofhunting.basicsummary.QBasicClubHuntingSummary;
import fi.riista.feature.huntingclub.permit.endofhunting.moosesummary.QMooseHuntingSummary;
import fi.riista.feature.permit.invoice.harvest.QPermitHarvestInvoice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

@Service
public class HarvestPermitModificationRestriction {

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @Resource
    private HarvestPermitSpeciesAmountRepository harvestPermitSpeciesAmountRepository;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean canModifyHarvestPermit(final HarvestPermit harvestPermit) {
        return hasSingleAmountForHarvestSpecies(harvestPermit) &&
                !hasReportedData(harvestPermit) &&
                !hasAmendmentPermits(harvestPermit);
    }

    private boolean hasReportedData(final HarvestPermit harvestPermit) {
        return hasReportedHarvests(harvestPermit) ||
                hasGroupHuntingDays(harvestPermit) ||
                hasPermitHolderFinishedMooseLikeHunting(harvestPermit) ||
                hasMooseHuntingSummaries(harvestPermit) ||
                hasBasicClubHuntingSummaries(harvestPermit) ||
                hasPermitHarvestInvoice(harvestPermit);
    }

    private boolean hasSingleAmountForHarvestSpecies(final HarvestPermit harvestPermit) {
        return hasSingleAmountForHarvestSpecies(harvestPermitSpeciesAmountRepository.findByHarvestPermit(harvestPermit));
    }

    static boolean hasSingleAmountForHarvestSpecies(final List<HarvestPermitSpeciesAmount> speciesAmounts) {
        return hasSingleAmount(speciesAmounts, HarvestPermitSpeciesAmount::getGameSpecies);
    }

    private static <T> boolean hasSingleAmount(final Collection<T> speciesAmounts,
                                               final Function<T, GameSpecies> propertyGetter) {
        return speciesAmounts.stream()
                // Count by species
                .collect(groupingBy(propertyGetter, counting()))
                // Any species has more than 1 amount?
                .values().stream().noneMatch(a -> a > 1);
    }

    private boolean hasAmendmentPermits(final HarvestPermit harvestPermit) {
        return 0 < harvestPermitRepository.count(QHarvestPermit.harvestPermit.originalPermit.eq(harvestPermit));
    }

    private boolean hasReportedHarvests(final HarvestPermit harvestPermit) {
        final QHarvest HARVEST = QHarvest.harvest;

        return 0 < jpqlQueryFactory
                .selectFrom(HARVEST)
                .where(HARVEST.harvestPermit.eq(harvestPermit))
                .fetchCount();
    }

    private boolean hasGroupHuntingDays(final HarvestPermit harvestPermit) {
        final QGroupHuntingDay DAY = QGroupHuntingDay.groupHuntingDay;
        final QHuntingClubGroup GROUP = QHuntingClubGroup.huntingClubGroup;

        return 0 < jpqlQueryFactory
                .selectFrom(DAY)
                .join(DAY.group, GROUP)
                .where(GROUP.harvestPermit.eq(harvestPermit))
                .fetchCount();
    }

    private boolean hasPermitHolderFinishedMooseLikeHunting(final HarvestPermit harvestPermit) {
        final QHarvestPermitSpeciesAmount SPA = QHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;

        return 0 < jpqlQueryFactory
                .selectFrom(SPA)
                .where(SPA.harvestPermit.eq(harvestPermit),
                        SPA.mooselikeHuntingFinished.isTrue())
                .fetchCount();
    }

    private boolean hasMooseHuntingSummaries(final HarvestPermit harvestPermit) {
        final QMooseHuntingSummary MHS = QMooseHuntingSummary.mooseHuntingSummary;

        return 0 < jpqlQueryFactory
                .selectFrom(MHS)
                .where(MHS.harvestPermit.eq(harvestPermit))
                .fetchCount();
    }

    private boolean hasBasicClubHuntingSummaries(final HarvestPermit harvestPermit) {
        final QBasicClubHuntingSummary BCH = QBasicClubHuntingSummary.basicClubHuntingSummary;
        final QHarvestPermitSpeciesAmount SPA = QHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;

        return 0 < jpqlQueryFactory
                .selectFrom(BCH)
                .join(BCH.speciesAmount, SPA)
                .where(SPA.harvestPermit.eq(harvestPermit))
                .fetchCount();
    }

    private boolean hasPermitHarvestInvoice(final HarvestPermit harvestPermit) {
        final QPermitHarvestInvoice HARVEST_INVOICE = QPermitHarvestInvoice.permitHarvestInvoice;
        final QHarvestPermitSpeciesAmount SPA = QHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;

        return 0 < jpqlQueryFactory
                .selectFrom(HARVEST_INVOICE)
                .join(HARVEST_INVOICE.speciesAmount, SPA)
                .where(SPA.harvestPermit.eq(harvestPermit))
                .fetchCount();
    }
}
