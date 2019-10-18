package fi.riista.feature.permitplanning.hirvityvitys.summary;

import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.Optional;

public enum JyvitysExcelSummaryStaticField {

    SUMMARY_TOTAL_QUOTA(new CellAddress("F2")),
    SUMMARY_ALLOCATION_SUGGESTION(new CellAddress("M4")),
    SUMMARY_TITLE_CELL(new CellAddress("A2")),
    SUMMARY_APPLICANT(new CellAddress("A5"), CellRangeAddress.valueOf("A5:A7")),
    SUMMARY_PRIVATE_LAND(new CellAddress("B5"), CellRangeAddress.valueOf("B5:B7")),
    SUMMARY_STATE_LAND(new CellAddress("C5"), CellRangeAddress.valueOf("C5:C7")),
    SUMMARY_TOTAL_LAND(new CellAddress("D5"), CellRangeAddress.valueOf("D5:D7")),
    SUMMARY_SHOOTERS(new CellAddress("F5"), CellRangeAddress.valueOf("E5:H5")),
    SUMMARY_SHOOTERS_ONLY_CLUB(new CellAddress("E6"), CellRangeAddress.valueOf("E6:E7")),
    SUMMARY_SHOOTERS_OTHER_CLUB_PASSIVE(new CellAddress("F6"), CellRangeAddress.valueOf("F6:F7")),
    SUMMARY_SHOOTERS_TOTAL(new CellAddress("G6"), CellRangeAddress.valueOf("G6:G7")),
    SUMMARY_SHOOTERS_TOTAL_PER_STATE_LAND(new CellAddress("H6"), CellRangeAddress.valueOf("H6:H7")),
    SUMMARY_APPLICATION_AMOUNT(new CellAddress("I5"), CellRangeAddress.valueOf("I5:I7")),
    SUMMARY_SUGGESTION_TOTAL(new CellAddress("M5"), CellRangeAddress.valueOf("M5:M7")),
    SUMMARY_SUGGESTION_ADULT(new CellAddress("N5"), CellRangeAddress.valueOf("N5:N7")),
    SUMMARY_SUGGESTION_CALF(new CellAddress("O5"), CellRangeAddress.valueOf("O5:O7")),
    SUMMARY_AREA_IN_OTHER_RHY(new CellAddress("P5"), CellRangeAddress.valueOf("P5:P7")),
    // Cells after this will have their addresses shifted after application row insertions
    SUMMARY_SUMMARY_TOTAL(new CellAddress("A8"), CellRangeAddress.valueOf("A8:P8")),
    SUMMARY_NOTICE_TITLE(new CellAddress("A11")),
    SUMMARY_NOTICE_LINE1(new CellAddress("A12")),
    SUMMARY_NOTICE_LINE2(new CellAddress("A13")),
    SUMMARY_NOTICE_LINE3(new CellAddress("A14")),
    SUMMARY_NOTICE_LINE4(new CellAddress("A15")),
    SUMMARY_NOTICE_LINE5(new CellAddress("A16")),
    SUMMARY_NOTICE_LINE6(new CellAddress("A17")),
    SUMMARY_NOTICE_LINE7(new CellAddress("A18"));


    private final CellAddress cellAddress;
    private final Optional<CellRangeAddress> mergedRegion;

    JyvitysExcelSummaryStaticField(final CellAddress cellAddress) {

        this.cellAddress = cellAddress;
        this.mergedRegion = Optional.empty();
    }

    JyvitysExcelSummaryStaticField(final CellAddress cellAddress, final CellRangeAddress mergedRegion) {

        this.cellAddress = cellAddress;
        this.mergedRegion = Optional.of(mergedRegion);
    }

    public CellAddress getCellAddress() {
        return cellAddress;
    }

    public Optional<CellRangeAddress> getMergedRegion() {
        return mergedRegion;
    }

    public String getColumn() {
        return String.valueOf(cellAddress.formatAsString().charAt(0));
    }

    public int getColumnNumber() {
        return cellAddress.getColumn();
    }
}
