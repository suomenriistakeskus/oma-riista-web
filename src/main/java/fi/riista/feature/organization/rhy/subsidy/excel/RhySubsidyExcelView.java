package fi.riista.feature.organization.rhy.subsidy.excel;

import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.rhy.subsidy.AllSubsidyAllocationInfoDTO;
import fi.riista.feature.organization.rhy.subsidy.StatisticsBasedSubsidyShareDTO;
import fi.riista.feature.organization.rhy.subsidy.SubsidyAllocatedToCriterionDTO;
import fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationStage4DTO;
import fi.riista.feature.organization.rhy.subsidy.SubsidyBatchInfoDTO;
import fi.riista.feature.organization.rhy.subsidy.SubsidyProportionDTO;
import fi.riista.feature.organization.rhy.subsidy.compensation.SubsidyAllocationCompensationBasis;
import fi.riista.feature.organization.rhy.subsidy.compensation.SubsidyAllocationCompensationResultDTO;
import fi.riista.feature.organization.rhy.subsidy.compensation.SubsidyCompensationOutputDTO;
import fi.riista.feature.organization.rhy.subsidy.excel.RhySubsidyExcelModel.AggregatedSubsidyAllocation;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.ExcelHelper;
import fi.riista.util.LocalisedString;
import fi.riista.util.MediaTypeExtras;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
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

    private final BigDecimal totalSubsidyAmountForBatch1;
    private final BigDecimal totalSubsidyAmountForBatch2;

    private final boolean isFirstSubsidyBatchAlreadyGranted;

    private final List<SubsidyAllocatedToCriterionDTO> criteriaSpecificAllocations;
    private final List<AggregatedSubsidyAllocation> allRkaAllocations;

    private final SubsidyAllocationCompensationResultDTO compensationResult;

    private final EnumLocaliser i18n;

    public RhySubsidyExcelView(@Nonnull final AllSubsidyAllocationInfoDTO allAllocationInfo,
                               @Nonnull final EnumLocaliser localiser) {

        requireNonNull(allAllocationInfo, "allAllocationInfo is null");

        this.subsidyYear = allAllocationInfo.getSubsidyYear();

        this.totalSubsidyAmountForBatch1 = allAllocationInfo.getTotalSubsidyAmountForBatch1();
        this.totalSubsidyAmountForBatch2 = allAllocationInfo.getTotalSubsidyAmountForBatch2();

        this.isFirstSubsidyBatchAlreadyGranted = allAllocationInfo.isSubsidyBatch1AlreadyGranted();

        this.criteriaSpecificAllocations = allAllocationInfo.getCriteriaSpecificAllocations();
        this.allRkaAllocations =
                RhySubsidyExcelModel.groupRhyAllocationsByRka(allAllocationInfo.getCalculatedRhyAllocations());

        this.compensationResult = allAllocationInfo.getCompensationResult();

        this.i18n = requireNonNull(localiser, "localiser is null");
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
        final int numHeaderRows = isFirstSubsidyBatchAlreadyGranted ? 10 : 8;
        final ExcelHelper sheetWrapper = new ExcelHelper(workbook).withFreezePane(2, numHeaderRows);

        addHeaderRows(sheetWrapper);

        allRkaAllocations.forEach(rka -> appendAllocationsOfRka(sheetWrapper, rka));

        // Append summary line which is a sum of all RKA summaries.

        sheetWrapper.appendRow()
                .appendEmptyCell(1)
                .appendTextCell(i18n.getTranslation("totalAll").toUpperCase());

        appendAggregateSubsidyAllocation(sheetWrapper, AggregatedSubsidyAllocation.aggregate(allRkaAllocations));

        sheetWrapper.autoSizeColumns();
    }

    private void addHeaderRows(final ExcelHelper sheetWrapper) {
        sheetWrapper
                .appendRow()
                .appendTextCellBold(format("%s %d", i18n.getTranslation("financialYear"), subsidyYear - 1))
                .appendRow()
                .appendTextCellBold(localise("RhySubsidyExcel.totalSubsidyAmountForYear", subsidyYear))
                .appendCurrencyCell(totalSubsidyAmountForBatch1.add(totalSubsidyAmountForBatch2));

        if (isFirstSubsidyBatchAlreadyGranted) {
            sheetWrapper
                    .appendRow()
                    .appendTextCellBold(i18n.getTranslation("RhySubsidyExcel.totalSubsidyAmountGrantedInBatch1"))
                    .appendCurrencyCell(totalSubsidyAmountForBatch1)

                    .appendRow()
                    .appendTextCellBold(i18n.getTranslation("RhySubsidyExcel.totalSubsidyAmountGrantedInBatch2"))
                    .appendCurrencyCell(totalSubsidyAmountForBatch2);
        }

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
                .appendEmptyCell(1);

        criteriaSpecificAllocations.forEach(allocation -> {

            final double percentage = allocation.getCriterion().getPercentageShare().doubleValue();

            sheetWrapper.appendPercentageCell(percentage).appendEmptyCell(1);
        });
    }

    private void addHeaderRowForAllocatedAmountsOfSubsidyCriteria(final ExcelHelper sheetWrapper) {
        sheetWrapper.appendRow()
                .appendTextCellBold(i18n.getTranslation("RhySubsidyExcel.allocatedAmountForSubsidyCriterion"))
                .appendEmptyCell(1);

        criteriaSpecificAllocations.forEach(allocation -> {

            sheetWrapper.appendCurrencyCell(allocation.getTotalAmount()).appendEmptyCell(1);
        });
    }

    private void addHeaderRowForCalculatedUnitAmountsOfSubsidyCriteria(final ExcelHelper sheetWrapper) {
        sheetWrapper.appendRow()
                .appendTextCellBold(i18n.getTranslation("RhySubsidyExcel.calculatedUnitAmountForSubsidyCriterion"))
                .appendEmptyCell(1);

        criteriaSpecificAllocations.forEach(allocation -> {

            sheetWrapper.appendCurrencyCell(allocation.getUnitAmount()).appendEmptyCell(1);
        });
    }

    private void addHeaderRowForDecrementCoefficientsOfCompensationRounds(final ExcelHelper sheetWrapper) {
        sheetWrapper.appendRow().appendEmptyCell(34);

        compensationResult.getCompensationBases().forEach(basis -> {

            final double percentage = basis.getDecrementCoefficient().movePointRight(2).doubleValue();

            sheetWrapper.appendPercentageCell(percentage).appendEmptyCell(2);
        });
    }

    private void addColumnTitleRow(final ExcelHelper sheetWrapper) {
        sheetWrapper.appendRow()
                .appendTextCell(i18n.getTranslation("rhyNumber"), HorizontalAlignment.RIGHT)
                .appendTextCell(i18n.getTranslation(OrganisationType.RHY));

        criteriaSpecificAllocations.forEach(allocation -> {

            final String criterionTitle = i18n.getTranslation(allocation.getCriterion().getRelatedStatisticItem());
            sheetWrapper.appendTextCellBold(criterionTitle).appendTextCellBold(i18n.getTranslation("subsidy"));

        });

        sheetWrapper
                .appendTextCellBold(i18n.getTranslation("RhySubsidyExcel.subsidyAmountOfBatch2BeforeRounding"))
                .appendTextCellBold(i18n.getTranslation("RhySubsidyExcel.stage2RemainderEuros"));

        if (isFirstSubsidyBatchAlreadyGranted) {
            sheetWrapper
                    .appendTextCellBold(i18n.getTranslation("RhySubsidyExcel.subsidyAmountOfBatch2"))
                    .appendTextCellBold(i18n.getTranslation("RhySubsidyExcel.subsidyAmountOfBatch1"));
        }

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
                .appendTextCellBold(i18n.getTranslation("RhySubsidyExcel.batch2AfterCompensation"));
    }

    private void appendAllocationsOfRka(final ExcelHelper sheetWrapper,
                                        final AggregatedSubsidyAllocation rkaAllocation) {

        final LocalisedString rkaName = rkaAllocation.summary.getOrganisation().getNameLocalisation();

        sheetWrapper.appendRow()
                .appendEmptyCell(1)
                .appendTextCellBold(i18n.getTranslation(rkaName).toUpperCase());

        rkaAllocation.allocations.forEach(rhyAllocation -> {

            sheetWrapper.appendRow()
                    .appendTextCell(rhyAllocation.getOrganisation().getOfficialCode(), HorizontalAlignment.RIGHT)
                    .appendTextCell(i18n.getTranslation(rhyAllocation.getOrganisation().getNameLocalisation()));

            appendRhySubsidyAllocation(sheetWrapper, rhyAllocation);
        });

        // Append RKA summary line.

        sheetWrapper.appendRow()
                .appendEmptyCell(1)
                .appendTextCell(i18n.getTranslation("total"));

        appendAggregateSubsidyAllocation(sheetWrapper, rkaAllocation);

        sheetWrapper.appendRow();
    }

    private void appendRhySubsidyAllocation(final ExcelHelper sheetWrapper,
                                            final SubsidyAllocationStage4DTO allocation) {

        appendCommonSubsidyAllocationData(sheetWrapper, allocation);

        final SubsidyBatchInfoDTO subsidyBatchInfo = allocation.getSubsidyBatchInfo();

        if (subsidyBatchInfo.isCalculatedSubsidyBelowLowerLimit()) {
            sheetWrapper
                    .appendCurrencyCell(subsidyBatchInfo.calculateDifferenceOfTotalSubsidyBeforeCompensationToLowerLimit())
                    .appendEmptyCell(1);
        } else {
            sheetWrapper
                    .appendEmptyCell(1)
                    .appendCurrencyCell(subsidyBatchInfo.calculateTotalSubsidyForCurrentYearBeforeCompensation());
        }

        if (compensationResult.getNumberOfRounds() > 0) {
            final String rhyCode = allocation.getOrganisation().getOfficialCode();

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
                                .appendCurrencyCell(compResult.getTotalSubsidyAfterCompensation());
                    }
                } else {
                    sheetWrapper.appendEmptyCell(3);
                }
            }

            sheetWrapper
                    .appendCurrencyCell(allocation.calculateTotalSubsidyForCurrentYearBeforeFinalRounding())
                    .appendCurrencyCell(getPositiveBigDecimalOrNull(allocation.getRemainderEurosGivenInStage4()));
        }

        sheetWrapper
                .appendCurrencyCell(allocation.calculateTotalSubsidyForCurrentYearAfterFinalRounding())
                .appendCurrencyCell(allocation.getCalculatedSubsidyAfterFinalRounding());
    }

    private void appendAggregateSubsidyAllocation(final ExcelHelper sheetWrapper,
                                                  final AggregatedSubsidyAllocation aggregate) {

        final SubsidyAllocationStage4DTO summary = aggregate.summary;

        appendCommonSubsidyAllocationData(sheetWrapper, summary);

        final List<SubsidyAllocationCompensationBasis> bases = compensationResult.getCompensationBases();
        final int numCompensationRounds = bases.size();

        if (aggregate.isSummaryOfAllRhys) {
            final SubsidyBatchInfoDTO subsidyBatchInfo = summary.getSubsidyBatchInfo();

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
                        .appendCurrencyCell(summary.calculateTotalSubsidyForCurrentYearBeforeFinalRounding())
                        .appendCurrencyCell(new BigDecimal(summary.getRemainderEurosGivenInStage4()));
            } else {
                sheetWrapper
                        .appendCurrencyCell(ZERO_MONETARY_AMOUNT)
                        .appendCurrencyCell(subsidyBatchInfo.getSubsidyCalculatedForSecondBatchBeforeCompensation());
            }
        } else {
            sheetWrapper.appendEmptyCell(2);

            if (numCompensationRounds > 0) {
                sheetWrapper
                        .appendEmptyCell(3 * numCompensationRounds)
                        .appendCurrencyCell(summary.calculateTotalSubsidyForCurrentYearBeforeFinalRounding())
                        .appendCurrencyCell(new BigDecimal(summary.getRemainderEurosGivenInStage4()));
            }
        }

        sheetWrapper
                .appendCurrencyCell(summary.calculateTotalSubsidyForCurrentYearAfterFinalRounding())
                .appendCurrencyCell(summary.getCalculatedSubsidyAfterFinalRounding());
    }

    private void appendCommonSubsidyAllocationData(final ExcelHelper sheetWrapper,
                                                   final SubsidyAllocationStage4DTO allocation) {

        final StatisticsBasedSubsidyShareDTO shares = allocation.getCalculatedShares();

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
                .appendCurrencyCell(getPositiveBigDecimalOrNull(allocation.getRemainderEurosGivenInStage2()));

        final SubsidyBatchInfoDTO subsidyBatchInfo = allocation.getSubsidyBatchInfo();

        if (isFirstSubsidyBatchAlreadyGranted) {
            sheetWrapper
                    .appendCurrencyCell(subsidyBatchInfo.getSubsidyCalculatedForSecondBatchBeforeCompensation())
                    .appendCurrencyCell(subsidyBatchInfo.getSubsidyGrantedInFirstBatch());
        }

        sheetWrapper
                .appendCurrencyCell(subsidyBatchInfo.calculateTotalSubsidyForCurrentYearBeforeCompensation())
                .appendCurrencyCell(subsidyBatchInfo.getSubsidyGrantedLastYear())
                .appendCurrencyCell(subsidyBatchInfo.getSubsidyLowerLimitBasedOnLastYear())
                .appendCurrencyCell(subsidyBatchInfo.calculateDifferenceOfTotalSubsidyBeforeCompensationToLowerLimit());
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
