package fi.riista.feature.permitplanning.hirvityvitys.summary;

import com.google.common.collect.ImmutableList;
import fi.riista.feature.permitplanning.hirvityvitys.JyvitysExcelCellAddressManipulation;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.BorderExtent;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.PatternFormatting;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.PropertyTemplate;

import java.util.List;

import static fi.riista.feature.permitplanning.hirvityvitys.JyvitysExcelCellColours.SUGGESTION_ADULTS_COLOUR;
import static fi.riista.feature.permitplanning.hirvityvitys.JyvitysExcelCellColours.SUGGESTION_CALFS_COLOUR;
import static fi.riista.feature.permitplanning.hirvityvitys.JyvitysExcelCellColours.SUGGESTION_TOTAL_COLOUR;
import static fi.riista.feature.permitplanning.hirvityvitys.JyvitysExcelCellColours.USER_MODIFIABLE_FIELD_COLOUR;
import static fi.riista.feature.permitplanning.hirvityvitys.summary.JyvitysExcelSummaryStaticField.SUMMARY_APPLICANT;
import static fi.riista.feature.permitplanning.hirvityvitys.summary.JyvitysExcelSummaryStaticField.SUMMARY_APPLICATION_AMOUNT;
import static fi.riista.feature.permitplanning.hirvityvitys.summary.JyvitysExcelSummaryStaticField.SUMMARY_AREA_IN_OTHER_RHY;
import static fi.riista.feature.permitplanning.hirvityvitys.summary.JyvitysExcelSummaryStaticField.SUMMARY_PRIVATE_LAND;
import static fi.riista.feature.permitplanning.hirvityvitys.summary.JyvitysExcelSummaryStaticField.SUMMARY_SHOOTERS_ONLY_CLUB;
import static fi.riista.feature.permitplanning.hirvityvitys.summary.JyvitysExcelSummaryStaticField.SUMMARY_SHOOTERS_OTHER_CLUB_PASSIVE;
import static fi.riista.feature.permitplanning.hirvityvitys.summary.JyvitysExcelSummaryStaticField.SUMMARY_SHOOTERS_TOTAL;
import static fi.riista.feature.permitplanning.hirvityvitys.summary.JyvitysExcelSummaryStaticField.SUMMARY_SHOOTERS_TOTAL_PER_STATE_LAND;
import static fi.riista.feature.permitplanning.hirvityvitys.summary.JyvitysExcelSummaryStaticField.SUMMARY_STATE_LAND;
import static fi.riista.feature.permitplanning.hirvityvitys.summary.JyvitysExcelSummaryStaticField.SUMMARY_SUGGESTION_ADULT;
import static fi.riista.feature.permitplanning.hirvityvitys.summary.JyvitysExcelSummaryStaticField.SUMMARY_SUGGESTION_CALF;
import static fi.riista.feature.permitplanning.hirvityvitys.summary.JyvitysExcelSummaryStaticField.SUMMARY_SUGGESTION_TOTAL;
import static fi.riista.feature.permitplanning.hirvityvitys.summary.JyvitysExcelSummaryStaticField.SUMMARY_TITLE_CELL;
import static fi.riista.feature.permitplanning.hirvityvitys.summary.JyvitysExcelSummaryStaticField.SUMMARY_TOTAL_LAND;
import static fi.riista.feature.permitplanning.hirvityvitys.summary.JyvitysExcelSummaryStaticField.SUMMARY_TOTAL_QUOTA;
import static fi.riista.feature.permitplanning.hirvityvitys.summary.JyvitysExcelSummaryTemplate.APPLICATION_START_ROW;
import static fi.riista.feature.permitplanning.hirvityvitys.summary.JyvitysExcelSummaryTemplate.RHY_NAME_CELL;

public class JyvitysExcelSummaryStyleApplier implements JyvitysExcelCellAddressManipulation {

    private static final List<JyvitysExcelSummaryStaticField> WRAPPED_TEXT_FIELDS = ImmutableList.of(
            SUMMARY_APPLICANT,
            SUMMARY_PRIVATE_LAND,
            SUMMARY_STATE_LAND,
            SUMMARY_TOTAL_LAND,
            SUMMARY_SHOOTERS_ONLY_CLUB,
            SUMMARY_SHOOTERS_OTHER_CLUB_PASSIVE,
            SUMMARY_SHOOTERS_TOTAL,
            SUMMARY_SUGGESTION_TOTAL,
            SUMMARY_SUGGESTION_ADULT,
            SUMMARY_SUGGESTION_CALF,
            SUMMARY_SHOOTERS_TOTAL_PER_STATE_LAND,
            SUMMARY_APPLICATION_AMOUNT,
            SUMMARY_AREA_IN_OTHER_RHY
    );
    private final Sheet sheet;
    private final CellStyle boldCellStyle;
    private final CellStyle wrappedTextCellStyle;
    private final CellStyle userModifiableCellStyle;
    private final CellStyle suggestionTotalCellStyle;
    private final CellStyle suggestionAdultsCellStyle;
    private final CellStyle suggestionCalfsCellStyle;
    private final int applicationCount;
    private final CellStyle areaInOtherRhyNotificationStyle;

    public static void apply(final Sheet sheet, final int applicationCount) {
        final JyvitysExcelSummaryStyleApplier instance = new JyvitysExcelSummaryStyleApplier(sheet, applicationCount);
        instance.applySummaryStyles();

    }

    private JyvitysExcelSummaryStyleApplier(final Sheet sheet, final int applicationCount) {
        this.sheet = sheet;
        this.applicationCount = applicationCount;
        final Workbook workbook = sheet.getWorkbook();
        this.boldCellStyle = workbook.createCellStyle();
        final Font bold = workbook.createFont();
        bold.setBold(true);
        this.boldCellStyle.setFont(bold);
        this.wrappedTextCellStyle = workbook.createCellStyle();
        this.wrappedTextCellStyle.setWrapText(true);
        this.wrappedTextCellStyle.setFont(bold);

        this.userModifiableCellStyle = workbook.createCellStyle();
        this.userModifiableCellStyle.setFillForegroundColor(USER_MODIFIABLE_FIELD_COLOUR);
        this.userModifiableCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        this.suggestionTotalCellStyle = workbook.createCellStyle();
        this.suggestionTotalCellStyle.setFillForegroundColor(SUGGESTION_TOTAL_COLOUR);
        this.suggestionTotalCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        this.suggestionTotalCellStyle.setFont(bold);
        this.suggestionTotalCellStyle.setWrapText(true);

        this.suggestionAdultsCellStyle = workbook.createCellStyle();
        this.suggestionAdultsCellStyle.setFillForegroundColor(SUGGESTION_ADULTS_COLOUR);
        this.suggestionAdultsCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        this.suggestionAdultsCellStyle.setFont(bold);
        this.suggestionAdultsCellStyle.setWrapText(true);

        this.suggestionCalfsCellStyle = workbook.createCellStyle();
        this.suggestionCalfsCellStyle.setFillForegroundColor(SUGGESTION_CALFS_COLOUR);
        this.suggestionCalfsCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        this.suggestionCalfsCellStyle.setFont(bold);
        this.suggestionCalfsCellStyle.setWrapText(true);

        this.areaInOtherRhyNotificationStyle = workbook.createCellStyle();
        this.areaInOtherRhyNotificationStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        this.areaInOtherRhyNotificationStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        this.areaInOtherRhyNotificationStyle.setFont(bold);
        this.areaInOtherRhyNotificationStyle.setWrapText(true);
    }

    private void applySummaryStyles() {
        applyStyleForStaticHeaders();
        applyStyleForWrappedTextHeaders();
        applyForApplicationCells();
        applyConditionalFormattingForAppliedAmounts();
    }

    private void applyStyleForStaticHeaders() {
        getCell(RHY_NAME_CELL).setCellStyle(boldCellStyle);
        getCell(SUMMARY_TITLE_CELL.getCellAddress()).setCellStyle(boldCellStyle);
        getCell(SUMMARY_TOTAL_QUOTA.getCellAddress()).setCellStyle(boldCellStyle);
    }

    private void applyConditionalFormattingForAppliedAmounts() {
        final SheetConditionalFormatting formatting = sheet.getSheetConditionalFormatting();
        final String firstApplicationAmountCell =
                SUMMARY_APPLICATION_AMOUNT.getColumn() + (APPLICATION_START_ROW + 1);
        final String lastApplicationAmountCell =
                SUMMARY_APPLICATION_AMOUNT.getColumn() + (APPLICATION_START_ROW + applicationCount);
        final String firstApplicationSuggestionTotalCell =
                SUMMARY_SUGGESTION_TOTAL.getColumn() + (APPLICATION_START_ROW + 1);
        // Highlight the cell if applied amount is less that suggested by the calculation
        final String highlightCondition =
                String.format("%s<%s", firstApplicationAmountCell, firstApplicationSuggestionTotalCell);
        final ConditionalFormattingRule rule = formatting.createConditionalFormattingRule(highlightCondition);

        final PatternFormatting patternFormatting = rule.createPatternFormatting();

        patternFormatting.setFillBackgroundColor(IndexedColors.RED.getIndex());
        patternFormatting.setFillPattern(PatternFormatting.SOLID_FOREGROUND);
        final CellRangeAddress region = CellRangeAddress.valueOf(
                String.format("%s:%s", firstApplicationAmountCell, lastApplicationAmountCell));
        CellRangeAddress[] regions = {region};
        formatting.addConditionalFormatting(regions, rule);
    }

    private void applyStyleForWrappedTextHeaders() {
        final PropertyTemplate propertyTemplate = new PropertyTemplate();
        WRAPPED_TEXT_FIELDS.forEach(field -> {
            final CellAddress address = field.getCellAddress();
            sheet.getRow(address.getRow()).getCell(address.getColumn()).setCellStyle(getStyleFor(field));
            field.getMergedRegion().ifPresent(borders -> {
                sheet.addMergedRegion(borders);
                propertyTemplate.drawBorders(borders, BorderStyle.MEDIUM, BorderExtent.OUTSIDE);
            });
        });
        propertyTemplate.applyBorders(sheet);
    }

    private void applyForApplicationCells() {
        for (int row = APPLICATION_START_ROW; row < APPLICATION_START_ROW + applicationCount; ++row) {
            sheet.getRow(row).getCell(JyvitysExcelSummaryStaticField.SUMMARY_APPLICANT.getCellAddress().getColumn()).setCellStyle(boldCellStyle);
            sheet.getRow(row).getCell(SUMMARY_SHOOTERS_ONLY_CLUB.getCellAddress().getColumn()).setCellStyle(userModifiableCellStyle);
            sheet.getRow(row).getCell(SUMMARY_SHOOTERS_OTHER_CLUB_PASSIVE.getCellAddress().getColumn()).setCellStyle(userModifiableCellStyle);
            sheet.getRow(row).getCell(SUMMARY_SUGGESTION_TOTAL.getCellAddress().getColumn()).setCellStyle(suggestionTotalCellStyle);
            sheet.getRow(row).getCell(SUMMARY_SUGGESTION_ADULT.getCellAddress().getColumn()).setCellStyle(suggestionAdultsCellStyle);
            sheet.getRow(row).getCell(SUMMARY_SUGGESTION_CALF.getCellAddress().getColumn()).setCellStyle(suggestionCalfsCellStyle);
            final Cell areaInOtherRhyCell = sheet.getRow(row).getCell(SUMMARY_AREA_IN_OTHER_RHY.getCellAddress().getColumn());
            if (StringUtils.isNotBlank(areaInOtherRhyCell.getStringCellValue())) {
                areaInOtherRhyCell.setCellStyle(areaInOtherRhyNotificationStyle);
            }
        }
    }

    @Override
    public Sheet getSheet() {
        return sheet;
    }

    private CellStyle getStyleFor(final JyvitysExcelSummaryStaticField key) {
        switch (key) {
            case SUMMARY_SUGGESTION_TOTAL:
                return suggestionTotalCellStyle;
            case SUMMARY_SUGGESTION_ADULT:
                return suggestionAdultsCellStyle;
            case SUMMARY_SUGGESTION_CALF:
                return suggestionCalfsCellStyle;
            default:
                return wrappedTextCellStyle;
        }
    }
}
