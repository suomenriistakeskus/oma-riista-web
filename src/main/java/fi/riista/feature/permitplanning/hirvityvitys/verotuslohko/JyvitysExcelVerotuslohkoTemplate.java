package fi.riista.feature.permitplanning.hirvityvitys.verotuslohko;

import com.google.common.base.Preconditions;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.permitplanning.hirvityvitys.JyvitysExcelCellAddressManipulation;
import fi.riista.feature.permitplanning.hirvityvitys.dto.JyvitysExcelApplicationDTO;
import fi.riista.feature.permitplanning.hirvityvitys.dto.JyvitysExcelApplicationVerotuslohkoDTO;
import fi.riista.feature.permitplanning.hirvityvitys.dto.JyvitysExcelVerotuslohkoDTO;
import org.apache.poi.ss.usermodel.BorderExtent;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.PropertyTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static fi.riista.feature.permitplanning.hirvityvitys.summary.JyvitysExcelSummaryTemplate.APPLICATION_START_ROW;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_AREA_LAND;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_AREA_PRIVATE;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_AREA_STATE;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_AREA_TOTAL;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_AREA_WATER;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_CALF_QUOTA;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_OFFICIAL_CODE;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_QUOTA_TO_ALLOCATE;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_TITLE;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoValueCellAddress.getValueCellAddressFor;
import static fi.riista.util.NumberUtils.squareMetersToHectares;

public class JyvitysExcelVerotuslohkoTemplate implements JyvitysExcelCellAddressManipulation {
    public static final int VEROTUSLOHKO_STATIC_CONTENT_HEIGHT_ROWS = 27;
    public static final int VEROTUSLOHKO_SHEET_COLUMN_COUNT = 15;

    private static final double INITIAL_VALUE = 0.0;

    private final Workbook workbook;
    private final EnumLocaliser i18n;
    private JyvitysExcelVerotuslohkoDTO verotuslohkoDTO;
    private int applicationCount;
    private final Sheet sheet;

    public JyvitysExcelVerotuslohkoTemplate(final Workbook workbook,
                                            final EnumLocaliser i18n,
                                            final JyvitysExcelVerotuslohkoDTO verotuslohkoDTO,
                                            final int applicationCount) {
        this.workbook = workbook;
        this.i18n = i18n;
        this.verotuslohkoDTO = verotuslohkoDTO;
        this.applicationCount = applicationCount;
        Preconditions.checkState(workbook.getNumberOfSheets() > 0, "Summary template must be applied before applying verotuslohko.");
        final String sheetname = String.format("%d-%s", workbook.getNumberOfSheets() + 1, verotuslohkoDTO.getName());
        this.sheet = workbook.createSheet(sheetname);
        // Freeze pane to keep headers visible
        sheet.createFreezePane(0, APPLICATION_START_ROW);
    }

    public JyvitysExcelVerotuslohkoTemplate initializeStaticContent() {

        addRowsForStaticContent();
        assignStaticValues();
        applyVerotuslohkoInfo();
        applyInitialValuesForUserFilledData();
        initializeApplicationRows();

        sheet.setDefaultColumnWidth(12);
        sheet.setColumnWidth(0, 50 * 256);
        sheet.setColumnWidth(1, 15 * 256);
        sheet.setColumnWidth(2, 15 * 256);
        sheet.setColumnWidth(3, 15 * 256);
        return this;
    }

    public JyvitysExcelVerotuslohkoTemplate applyApplicationData(final List<JyvitysExcelApplicationDTO> applicationDTOS) {
        IntStream.range(0, applicationDTOS.size())
                .forEach(counter -> {
                    final JyvitysExcelApplicationDTO applicationDTO = applicationDTOS.get(counter);
                    final JyvitysExcelApplicationVerotuslohkoDTO applicationVerotuslohkoDTO =
                            applicationDTO.getApplicationVerotuslohkoInfo(verotuslohkoDTO.getOfficialCode());
                    JyvitysExcelVerotuslohkoApplicationApplier.apply(applicationDTO, applicationVerotuslohkoDTO, sheet.getRow(counter + APPLICATION_START_ROW));
                });
        return this;
    }

    public JyvitysExcelVerotuslohkoTemplate applyStyles(final int applicationCount) {
        JyvitysExcelVerotuslohkoStyleApplier.apply(sheet, applicationCount);
        return this;
    }

    public JyvitysExcelVerotuslohkoTemplate applyFormulas() {
        // Zero-based
        Preconditions.checkState(sheet.getLastRowNum() > VEROTUSLOHKO_STATIC_CONTENT_HEIGHT_ROWS - 1, "Application rows must be inserted before formulas");
        JyvitysExcelVerotuslohkoFormulas.getVerotuslohkoFormulas(applicationCount).forEach(cf -> {
            getCell(address(cf.getCellAddress())).setCellFormula(cf.getFormula());
        });
        return this;
    }

    private void addRowsForStaticContent() {
        for (int i = 0; i < VEROTUSLOHKO_STATIC_CONTENT_HEIGHT_ROWS; ++i) {
            appendFullWidthRow(i);
        }
    }

    private void initializeApplicationRows() {
        if (applicationCount > 0) {
            final int startRow = APPLICATION_START_ROW;
            final int endRow = sheet.getLastRowNum();
            sheet.shiftRows(startRow, endRow, applicationCount);

            IntStream.range(0, applicationCount)
                    .forEach(counter -> appendFullWidthRow(counter + APPLICATION_START_ROW));
        }
    }

    private void applyVerotuslohkoInfo() {
        getCell(getValueCellAddressFor(VEROTUSLOHKO_TITLE)).setCellValue(verotuslohkoDTO.getName());
        getCell(getValueCellAddressFor(VEROTUSLOHKO_OFFICIAL_CODE)).setCellValue(verotuslohkoDTO.getOfficialCode());
        getCell(getValueCellAddressFor(VEROTUSLOHKO_AREA_PRIVATE)).setCellValue(squareMetersToHectares(verotuslohkoDTO.getPrivateLandSize()));
        getCell(getValueCellAddressFor(VEROTUSLOHKO_AREA_STATE)).setCellValue(squareMetersToHectares(verotuslohkoDTO.getStateLandSize()));
        getCell(getValueCellAddressFor(VEROTUSLOHKO_AREA_LAND)).setCellValue(squareMetersToHectares(verotuslohkoDTO.getLandSize()));
        getCell(getValueCellAddressFor(VEROTUSLOHKO_AREA_WATER)).setCellValue(squareMetersToHectares(verotuslohkoDTO.getWaterSize()));
        getCell(getValueCellAddressFor(VEROTUSLOHKO_AREA_TOTAL)).setCellValue(squareMetersToHectares(verotuslohkoDTO.getAreaSize()));
    }

    private void applyInitialValuesForUserFilledData() {
        getCell(getValueCellAddressFor(VEROTUSLOHKO_QUOTA_TO_ALLOCATE)).setCellValue(INITIAL_VALUE);
        getCell(getValueCellAddressFor(VEROTUSLOHKO_CALF_QUOTA)).setCellValue(INITIAL_VALUE);
    }


    private void assignStaticValues() {
        Arrays.stream(JyvitysExcelVerotuslohkoStaticField.values()).forEach(this::applyValueAndStyle);
    }

    private void applyValueAndStyle(final JyvitysExcelVerotuslohkoStaticField field) {
        final Cell cell = getCell(field.getCellAddress());
        Preconditions.checkNotNull(cell, "Cannot find cell for field " + field);
        final String localisationKey = getLocalisationKey(EnumLocaliser.resourceKey(field));
        cell.setCellValue(localisationKey);

        final PropertyTemplate propertyTemplate = new PropertyTemplate();
        field.getBorders().ifPresent(range -> applyBorders(range, propertyTemplate));
        propertyTemplate.applyBorders(sheet);
    }

    private void applyBorders(final CellRangeAddress address, final PropertyTemplate propertyTemplate) {
        propertyTemplate.drawBorders(address, BorderStyle.MEDIUM, BorderExtent.OUTSIDE);
    }

    private void appendFullWidthRow(final int rowNum) {
        final Row row = sheet.createRow(rowNum);
        IntStream.range(0, VEROTUSLOHKO_SHEET_COLUMN_COUNT)
                .forEach(cell -> row.createCell(cell));
    }

    private String getLocalisationKey(final String key) {
        return i18n.getTranslation(key);
    }

    @Override
    public Sheet getSheet() {
        return sheet;
    }
}
