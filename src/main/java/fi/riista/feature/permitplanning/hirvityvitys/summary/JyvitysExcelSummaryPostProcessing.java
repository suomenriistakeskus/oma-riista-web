package fi.riista.feature.permitplanning.hirvityvitys.summary;

import com.google.common.collect.ImmutableMap;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.permitplanning.hirvityvitys.dto.JyvitysExcelVerotuslohkoDTO;
import org.apache.poi.ss.usermodel.BorderExtent;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.PropertyTemplate;

import java.util.List;
import java.util.Map;

import static fi.riista.feature.permitplanning.hirvityvitys.JyvitysExcelCellColours.SUGGESTION_ADULTS_COLOUR;
import static fi.riista.feature.permitplanning.hirvityvitys.JyvitysExcelCellColours.SUGGESTION_CALFS_COLOUR;
import static fi.riista.feature.permitplanning.hirvityvitys.JyvitysExcelCellColours.SUGGESTION_TOTAL_COLOUR;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_SUGGESTION_TOTAL;

/**
 * Class for inserting extra columns after other data has beed filled out. The data in question is jyvitys data
 * for extra permits suggested by the calculation.
 */
public class JyvitysExcelSummaryPostProcessing {

    private static CellAddress FIRST_POST_PROCESSING_COLUMN = new CellAddress("R5");
    private static CellAddress FIRST_LOHKO_NAME_CELL = new CellAddress("R6");
    // First cell containing application spesific post processing data
    private static CellAddress FIRST_APPLICATION_DATA_CELL = new CellAddress("R8");
    private final CellStyle percentageStyle;
    private final CellStyle decisionTotalCellStyle;
    private final CellStyle decisionAdultsCellStyle;
    private final CellStyle decisionCalfsCellStyle;
    private final CellStyle boldCellStyle;

    private enum PostProcessingHeader {
        APPLICANT_PERMITS_BY_LOHKO,
        EXCESS_PERMIT_JYVITYS,
        DECISION_TITLE,
        DECISION_TOTAL,
        DECISION_ADULT,
        DECISION_CALF
    }

    private final Workbook workbook;
    private final Sheet summarySheet;
    private final EnumLocaliser i18n;
    private final List<JyvitysExcelVerotuslohkoDTO> lohkoList;
    private final int applicationCount;
    private final Map<PostProcessingHeader, CellAddress> cellAddresses;

    public static void apply(final Workbook workbook,
                             final EnumLocaliser i18n,
                             final List<JyvitysExcelVerotuslohkoDTO> lohkoList,
                             final int applicationCount) {
        final JyvitysExcelSummaryPostProcessing instance = new JyvitysExcelSummaryPostProcessing(workbook, i18n, lohkoList, applicationCount);
        instance.applyHeaders();
        instance.applyData();
    }

    private JyvitysExcelSummaryPostProcessing(final Workbook workbook,
                                              final EnumLocaliser i18n,
                                              final List<JyvitysExcelVerotuslohkoDTO> lohkoList,
                                              final int applicationCount) {
        this.workbook = workbook;
        this.summarySheet = workbook.getSheetAt(0);
        this.i18n = i18n;
        this.lohkoList = lohkoList;
        this.applicationCount = applicationCount;
        this.cellAddresses = calculateHeaderCells(lohkoList.size());
        this.percentageStyle = workbook.createCellStyle();
        this.percentageStyle.setDataFormat(workbook.createDataFormat().getFormat("#0 %"));
        this.boldCellStyle = workbook.createCellStyle();
        final Font bold = workbook.createFont();
        bold.setBold(true);
        this.boldCellStyle.setFont(bold);
        this.decisionTotalCellStyle = workbook.createCellStyle();
        this.decisionTotalCellStyle.setFillForegroundColor(SUGGESTION_TOTAL_COLOUR);
        this.decisionTotalCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        this.decisionTotalCellStyle.setWrapText(true);
        this.decisionTotalCellStyle.setFont(bold);

        this.decisionAdultsCellStyle = workbook.createCellStyle();
        this.decisionAdultsCellStyle.setFillForegroundColor(SUGGESTION_ADULTS_COLOUR);
        this.decisionAdultsCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        this.decisionAdultsCellStyle.setWrapText(true);
        this.decisionAdultsCellStyle.setFont(bold);

        this.decisionCalfsCellStyle = workbook.createCellStyle();
        this.decisionCalfsCellStyle.setFillForegroundColor(SUGGESTION_CALFS_COLOUR);
        this.decisionCalfsCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        this.decisionCalfsCellStyle.setWrapText(true);
        this.decisionCalfsCellStyle.setFont(bold);
    }

    private void applyData() {
        // Loop through applications, rows are shifted relative to the uppper left hand corner of application spesific fields
        for (int rowShift = 0; rowShift < applicationCount; ++rowShift) {
            final Row row = summarySheet.getRow(FIRST_APPLICATION_DATA_CELL.getRow() + rowShift);
            applyLohkoDataForApplication(row);
            addApplicationDecisionColumns(row);
        }
    }

    private void applyLohkoDataForApplication(final Row row) {
        final int lohkos = lohkoList.size();
        final int formulaRowNum = row.getRowNum() + 1;

        // Loop through verotuslohkos shifting column in single application row
        for (int lohko = 0; lohko < lohkos; ++lohko) {
            // Address in verotuslohko sheet of suggestion for this application
            final String suggestionForLohkoTotalCellAddress = VEROTUSLOHKO_SUGGESTION_TOTAL.getColumn() + formulaRowNum;
            final String appliedAmountCellAddress = JyvitysExcelSummaryStaticField.SUMMARY_APPLICATION_AMOUNT.getColumn() + formulaRowNum;
            final String suggestionForApplicationTotalCellAddress = JyvitysExcelSummaryStaticField.SUMMARY_SUGGESTION_TOTAL.getColumn() + formulaRowNum;
            final String lohkoSheetName = workbook.getSheetAt(lohko + 1).getSheetName();

            // Add cell for percentage of suggested permits in this lohko
            final Cell applicationPermitsInLohkoPercentageCell = row.createCell(FIRST_APPLICATION_DATA_CELL.getColumn() + lohko);
            applicationPermitsInLohkoPercentageCell.setCellFormula(
                    getPermitsInLohkoFormula(suggestionForLohkoTotalCellAddress, suggestionForApplicationTotalCellAddress, lohkoSheetName));
            applicationPermitsInLohkoPercentageCell.setCellStyle(percentageStyle);

            // Add suggested number of excess (when suggested amount is higher than applied amount) permits for this lohko
            final Cell excessSuggestionForLohkoCell = row.createCell(FIRST_APPLICATION_DATA_CELL.getColumn() + lohko + lohkos);
            final String percentageCellAddress = applicationPermitsInLohkoPercentageCell.getAddress().formatAsString();
            excessSuggestionForLohkoCell.setCellFormula(getExcessPermitSuggestionFormula(suggestionForLohkoTotalCellAddress, appliedAmountCellAddress, percentageCellAddress));

        }
    }

    private void addApplicationDecisionColumns(Row row) {
        final Cell total = row.createCell(row.getLastCellNum());
        total.setCellStyle(decisionTotalCellStyle);
        total.setCellValue(0.0);
        final Cell adults = row.createCell(row.getLastCellNum());
        adults.setCellStyle(decisionAdultsCellStyle);
        adults.setCellValue(0.0);
        final Cell calfs = row.createCell(row.getLastCellNum());
        calfs.setCellStyle(decisionCalfsCellStyle);
        calfs.setCellValue(0.0);
    }

    private static String getPermitsInLohkoFormula(final String suggestionForLohkoTotalCellAddress,
                                                   final String suggestionForApplicationTotalCellAddress,
                                                   final String lohkoSheetName) {
        return String.format("'%s'!%s / %s",
                lohkoSheetName,
                suggestionForLohkoTotalCellAddress,
                suggestionForApplicationTotalCellAddress);
    }

    private static String getExcessPermitSuggestionFormula(final String suggestionTotalCellAddress,
                                                           final String appliedAmountCellAddress,
                                                           final String percentageCellAddress) {
        return String.format("ROUND(IF((%s-%s)*%s>0,(%s-%s)*%s, 0),0)",
                suggestionTotalCellAddress, appliedAmountCellAddress, percentageCellAddress,
                suggestionTotalCellAddress, appliedAmountCellAddress, percentageCellAddress);
    }

    private void applyHeaders() {
        cellAddresses.entrySet().forEach(entry -> {
            final CellAddress address = entry.getValue();
            final Cell cell = summarySheet.getRow(address.getRow()).createCell(address.getColumn());
            cell.setCellValue(getTranslation(entry.getKey()));
            cell.setCellStyle(getStyleForHeader(entry.getKey()));
        });

        final Row lohkoNamesRow = summarySheet.getRow(FIRST_LOHKO_NAME_CELL.getRow());
        // Lohkos are added twice
        appendAllLohkoNames(lohkoNamesRow);
        appendAllLohkoNames(lohkoNamesRow);
        addMergedRegions();
    }

    private CellStyle getStyleForHeader(PostProcessingHeader key) {
        switch (key) {
            case DECISION_TOTAL:
                return decisionTotalCellStyle;
            case DECISION_ADULT:
                return decisionAdultsCellStyle;
            case DECISION_CALF:
                return decisionCalfsCellStyle;
            default:
                return null;
        }
    }

    private void appendAllLohkoNames(final Row lohkoNamesRow) {
        final PropertyTemplate propertyTemplate = new PropertyTemplate();
        lohkoList.forEach(lohko -> {
            final Cell cell = lohkoNamesRow.createCell(lohkoNamesRow.getLastCellNum());
            cell.setCellValue(lohko.getName());
            final String startRegion = cell.getAddress().formatAsString();
            final String endRegion = shift(cell.getAddress(), 1, 0).formatAsString();
            final CellRangeAddress range = CellRangeAddress.valueOf(String.format("%s:%s", startRegion, endRegion));
            applyMergeRegionAndBorders(propertyTemplate, range);
        });
        propertyTemplate.applyBorders(summarySheet);
    }

    private String getTranslation(final PostProcessingHeader excessPermitJyvitys) {
        return i18n.getTranslation(EnumLocaliser.resourceKey(excessPermitJyvitys));
    }

    private void addMergedRegions() {
        final PropertyTemplate propertyTemplate = new PropertyTemplate();
        if (lohkoList.size() > 1) {
            applyMergeAndBorderForHeader(propertyTemplate, PostProcessingHeader.APPLICANT_PERMITS_BY_LOHKO, 1, lohkoList.size());
            applyMergeAndBorderForHeader(propertyTemplate, PostProcessingHeader.EXCESS_PERMIT_JYVITYS, 1, lohkoList.size());
        }
        applyMergeAndBorderForHeader(propertyTemplate, PostProcessingHeader.DECISION_TOTAL, 3, 1);
        applyMergeAndBorderForHeader(propertyTemplate, PostProcessingHeader.DECISION_ADULT, 3, 1);
        applyMergeAndBorderForHeader(propertyTemplate, PostProcessingHeader.DECISION_CALF, 3, 1);

        propertyTemplate.applyBorders(summarySheet);
    }

    private void applyMergeAndBorderForHeader(final PropertyTemplate propertyTemplate, final PostProcessingHeader header,
                                              final int spanRows, final int spanColumns) {
        final CellAddress start = this.cellAddresses.get(header);
        final CellAddress end = shift(start, spanRows - 1, spanColumns - 1);
        final CellRangeAddress range = CellRangeAddress.valueOf(String.format("%s:%s", start, end));
        applyMergeRegionAndBorders(propertyTemplate, range);
    }

    private void applyMergeRegionAndBorders(PropertyTemplate propertyTemplate, CellRangeAddress range) {
        summarySheet.addMergedRegion(range);
        propertyTemplate.drawBorders(range, BorderStyle.MEDIUM, BorderExtent.OUTSIDE);
    }

    // Calculate the location for header cells depending on the number of verotuslohkos in rhy
    private static Map<PostProcessingHeader, CellAddress> calculateHeaderCells(final int numberOfLohkos) {
        return ImmutableMap.<PostProcessingHeader, CellAddress>builder()
                .put(PostProcessingHeader.APPLICANT_PERMITS_BY_LOHKO, FIRST_POST_PROCESSING_COLUMN)
                .put(PostProcessingHeader.EXCESS_PERMIT_JYVITYS, shift(FIRST_POST_PROCESSING_COLUMN, 0, numberOfLohkos))
                .put(PostProcessingHeader.DECISION_TITLE, shift(FIRST_POST_PROCESSING_COLUMN, -1, 2 * numberOfLohkos))
                .put(PostProcessingHeader.DECISION_TOTAL, shift(FIRST_POST_PROCESSING_COLUMN, 0, 2 * numberOfLohkos))
                .put(PostProcessingHeader.DECISION_ADULT, shift(FIRST_POST_PROCESSING_COLUMN, 0, 2 * numberOfLohkos + 1))
                .put(PostProcessingHeader.DECISION_CALF, shift(FIRST_POST_PROCESSING_COLUMN, 0, 2 * numberOfLohkos + 2))
                .build();
    }

    private static CellAddress shift(final CellAddress address, int rows, int columns) {
        return new CellAddress(address.getRow() + rows, address.getColumn() + columns);
    }

}
