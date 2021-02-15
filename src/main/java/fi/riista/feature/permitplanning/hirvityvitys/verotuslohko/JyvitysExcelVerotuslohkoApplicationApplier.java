package fi.riista.feature.permitplanning.hirvityvitys.verotuslohko;

import fi.riista.feature.permitplanning.hirvityvitys.dto.JyvitysExcelApplicationDTO;
import fi.riista.feature.permitplanning.hirvityvitys.dto.JyvitysExcelApplicationVerotuslohkoDTO;
import fi.riista.feature.permitplanning.hirvityvitys.summary.JyvitysExcelSummaryStaticField;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellAddress;

import static fi.riista.feature.permitplanning.hirvityvitys.summary.JyvitysExcelSummaryStaticField.SUMMARY_SHOOTERS_ONLY_CLUB;
import static fi.riista.feature.permitplanning.hirvityvitys.summary.JyvitysExcelSummaryStaticField.SUMMARY_SHOOTERS_OTHER_CLUB_PASSIVE;
import static fi.riista.feature.permitplanning.hirvityvitys.summary.JyvitysExcelSummaryStaticField.SUMMARY_STATE_LAND;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_AMOUNT_BY_LAND;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_AMOUNT_BY_SHOOTERS;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_AMOUNT_TOTAL;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_APPLICANT;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_APPLICATION_AMOUNT;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_APPLICATION_NUMBER;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_CALF_QUOTA;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_PERMITS_PER_1000_HA;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_PERMITS_PER_SHOOTER;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_PRIVATE_LAND;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_SHOOTERS_ONLY_CLUB;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_SHOOTERS_OTHER_CLUB_PASSIVE;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_SHOOTERS_TOTAL;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_SHOOTERS_TOTAL_PER_STATE_LAND;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_STATE_LAND;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_STATE_LAND_ACCEPTED_FOR_JYVITYS;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_SUGGESTION_ADULT;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_SUGGESTION_TOTAL;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_TOTAL_LAND;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoValueCellAddress.getValueCellAddressWithApplicationsFor;
import static fi.riista.util.NumberUtils.squareMetersToHectares;

/**
 * Class for applying applicationdata for a single application on verotuslohko sheet. Static fields must be applied for
 * all sheets before applying application data.
 */
public class JyvitysExcelVerotuslohkoApplicationApplier {

    private final JyvitysExcelApplicationDTO applicationDTO;
    private JyvitysExcelApplicationVerotuslohkoDTO applicationVerotuslohkoDTO;
    private final Row row;
    private final String rowFormulaNumber;

    private JyvitysExcelVerotuslohkoApplicationApplier(final JyvitysExcelApplicationDTO applicationDTO,
                                                       final JyvitysExcelApplicationVerotuslohkoDTO applicationVerotuslohkoDTO,
                                                       final Row row) {
        this.applicationDTO = applicationDTO;
        this.applicationVerotuslohkoDTO = applicationVerotuslohkoDTO;
        this.row = row;
        this.rowFormulaNumber = String.valueOf(row.getRowNum() + 1); // rowNum is Zero-based
    }

    public static void apply(final JyvitysExcelApplicationDTO applicationDTO,
                             final JyvitysExcelApplicationVerotuslohkoDTO applicationVerotuslohkoDTO,
                             final Row row) {
        final JyvitysExcelVerotuslohkoApplicationApplier applier =
                new JyvitysExcelVerotuslohkoApplicationApplier(applicationDTO, applicationVerotuslohkoDTO, row);
        applier.applyDto();
    }

    private void applyDto() {
        final String stateLandAddress = getCellAddress(VEROTUSLOHKO_STATE_LAND);

        // Hakemusnumero
        getCell(VEROTUSLOHKO_APPLICATION_NUMBER).setCellValue(applicationDTO.getApplicationNumber());
        // Hakija
        getCell(VEROTUSLOHKO_APPLICANT).setCellValue(applicationDTO.getApplicant());
        // Yksityismaat
        getCell(VEROTUSLOHKO_PRIVATE_LAND).setCellValue(squareMetersToHectares(applicationVerotuslohkoDTO.getPrivateLandSize()));
        //Valtionmaa
        getCell(VEROTUSLOHKO_STATE_LAND).setCellValue(squareMetersToHectares(applicationVerotuslohkoDTO.getStateLandSize()));
        //Ampujien jyvitykseen hyväksytty valtionmaa
        getCell(VEROTUSLOHKO_STATE_LAND_ACCEPTED_FOR_JYVITYS).setCellFormula(getAcceptedStateLandFormula(stateLandAddress));
        // Koko alue
        getCell(VEROTUSLOHKO_TOTAL_LAND).setCellFormula(getTotalLandFormula(stateLandAddress));
        // ei/ei ampujat
        getCell(VEROTUSLOHKO_SHOOTERS_ONLY_CLUB).setCellFormula(getCellAddressOnSummarySheet(SUMMARY_SHOOTERS_ONLY_CLUB));
        // kyllä ei ampujat
        getCell(VEROTUSLOHKO_SHOOTERS_OTHER_CLUB_PASSIVE).setCellFormula(getCellAddressOnSummarySheet(SUMMARY_SHOOTERS_OTHER_CLUB_PASSIVE));
        //Hyväksytyt ampujat yhteensä
        getCell(VEROTUSLOHKO_SHOOTERS_TOTAL).setCellFormula(getShooterTotalFormula());
        // Ampujat valtionmaan suhteessa
        getCell(VEROTUSLOHKO_SHOOTERS_TOTAL_PER_STATE_LAND).setCellFormula(getShooterPerStateLandFormula());
        // Haettu lupamäärä
        getCell(VEROTUSLOHKO_APPLICATION_AMOUNT).setCellValue(applicationDTO.getAppliedAmount());
        // Mailla
        getCell(VEROTUSLOHKO_AMOUNT_BY_LAND).setCellFormula(getAmountByLandFormula());
        // Ampujilla
        getCell(VEROTUSLOHKO_AMOUNT_BY_SHOOTERS).setCellFormula(getAmountByShootersFormula());
        // Yht.
        getCell(VEROTUSLOHKO_AMOUNT_TOTAL).setCellFormula(getAmountTotalFormula());
        // pyyntilupa kpl hakija
        getCell(VEROTUSLOHKO_SUGGESTION_TOTAL).setCellFormula(getCellAddress(VEROTUSLOHKO_AMOUNT_TOTAL));
        // Aikuiset
        getCell(VEROTUSLOHKO_SUGGESTION_ADULT).setCellFormula(getAdultSuggestionFormula());
    }

    private static String getAcceptedStateLandFormula(String stateLandAddress) {
        // TODO: OR-5059: Remove this column
        return String.format("%s", stateLandAddress);
    }

    private String getTotalLandFormula(String stateLandAddress) {
        return String.format("%s + %s", getCellAddress(VEROTUSLOHKO_PRIVATE_LAND), stateLandAddress);
    }

    private String getShooterTotalFormula() {
        return String.format("%s + %s", getCellAddress(VEROTUSLOHKO_SHOOTERS_ONLY_CLUB), getCellAddress(VEROTUSLOHKO_SHOOTERS_OTHER_CLUB_PASSIVE));
    }

    private String getAmountByLandFormula() {
        return String.format("%s * %s / 1000", getCellAddress(VEROTUSLOHKO_PRIVATE_LAND), getLandMultiplierCell());
    }

    private String getAmountByShootersFormula() {
        return String.format("%s * %s", getCellAddress(VEROTUSLOHKO_SHOOTERS_TOTAL_PER_STATE_LAND), getShooterMultiplierCell());
    }

    private String getAmountTotalFormula() {
        return String.format("%s + %s", getCellAddress(VEROTUSLOHKO_AMOUNT_BY_LAND), getCellAddress(VEROTUSLOHKO_AMOUNT_BY_SHOOTERS));
    }

    private String getShooterPerStateLandFormula() {
        final String summaryStateLandCell = getCellAddressOnSummarySheet(SUMMARY_STATE_LAND);
        return String.format("IF( %s > 0, %s / %s * %s, 0 )",
                summaryStateLandCell,
                getCellAddress(VEROTUSLOHKO_STATE_LAND_ACCEPTED_FOR_JYVITYS),
                summaryStateLandCell,
                getCellAddress(VEROTUSLOHKO_SHOOTERS_TOTAL));
    }

    private String getAdultSuggestionFormula() {
        final int applicationCount = calculateApplicationCount();
        final CellAddress calfQuotaValueCellAddress = getValueCellAddressWithApplicationsFor(VEROTUSLOHKO_CALF_QUOTA, applicationCount);
        return String.format("ROUND( ( 2 * %s * (100- %s ) / (200- %s ) ), 0 )",
                getCellAddress(VEROTUSLOHKO_SUGGESTION_TOTAL),
                calfQuotaValueCellAddress,
                calfQuotaValueCellAddress);
    }

    private String getLandMultiplierCell() {
        final int applicationCount = calculateApplicationCount();
        return getValueCellAddressWithApplicationsFor(VEROTUSLOHKO_PERMITS_PER_1000_HA, applicationCount).formatAsString();
    }

    private String getShooterMultiplierCell() {
        final int applicationCount = calculateApplicationCount();
        return getValueCellAddressWithApplicationsFor(VEROTUSLOHKO_PERMITS_PER_SHOOTER, applicationCount).formatAsString();
    }

    private String getCellAddressOnSummarySheet(final JyvitysExcelSummaryStaticField summarySheetColumn) {
        final String summarySheetName = row.getSheet().getWorkbook().getSheetAt(0).getSheetName();

        return String.format("'%s'!%s", summarySheetName, summarySheetColumn.getColumn() + rowFormulaNumber);
    }

    private Cell getCell(final JyvitysExcelVerotuslohkoStaticField column) {
        return row.getCell(column.getColumnNumber());
    }

    private String getCellAddress(final JyvitysExcelVerotuslohkoStaticField column) {
        return column.getColumn() + rowFormulaNumber;
    }

    private int calculateApplicationCount() {
        return row.getSheet().getLastRowNum() + 1 - JyvitysExcelVerotuslohkoTemplate.VEROTUSLOHKO_STATIC_CONTENT_HEIGHT_ROWS;
    }
}
