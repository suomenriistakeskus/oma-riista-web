package fi.riista.feature.permitplanning.hirvityvitys.verotuslohko;

import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.Optional;

public enum JyvitysExcelVerotuslohkoStaticField {

    VEROTUSLOHKO_SHEET_TITLE(address("A2")),
    VEROTUSLOHKO_APPLICANT(address("A5"), CellRangeAddress.valueOf("A5:A7")),
    VEROTUSLOHKO_PRIVATE_LAND(address("B5"), CellRangeAddress.valueOf("B5:B7")),
    VEROTUSLOHKO_STATE_LAND(address("C5"), CellRangeAddress.valueOf("C5:C7")),
    VEROTUSLOHKO_STATE_LAND_ACCEPTED_FOR_JYVITYS(address("D5"), CellRangeAddress.valueOf("D5:D7")),
    VEROTUSLOHKO_TOTAL_LAND(address("E5"), CellRangeAddress.valueOf("E5:E7")),
    VEROTUSLOHKO_SHOOTERS(address("G5"), CellRangeAddress.valueOf("F5:I5")),
    VEROTUSLOHKO_SHOOTERS_ONLY_CLUB(address("F6"), CellRangeAddress.valueOf("F6:F7")),
    VEROTUSLOHKO_SHOOTERS_OTHER_CLUB_PASSIVE(address("G6"), CellRangeAddress.valueOf("G6:G7")),
    VEROTUSLOHKO_SHOOTERS_TOTAL(address("H6"), CellRangeAddress.valueOf("H6:H7")),
    VEROTUSLOHKO_SHOOTERS_TOTAL_PER_STATE_LAND(address("I6"), CellRangeAddress.valueOf("I6:I7")),
    VEROTUSLOHKO_APPLICATION_AMOUNT(address("J5"), CellRangeAddress.valueOf("J5:J7")),
    VEROTUSLOHKO_AMOUNT_BY_LAND(address("K6"), CellRangeAddress.valueOf("K6:K7")),
    VEROTUSLOHKO_AMOUNT_BY_SHOOTERS(address("L6"), CellRangeAddress.valueOf("L6:L7")),
    VEROTUSLOHKO_AMOUNT_TOTAL(address("M6"), CellRangeAddress.valueOf("M6:M7")),
    VEROTUSLOHKO_SUGGESTION_TOTAL(address("N5"), CellRangeAddress.valueOf("N5:N7")),
    VEROTUSLOHKO_SUGGESTION_ADULT(address("O5"), CellRangeAddress.valueOf("O5:O7")),
    VEROTUSLOHKO_TITLE(address("F1")),
    VEROTUSLOHKO_OFFICIAL_CODE(address("G1")),
    VEROTUSLOHKO_CALCULATED_AMOUNTS(address("K5"), CellRangeAddress.valueOf("K5:M5")),

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
