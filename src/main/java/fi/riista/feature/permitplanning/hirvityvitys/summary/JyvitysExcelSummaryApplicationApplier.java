package fi.riista.feature.permitplanning.hirvityvitys.summary;

import fi.riista.feature.permitplanning.hirvityvitys.dto.JyvitysExcelApplicationDTO;
import fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.Iterator;
import java.util.stream.Collectors;

import static fi.riista.feature.permitplanning.hirvityvitys.summary.JyvitysExcelSummaryStaticField.SUMMARY_APPLICANT;
import static fi.riista.feature.permitplanning.hirvityvitys.summary.JyvitysExcelSummaryStaticField.SUMMARY_APPLICATION_AMOUNT;
import static fi.riista.feature.permitplanning.hirvityvitys.summary.JyvitysExcelSummaryStaticField.SUMMARY_APPLICATION_NUMBER;
import static fi.riista.feature.permitplanning.hirvityvitys.summary.JyvitysExcelSummaryStaticField.SUMMARY_AREA_IN_OTHER_RHY;
import static fi.riista.feature.permitplanning.hirvityvitys.summary.JyvitysExcelSummaryStaticField.SUMMARY_PRIVATE_LAND;
import static fi.riista.feature.permitplanning.hirvityvitys.summary.JyvitysExcelSummaryStaticField.SUMMARY_SHOOTERS_ONLY_CLUB;
import static fi.riista.feature.permitplanning.hirvityvitys.summary.JyvitysExcelSummaryStaticField.SUMMARY_SHOOTERS_OTHER_CLUB_PASSIVE;
import static fi.riista.feature.permitplanning.hirvityvitys.summary.JyvitysExcelSummaryStaticField.SUMMARY_SHOOTERS_TOTAL;
import static fi.riista.feature.permitplanning.hirvityvitys.summary.JyvitysExcelSummaryStaticField.SUMMARY_SHOOTERS_TOTAL_PER_STATE_LAND;
import static fi.riista.feature.permitplanning.hirvityvitys.summary.JyvitysExcelSummaryStaticField.SUMMARY_STATE_LAND;
import static fi.riista.feature.permitplanning.hirvityvitys.summary.JyvitysExcelSummaryStaticField.SUMMARY_SUGGESTION_ADULT;
import static fi.riista.feature.permitplanning.hirvityvitys.summary.JyvitysExcelSummaryStaticField.SUMMARY_SUGGESTION_CALF;
import static fi.riista.feature.permitplanning.hirvityvitys.summary.JyvitysExcelSummaryStaticField.SUMMARY_SUGGESTION_TOTAL;
import static fi.riista.feature.permitplanning.hirvityvitys.summary.JyvitysExcelSummaryStaticField.SUMMARY_TOTAL_LAND;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_PRIVATE_LAND;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_SHOOTERS_TOTAL_PER_STATE_LAND;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_STATE_LAND;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_SUGGESTION_ADULT;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_SUGGESTION_TOTAL;

/**
 * Class for applying application data for single application on summary sheet. Static fields must be applied for all
 * sheets before applying application data
 */
public class JyvitysExcelSummaryApplicationApplier {

    private final JyvitysExcelApplicationDTO applicationDTO;
    private final Row row;
    private final String rowFormulaNumber;

    public static void apply(final JyvitysExcelApplicationDTO applicationDTO,
                             final Row row) {
        new JyvitysExcelSummaryApplicationApplier(applicationDTO, row).applyDto();

    }

    private JyvitysExcelSummaryApplicationApplier(final JyvitysExcelApplicationDTO applicationDTO,
                                                  final Row row) {
        this.applicationDTO = applicationDTO;
        this.row = row;
        this.rowFormulaNumber = String.valueOf(row.getRowNum() + 1); // Zero-based
    }

    private void applyDto() {
        getCell(SUMMARY_APPLICATION_NUMBER).setCellValue(applicationDTO.getApplicationNumber());
        getCell(SUMMARY_APPLICANT).setCellValue(applicationDTO.getApplicant());
        final String formula = sumOfVerotusLohkoSheetsColumn(VEROTUSLOHKO_PRIVATE_LAND);
        getCell(SUMMARY_PRIVATE_LAND).setCellFormula(formula);
        getCell(SUMMARY_STATE_LAND).setCellFormula(sumOfVerotusLohkoSheetsColumn(VEROTUSLOHKO_STATE_LAND));
        getCell(SUMMARY_TOTAL_LAND).setCellFormula(getTotalLandFormula());
        getCell(SUMMARY_SHOOTERS_ONLY_CLUB).setCellValue(applicationDTO.getShooterOnlyClub());
        getCell(SUMMARY_SHOOTERS_OTHER_CLUB_PASSIVE).setCellValue(applicationDTO.getShooterOtherClubPassive());
        getCell(SUMMARY_SHOOTERS_TOTAL).setCellFormula(getTotalShootersFormula());
        getCell(SUMMARY_SHOOTERS_TOTAL_PER_STATE_LAND).setCellFormula(sumOfVerotusLohkoSheetsColumn(VEROTUSLOHKO_SHOOTERS_TOTAL_PER_STATE_LAND));
        getCell(SUMMARY_APPLICATION_AMOUNT).setCellValue(applicationDTO.getAppliedAmount());
        getCell(SUMMARY_SUGGESTION_TOTAL).setCellFormula(suggestionTotalFormula());
        getCell(SUMMARY_SUGGESTION_ADULT).setCellFormula(sumOfVerotusLohkoSheetsColumn(VEROTUSLOHKO_SUGGESTION_ADULT));
        getCell(SUMMARY_SUGGESTION_CALF).setCellFormula(getSuggestionCalfFormula());
        getCell(SUMMARY_AREA_IN_OTHER_RHY).setCellValue(applicationDTO.getOtherRhysInArea().stream().collect(Collectors.joining("\n")));
    }

    private String getSuggestionCalfFormula() {
        return String.format("(%s - %s) * 2", getCellAddress(SUMMARY_SUGGESTION_TOTAL), getCellAddress(SUMMARY_SUGGESTION_ADULT));
    }

    private String getTotalShootersFormula() {
        return String.format("%s + %s",
                SUMMARY_SHOOTERS_ONLY_CLUB.getColumn() + rowFormulaNumber,
                SUMMARY_SHOOTERS_OTHER_CLUB_PASSIVE.getColumn() + rowFormulaNumber);
    }

    private String getTotalLandFormula() {
        return String.format("%s + %s",
                SUMMARY_PRIVATE_LAND.getColumn() + rowFormulaNumber,
                SUMMARY_STATE_LAND.getColumn() + rowFormulaNumber);
    }

    private String suggestionTotalFormula() {
        return String.format("ROUND( %s , 0)", sumOfVerotusLohkoSheetsColumn(VEROTUSLOHKO_SUGGESTION_TOTAL));
    }

    private String sumOfVerotusLohkoSheetsColumn(final JyvitysExcelVerotuslohkoStaticField verotuslohkoField) {
        final StringBuilder builder = new StringBuilder();
        final Iterator<Sheet> sheetIterator = row.getSheet().getWorkbook().sheetIterator();
        sheetIterator.next(); // Skip summary
        while (sheetIterator.hasNext()) {
            final Sheet lohkoSheet = sheetIterator.next();
            builder.append(String.format("'%s'!%s", lohkoSheet.getSheetName(), verotuslohkoField.getColumn() + rowFormulaNumber));
            if (sheetIterator.hasNext()) {
                builder.append(" + ");
            }
        }
        return builder.toString();
    }

    private Cell getCell(final JyvitysExcelSummaryStaticField column) {
        return row.getCell(column.getColumnNumber());
    }

    private String getCellAddress(final JyvitysExcelSummaryStaticField column) {
        return getCell(column).getAddress().formatAsString();
    }
}
