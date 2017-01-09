package fi.riista.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.springframework.util.StringUtils;

import java.util.Date;

public class ExcelHelper {
    private final Sheet sheet;

    private final CellStyle dateTimeStyle;
    private final CellStyle dateStyle;
    private final CellStyle timeStyle;

    private Row currentRow;
    private int currentRowIndex;
    private int currentColumnIndex;
    private int maxLastCellNum = 0;

    public ExcelHelper(final Workbook workbook) {
        this(workbook, null);
    }

    public ExcelHelper(final Workbook wb, final String sheetName) {
        this.sheet = sheetName != null ? wb.createSheet(sheetName) : wb.createSheet();
        this.sheet.setDefaultColumnWidth(20);

        this.dateTimeStyle = wb.createCellStyle();
        this.dateTimeStyle.setDataFormat(wb.createDataFormat().getFormat("d.m.yyyy h:mm"));

        this.dateStyle = wb.createCellStyle();
        this.dateStyle.setDataFormat(wb.createDataFormat().getFormat("d.m.yyyy"));

        this.timeStyle = wb.createCellStyle();
        this.timeStyle.setDataFormat(wb.createDataFormat().getFormat("h:mm"));
    }

    public ExcelHelper autoSizeColumns() {
        updateMaxLastCellNum();
        for (int i = 0; i < maxLastCellNum; i++) {
            this.sheet.autoSizeColumn(i);
        }
        return this;
    }

    public ExcelHelper appendHeaderRow(String[] headers) {
        appendRow();
        for (String header : headers) {
            appendTextCell(header);
        }
        return this;
    }

    public ExcelHelper appendRow() {
        updateMaxLastCellNum();
        currentRow = sheet.createRow(currentRowIndex++);
        currentColumnIndex = 0;
        return this;
    }

    private void updateMaxLastCellNum() {
        if (currentRow != null) {
            maxLastCellNum = Math.max(maxLastCellNum, currentRow.getLastCellNum());
        }
    }

    public ExcelHelper appendEmptyCell(int count) {
        currentColumnIndex += count;
        return this;
    }

    public ExcelHelper appendNumberCell(final Number value) {
        if (value != null) {
            final Cell cell = currentRow.createCell(currentColumnIndex++);
            //To prevent problems with Float type the value is always converted to String.
            //Example: Float.valueOf("1.11").doubleValue() is 1.1100000143051147
            cell.setCellValue(Double.valueOf(value.toString()));
        } else {
            currentColumnIndex++;
        }
        return this;
    }

    public ExcelHelper appendDoubleCell(final Double value, final int precision) {
        if (value != null) {
            final Cell cell = currentRow.createCell(currentColumnIndex++);
            final String formatPattern = "%." + precision + "f";
            cell.setCellValue(Double.valueOf(String.format(formatPattern, value)));
        } else {
            currentColumnIndex++;
        }
        return this;
    }

    public ExcelHelper appendTextCell(final String value) {
        if (value != null && !StringUtils.isEmpty(value)) {
            currentRow.createCell(currentColumnIndex++).setCellValue(value);
        } else {
            currentColumnIndex++;
        }
        return this;
    }

    public ExcelHelper appendBoolCell(final Boolean value) {
        if (value != null) {
            currentRow.createCell(currentColumnIndex++).setCellValue(value);
        } else {
            currentColumnIndex++;
        }
        return this;
    }

    public ExcelHelper appendDateCell(final Date value) {
        return appendDateCell(value, dateStyle);
    }

    public ExcelHelper appendDateCell(final LocalDate value) {
        return appendDateCell(value != null ? value.toDate() : null, dateStyle);
    }

    public ExcelHelper appendTimeCell(final Date value) {
        return appendDateCell(value, timeStyle);
    }

    public ExcelHelper appendDateTimeCell(final Date value) {
        return appendDateCell(value, dateTimeStyle);
    }

    public ExcelHelper appendDateTimeCell(final LocalDateTime value) {
        return appendDateTimeCell(value != null ? value.toDate() : null);
    }

    private ExcelHelper appendDateCell(final Date value, CellStyle cellStyle) {
        if (value != null) {
            final Cell cell = currentRow.createCell(currentColumnIndex++);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(value);
        } else {
            currentColumnIndex++;
        }
        return this;
    }

}
