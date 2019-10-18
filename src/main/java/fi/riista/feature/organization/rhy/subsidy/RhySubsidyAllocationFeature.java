package fi.riista.feature.organization.rhy.subsidy;

import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.organization.rhy.QRiistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysNameService;
import fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticsExportDTO;
import fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticsExportService;
import fi.riista.feature.organization.rhy.subsidy.excel.RhySubsidyExcelView;
import io.vavr.Tuple;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import static fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsState.APPROVED;
import static fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsState.UNDER_INSPECTION;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationStage1Calculation.calculateStatisticsBasedSubsidyAllocation;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationStage2Calculation.calculateAndAllocateRemainder;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationStage3Calculation.addSubsidyBatchInfo;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationStage4Calculation.calculateCompensation;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationToCriteriaCalculation.calculateAllocationOfRhySubsidyToEachCriterion;
import static fi.riista.util.Collect.tuplesToMap;

@Service
public class RhySubsidyAllocationFeature {

    @Resource
    private AnnualStatisticsExportService statisticsExportService;

    @Resource
    private RiistanhoitoyhdistysNameService rhyNameService;

    @Resource
    private JPQLQueryFactory queryFactory;

    @Resource
    private MessageSource messageSource;

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    @Transactional(readOnly = true)
    public RhySubsidyExcelView exportSubsidyAllocations(@Nonnull final SubsidyAllocationInputDTO dto,
                                                        @Nonnull final Locale locale) {

        final BigDecimal totalAllocatableSubsidyAmount = dto.getTotalSubsidyAmount();

        final int subsidyYear = dto.getSubsidyYear();
        final int statisticsYear = subsidyYear - 1;

        final RhySubsidyMergeResolver mergeResolver = createRhySubsidyMergeResolver(subsidyYear);

        // TODO Do filtering only by APPROVED state.
        final List<AnnualStatisticsExportDTO> mergedRhyStatistics = mergeResolver.mergeStatistics(
                statisticsExportService.exportAnnualStatistics(statisticsYear, EnumSet.of(UNDER_INSPECTION, APPROVED)));

        final PreviouslyGrantedSubsidiesDTO mergedPreviouslyGrantedSubsidies =
                mergeResolver.mergePreviouslyGrantedSubsidies(fetchPreviouslyGrantedSubsidies(subsidyYear));

        final List<SubsidyAllocatedToCriterionDTO> criteriaSpecificAllocations =
                calculateAllocationOfRhySubsidyToEachCriterion(
                        mergedRhyStatistics, totalAllocatableSubsidyAmount, subsidyYear);

        final List<BasicSubsidyAllocationDTO> statisticsBasedAllocations =
                calculateStatisticsBasedSubsidyAllocation(mergedRhyStatistics, criteriaSpecificAllocations);

        final List<BasicSubsidyAllocationDTO> rhyAllocationsWithSubsidyPotFullyConsumed =
                calculateAndAllocateRemainder(totalAllocatableSubsidyAmount, statisticsBasedAllocations);

        final List<SubsidyAllocationStage3DTO> rhyAllocationsWithPreviouslyGrantedSubsidies =
                addSubsidyBatchInfo(rhyAllocationsWithSubsidyPotFullyConsumed, mergedPreviouslyGrantedSubsidies);

        final SubsidyAllocationStage4ResultDTO stage4Result =
                calculateCompensation(rhyAllocationsWithPreviouslyGrantedSubsidies);

        final AllSubsidyAllocationInfoDTO allAllocationInfo = new AllSubsidyAllocationInfoDTO(
                subsidyYear,
                mergedPreviouslyGrantedSubsidies.getTotalSubsidyAmountGrantedInFirstBatchOfCurrentYear(),
                totalAllocatableSubsidyAmount,
                criteriaSpecificAllocations,
                stage4Result.getRhyAllocations(),
                stage4Result.getCompensationResult());

        return new RhySubsidyExcelView(allAllocationInfo, getLocaliser(locale));
    }

    private RhySubsidyMergeResolver createRhySubsidyMergeResolver(final int subsidyYear) {
        return new RhySubsidyMergeResolver(subsidyYear, rhyNameService.getRhyNames(subsidyYear));
    }

    private PreviouslyGrantedSubsidiesDTO fetchPreviouslyGrantedSubsidies(final int subsidyYear) {
        return new PreviouslyGrantedSubsidiesDTO(
                groupTotalSubsidyGrantedForYearByRhyCode(subsidyYear - 1),
                groupSubsidyOfBatch1GrantedForYearByRhyCode(subsidyYear));
    }

    private Map<String, BigDecimal> groupTotalSubsidyGrantedForYearByRhyCode(final int year) {
        return groupSubsidiesGrantedForYearByRhyCode(year, s -> s.amountOfBatch1.add(s.amountOfBatch2));
    }

    private Map<String, BigDecimal> groupSubsidyOfBatch1GrantedForYearByRhyCode(final int year) {
        return groupSubsidiesGrantedForYearByRhyCode(year, s -> s.amountOfBatch1);
    }

    private Map<String, BigDecimal> groupSubsidiesGrantedForYearByRhyCode(final int year,
                                                                          final Function<QRhySubsidy, NumberExpression<BigDecimal>> amountExtractor) {
        final QRhySubsidy SUBSIDY = QRhySubsidy.rhySubsidy;
        final QRiistanhoitoyhdistys RHY = QRiistanhoitoyhdistys.riistanhoitoyhdistys;

        final NumberExpression<BigDecimal> amount = amountExtractor.apply(SUBSIDY);

        return queryFactory
                .select(RHY.officialCode, amount)
                .from(SUBSIDY)
                .innerJoin(SUBSIDY.rhy, RHY)
                .where(SUBSIDY.year.eq(year))
                .fetch()
                .stream()
                .map(t -> {
                    final String rhyCode = t.get(RHY.officialCode);

                    // Set scale correct for monetary amount.
                    final BigDecimal moneyAmount = SubsidyCalculation.round(t.get(amount), 2);

                    return Tuple.of(rhyCode, moneyAmount);
                })
                .collect(tuplesToMap());
    }

    private EnumLocaliser getLocaliser(final Locale locale) {
        return new EnumLocaliser(messageSource, locale);
    }
}
