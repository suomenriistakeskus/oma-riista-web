package fi.riista.feature.permitplanning.hirvityvitys.verotuslohko;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import fi.riista.feature.permitplanning.hirvityvitys.JyvitysExcelCellFormula;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fi.riista.feature.permitplanning.hirvityvitys.summary.JyvitysExcelSummaryTemplate.APPLICATION_START_ROW;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_APPLICATION_AMOUNT;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_AREA_FOR_CALCULATION_PRIVATE;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_AREA_FOR_CALCULATION_STATE;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_AREA_FOR_CALCULATION_TOTAL;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_AREA_STATE;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_PERMITS_PER_1000_HA;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_PERMITS_PER_SHOOTER;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_PRIVATE_LAND;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_QUOTA_BY_LAND;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_QUOTA_BY_SHOOTERS;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_QUOTA_TO_ALLOCATE;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_SHOOTERS_ONLY_CLUB;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_SHOOTERS_OTHER_CLUB_PASSIVE;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_SHOOTERS_TOTAL;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_SHOOTERS_TOTAL_PER_STATE_LAND;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_STATE_LAND;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_SUGGESTION_ADULT;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_SUGGESTION_TOTAL;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoStaticField.VEROTUSLOHKO_TOTAL_LAND;
import static fi.riista.feature.permitplanning.hirvityvitys.verotuslohko.JyvitysExcelVerotuslohkoValueCellAddress.getValueCellAddressWithApplicationsFor;

/**
 * Class gathering non-application spesific formulas in a verotuslohko sheet.
 */
public class JyvitysExcelVerotuslohkoFormulas {

    public static List<JyvitysExcelCellFormula> getVerotuslohkoFormulas(final int applicationCount) {
        return Streams.concat(
                getCommonFormulas(applicationCount).stream(),
                createVerotusLohkoFormulas(applicationCount))
                .collect(Collectors.toList());
    }

    private static final ImmutableList<JyvitysExcelVerotuslohkoStaticField> SUM_FIELDS =
            ImmutableList.of(
                    VEROTUSLOHKO_PRIVATE_LAND,
                    VEROTUSLOHKO_STATE_LAND,
                    VEROTUSLOHKO_TOTAL_LAND,
                    VEROTUSLOHKO_SHOOTERS_ONLY_CLUB,
                    VEROTUSLOHKO_SHOOTERS_OTHER_CLUB_PASSIVE,
                    VEROTUSLOHKO_SHOOTERS_TOTAL,
                    VEROTUSLOHKO_SHOOTERS_TOTAL_PER_STATE_LAND,
                    VEROTUSLOHKO_APPLICATION_AMOUNT,
                    VEROTUSLOHKO_SUGGESTION_TOTAL,
                    VEROTUSLOHKO_SUGGESTION_ADULT
            );

    private static Stream<JyvitysExcelCellFormula> createVerotusLohkoFormulas(final int applicationCount) {
        return Stream.of(
                formula(applicationCount, VEROTUSLOHKO_QUOTA_BY_LAND, getQuotaByLandFormula(applicationCount)),
                formula(applicationCount, VEROTUSLOHKO_QUOTA_BY_SHOOTERS, getQuotaByShootersFormula(applicationCount)),
                formula(applicationCount, VEROTUSLOHKO_PERMITS_PER_1000_HA, getPermitsByLandMultiplierFormula(applicationCount)),
                formula(applicationCount, VEROTUSLOHKO_PERMITS_PER_SHOOTER, getPermitsByShootersMultiplierFormula(applicationCount)),
                formula(applicationCount, VEROTUSLOHKO_AREA_FOR_CALCULATION_TOTAL, getAreaForPermitsTotalFormula(applicationCount)),
                formula(applicationCount, VEROTUSLOHKO_AREA_FOR_CALCULATION_STATE, getStateAreaFormula(applicationCount)),
                formula(applicationCount, VEROTUSLOHKO_AREA_FOR_CALCULATION_PRIVATE, getPrivateLandFormula(applicationCount))

        );
    }

    private static JyvitysExcelCellFormula formula(int applicationCount, JyvitysExcelVerotuslohkoStaticField cell, String formula) {
        return JyvitysExcelCellFormula.of(getValueCellAddressWithApplicationsFor(cell, applicationCount).formatAsString(), formula);
    }

    // Pyyntilupien perusteena laskettava pinta-ala (valtion maat ja yksityismaat ha:
    private static String getAreaForPermitsTotalFormula(final int applicationCount) {
        return String.format("%s + %s",
                getValueCellAddressWithApplicationsFor(VEROTUSLOHKO_AREA_FOR_CALCULATION_STATE, applicationCount),
                getValueCellAddressWithApplicationsFor(VEROTUSLOHKO_AREA_FOR_CALCULATION_PRIVATE, applicationCount));
    }

    // Valtion maiden pinta-ala (ha)(kaikki alueet, myös vuokratut lasketaan mukaan valtionmaihin):
    private static String getStateAreaFormula(final int applicationCount) {
        return getValueCellAddressWithApplicationsFor(VEROTUSLOHKO_AREA_STATE, applicationCount).formatAsString();
    }

    //Tuottolaskelmassa mukana yksityismaiden pinta-ala (ha):
    private static String getPrivateLandFormula(final int applicationCount) {
        return VEROTUSLOHKO_PRIVATE_LAND.getColumn() + getApplicationsTotalRow(applicationCount);
    }

    // koko metsästysalue lupaa / 1000 hehtaaria.
    private static String getPermitsByLandMultiplierFormula(final int applicationCount) {
        return String.format("%s / %s * 1000",
                getValueCellAddressWithApplicationsFor(VEROTUSLOHKO_QUOTA_TO_ALLOCATE, applicationCount),
                getValueCellAddressWithApplicationsFor(VEROTUSLOHKO_AREA_FOR_CALCULATION_TOTAL, applicationCount));
    }

    // lupaa / ampuja.
    private static String getPermitsByShootersMultiplierFormula(final int applicationCount) {
        final String totalShootersCell =
                VEROTUSLOHKO_SHOOTERS_TOTAL_PER_STATE_LAND.getColumn() +
                        getApplicationsTotalRow(applicationCount);
        final String formula = String.format("%s / %s",
                getValueCellAddressWithApplicationsFor(VEROTUSLOHKO_QUOTA_BY_SHOOTERS, applicationCount),
                totalShootersCell);
        return String.format("IF( %s > 0, %s, 0)",
                totalShootersCell,
                formula);
    }

    // Ampujilla jaettava osuus on.
    private static String getQuotaByShootersFormula(final int applicationCount) {
        return String.format("%s - %s",
                getValueCellAddressWithApplicationsFor(VEROTUSLOHKO_QUOTA_TO_ALLOCATE, applicationCount),
                getValueCellAddressWithApplicationsFor(VEROTUSLOHKO_QUOTA_BY_LAND, applicationCount));
    }

    // Vuokramaiden osuus on
    private static String getQuotaByLandFormula(final int applicationCount) {
        return String.format("%s * %s / 1000",
                getValueCellAddressWithApplicationsFor(VEROTUSLOHKO_PERMITS_PER_1000_HA, applicationCount),
                VEROTUSLOHKO_PRIVATE_LAND.getColumn() + getApplicationsTotalRow(applicationCount));
    }

    private static List<JyvitysExcelCellFormula> getCommonFormulas(final int applicationCount) {
        final ImmutableList.Builder<JyvitysExcelCellFormula> builder = ImmutableList.builder();

        SUM_FIELDS.forEach(column -> builder.add(createSumFormula(applicationCount, column)));
        return builder.build();
    }

    private static JyvitysExcelCellFormula createSumFormula(final int applicationCount, final JyvitysExcelVerotuslohkoStaticField column) {
        final int startRow = APPLICATION_START_ROW + 1;
        final int endRow = startRow + applicationCount - 1;
        final int cellRow = getApplicationsTotalRow(applicationCount);
        final String formula = String.format("SUM(%s%d:%s%d)", column.getColumn(), startRow, column.getColumn(), endRow);
        return JyvitysExcelCellFormula.of(column.getColumn() + cellRow, formula);
    }

    private static int getApplicationsTotalRow(final int applicationCount) {
        return APPLICATION_START_ROW + 1 + applicationCount;
    }
}
