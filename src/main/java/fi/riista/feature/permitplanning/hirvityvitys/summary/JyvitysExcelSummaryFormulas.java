package fi.riista.feature.permitplanning.hirvityvitys.summary;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import fi.riista.feature.permitplanning.hirvityvitys.JyvitysExcelCellFormula;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fi.riista.feature.permitplanning.hirvityvitys.summary.JyvitysExcelSummaryStaticField.SUMMARY_APPLICATION_AMOUNT;
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
import static fi.riista.feature.permitplanning.hirvityvitys.summary.JyvitysExcelSummaryTemplate.APPLICATION_START_ROW;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_QUOTA_TO_ALLOCATE;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoValueCellAddress.getValueCellAddressWithApplicationsFor;

/**
 * Class gathering non-application spesific formulas in the summary sheet.
 */
public class JyvitysExcelSummaryFormulas {


    private static final String TOTAL_QUOTA_VALUE_CELL = "H2";

    private static final ImmutableList<JyvitysExcelSummaryStaticField> SUM_FIELDS =
            ImmutableList.of(
                    SUMMARY_PRIVATE_LAND,
                    SUMMARY_STATE_LAND,
                    SUMMARY_TOTAL_LAND,
                    SUMMARY_SHOOTERS_ONLY_CLUB,
                    SUMMARY_SHOOTERS_OTHER_CLUB_PASSIVE,
                    SUMMARY_SHOOTERS_TOTAL,
                    SUMMARY_SHOOTERS_TOTAL_PER_STATE_LAND,
                    SUMMARY_APPLICATION_AMOUNT,
                    SUMMARY_SUGGESTION_TOTAL,
                    SUMMARY_SUGGESTION_ADULT,
                    SUMMARY_SUGGESTION_CALF
            );

    public static List<JyvitysExcelCellFormula> getSummaryFormulas(final Workbook workbook, final int applicationCount) {
        return Streams.concat(
                getCommonFormulas(applicationCount).stream(),
                Stream.of(createTotalQuotaFormula(workbook, applicationCount)))
                .collect(Collectors.toList());

    }

    private static List<JyvitysExcelCellFormula> getCommonFormulas(final int applicationCount) {
        final ImmutableList.Builder<JyvitysExcelCellFormula> builder = ImmutableList.builder();

        SUM_FIELDS.forEach(column -> builder.add(createSumFormula(applicationCount, column)));
        return builder.build();
    }

    private static JyvitysExcelCellFormula createTotalQuotaFormula(final Workbook workbook, final int applicationCount) {
        final StringBuilder totalQuotaFormula = new StringBuilder();
        final Iterator<Sheet> sheetIterator = workbook.sheetIterator();
        sheetIterator.next(); // Skip summary
        while (sheetIterator.hasNext()) {
            final Sheet lohko = sheetIterator.next();
            totalQuotaFormula.append(
                    String.format("'%s'!%s",
                            lohko.getSheetName(),
                            getValueCellAddressWithApplicationsFor(VEROTUSLOHKO_QUOTA_TO_ALLOCATE, applicationCount).formatAsString()));
            if (sheetIterator.hasNext()) {
                totalQuotaFormula.append(" + ");
            }
        }
        return JyvitysExcelCellFormula.of(TOTAL_QUOTA_VALUE_CELL, totalQuotaFormula.toString());
    }

    private static JyvitysExcelCellFormula createSumFormula(final int applicationCount, final JyvitysExcelSummaryStaticField column) {
        final int startRow = APPLICATION_START_ROW + 1;
        final int endRow = startRow + applicationCount - 1;
        final int cellRow = endRow + 1;
        final String formula = String.format("SUM(%s%d:%s%d)", column.getColumn(), startRow, column.getColumn(), endRow);
        return JyvitysExcelCellFormula.of(column.getColumn() + cellRow, formula);
    }
}
