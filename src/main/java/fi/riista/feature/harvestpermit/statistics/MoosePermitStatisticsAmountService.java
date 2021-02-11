package fi.riista.feature.harvestpermit.statistics;

import com.google.common.collect.Maps;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.gamediary.QGameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.QHarvestPermit;
import fi.riista.feature.harvestpermit.QHarvestPermitSpeciesAmount;
import fi.riista.feature.permit.application.QHarvestPermitApplication;
import fi.riista.feature.permit.application.QHarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.decision.QPermitDecision;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Set;

import static com.querydsl.core.group.GroupBy.groupBy;

@Service
public class MoosePermitStatisticsAmountService {

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Transactional(readOnly = true, noRollbackFor = RuntimeException.class)
    public Map<Long, MoosePermitStatisticsAmountDTO> findPermitAmounts(final Set<Long> permitIds,
                                                                       final int speciesCode,
                                                                       final int huntingYear) {

        final Map<Long, HarvestPermitSpeciesAmount> amounts = findOriginalAmounts(permitIds, speciesCode, huntingYear);
        final Map<Long, Float> amendmentAmounts = countSumOfAmendmentAmounts(permitIds, speciesCode, huntingYear);
        final Map<Long, Float> applicationAmounts = countSumOfApplicationAmounts(permitIds, speciesCode);

        return Maps.transformEntries(applicationAmounts, (permitId, applicationAmount) -> {
            final float amendmendPermitAmount = amendmentAmounts.getOrDefault(permitId, 0f);
            final HarvestPermitSpeciesAmount originalSpeciesAmount = amounts.getOrDefault(permitId, null);

            return MoosePermitStatisticsAmountDTO.create(originalSpeciesAmount, amendmendPermitAmount, applicationAmount);
        });
    }

    private Map<Long, HarvestPermitSpeciesAmount> findOriginalAmounts(final Set<Long> permitIds,
                                                                      final int speciesCode,
                                                                      final int huntingYear) {
        final QHarvestPermit PERMIT = QHarvestPermit.harvestPermit;
        final QHarvestPermitSpeciesAmount SPECIES_AMOUNT = QHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;
        final QGameSpecies SPECIES = QGameSpecies.gameSpecies;

        final NumberPath<Long> permitId = PERMIT.id;

        return jpqlQueryFactory.select(permitId, SPECIES_AMOUNT)
                .from(SPECIES_AMOUNT)
                .join(SPECIES_AMOUNT.harvestPermit, PERMIT)
                .join(SPECIES_AMOUNT.gameSpecies, SPECIES)
                .where(permitId.in(permitIds),
                        SPECIES.officialCode.eq(speciesCode),
                        PERMIT.permitYear.eq(huntingYear))
                .transform(groupBy(permitId).as(SPECIES_AMOUNT));
    }

    private Map<Long, Float> countSumOfAmendmentAmounts(final Set<Long> originalPermitIds,
                                                        final int speciesCode,
                                                        final int huntingYear) {

        final QHarvestPermitSpeciesAmount SPECIES_AMOUNT = QHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;
        final QGameSpecies SPECIES = QGameSpecies.gameSpecies;
        final QHarvestPermit AMENDMENT_PERMIT = new QHarvestPermit("amendmentPermit");

        final Expression<Long> originalPermitId = AMENDMENT_PERMIT.originalPermit.id;
        final Expression<Float> sumOfAmendmentPermitAmounts = SPECIES_AMOUNT.specimenAmount.sum();

        return jpqlQueryFactory.select(originalPermitId, sumOfAmendmentPermitAmounts)
                .from(SPECIES_AMOUNT)
                .join(SPECIES_AMOUNT.gameSpecies, SPECIES)
                .join(SPECIES_AMOUNT.harvestPermit, AMENDMENT_PERMIT)
                .where(AMENDMENT_PERMIT.originalPermit.id.in(originalPermitIds),
                        SPECIES.officialCode.eq(speciesCode),
                        SPECIES_AMOUNT.validOnHuntingYear(huntingYear))
                .groupBy(originalPermitId)
                .transform(groupBy(originalPermitId).as(sumOfAmendmentPermitAmounts));
    }

    private Map<Long, Float> countSumOfApplicationAmounts(final Set<Long> permitIds, final int speciesCode) {
        final QHarvestPermit PERMIT = QHarvestPermit.harvestPermit;
        final QPermitDecision DECISION = QPermitDecision.permitDecision;
        final QHarvestPermitApplication APPLICATION = QHarvestPermitApplication.harvestPermitApplication;
        final QHarvestPermitApplicationSpeciesAmount APPLICATION_AMOUNT = QHarvestPermitApplicationSpeciesAmount.harvestPermitApplicationSpeciesAmount;
        final QGameSpecies SPECIES = QGameSpecies.gameSpecies;

        final NumberPath<Long> permitId = PERMIT.id;
        final Expression<Float> sumOfAmounts = APPLICATION_AMOUNT.specimenAmount.sum();

        return jpqlQueryFactory.select(permitId, sumOfAmounts)
                .from(PERMIT)
                .join(PERMIT.permitDecision, DECISION)
                .join(DECISION.application, APPLICATION)
                .join(APPLICATION.speciesAmounts, APPLICATION_AMOUNT)
                .join(APPLICATION_AMOUNT.gameSpecies, SPECIES)
                .where(PERMIT.id.in(permitIds), SPECIES.officialCode.eq(speciesCode))
                .groupBy(permitId)
                .transform(groupBy(permitId).as(sumOfAmounts));
    }
}
