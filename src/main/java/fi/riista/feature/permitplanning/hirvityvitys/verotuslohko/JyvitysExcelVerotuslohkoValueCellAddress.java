package fi.riista.feature.permitplanning.hirvityvitys.verotuslohko;

import org.apache.poi.ss.util.CellAddress;

import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_CALCULATED_AMOUNTS;

/**
 * Class for acquiring cell addresses for cell containing the field values. Some cells are located
 * underneath application rows so their position changes according to number of applications for the RHY.
 */
public class JyvitysExcelVerotuslohkoValueCellAddress {
    public static CellAddress getValueCellAddressFor(JyvitysExcelVerotuslohkoStaticField field) {
        return getValueCellAddressFor(field, 0);
    }

    public static CellAddress getValueCellAddressWithApplicationsFor(final JyvitysExcelVerotuslohkoStaticField field,
                                                                     final int applicationCount) {
        return getValueCellAddressFor(field, applicationCount);
    }

    private static CellAddress getValueCellAddressFor(final JyvitysExcelVerotuslohkoStaticField field,
                                                      final int applications) {
        // Fields after VEROTUSLOHKO_CALCULATED_AMOUNTS are after application rows
        final int applicationShift = field.ordinal() > VEROTUSLOHKO_CALCULATED_AMOUNTS.ordinal() ? applications : 0;
        switch (field) {
            case VEROTUSLOHKO_TITLE:
            case VEROTUSLOHKO_OFFICIAL_CODE:
                // Value if below the title cell
                return shift(field, 1 + applicationShift, 0);
            case VEROTUSLOHKO_QUOTA_TO_ALLOCATE:
            case VEROTUSLOHKO_QUOTA_BY_LAND:
            case VEROTUSLOHKO_QUOTA_BY_SHOOTERS:
            case VEROTUSLOHKO_CALF_QUOTA:
                // Value if two cells to the right
                return shift(field, 0 + applicationShift, 2);
            case VEROTUSLOHKO_PERMITS_PER_1000_HA:
            case VEROTUSLOHKO_PERMITS_PER_SHOOTER:
                // Value is before the title cell
                return shift(field, 0 + applicationShift, -1);
            case VEROTUSLOHKO_AREA_FOR_CALCULATION_TOTAL:
            case VEROTUSLOHKO_AREA_FOR_CALCULATION_STATE:
            case VEROTUSLOHKO_AREA_FOR_CALCULATION_PRIVATE:
                return shift(field, 0 + applicationShift, 9);
            default:
                // Other values are one cell to the right
                return shift(field, 0 + applicationShift, 1);
        }
    }


    private static CellAddress shift(final JyvitysExcelVerotuslohkoStaticField field,
                                     final int rowShift, final int columnShift) {
        final CellAddress cellAddress = field.getCellAddress();
        return new CellAddress(cellAddress.getRow() + rowShift, cellAddress.getColumn() + columnShift);
    }
}
