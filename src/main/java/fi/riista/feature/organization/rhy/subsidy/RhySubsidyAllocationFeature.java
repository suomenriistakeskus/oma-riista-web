package fi.riista.feature.organization.rhy.subsidy;

import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.organization.rhy.QRiistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysNameService;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsState;
import fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticsExportDTO;
import fi.riista.feature.organization.rhy.subsidy.excel.RhySubsidyExcelView;
import fi.riista.feature.organization.rhy.subsidy2019.AllSubsidyAllocation2019InfoDTO;
import fi.riista.feature.organization.rhy.subsidy2019.SubsidyAllocation2019ResultDTO;
import fi.riista.feature.organization.rhy.subsidy2019.SubsidyAllocation2019Stage3Calculation;
import fi.riista.feature.organization.rhy.subsidy2019.SubsidyAllocation2019Stage3DTO;
import fi.riista.feature.organization.rhy.subsidy2019.SubsidyAllocation2019Stage4Calculation;
import fi.riista.feature.organization.rhy.subsidy2019.excel.RhySubsidy2019ExcelView;
import io.vavr.Tuple;
import org.iban4j.Iban;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsState.APPROVED;
import static fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsState.UNDER_INSPECTION;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationConstants.SubsidyAllocationRule.SUM_OF_TWO_ANNUAL_STATISTICS;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationStage1Calculation.calculateStatisticsBasedSubsidyAllocation;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationStage2Calculation.calculateRemainder;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationToCriteriaCalculation.calculateAllocationOfRhySubsidyToEachCriterion;
import static fi.riista.util.Collect.tuplesToMap;

@Service
public class RhySubsidyAllocationFeature {

    @Resource
    private RhySubsidyStatisticsExportService exportService;

    @Resource
    private RiistanhoitoyhdistysNameService rhyNameService;

    @Resource
    private JPQLQueryFactory queryFactory;

    @Resource
    private MessageSource messageSource;

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    @Transactional(readOnly = true)
    public RhySubsidy2019ExcelView exportSubsidyAllocationsForYear2019(@Nonnull final SubsidyAllocationInputDTO dto,
                                                                       @Nonnull final Locale locale) {
        final int subsidyYear = dto.getSubsidyYear();
        checkArgument(subsidyYear == 2019, "Expected year 2019");

        final BigDecimal totalAllocatableSubsidyAmount = dto.getTotalSubsidyAmount();

        final RhySubsidyMergeResolver mergeResolver = createRhySubsidyMergeResolver(subsidyYear);

        // TODO Do filtering only by APPROVED state.
        final List<AnnualStatisticsExportDTO> mergedRhyStatistics = mergeResolver.mergeStatistics(
                exportService.exportWeightedAnnualStatistics(
                        subsidyYear, subsidyYear - 1, EnumSet.of(UNDER_INSPECTION, APPROVED)));

        final PreviouslyGrantedSubsidiesDTO mergedPreviouslyGrantedSubsidies =
                mergeResolver.mergePreviouslyGrantedSubsidies(fetchPreviouslyGrantedSubsidies(subsidyYear));

        final List<SubsidyAllocatedToCriterionDTO> criteriaSpecificAllocations =
                calculateAllocationOfRhySubsidyToEachCriterion(
                        mergedRhyStatistics, totalAllocatableSubsidyAmount, subsidyYear);

        final List<RhySubsidyStage1DTO> statisticsBasedAllocations =
                calculateStatisticsBasedSubsidyAllocation(subsidyYear, mergedRhyStatistics, criteriaSpecificAllocations);

        final List<RhySubsidyStage2DTO> rhyAllocationsWithSubsidyPotFullyConsumed =
                calculateRemainder(totalAllocatableSubsidyAmount, statisticsBasedAllocations);

        final List<SubsidyAllocation2019Stage3DTO> rhyAllocationsWithPreviouslyGrantedSubsidies =
                SubsidyAllocation2019Stage3Calculation.addSubsidyBatchInfo(
                        rhyAllocationsWithSubsidyPotFullyConsumed, mergedPreviouslyGrantedSubsidies);

        final SubsidyAllocation2019ResultDTO allocationResult = SubsidyAllocation2019Stage4Calculation
                .calculateCompensation(rhyAllocationsWithPreviouslyGrantedSubsidies);

        final AllSubsidyAllocation2019InfoDTO allAllocationInfo = new AllSubsidyAllocation2019InfoDTO(
                subsidyYear,
                mergedPreviouslyGrantedSubsidies.getTotalSubsidyAmountGrantedInFirstBatchOfCurrentYear(),
                totalAllocatableSubsidyAmount,
                criteriaSpecificAllocations,
                allocationResult.getRhyAllocations(),
                allocationResult.getCompensationResult());

        return new RhySubsidy2019ExcelView(allAllocationInfo, getLocaliser(locale));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    @Transactional(readOnly = true)
    public RhySubsidyExcelView exportSubsidyAllocations(@Nonnull final SubsidyAllocationInputDTO dto,
                                                        @Nonnull final Locale locale) {
        final AllSubsidyAllocationInfoDTO allAllocationInfo = calculateSubsidyAllocations(dto);
        return new RhySubsidyExcelView(allAllocationInfo, getLocaliser(locale));
    }

    // For testing
    /*package*/ AllSubsidyAllocationInfoDTO calculateSubsidyAllocations(@Nonnull final SubsidyAllocationInputDTO dto) {

        final int subsidyYear = dto.getSubsidyYear();
        checkArgument(subsidyYear >= 2020, "Expected year 2020 or later");

        final BigDecimal totalAllocatableSubsidyAmount = dto.getTotalSubsidyAmount();

        final SubsidyAllocationConstants.SubsidyAllocationRule allocationRule =
                SubsidyAllocationConstants.getAllocationRule(subsidyYear);

        // TODO Do filtering only by APPROVED state.
        final EnumSet<RhyAnnualStatisticsState> states = EnumSet.of(UNDER_INSPECTION, APPROVED);

        final RhySubsidyMergeResolver mergeResolver = createRhySubsidyMergeResolver(subsidyYear);
        final List<AnnualStatisticsExportDTO> mergedRhyStatistics;

        if (allocationRule == SUM_OF_TWO_ANNUAL_STATISTICS) {
            // Export and merge first year statistics
            final List<AnnualStatisticsExportDTO> previousYearStatistics =
                    exportService.exportWeightedAnnualStatistics(subsidyYear, subsidyYear - 2, states);

            final RhySubsidyMergeResolver previousYearMergeResolver = createRhySubsidyMergeResolver(subsidyYear - 1);
            final List<AnnualStatisticsExportDTO> previousYearMergeResult =
                    previousYearMergeResolver.mergeStatistics(previousYearStatistics);

            // Export second year statistics
            final List<AnnualStatisticsExportDTO> statistics = exportService
                    .exportWeightedAnnualStatistics(subsidyYear, subsidyYear - 1, states);

            // Combine with merged first year statistics
            final List<AnnualStatisticsExportDTO> compoundMergedStatistics =
                    previousYearMergeResolver.combine(previousYearMergeResult, statistics);

            // Merge with second year rhy merges
            mergedRhyStatistics = mergeResolver.mergeStatistics(compoundMergedStatistics);

        } else {
            final List<AnnualStatisticsExportDTO> statistics =
                    exportService.exportWeightedAnnualStatistics(subsidyYear, subsidyYear - 1, states);

            mergedRhyStatistics = mergeResolver.mergeStatistics(statistics);
        }

        final Map<Long, Iban> rhyIdToIbanMapping = new HashMap<>();
        mergedRhyStatistics.forEach(stats ->
                rhyIdToIbanMapping.put(stats.getOrganisation().getId(), stats.getBasicInfo().getIban()));

        final PreviouslyGrantedSubsidiesDTO mergedPreviouslyGrantedSubsidies =
                mergeResolver.mergePreviouslyGrantedSubsidies(fetchPreviouslyGrantedSubsidies(subsidyYear));

        final List<SubsidyAllocatedToCriterionDTO> criteriaSpecificAllocations =
                calculateAllocationOfRhySubsidyToEachCriterion(
                        mergedRhyStatistics, totalAllocatableSubsidyAmount, subsidyYear);

        final List<RhySubsidyStage1DTO> statisticsBasedAllocations =
                calculateStatisticsBasedSubsidyAllocation(subsidyYear, mergedRhyStatistics, criteriaSpecificAllocations);

        final List<RhySubsidyStage2DTO> rhyAllocationsWithSubsidyPotFullyConsumed =
                calculateRemainder(totalAllocatableSubsidyAmount, statisticsBasedAllocations);

        final List<RhySubsidyStage3DTO> rhyAllocationsWithPreviouslyGrantedSubsidies =
                SubsidyAllocationStage3Calculation.addSubsidyComparisonToLastYear(
                        rhyAllocationsWithSubsidyPotFullyConsumed, mergedPreviouslyGrantedSubsidies);

        final SubsidyAllocationStage4ResultDTO stage4Result = SubsidyAllocationStage4Calculation
                .calculateCompensation(rhyAllocationsWithPreviouslyGrantedSubsidies);

        final List<RhySubsidyStage5DTO> resultRhyAllocations =
                SubsidyAllocationStage5Calculation.divideIntoTwoBatches(stage4Result.getRhyAllocations());

        return new AllSubsidyAllocationInfoDTO(
                subsidyYear,
                criteriaSpecificAllocations,
                resultRhyAllocations,
                stage4Result.getCompensationResult(),
                rhyIdToIbanMapping);
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
