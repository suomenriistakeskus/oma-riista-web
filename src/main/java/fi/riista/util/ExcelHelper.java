package fi.riista.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

public class ExcelHelper {

    private final Sheet sheet;

    private final CellStyle boldStyle;
    private final CellStyle dateTimeStyle;
    private final CellStyle dateStyle;
    private final CellStyle timeStyle;
    private final CellStyle percentageStyle;
    private final CellStyle currencyStyle;

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

        this.boldStyle = wb.createCellStyle();
        final Font boldFont = wb.createFont();
        boldFont.setBold(true);
        this.boldStyle.setFont(boldFont);

        this.dateTimeStyle = wb.createCellStyle();
        this.dateTimeStyle.setDataFormat(wb.createDataFormat().getFormat("d.m.yyyy h:mm"));

        this.dateStyle = wb.createCellStyle();
        this.dateStyle.setDataFormat(wb.createDataFormat().getFormat("d.m.yyyy"));

        this.timeStyle = wb.createCellStyle();
        this.timeStyle.setDataFormat(wb.createDataFormat().getFormat("h:mm"));

        this.percentageStyle = wb.createCellStyle();
        this.percentageStyle.setDataFormat(wb.createDataFormat().getFormat("#0.00 %"));

        this.currencyStyle = wb.createCellStyle();
        this.currencyStyle.setDataFormat(wb.createDataFormat().getFormat("##,##0.00 €"));
    }

    public ExcelHelper autoSizeColumns() {
        updateMaxLastCellNum();
        for (int i = 0; i < maxLastCellNum; i++) {
            this.sheet.autoSizeColumn(i);
        }
        return this;
    }

    public ExcelHelper appendHeaderRow(final String[] headers) {
        appendRow();
        for (String header : headers) {
            appendTextCell(header);
        }
        return this;
    }

    public ExcelHelper appendHeaderRow(final Iterable<String> headers) {
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
            final Cell cell = currentRow.createCell(currentColumnIndex);
            // To prevent problems with Float type the value is always converted to String.
            // Example: Float.valueOf("1.11").doubleValue() is 1.1100000143051147
            cell.setCellValue(Double.valueOf(value.toString()));
        }
        currentColumnIndex++;
        return this;
    }

    public ExcelHelper appendDoubleCell(final Double value, final int precision) {
        if (value != null) {
            final Cell cell = currentRow.createCell(currentColumnIndex);
            final String formatPattern = "%." + precision + "f";
            cell.setCellValue(Double.valueOf(String.format(formatPattern, value)));
        }
        currentColumnIndex++;
        return this;
    }

    public ExcelHelper appendTextCell(final String value) {
        appendTextCellInternal(value);
        return this;
    }

    public ExcelHelper appendTextCellBold(final String value) {
        appendTextCellInternal(value).ifPresent(cell -> cell.setCellStyle(boldStyle));
        return this;
    }

    public ExcelHelper appendTextCell(final String value, final HorizontalAlignment aligment) {
        appendTextCellInternal(value).ifPresent(cell -> {
            final CellStyle style = cell.getRow().getSheet().getWorkbook().createCellStyle();
            style.setAlignment(aligment);
            cell.setCellStyle(style);
        });
        return this;
    }

    private Optional<Cell> appendTextCellInternal(final String value) {
        Cell cell = null;

        if (!StringUtils.isEmpty(value)) {
            cell = currentRow.createCell(currentColumnIndex);
            cell.setCellValue(value);
        }
        currentColumnIndex++;
        return Optional.ofNullable(cell);
    }

    public ExcelHelper appendBoolCell(final Boolean value) {
        if (value != null) {
            currentRow.createCell(currentColumnIndex).setCellValue(value);
        }
        currentColumnIndex++;
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
            final Cell cell = currentRow.createCell(currentColumnIndex);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(value);
        }
        currentColumnIndex++;
        return this;
    }

    public ExcelHelper appendPercentageCell(final Double value) {
        if (value != null) {
            final Cell cell = currentRow.createCell(currentColumnIndex);
            cell.setCellValue(value / 100.0);
            cell.setCellStyle(percentageStyle);
        }
        currentColumnIndex++;
        return this;
    }

    public ExcelHelper appendCurrencyCell(final BigDecimal value) {
        if (value != null) {
            final Cell cell = currentRow.createCell(currentColumnIndex);
            cell.setCellValue(value.doubleValue());
            cell.setCellStyle(currencyStyle);
        }
        currentColumnIndex++;
        return this;
    }

    public ExcelHelper createFreezePane(final int colSplit, final int rowSplit) {
        sheet.createFreezePane(colSplit, rowSplit);
        return this;
    }
}
