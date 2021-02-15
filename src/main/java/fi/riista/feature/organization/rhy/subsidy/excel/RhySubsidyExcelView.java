package fi.riista.feature.organization.rhy.subsidy.excel;

import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.rhy.subsidy.AllSubsidyAllocationInfoDTO;
import fi.riista.feature.organization.rhy.subsidy.RhySubsidyStage5DTO;
import fi.riista.feature.organization.rhy.subsidy.StatisticsBasedSubsidyShareDTO;
import fi.riista.feature.organization.rhy.subsidy.SubsidyAllocatedToCriterionDTO;
import fi.riista.feature.organization.rhy.subsidy.SubsidyCalculationStage5DTO;
import fi.riista.feature.organization.rhy.subsidy.SubsidyComparisonToLastYearDTO;
import fi.riista.feature.organization.rhy.subsidy.SubsidyProportionDTO;
import fi.riista.feature.organization.rhy.subsidy.SubsidyRoundingDTO;
import fi.riista.feature.organization.rhy.subsidy.compensation.SubsidyAllocationCompensationBasis;
import fi.riista.feature.organization.rhy.subsidy.compensation.SubsidyAllocationCompensationResultDTO;
import fi.riista.feature.organization.rhy.subsidy.compensation.SubsidyCompensationOutputDTO;
import fi.riista.feature.organization.rhy.subsidy.excel.RhySubsidyExcelModel.AggregatedSubsidyAllocation;
import fi.riista.feature.organization.rhy.subsidy.excel.RhySubsidyExcelModel.RkaSubsidyAllocation;
import fi.riista.feature.organization.rhy.subsidy.excel.RhySubsidyExcelModel.TotalSubsidyAllocation;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.ExcelHelper;
import fi.riista.util.LocalisedString;
import fi.riista.util.MediaTypeExtras;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.iban4j.Iban;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static fi.riista.config.Constants.ZERO_MONETARY_AMOUNT;
import static fi.riista.util.DateUtil.now;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class RhySubsidyExcelView extends AbstractXlsxView {

    private final int subsidyYear;

    private final List<SubsidyAllocatedToCriterionDTO> criteriaSpecificAllocations;
    private final List<RkaSubsidyAllocation> allRkaAllocations;
    private final TotalSubsidyAllocation totalAllocation;

    private final SubsidyAllocationCompensationResultDTO compensationResult;

    private final EnumLocaliser i18n;

    private final Map<Long, Iban> rhyIdToIbanMapping;

    public RhySubsidyExcelView(@Nonnull final AllSubsidyAllocationInfoDTO allAllocationInfo,
                               @Nonnull final EnumLocaliser localiser) {

        requireNonNull(allAllocationInfo, "allAllocationInfo is null");

        this.subsidyYear = allAllocationInfo.getSubsidyYear();

        this.criteriaSpecificAllocations = allAllocationInfo.getCriteriaSpecificAllocations();
        this.allRkaAllocations =
                RhySubsidyExcelModel.groupRhyAllocationsByRka(allAllocationInfo.getCalculatedRhyAllocations());
        this.totalAllocation = RhySubsidyExcelModel.aggregate(allRkaAllocations);

        this.compensationResult = allAllocationInfo.getCompensationResult();

        this.i18n = requireNonNull(localiser, "localiser is null");

        this.rhyIdToIbanMapping = allAllocationInfo.getRhyIdToIbanMapping();
    }

    @Override
    protected final void buildExcelDocument(final Map<String, Object> map,
                                            final Workbook workbook,
                                            final HttpServletRequest request,
                                            final HttpServletResponse response) {

        setContentType(MediaTypeExtras.APPLICATION_EXCEL_VALUE);
        response.setCharacterEncoding(Constants.DEFAULT_ENCODING);
        ContentDispositionUtil.addHeader(response, constructFilename());

        populate(workbook);
    }

    private String constructFilename() {
        return format("RHY-valtionapulaskelma-%d-%s.xlsx", subsidyYear, Constants.FILENAME_TS_PATTERN.print(now()));
    }

    private void populate(final Workbook workbook) {
        final ExcelHelper sheetWrapper = new ExcelHelper(workbook).withFreezePane(2, 10);

        addHeaderRows(sheetWrapper);

        allRkaAllocations.forEach(rka -> appendAllocationsOfRka(sheetWrapper, rka));

        // Append summary line which is a sum of all RKA summaries.

        sheetWrapper.appendRow()
                .appendEmptyCell(1)
                .appendTextCell(i18n.getTranslation("totalAll").toUpperCase())
                .appendEmptyCell(1);

        appendAggregateSubsidyAllocation(sheetWrapper, totalAllocation);

        sheetWrapper.autoSizeColumns();
    }

    private void addHeaderRows(final ExcelHelper sheetWrapper) {
        sheetWrapper
                .appendRow()
                .appendTextCellBold(format("%s %d", i18n.getTranslation("financialYear"), subsidyYear - 1))
                .appendRow()
                .appendTextCellBold(localise("RhySubsidyExcel.totalSubsidyAmountForYear", subsidyYear))
                .appendCurrencyCell(totalAllocation.getSummary().getStage4Rounding().getSubsidyAfterRounding());

        sheetWrapper
                .appendRow()
                .appendTextCellBold(i18n.getTranslation("RhySubsidyExcel.totalSubsidyAmountGrantedInBatch1"))
                .appendCurrencyCell(totalAllocation.getSummary().getSubsidyOfBatch1())

                .appendRow()
                .appendTextCellBold(i18n.getTranslation("RhySubsidyExcel.totalSubsidyAmountGrantedInBatch2"))
                .appendCurrencyCell(totalAllocation.getSummary().getSubsidyOfBatch2());

        addSubsidyCriteriaHeaderRows(sheetWrapper);
        addHeaderRowForDecrementCoefficientsOfCompensationRounds(sheetWrapper);

        // Add empty header row.
        sheetWrapper.appendRow();

        addColumnTitleRow(sheetWrapper);
    }

    private void addSubsidyCriteriaHeaderRows(final ExcelHelper sheetWrapper) {
        addHeaderRowForPercentageSharesOfSubsidyCriteria(sheetWrapper);
        addHeaderRowForAllocatedAmountsOfSubsidyCriteria(sheetWrapper);
        addHeaderRowForCalculatedUnitAmountsOfSubsidyCriteria(sheetWrapper);
    }

    private void addHeaderRowForPercentageSharesOfSubsidyCriteria(final ExcelHelper sheetWrapper) {
        sheetWrapper.appendRow()
                .appendTextCellBold(i18n.getTranslation("RhySubsidyExcel.subsidyCriterionPercentageShare"))
                .appendEmptyCell(2);

        criteriaSpecificAllocations.forEach(allocation -> {

            final double percentage = allocation.getCriterion().getPercentageShare().doubleValue();

            sheetWrapper.appendPercentageCell(percentage).appendEmptyCell(1);
        });
    }

    private void addHeaderRowForAllocatedAmountsOfSubsidyCriteria(final ExcelHelper sheetWrapper) {
        sheetWrapper.appendRow()
                .appendTextCellBold(i18n.getTranslation("RhySubsidyExcel.allocatedAmountForSubsidyCriterion"))
                .appendEmptyCell(2);

        criteriaSpecificAllocations.forEach(allocation -> {

            sheetWrapper.appendCurrencyCell(allocation.getTotalAmount()).appendEmptyCell(1);
        });
    }

    private void addHeaderRowForCalculatedUnitAmountsOfSubsidyCriteria(final ExcelHelper sheetWrapper) {
        sheetWrapper.appendRow()
                .appendTextCellBold(i18n.getTranslation("RhySubsidyExcel.calculatedUnitAmountForSubsidyCriterion"))
                .appendEmptyCell(2);

        criteriaSpecificAllocations.forEach(allocation -> {

            sheetWrapper.appendCurrencyCell(allocation.getUnitAmount()).appendEmptyCell(1);
        });
    }

    private void addHeaderRowForDecrementCoefficientsOfCompensationRounds(final ExcelHelper sheetWrapper) {
        sheetWrapper.appendRow().appendEmptyCell(35);

        compensationResult.getCompensationBases().forEach(basis -> {

            final double percentage = basis.getDecrementCoefficient().movePointRight(2).doubleValue();

            sheetWrapper.appendPercentageCell(percentage).appendEmptyCell(2);
        });
    }

    private void addColumnTitleRow(final ExcelHelper sheetWrapper) {
        sheetWrapper.appendRow()
                .appendTextCellBold(i18n.getTranslation("rhyNumber"), HorizontalAlignment.RIGHT)
                .appendTextCellBold(i18n.getTranslation(OrganisationType.RHY))
                .appendTextCellBold(i18n.getTranslation("RhySubsidyExcel.bankAccount"));

        criteriaSpecificAllocations.forEach(allocation -> {

            final String criterionTitle = String.format("%s (%s)",
                    i18n.getTranslation(allocation.getCriterion().getRelatedStatisticItem()),
                    i18n.getTranslation("RhySubsidyExcel.ratio"));
            sheetWrapper.appendTextCellBold(criterionTitle).appendTextCellBold(i18n.getTranslation("subsidy"));

        });

        sheetWrapper
                .appendTextCellBold(localise("RhySubsidyExcel.statisticsBasedSubsidyAmountForYear", subsidyYear))
                .appendTextCellBold(i18n.getTranslation("RhySubsidyExcel.stage2RemainderEuros"));

        final int lastYear = subsidyYear - 1;

        sheetWrapper
                .appendTextCellBold(localise("RhySubsidyExcel.subsidyAmountForYear", subsidyYear))
                .appendTextCellBold(localise("RhySubsidyExcel.subsidyAmountForYear", lastYear))
                .appendTextCellBold(localise("RhySubsidyExcel.subsidyLowerLimitBasedOnYear", lastYear))
                .appendTextCellBold(localise(
                        "RhySubsidyExcel.differenceOfCalculatedSubsidyToLowerLimit", lastYear, subsidyYear))
                .appendTextCellBold(i18n.getTranslation("RhySubsidyExcel.negativeSubsidyDifference"))
                .appendTextCellBold(i18n.getTranslation("RhySubsidyExcel.subsidyAboveLowerLimit"));

        final int numCompensationRounds = compensationResult.getNumberOfRounds();

        if (numCompensationRounds > 0) {
            for (int i = 1; i <= numCompensationRounds; i++) {
                sheetWrapper
                        .appendTextCellBold(localise("RhySubsidyExcel.decrementOnCompensationRound", i))
                        .appendTextCellBold(localise("RhySubsidyExcel.compensationNeedAfterRound", i))
                        .appendTextCellBold(localise("RhySubsidyExcel.sumOfDownscalableSubsidiesAfterCompensationRound", i));
            }

            sheetWrapper
                    .appendTextCellBold(localise("RhySubsidyExcel.unroundedTotalSubsidyAfterCompensation", subsidyYear))
                    .appendTextCellBold(i18n.getTranslation("RhySubsidyExcel.stage4RemainderEuros"));
        }

        sheetWrapper
                .appendTextCellBold(localise("RhySubsidyExcel.totalSubsidyAfterCompensation", subsidyYear))
                .appendTextCellBold(i18n.getTranslation("RhySubsidyExcel.batch1AfterCompensation"))
                .appendTextCellBold(i18n.getTranslation("RhySubsidyExcel.batch2AfterCompensation"));
    }

    private void appendAllocationsOfRka(final ExcelHelper sheetWrapper, final RkaSubsidyAllocation rkaAllocation) {
        final LocalisedString rkaName = rkaAllocation.getRka().getNameLocalisation();

        sheetWrapper.appendRow()
                .appendEmptyCell(1)
                .appendTextCellBold(i18n.getTranslation(rkaName).toUpperCase());

        rkaAllocation.getRhyAllocations().forEach(rhyAllocation -> {

            final OrganisationNameDTO rhy = rhyAllocation.getRhy();
            final Iban iban = rhyIdToIbanMapping.get(rhy.getId());

            sheetWrapper.appendRow()
                    .appendTextCell(rhy.getOfficialCode(), HorizontalAlignment.RIGHT)
                    .appendTextCell(i18n.getTranslation(rhy.getNameLocalisation()))
                    .appendTextCell(iban != null ? iban.toFormattedString() : "");

            appendRhySubsidyAllocation(sheetWrapper, rhyAllocation);
        });

        // Append RKA summary line.

        sheetWrapper.appendRow()
                .appendEmptyCell(1)
                .appendTextCell(i18n.getTranslation("total"))
                .appendEmptyCell(1);

        appendAggregateSubsidyAllocation(sheetWrapper, rkaAllocation);

        sheetWrapper.appendRow();
    }

    private void appendRhySubsidyAllocation(final ExcelHelper sheetWrapper, final RhySubsidyStage5DTO rhyAllocation) {
        final SubsidyCalculationStage5DTO calculation = rhyAllocation.getCalculation();

        appendSubsidyCalcucationData(sheetWrapper, calculation);

        final SubsidyComparisonToLastYearDTO subsidyComparison = calculation.getSubsidyComparisonToLastYear();

        if (subsidyComparison.isCalculatedSubsidyBelowLowerLimit()) {
            sheetWrapper
                    .appendCurrencyCell(subsidyComparison.computeDifferenceOfCalculatedStatisticsToLowerLimit())
                    .appendEmptyCell(1);
        } else {
            sheetWrapper
                    .appendEmptyCell(1)
                    .appendCurrencyCell(subsidyComparison.getSubsidyCalculatedBasedOnStatistics());
        }

        final SubsidyRoundingDTO stage4Rounding = calculation.getStage4Rounding();

        if (compensationResult.getNumberOfRounds() > 0) {
            final String rhyCode = rhyAllocation.getRhy().getOfficialCode();

            final List<SubsidyCompensationOutputDTO> compensationResults =
                    compensationResult.getCompensationOutputs(rhyCode);

            for (final SubsidyCompensationOutputDTO compResult : compensationResults) {
                if (compResult.isDownscaled()) {
                    sheetWrapper.appendCurrencyCell(compResult.getDecrement());

                    if (compResult.isCompensationNeeded()) {
                        sheetWrapper
                                .appendCurrencyCell(compResult.countAmountOfCompensationNeed().negate())
                                .appendEmptyCell(1);
                    } else {
                        sheetWrapper
                                .appendEmptyCell(1)
                                .appendCurrencyCell(compResult.getSubsidyAfterCompensation());
                    }
                } else {
                    sheetWrapper.appendEmptyCell(3);
                }
            }

            sheetWrapper
                    .appendCurrencyCell(stage4Rounding.getSubsidyBeforeRounding())
                    .appendCurrencyCell(getPositiveBigDecimalOrNull(stage4Rounding.getGivenRemainderEuros()));
        }

        sheetWrapper
                .appendCurrencyCell(stage4Rounding.getSubsidyAfterRounding())
                .appendCurrencyCell(calculation.getSubsidyOfBatch1())
                .appendCurrencyCell(calculation.getSubsidyOfBatch2());
    }

    private void appendAggregateSubsidyAllocation(final ExcelHelper sheetWrapper,
                                                  final AggregatedSubsidyAllocation aggregate) {

        final SubsidyCalculationStage5DTO summary = aggregate.getSummary();

        appendSubsidyCalcucationData(sheetWrapper, summary);

        final List<SubsidyAllocationCompensationBasis> bases = compensationResult.getCompensationBases();
        final int numCompensationRounds = bases.size();

        final SubsidyRoundingDTO stage4Rounding = summary.getStage4Rounding();

        if (aggregate.isSummaryOfAllRhys()) {
            final SubsidyComparisonToLastYearDTO subsidyComparison = summary.getSubsidyComparisonToLastYear();

            if (numCompensationRounds > 0) {
                for (final SubsidyAllocationCompensationBasis basis : bases) {
                    sheetWrapper
                            .appendCurrencyCell(basis.getTotalCompensationNeed().negate())
                            .appendCurrencyCell(basis.getSumOfSubsidiesAboveLowerLimit())
                            .appendEmptyCell(1);
                }

                sheetWrapper
                        .appendCurrencyCell(ZERO_MONETARY_AMOUNT)
                        .appendEmptyCell(1)
                        .appendCurrencyCell(stage4Rounding.getSubsidyBeforeRounding())
                        .appendCurrencyCell(new BigDecimal(stage4Rounding.getGivenRemainderEuros()));
            } else {
                sheetWrapper
                        .appendCurrencyCell(ZERO_MONETARY_AMOUNT)
                        .appendCurrencyCell(subsidyComparison.getSubsidyCalculatedBasedOnStatistics());
            }
        } else {
            sheetWrapper.appendEmptyCell(2);

            if (numCompensationRounds > 0) {
                sheetWrapper
                        .appendEmptyCell(3 * numCompensationRounds)
                        .appendCurrencyCell(stage4Rounding.getSubsidyBeforeRounding())
                        .appendCurrencyCell(new BigDecimal(stage4Rounding.getGivenRemainderEuros()));
            }
        }

        sheetWrapper
                .appendCurrencyCell(stage4Rounding.getSubsidyAfterRounding())
                .appendCurrencyCell(summary.getSubsidyOfBatch1())
                .appendCurrencyCell(summary.getSubsidyOfBatch2());
    }

    private static void appendSubsidyCalcucationData(final ExcelHelper sheetWrapper,
                                                     final SubsidyCalculationStage5DTO calculation) {

        final StatisticsBasedSubsidyShareDTO shares = calculation.getCalculatedShares();

        appendSubsidyProportion(sheetWrapper, shares.getRhyMembers());
        appendSubsidyProportion(sheetWrapper, shares.getHunterExamTrainingEvents());
        appendSubsidyProportion(sheetWrapper, shares.getOtherTrainingEvents());
        appendSubsidyProportion(sheetWrapper, shares.getStudentAndYouthTrainingEvents());
        appendSubsidyProportion(sheetWrapper, shares.getHuntingControlEvents());
        appendSubsidyProportion(sheetWrapper, shares.getSumOfLukeCalculations());
        appendSubsidyProportion(sheetWrapper, shares.getLukeCarnivoreContactPersons());
        appendSubsidyProportion(sheetWrapper, shares.getMooselikeTaxationPlanningEvents());
        appendSubsidyProportion(sheetWrapper, shares.getWolfTerritoryWorkgroups());
        appendSubsidyProportion(sheetWrapper, shares.getSrvaMooselikeEvents());
        appendSubsidyProportion(sheetWrapper, shares.getSoldMhLicenses());

        sheetWrapper
                .appendCurrencyCell(shares.countSumOfAllShares())
                .appendCurrencyCell(getPositiveBigDecimalOrNull(calculation.getRemainderEurosGivenInStage2()));

        final SubsidyComparisonToLastYearDTO subsidyComparison = calculation.getSubsidyComparisonToLastYear();

        sheetWrapper
                .appendCurrencyCell(subsidyComparison.getSubsidyCalculatedBasedOnStatistics())
                .appendCurrencyCell(subsidyComparison.getSubsidyGrantedLastYear())
                .appendCurrencyCell(subsidyComparison.getSubsidyLowerLimitBasedOnLastYear())
                .appendCurrencyCell(subsidyComparison.computeDifferenceOfCalculatedStatisticsToLowerLimit());
    }

    private static void appendSubsidyProportion(final ExcelHelper sheetWrapper, final SubsidyProportionDTO dto) {
        sheetWrapper
                .appendNumberCell(dto.getQuantity())
                .appendCurrencyCell(dto.getCalculatedAmount());
    }

    private String localise(final String localisationKey, final int... localisationParameters) {
        return i18n.getTranslation(
                localisationKey,
                IntStream.of(localisationParameters).mapToObj(String::valueOf).toArray(String[]::new));
    }

    private static BigDecimal getPositiveBigDecimalOrNull(final int amount) {
        return amount > 0 ? new BigDecimal(amount) : null;
    }
}
