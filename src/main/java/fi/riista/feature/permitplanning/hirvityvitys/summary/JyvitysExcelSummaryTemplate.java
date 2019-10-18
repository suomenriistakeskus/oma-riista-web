package fi.riista.feature.permitplanning.hirvityvitys.summary;

import com.google.common.base.Preconditions;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.permitplanning.hirvityvitys.JyvitysExcelCellAddressManipulation;
import fi.riista.feature.permitplanning.hirvityvitys.dto.JyvitysExcelApplicationDTO;
import fi.riista.feature.permitplanning.hirvityvitys.dto.JyvitysExcelRhyDTO;
import org.apache.poi.ss.usermodel.BorderExtent;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.PropertyTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static fi.riista.feature.permitplanning.hirvityvitys.summary.JyvitysExcelSummaryStaticField.SUMMARY_APPLICANT;
import static fi.riista.feature.permitplanning.hirvityvitys.summary.JyvitysExcelSummaryStaticField.SUMMARY_AREA_IN_OTHER_RHY;

public class JyvitysExcelSummaryTemplate implements JyvitysExcelCellAddressManipulation {

    public static final String LOCALISATION_PREFIX = "JyvitysExcelSummaryTemplate.";
    public static final int SHEET_COLUMN_COUNT = 16;
    public static final int STATIC_CONTENT_HEIGHT_ROWS = 20;
    public static final int APPLICATION_START_ROW = 7;
    public static final CellAddress RHY_NAME_CELL = new CellAddress("A1");

    private final Workbook workbook;
    private final EnumLocaliser i18n;
    private JyvitysExcelRhyDTO rhyDTO;
    private int applicationCount;
    private final Sheet sheet;

    public JyvitysExcelSummaryTemplate(final Workbook workbook,
                                       final EnumLocaliser i18n,
                                       final JyvitysExcelRhyDTO rhyDTO,
                                       final int applicationCount) {
        this.workbook = workbook;
        this.i18n = i18n;
        this.rhyDTO = rhyDTO;
        this.applicationCount = applicationCount;
        Preconditions.checkState(workbook.getNumberOfSheets() == 0, "Summary template must be applied on a empty workbook.");
        final String sheetname = String.format("1-%s", getLocalisationKey(LOCALISATION_PREFIX + "summary"));
        this.sheet = workbook.createSheet(sheetname);
    }

    public void initializeStaticContent() {

        addRowsForStaticContent();
        assignStaticValues();
        applyRhyInformation();
        initializeApplicationRows();
        sheet.setDefaultColumnWidth(15);
        sheet.setColumnWidth(SUMMARY_APPLICANT.getColumnNumber(), 50 * 256);
        sheet.setColumnWidth(SUMMARY_AREA_IN_OTHER_RHY.getColumnNumber(), 30 * 256);

        // "Hide" J,K,L columns in order to match example excel formulas
        sheet.groupColumn(9, 11);
        sheet.setColumnGroupCollapsed(9, true);

        // Freeze pane to keep headers and appicant names visible
        sheet.createFreezePane(1, APPLICATION_START_ROW);
    }

    public JyvitysExcelSummaryTemplate applyFormulas() {
        // Zero-based
        Preconditions.checkState(sheet.getLastRowNum() > STATIC_CONTENT_HEIGHT_ROWS - 1, "Application rows must be inserted before formulas");
        Preconditions.checkState(workbook.getNumberOfSheets() > 1, "Verotuslohko sheets must be inserted before formulas");
        final int applicationCount = sheet.getLastRowNum() + 1 - STATIC_CONTENT_HEIGHT_ROWS;

        JyvitysExcelSummaryFormulas.getSummaryFormulas(workbook, applicationCount).forEach(cf -> {
            getCell(address(cf.getCellAddress())).setCellFormula(cf.getFormula());
        });
        return this;
    }

    public JyvitysExcelSummaryTemplate applyApplicationData(final List<JyvitysExcelApplicationDTO> applicationDTOS) {
        IntStream.range(0, applicationDTOS.size())
                .forEach(counter -> {
                    final Row row = sheet.getRow(counter + APPLICATION_START_ROW);
                    final JyvitysExcelApplicationDTO applicationDTO = applicationDTOS.get(counter);
                    JyvitysExcelSummaryApplicationApplier.apply(applicationDTO, row);
                });
        return this;
    }

    public JyvitysExcelSummaryTemplate applyStyle() {
        JyvitysExcelSummaryStyleApplier.apply(sheet, applicationCount);
        return this;
    }

    private void addRowsForStaticContent() {
        for (int i = 0; i < STATIC_CONTENT_HEIGHT_ROWS; ++i) {
            appendFullWidthRow(i);
        }
    }

    private void initializeApplicationRows() {
        if (applicationCount > 0) {
            final int startRow = APPLICATION_START_ROW;
            final int endRow = sheet.getLastRowNum();
            sheet.shiftRows(startRow, endRow, applicationCount);

            IntStream.range(0, applicationCount)
                    .forEach(row -> appendFullWidthRow(APPLICATION_START_ROW + row));

        }
    }

    private void applyRhyInformation() {
        sheet.getRow(RHY_NAME_CELL.getRow()).getCell(RHY_NAME_CELL.getColumn()).setCellValue(rhyDTO.getName());
    }

    private void assignStaticValues() {
        Arrays.stream(JyvitysExcelSummaryStaticField.values()).forEach(this::applyValueAndStyle);
    }

    private void applyValueAndStyle(final JyvitysExcelSummaryStaticField field) {
        final Cell cell = getCell(field.getCellAddress());
        Preconditions.checkNotNull(cell, "Cannot find cell for field " + field);
        final String localisationKey = getLocalisationKey(EnumLocaliser.resourceKey(field));

        cell.setCellValue(localisationKey);

        final PropertyTemplate propertyTemplate = new PropertyTemplate();
        field.getMergedRegion().ifPresent(range -> applyBorders(range, propertyTemplate));
        propertyTemplate.applyBorders(sheet);
    }

    private void applyBorders(CellRangeAddress address, final PropertyTemplate propertyTemplate) {
        propertyTemplate.drawBorders(address, BorderStyle.MEDIUM, BorderExtent.OUTSIDE);
    }

    private void appendFullWidthRow(final int rowNum) {
        final Row row = sheet.createRow(rowNum);
        final int sheetColumnCount = SHEET_COLUMN_COUNT;

        IntStream.range(0, sheetColumnCount)
                .forEach(cellNum -> row.createCell(cellNum));
    }

    private String getLocalisationKey(final String key) {
        return i18n.getTranslation(key);
    }

    @Override
    public Sheet getSheet() {
        return sheet;
    }
}
