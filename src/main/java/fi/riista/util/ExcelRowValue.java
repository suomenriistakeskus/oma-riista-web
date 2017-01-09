package fi.riista.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;

public abstract class ExcelRowValue<T> {
    protected final T value;

    private ExcelRowValue(T value) {
        this.value = value;
    }

    public abstract void setCellValue(Cell cell);

    public static ExcelRowValue<String> from(String value) {
        return new RowValueString(value);
    }

    public static ExcelRowValue<Boolean> from(Boolean value) {
        return new RowValueBoolean(value);
    }

    public static ExcelRowValue<Integer> from(Integer value) {
        return new RowValueInteger(value);
    }

    private static class RowValueString extends ExcelRowValue<String> {

        RowValueString(String value) {
            super(value);
        }

        @Override
        public void setCellValue(Cell cell) {
            cell.setCellType(CellType.STRING);
            cell.setCellValue(value);
        }
    }

    private static class RowValueBoolean extends ExcelRowValue<Boolean> {

        RowValueBoolean(Boolean value) {
            super(value);
        }

        @Override
        public void setCellValue(Cell cell) {
            cell.setCellType(CellType.BOOLEAN);
            cell.setCellValue(value);
        }
    }

    private static class RowValueInteger extends ExcelRowValue<Integer> {

        RowValueInteger(Integer value) {
            super(value);
        }

        @Override
        public void setCellValue(Cell cell) {
            cell.setCellType(CellType.NUMERIC);
            cell.setCellValue(value);
        }
    }
}
