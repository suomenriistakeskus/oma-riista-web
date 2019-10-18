package fi.riista.feature.permitplanning.hirvityvitys.verotuslohko;

import com.google.common.collect.ImmutableList;
import fi.riista.feature.permitplanning.hirvityvitys.JyvitysExcelCellAddressManipulation;
import org.apache.poi.ss.usermodel.BorderExtent;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.PropertyTemplate;

import java.util.List;

import static fi.riista.feature.permitplanning.hirvityvitys.JyvitysExcelCellColours.SUGGESTION_ADULTS_COLOUR;
import static fi.riista.feature.permitplanning.hirvityvitys.JyvitysExcelCellColours.SUGGESTION_TOTAL_COLOUR;
import static fi.riista.feature.permitplanning.hirvityvitys.JyvitysExcelCellColours.USER_MODIFIABLE_FIELD_COLOUR;
import static fi.riista.feature.permitplanning.hirvityvitys.summary.JyvitysExcelSummaryTemplate.APPLICATION_START_ROW;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_AMOUNT_BY_LAND;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_AMOUNT_BY_SHOOTERS;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_AMOUNT_TOTAL;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_APPLICANT;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_APPLICATION_AMOUNT;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_CALF_QUOTA;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_OFFICIAL_CODE;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_PRIVATE_LAND;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_QUOTA_TO_ALLOCATE;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_SHEET_TITLE;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_SHOOTERS_ONLY_CLUB;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_SHOOTERS_OTHER_CLUB_PASSIVE;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_SHOOTERS_TOTAL;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_SHOOTERS_TOTAL_PER_STATE_LAND;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_STATE_LAND;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_STATE_LAND_ACCEPTED_FOR_JYVITYS;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_STATS;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_SUGGESTION_ADULT;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_SUGGESTION_TOTAL;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_SUMMARY_TOTAL;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_TITLE;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_TOTAL_LAND;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoValueCellAddress.getValueCellAddressFor;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoValueCellAddress.getValueCellAddressWithApplicationsFor;

public class JyvitysExcelVerotuslohkoStyleApplier implements JyvitysExcelCellAddressManipulation {
    private static final List<JyvitysExcelVerotuslohkoStaticField> WRAPPED_TEXT_FIELDS = ImmutableList.of(
            VEROTUSLOHKO_APPLICANT,
            VEROTUSLOHKO_PRIVATE_LAND,
            VEROTUSLOHKO_STATE_LAND,
            VEROTUSLOHKO_STATE_LAND_ACCEPTED_FOR_JYVITYS,
            VEROTUSLOHKO_TOTAL_LAND,
            VEROTUSLOHKO_SHOOTERS_ONLY_CLUB,
            VEROTUSLOHKO_SHOOTERS_OTHER_CLUB_PASSIVE,
            VEROTUSLOHKO_SHOOTERS_TOTAL,
            VEROTUSLOHKO_SHOOTERS_TOTAL_PER_STATE_LAND,
            VEROTUSLOHKO_APPLICATION_AMOUNT,
            VEROTUSLOHKO_SUGGESTION_TOTAL,
            VEROTUSLOHKO_SUGGESTION_ADULT,
            VEROTUSLOHKO_AMOUNT_BY_LAND,
            VEROTUSLOHKO_AMOUNT_BY_SHOOTERS,
            VEROTUSLOHKO_AMOUNT_TOTAL
    );

    private final Sheet sheet;
    private final CellStyle boldCellStyle;
    private final CellStyle wrappedTextCellStyle;
    private final CellStyle userModifiableCellStyle;
    private final CellStyle suggestionTotalCellStyle;
    private final CellStyle suggestionAdultsCellStyle;
    private final int applicationCount;

    public static void apply(final Sheet sheet, final int applicationCount) {
        final JyvitysExcelVerotuslohkoStyleApplier instance = new JyvitysExcelVerotuslohkoStyleApplier(sheet, applicationCount);
        instance.applyVerotuslohkoStyles();

    }

    private JyvitysExcelVerotuslohkoStyleApplier(final Sheet sheet, final int applicationCount) {
        this.sheet = sheet;
        this.applicationCount = applicationCount;
        final Workbook workbook = sheet.getWorkbook();
        this.boldCellStyle = workbook.createCellStyle();
        final Font bold = workbook.createFont();
        bold.setBold(true);
        this.boldCellStyle.setFont(bold);
        this.wrappedTextCellStyle = workbook.createCellStyle();
        this.wrappedTextCellStyle.setFont(bold);
        this.wrappedTextCellStyle.setWrapText(true);

        this.userModifiableCellStyle = workbook.createCellStyle();
        this.userModifiableCellStyle.setFillForegroundColor(USER_MODIFIABLE_FIELD_COLOUR);
        this.userModifiableCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        this.userModifiableCellStyle.setFont(bold);

        this.suggestionTotalCellStyle = workbook.createCellStyle();
        this.suggestionTotalCellStyle.setFillForegroundColor(SUGGESTION_TOTAL_COLOUR);
        this.suggestionTotalCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        this.suggestionTotalCellStyle.setWrapText(true);
        this.suggestionTotalCellStyle.setFont(bold);

        this.suggestionAdultsCellStyle = workbook.createCellStyle();
        this.suggestionAdultsCellStyle.setFillForegroundColor(SUGGESTION_ADULTS_COLOUR);
        this.suggestionAdultsCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        this.suggestionAdultsCellStyle.setWrapText(true);
        this.suggestionAdultsCellStyle.setFont(bold);
    }

    private void applyVerotuslohkoStyles() {
        setStyleForStaticHeaders();
        applyStyleForWrappedTextHeaders();
        applyForApplicationCells();

        applyStyleForUserModifiableFields();

    }

    private void applyStyleForUserModifiableFields() {
        getValueCellFor(VEROTUSLOHKO_QUOTA_TO_ALLOCATE).setCellStyle(userModifiableCellStyle);
        getValueCellFor(VEROTUSLOHKO_CALF_QUOTA).setCellStyle(userModifiableCellStyle);
    }

    private void setStyleForStaticHeaders() {
        getCell(VEROTUSLOHKO_SHEET_TITLE.getCellAddress()).setCellStyle(boldCellStyle);
        getCell(VEROTUSLOHKO_TITLE.getCellAddress()).setCellStyle(boldCellStyle);
        getCell(getValueCellAddressFor(VEROTUSLOHKO_TITLE)).setCellStyle(boldCellStyle);
        getCell(VEROTUSLOHKO_OFFICIAL_CODE.getCellAddress()).setCellStyle(boldCellStyle);
        getCell(getValueCellAddressFor(VEROTUSLOHKO_OFFICIAL_CODE)).setCellStyle(boldCellStyle);
        getCell(getValueCellAddressWithApplicationsFor(VEROTUSLOHKO_SUMMARY_TOTAL, applicationCount)).setCellStyle(boldCellStyle);
        getCell(getValueCellAddressWithApplicationsFor(VEROTUSLOHKO_STATS, applicationCount)).setCellStyle(boldCellStyle);
    }

    private void applyStyleForWrappedTextHeaders() {
        final PropertyTemplate propertyTemplate = new PropertyTemplate();
        WRAPPED_TEXT_FIELDS.forEach(field -> {
            final CellAddress address = field.getCellAddress();
            sheet.getRow(address.getRow()).getCell(address.getColumn()).setCellStyle(getStyleFor(field));
            field.getBorders().ifPresent(borders -> {
                sheet.addMergedRegion(borders);
                propertyTemplate.drawBorders(borders, BorderStyle.MEDIUM, BorderExtent.OUTSIDE);
            });
        });
        propertyTemplate.applyBorders(sheet);
    }

    private Cell getValueCellFor(final JyvitysExcelVerotuslohkoStaticField field) {
        final CellAddress address = JyvitysExcelVerotuslohkoValueCellAddress.getValueCellAddressWithApplicationsFor(field, applicationCount);
        return sheet.getRow(address.getRow()).getCell(address.getColumn());
    }

    private void applyForApplicationCells() {
        for (int row = APPLICATION_START_ROW; row < APPLICATION_START_ROW + applicationCount; ++row) {
            sheet.getRow(row).getCell(VEROTUSLOHKO_APPLICANT.getCellAddress().getColumn()).setCellStyle(boldCellStyle);
            sheet.getRow(row).getCell(VEROTUSLOHKO_STATE_LAND.getCellAddress().getColumn()).setCellStyle(userModifiableCellStyle);
            sheet.getRow(row).getCell(VEROTUSLOHKO_PRIVATE_LAND.getCellAddress().getColumn()).setCellStyle(userModifiableCellStyle);
            sheet.getRow(row).getCell(VEROTUSLOHKO_SUGGESTION_TOTAL.getCellAddress().getColumn()).setCellStyle(suggestionTotalCellStyle);
            sheet.getRow(row).getCell(VEROTUSLOHKO_SUGGESTION_ADULT.getCellAddress().getColumn()).setCellStyle(suggestionAdultsCellStyle);
        }
    }

    private CellStyle getStyleFor(final JyvitysExcelVerotuslohkoStaticField key) {
        switch (key) {
            case VEROTUSLOHKO_SUGGESTION_TOTAL:
                return suggestionTotalCellStyle;
            case VEROTUSLOHKO_SUGGESTION_ADULT:
                return suggestionAdultsCellStyle;
            default:
                return wrappedTextCellStyle;
        }
    }

    @Override
    public Sheet getSheet() {
        return sheet;
    }
}
