package fi.riista.feature.permitplanning.hirvityvitys.verotuslohko;

import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.Optional;

public enum JyvitysExcelVerotuslohkoStaticField {

    VEROTUSLOHKO_SHEET_TITLE(address("A2")),
    VEROTUSLOHKO_APPLICATION_NUMBER(address("A5"), CellRangeAddress.valueOf("A5:A7")),
    VEROTUSLOHKO_APPLICANT(address("B5"), CellRangeAddress.valueOf("B5:B7")),
    VEROTUSLOHKO_PRIVATE_LAND(address("C5"), CellRangeAddress.valueOf("C5:C7")),
    VEROTUSLOHKO_STATE_LAND(address("D5"), CellRangeAddress.valueOf("D5:D7")),
    VEROTUSLOHKO_STATE_LAND_ACCEPTED_FOR_JYVITYS(address("E5"), CellRangeAddress.valueOf("E5:E7")),
    VEROTUSLOHKO_TOTAL_LAND(address("F5"), CellRangeAddress.valueOf("F5:F7")),
    VEROTUSLOHKO_SHOOTERS(address("H5"), CellRangeAddress.valueOf("G5:J5")),
    VEROTUSLOHKO_SHOOTERS_ONLY_CLUB(address("G6"), CellRangeAddress.valueOf("G6:G7")),
    VEROTUSLOHKO_SHOOTERS_OTHER_CLUB_PASSIVE(address("H6"), CellRangeAddress.valueOf("H6:H7")),
    VEROTUSLOHKO_SHOOTERS_TOTAL(address("I6"), CellRangeAddress.valueOf("I6:I7")),
    VEROTUSLOHKO_SHOOTERS_TOTAL_PER_STATE_LAND(address("J6"), CellRangeAddress.valueOf("J6:J7")),
    VEROTUSLOHKO_APPLICATION_AMOUNT(address("K5"), CellRangeAddress.valueOf("K5:K7")),
    VEROTUSLOHKO_AMOUNT_BY_LAND(address("L6"), CellRangeAddress.valueOf("L6:L7")),
    VEROTUSLOHKO_AMOUNT_BY_SHOOTERS(address("M6"), CellRangeAddress.valueOf("M6:M7")),
    VEROTUSLOHKO_AMOUNT_TOTAL(address("N6"), CellRangeAddress.valueOf("N6:N7")),
    VEROTUSLOHKO_SUGGESTION_TOTAL(address("O5"), CellRangeAddress.valueOf("O5:O7")),
    VEROTUSLOHKO_SUGGESTION_ADULT(address("P5"), CellRangeAddress.valueOf("P5:P7")),
    VEROTUSLOHKO_TITLE(address("F1")),
    VEROTUSLOHKO_OFFICIAL_CODE(address("G1")),
    VEROTUSLOHKO_CALCULATED_AMOUNTS(address("L5"), CellRangeAddress.valueOf("L5:N5")),

    // Cells after this will have their addresses shifted downward after application row insertions
    VEROTUSLOHKO_SUMMARY_TOTAL(address("A8"), CellRangeAddress.valueOf("A8:O8")),
    VEROTUSLOHKO_QUOTA_TO_ALLOCATE(address("A10"), CellRangeAddress.valueOf("A10:M16")),
    VEROTUSLOHKO_BLOCK_QUOTA_UNIT(address("D10")),
    VEROTUSLOHKO_QUOTA_BY_LAND(address("A11")),
    VEROTUSLOHKO_LAND_QUOTA_UNIT(address("D11")),
    VEROTUSLOHKO_PERMITS_PER_1000_HA(address("H11")),
    VEROTUSLOHKO_QUOTA_BY_SHOOTERS(address("A12")),
    VEROTUSLOHKO_SHOOTER_QUOTA_UNIT(address("D12")),
    VEROTUSLOHKO_PERMITS_PER_SHOOTER(address("H12")),
    VEROTUSLOHKO_CALF_QUOTA(address("A13")),
    VEROTUSLOHKO_AREA_FOR_CALCULATION_TOTAL(address("A14"), CellRangeAddress.valueOf("A14:J16")),
    VEROTUSLOHKO_AREA_FOR_CALCULATION_STATE(address("A15")),
    VEROTUSLOHKO_AREA_FOR_CALCULATION_PRIVATE(address("A16")),
    VEROTUSLOHKO_STATS(address("A18"), CellRangeAddress.valueOf("A18:B27")),
    VEROTUSLOHKO_AREA_PRIVATE(address("A19")),
    VEROTUSLOHKO_AREA_STATE(address("A21")),
    VEROTUSLOHKO_AREA_LAND(address("A23")),
    VEROTUSLOHKO_AREA_WATER(address("A25")),
    VEROTUSLOHKO_AREA_TOTAL(address("A27")),
    ;

    private static CellAddress address(String address) {
        return new CellAddress(address);
    }

    private final CellAddress cellAddress;
    private final Optional<CellRangeAddress> borders;

    JyvitysExcelVerotuslohkoStaticField(final CellAddress cellAddress) {
        this.cellAddress = cellAddress;
        this.borders = Optional.empty();
    }

    JyvitysExcelVerotuslohkoStaticField(final CellAddress cellAddress, CellRangeAddress borders) {
        this.cellAddress = cellAddress;
        this.borders = Optional.of(borders);
    }

    public CellAddress getCellAddress() {
        return cellAddress;
    }

    public Optional<CellRangeAddress> getBorders() {
        return borders;
    }

    public String getColumn() {
        return String.valueOf(cellAddress.formatAsString().charAt(0));
    }

    public int getColumnNumber() {
        return cellAddress.getColumn();
    }
}
