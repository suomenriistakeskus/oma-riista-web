package fi.riista.feature.permit.invoice.harvest.excel;

import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.huntingclub.permit.statistics.HarvestCountDTO;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.ExcelHelper;
import fi.riista.util.LocalisedString;
import fi.riista.util.MediaTypeExtras;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

import static fi.riista.feature.permit.invoice.harvest.excel.MooselikeHarvestPaymentSummaryExcelTitle.ADULTS;
import static fi.riista.feature.permit.invoice.harvest.excel.MooselikeHarvestPaymentSummaryExcelTitle.AMENDMENT;
import static fi.riista.feature.permit.invoice.harvest.excel.MooselikeHarvestPaymentSummaryExcelTitle.AREA;
import static fi.riista.feature.permit.invoice.harvest.excel.MooselikeHarvestPaymentSummaryExcelTitle.CALCULATED_PAYMENT_AMOUNT;
import static fi.riista.feature.permit.invoice.harvest.excel.MooselikeHarvestPaymentSummaryExcelTitle.DEFICIENT_AMOUNT;
import static fi.riista.feature.permit.invoice.harvest.excel.MooselikeHarvestPaymentSummaryExcelTitle.DIFFERENCE;
import static fi.riista.feature.permit.invoice.harvest.excel.MooselikeHarvestPaymentSummaryExcelTitle.EMPTY_PLACEHOLDER;
import static fi.riista.feature.permit.invoice.harvest.excel.MooselikeHarvestPaymentSummaryExcelTitle.FEMALE;
import static fi.riista.feature.permit.invoice.harvest.excel.MooselikeHarvestPaymentSummaryExcelTitle.HARVEST;
import static fi.riista.feature.permit.invoice.harvest.excel.MooselikeHarvestPaymentSummaryExcelTitle.HARVEST_ADULT;
import static fi.riista.feature.permit.invoice.harvest.excel.MooselikeHarvestPaymentSummaryExcelTitle.HARVEST_YOUNG;
import static fi.riista.feature.permit.invoice.harvest.excel.MooselikeHarvestPaymentSummaryExcelTitle.MALE;
import static fi.riista.feature.permit.invoice.harvest.excel.MooselikeHarvestPaymentSummaryExcelTitle.RECEIVED_AMOUNT;
import static fi.riista.feature.permit.invoice.harvest.excel.MooselikeHarvestPaymentSummaryExcelTitle.SHEET_NAME_1;
import static fi.riista.feature.permit.invoice.harvest.excel.MooselikeHarvestPaymentSummaryExcelTitle.SHEET_NAME_2;
import static fi.riista.feature.permit.invoice.harvest.excel.MooselikeHarvestPaymentSummaryExcelTitle.SHEET_NAME_3;
import static fi.riista.feature.permit.invoice.harvest.excel.MooselikeHarvestPaymentSummaryExcelTitle.SURPLUS_AMOUNT;
import static fi.riista.feature.permit.invoice.harvest.excel.MooselikeHarvestPaymentSummaryExcelTitle.TOTAL;
import static fi.riista.feature.permit.invoice.harvest.excel.MooselikeHarvestPaymentSummaryExcelTitle.TOTAL_MONETARY_AMOUNT;
import static fi.riista.feature.permit.invoice.harvest.excel.MooselikeHarvestPaymentSummaryExcelTitle.YOUNG;
import static fi.riista.util.DateUtil.now;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

public class MooselikeHarvestPaymentSummaryExcelView extends AbstractXlsxView {

    private static final List<HeaderColumn> SHEET_1_HEADER_ROW_1 = asList(
            column(AREA),
            column(HARVEST, 2),
            column(RECEIVED_AMOUNT),
            column(DIFFERENCE),
            column(SURPLUS_AMOUNT),
            column(DEFICIENT_AMOUNT));

    private static final List<HeaderColumn> SHEET_1_HEADER_ROW_2 = asList(
            column(EMPTY_PLACEHOLDER),
            column(ADULTS),
            column(YOUNG),
            column(EMPTY_PLACEHOLDER),
            column(EMPTY_PLACEHOLDER),
            column(EMPTY_PLACEHOLDER),
            column(EMPTY_PLACEHOLDER));

    private static final List<HeaderColumn> SHEET_2_HEADER_ROW_1 = asList(
            column(AREA),
            column(HARVEST, 3),
            column(AMENDMENT, 2),
            column(CALCULATED_PAYMENT_AMOUNT, 3));

    private static final List<HeaderColumn> SHEET_2_HEADER_ROW_2 = asList(
            column(EMPTY_PLACEHOLDER),
            column(ADULTS),
            column(YOUNG),
            column(TOTAL),
            column(ADULTS),
            column(YOUNG),
            column(ADULTS),
            column(YOUNG),
            column(TOTAL_MONETARY_AMOUNT));

    private static final List<HeaderColumn> SHEET_3_HEADER_ROW_1 = asList(
            column(AREA),
            column(HARVEST_ADULT, 3),
            column(HARVEST_YOUNG, 3),
            column(HARVEST));

    private static final List<HeaderColumn> SHEET_3_HEADER_ROW_2 = asList(
            column(EMPTY_PLACEHOLDER),
            column(MALE),
            column(FEMALE),
            column(TOTAL),
            column(MALE),
            column(FEMALE),
            column(TOTAL),
            column(TOTAL));

    private final int huntingYear;
    private final LocalisedString speciesName;

    private final List<MooselikeHarvestPaymentSummaryDTO> rkaList;
    private final MooselikeHarvestPaymentSummaryDTO totalSummary;

    private final EnumLocaliser i18n;

    public MooselikeHarvestPaymentSummaryExcelView(final int huntingYear,
                                                   @Nonnull final LocalisedString speciesName,
                                                   @Nonnull final List<MooselikeHarvestPaymentSummaryDTO> rkaList,
                                                   @Nonnull final EnumLocaliser localiser) {

        this.huntingYear = huntingYear;
        this.speciesName = requireNonNull(speciesName);

        this.rkaList = requireNonNull(rkaList);
        this.totalSummary = MooselikeHarvestPaymentSummaryDTO.createSummary(rkaList);

        this.i18n = requireNonNull(localiser);
    }

    private String createFilename() {
        return format(
                "%d_saaliit_ja_suoritukset_kohdennettuina-%s.xlsx",
                huntingYear, Constants.FILENAME_TS_PATTERN.print(now()));
    }

    @Override
    protected void buildExcelDocument(final Map<String, Object> map,
                                      final Workbook workbook,
                                      final HttpServletRequest request,
                                      final HttpServletResponse response) {

        setContentType(MediaTypeExtras.APPLICATION_EXCEL_VALUE);
        response.setCharacterEncoding(Constants.DEFAULT_ENCODING);
        ContentDispositionUtil.addHeader(response, createFilename());

        createFirstSheet(workbook);
        createSecondSheet(workbook);
        createThirdSheet(workbook);
    }

    private void createFirstSheet(final Workbook workbook) {
        final ExcelHelper sheetWrapper =
                createSheet(workbook, SHEET_NAME_1, SHEET_1_HEADER_ROW_1, SHEET_1_HEADER_ROW_2);

        rkaList.forEach(rka -> {
            appendDataRowForFirstSheet(sheetWrapper, rka, getCombinedOfficialCodeAndName(rka.getOrganisation()));
        });

        // Append total summary row.
        appendDataRowForFirstSheet(sheetWrapper, totalSummary, i18n.getTranslation(TOTAL));

        sheetWrapper.autoSizeColumns();
    }

    private void createSecondSheet(final Workbook workbook) {
        final ExcelHelper sheetWrapper =
                createSheet(workbook, SHEET_NAME_2, SHEET_2_HEADER_ROW_1, SHEET_2_HEADER_ROW_2);

        rkaList.forEach(rka -> {
            appendDataRowForSecondSheet(sheetWrapper, rka, getCombinedOfficialCodeAndName(rka.getOrganisation()));
        });

        // Append total summary row.
        appendDataRowForSecondSheet(sheetWrapper, totalSummary, i18n.getTranslation(TOTAL));

        sheetWrapper.autoSizeColumns();
    }

    private void createThirdSheet(final Workbook workbook) {
        final ExcelHelper sheetWrapper =
                createSheet(workbook, SHEET_NAME_3, SHEET_3_HEADER_ROW_1, SHEET_3_HEADER_ROW_2);

        rkaList.forEach(rka -> {
            appendDataRowForThirdSheet(sheetWrapper, rka, getCombinedOfficialCodeAndName(rka.getOrganisation()));
        });

        // Append total summary row.
        appendDataRowForThirdSheet(sheetWrapper, totalSummary, i18n.getTranslation(TOTAL));

        sheetWrapper.autoSizeColumns();
    }

    private ExcelHelper createSheet(final Workbook workbook,
                                    final MooselikeHarvestPaymentSummaryExcelTitle sheetTitle,
                                    final List<HeaderColumn> firstRowHeaderColumns,
                                    final List<HeaderColumn> secondRowHeaderColumns) {

        final String sheetName = i18n.getTranslation(sheetTitle);

        // Freeze header rows.
        final ExcelHelper sheet = new ExcelHelper(workbook, sheetName).withFreezedRows(4);

        addCommonHeaderRows(sheet, sheetName);
        addHeaderRow(sheet, firstRowHeaderColumns);
        addHeaderRow(sheet, secondRowHeaderColumns);

        return sheet;
    }

    private void addCommonHeaderRows(final ExcelHelper sheetWrapper, final String sheetName) {
        final String title = format("%s, %s %d", sheetName, i18n.getTranslation(speciesName), huntingYear);

        sheetWrapper.appendRow().appendTextCellBold(title).spanCurrentColumn(5).appendRow();
    }

    private void addHeaderRow(final ExcelHelper sheetWrapper, final List<HeaderColumn> headerColumns) {
        sheetWrapper.appendRow();

        headerColumns.forEach(headerCol -> {

            final String title = i18n.getTranslation(headerCol.title);

            sheetWrapper.appendTextCellBold(title);
            sheetWrapper.spanCurrentColumn(headerCol.colspan);
        });
    }

    private static void appendDataRowForFirstSheet(final ExcelHelper sheetWrapper,
                                                   final MooselikeHarvestPaymentSummaryDTO dto,
                                                   final String name) {

        sheetWrapper.appendRow()
                .appendTextCell(name)
                .appendNumberCell(dto.getHarvestCounts().getNumberOfAdults())
                .appendNumberCell(dto.getHarvestCounts().getNumberOfYoung())
                .appendCurrencyCell(dto.getReceivedAmount())
                .appendCurrencyCell(dto.getReceicedAmountSubtractedByAmountBasedOnActualHarvestCount())
                .appendCurrencyCell(dto.getSurplusAmount())
                .appendCurrencyCell(dto.getDeficientAmount());
    }

    private static void appendDataRowForSecondSheet(final ExcelHelper sheetWrapper,
                                                    final MooselikeHarvestPaymentSummaryDTO dto,
                                                    final String name) {

        final HarvestCountDTO harvestCounts = dto.getHarvestCounts();

        final int numAdults = harvestCounts.getNumberOfAdults();
        final int numYoung = harvestCounts.getNumberOfYoung();

        final int numNonEdibleAdults = harvestCounts.getNumberOfNonEdibleAdults();
        final int numNonEdibleYoung = harvestCounts.getNumberOfNonEdibleYoungs();

        sheetWrapper.appendRow()
                .appendTextCell(name)
                .appendNumberCell(numAdults)
                .appendNumberCell(numYoung)
                .appendNumberCell(harvestCounts.getTotal())
                .appendNumberCell(numNonEdibleAdults)
                .appendNumberCell(numNonEdibleYoung)
                .appendNumberCell(numAdults - numNonEdibleAdults)
                .appendNumberCell(numYoung - numNonEdibleYoung)
                .appendCurrencyCell(dto.getChargeableAmountBasedOnHarvestCount());
    }

    private static void appendDataRowForThirdSheet(final ExcelHelper sheetWrapper,
                                                   final MooselikeHarvestPaymentSummaryDTO dto,
                                                   final String name) {

        final HarvestCountDTO harvestCounts = dto.getHarvestCounts();

        sheetWrapper.appendRow()
                .appendTextCell(name)
                .appendNumberCell(harvestCounts.getNumberOfAdultMales())
                .appendNumberCell(harvestCounts.getNumberOfAdultFemales())
                .appendNumberCell(harvestCounts.getNumberOfAdults())
                .appendNumberCell(harvestCounts.getNumberOfYoungMales())
                .appendNumberCell(harvestCounts.getNumberOfYoungFemales())
                .appendNumberCell(harvestCounts.getNumberOfYoung())
                .appendNumberCell(harvestCounts.getTotal());
    }

    private String getCombinedOfficialCodeAndName(final OrganisationNameDTO dto) {
        final String localisedName = i18n.getTranslation(dto.getNameLocalisation());
        return format("%s %s", dto.getOfficialCode(), localisedName);
    }

    private static HeaderColumn column(final MooselikeHarvestPaymentSummaryExcelTitle title) {
        return column(title, 1);
    }

    private static HeaderColumn column(final MooselikeHarvestPaymentSummaryExcelTitle title,
                                       final int colspan) {

        return new HeaderColumn(title, colspan);
    }

    private static final class HeaderColumn {

        public final MooselikeHarvestPaymentSummaryExcelTitle title;
        public final int colspan;

        HeaderColumn(final MooselikeHarvestPaymentSummaryExcelTitle title, final int colspan) {
            this.title = requireNonNull(title);
            this.colspan = colspan;
        }
    }
}
