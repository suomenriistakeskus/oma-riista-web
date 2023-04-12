package fi.riista.util;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.ss.util.RegionUtil;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.apache.poi.ss.usermodel.BorderStyle.NONE;
import static org.apache.poi.ss.usermodel.BorderStyle.THIN;

public class ExcelHelper {

    private final Sheet sheet;

    private final CellStyle boldStyle;
    private final CellStyle dateTimeStyle;
    private final CellStyle dateStyle;
    private final CellStyle timeStyle;
    private final CellStyle percentageStyle;
    private final CellStyle currencyStyle;
    private final CellStyle wrappedTextStyle;

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

        this.wrappedTextStyle = wb.createCellStyle();
        this.wrappedTextStyle.setWrapText(true);
    }

    public ExcelHelper setDefaultColumnWidth(int width) {
        this.sheet.setDefaultColumnWidth(width);
        return this;
    }

    public ExcelHelper autoSizeColumns() {
        updateMaxLastCellNum();
        for (int i = 0; i < maxLastCellNum; i++) {
            this.sheet.autoSizeColumn(i);
        }
        return this;
    }

    public ExcelHelper autoSizeColumn(int index) {
        this.sheet.autoSizeColumn(index);
        return this;
    }

    public ExcelHelper setColumnWidth(final int columnIndex, final int width) {
        sheet.setColumnWidth(columnIndex, width);
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

    public ExcelHelper applyBordersToCurrentRow() {
        final int rowNum = currentRow.getRowNum();
        final int columnNum = currentColumnIndex - 1;
        final CellRangeAddress range = new CellRangeAddress(rowNum, rowNum, 0, columnNum);
        createBorders(range, IndexedColors.BLACK.getIndex(), THIN, THIN, NONE, NONE);
        return this;
    }

    public ExcelHelper appendRow() {
        updateMaxLastCellNum();
        currentRow = sheet.createRow(currentRowIndex++);
        currentColumnIndex = 0;
        return this;
    }

    public ExcelHelper setCurrentRowHeight(final int points) {
        currentRow.setHeightInPoints(points);
        return this;
    }

    private void updateMaxLastCellNum() {
        if (currentRow != null) {
            maxLastCellNum = Math.max(maxLastCellNum, currentRow.getLastCellNum());
        }
    }

    public ExcelHelper appendEmptyCell(int count) {
        for (int i = 0; i < count; i++) {
            currentRow.createCell(currentColumnIndex);
            currentColumnIndex++;
        }
        return this;
    }

    public ExcelHelper spanCurrentColumn(int colSpan) {
        return spanCurrentColumn(colSpan, NONE, NONE, NONE, NONE);
    }

    public ExcelHelper spanCurrentColumnWithBottomBorder(int colSpan, BorderStyle bottomBorderStyle) {
        return spanCurrentColumn(colSpan, NONE, bottomBorderStyle, NONE, NONE);
    }

    public ExcelHelper spanCurrentColumnBordered(int colSpan) {
        return spanCurrentColumn(colSpan, THIN, THIN, THIN, THIN);
    }

    public ExcelHelper spanCurrentColumn(int colSpan,
                                         BorderStyle topBorderStyle, BorderStyle bottomBorderStyle,
                                         BorderStyle leftBorderStyle, BorderStyle rightBorderStyle) {
        if (colSpan > 1) {
            final int romNum = currentRow.getRowNum();
            final int spanStart = currentColumnIndex - 1; // Merged cell already added
            final int spanEnd = spanStart + colSpan - 1;
            final CellRangeAddress range = new CellRangeAddress(romNum, romNum, spanStart, spanEnd);
            this.sheet.addMergedRegion(range);

            if (topBorderStyle != NONE || bottomBorderStyle != NONE ||
                    leftBorderStyle != NONE || rightBorderStyle != NONE) {
                createBorders(range, IndexedColors.BLACK.getIndex(), topBorderStyle, bottomBorderStyle, leftBorderStyle, rightBorderStyle);
            }
            currentColumnIndex = spanEnd + 1;
        }
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

    public ExcelHelper appendNumberCell(final Number value, final CellStyle style) {
        final Cell cell = currentRow.createCell(currentColumnIndex);
        cell.setCellStyle(style);
        if (value != null) {
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

    public ExcelHelper appendBigDecimalCell(final BigDecimal value, final CellStyle style) {
        if (value != null) {
            final Cell cell = currentRow.createCell(currentColumnIndex);
            cell.setCellStyle(style);
            cell.setCellValue(value.doubleValue());
        }
        currentColumnIndex++;
        return this;
    }

    public ExcelHelper appendTextCell(final String value) {
        appendTextCellInternal(value);
        return this;
    }

    public ExcelHelper appendWrappedTextCell(final String value) {
        final Optional<Cell> cell = appendTextCellInternal(value);
        cell.ifPresent(c -> c.setCellStyle(wrappedTextStyle));
        return this;
    }

    public ExcelHelper appendTextCellBold(final String value) {
        appendTextCellInternal(value).ifPresent(cell -> cell.setCellStyle(boldStyle));
        return this;
    }

    public ExcelHelper appendTextCellBold(final String value, final HorizontalAlignment aligment) {
        appendTextCellInternal(value).ifPresent(cell -> {
            cell.setCellStyle(boldStyle);
            CellUtil.setAlignment(cell, aligment);
        });
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

    public ExcelHelper appendTextCellWrapping(final String value) {
        appendTextCellInternal(value).ifPresent(cell -> {
            cell.setCellStyle(wrappedTextStyle);
        });
        return this;
    }

    private Optional<Cell> appendTextCellInternal(final String value) {
        Cell cell = null;

        if (StringUtils.hasText(value)) {
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

    public ExcelHelper appendDateCell(final LocalDate value, final CellStyle style) {
        return appendDateCell(value != null ? value.toDate() : null, style);
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

    public ExcelHelper appendDateTimeCell(final LocalDateTime value, final CellStyle style) {
        return appendDateCell(value != null ? value.toDate() : null, style);
    }

    private ExcelHelper appendDateCell(final Date value, CellStyle cellStyle) {
        final Cell cell = currentRow.createCell(currentColumnIndex);
        cell.setCellStyle(cellStyle);
        if (value != null) {
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

    public ExcelHelper withFreezedRows(final int numFreezedRows) {
        sheet.createFreezePane(0, numFreezedRows);
        return this;
    }

    public ExcelHelper withFreezePane(final int colSplit, final int rowSplit) {
        sheet.createFreezePane(colSplit, rowSplit);
        return this;
    }

    public Sheet getSheet() {
        return sheet;
    }

    public ExcelHelper withFont(Font font) {
        final Cell cell = currentRow.getCell(currentColumnIndex - 1);
        CellUtil.setFont(cell, font);

        return this;
    }

    public ExcelHelper withBorders(final BorderStyle topBorderStyle, final BorderStyle bottomBorderStyle,
                                   final BorderStyle leftBorderStyle, final BorderStyle rightBorderStyle) {
        final int rowNum = currentRow.getRowNum();
        final int columnNum = currentColumnIndex - 1;
        final CellRangeAddress range = new CellRangeAddress(rowNum, rowNum, columnNum, columnNum);

        createBorders(range, IndexedColors.BLACK.getIndex(), topBorderStyle, bottomBorderStyle, leftBorderStyle, rightBorderStyle);

        return this;
    }

    private void createBorders(final CellRangeAddress range,
                               final int color,
                               final BorderStyle topBorderStyle, final BorderStyle bottomBorderStyle,
                               final BorderStyle leftBorderStyle, final BorderStyle rightBorderStyle) {

        RegionUtil.setBorderTop(topBorderStyle, range, sheet);
        RegionUtil.setTopBorderColor(color, range, sheet);

        RegionUtil.setBorderBottom(bottomBorderStyle, range, sheet);
        RegionUtil.setBottomBorderColor(color, range, sheet);

        RegionUtil.setBorderLeft(leftBorderStyle, range, sheet);
        RegionUtil.setLeftBorderColor(color, range, sheet);

        RegionUtil.setBorderRight(rightBorderStyle, range, sheet);
        RegionUtil.setRightBorderColor(color, range, sheet);
    }

    public ExcelHelper appendTextCell(final String value, final CellStyle style) {
        final Cell cell = currentRow.createCell(currentColumnIndex);

        if (StringUtils.hasText(value)) {
            cell.setCellValue(value);
        }
        cell.setCellStyle(style);

        currentColumnIndex++;

        return this;
    }

    public ExcelHelper appendFormula(final String formula, final int... cellIndex) {
        return appendFormula(formula, null, cellIndex);
    }

    public ExcelHelper appendFormula(final String formula, final CellStyle style, final int... cellIndex) {
        List<Object> cellAddresses = new ArrayList<>();
        for (int i : cellIndex) {
            cellAddresses.add(currentRow.getCell(currentColumnIndex + i).getAddress().formatAsString());
        }
        final String formattedFormula = String.format(formula, cellAddresses.toArray());

        final Cell cell = currentRow.createCell(currentColumnIndex++);
        if (style != null) {
            cell.setCellStyle(style);
        }

        cell.setCellFormula(formattedFormula);

        return this;
    }

    public ExcelHelper appendColumnSummationFrom(final int startRow, final CellStyle style) {
        final String startCellAddr = sheet.getRow(startRow).getCell(currentColumnIndex).getAddress().formatAsString();
        final String lastCellAddr = sheet.getRow(currentRow.getRowNum() - 1).getCell(currentColumnIndex).getAddress().formatAsString();

        String formula = String.format("SUM(%s:%s)", startCellAddr, lastCellAddr);

        final Cell cell = currentRow.createCell(currentColumnIndex++);
        if (style != null) {
            cell.setCellStyle(style);
        }

        cell.setCellFormula(formula);

        return this;
    }

    public ExcelHelper addMergedRegion(final int rowStart,
                                       final int rowEnd,
                                       final int columnStart,
                                       final int columnEnd) {
        final CellRangeAddress range = new CellRangeAddress(rowStart, rowEnd, columnStart, columnEnd);
        this.sheet.addMergedRegion(range);

        return this;
    }

    public ExcelHelper addCellStyleForRegion(final CellStyle style,
                                             final int startRow,
                                             final int endRow,
                                             final int startColumn,
                                             final int endColumn) {
        for (int i = startColumn; i <= endColumn; i++) {
            for (int j = startRow; j <= endRow; j++) {
                addCellStyle(style, j, i);
            }
        }

        return this;
    }

    public ExcelHelper addCellStyle(final CellStyle style, final int row, final int column) {
        final Row cellRow = this.sheet.getRow(row);
        final Cell cell = cellRow.getCell(column);
        cell.setCellStyle(style);

        return this;
    }
}
