package fi.riista.feature.permit.application.lawsectionten.period;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import static fi.riista.feature.permit.application.lawsectionten.period.LawSectionTenPermitApplicationSpeciesPeriodSeasons.getPermitSeasons;
import static fi.riista.util.DateUtil.DATE_FORMAT_FINNISH;

public class LawSectionTenPermitApplicationSpeciesPeriodValidator {

    public static void validatePeriod(final int gameSpeciesCode,
                                      final LocalDate beginDate,
                                      final LocalDate endDate) {

        if (beginDate == null || endDate == null || beginDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Illegal period");
        }

        final LawSectionTenPermitApplicationSpeciesPeriodSeasons seasons = getPermitSeasons(gameSpeciesCode);
        if (seasons == null) {
            throw new IllegalArgumentException("Incorrect species for law section 10 period");
        }

        if (!seasons.contains(beginDate, endDate)) {
            failValidation(gameSpeciesCode, beginDate, endDate);
        }
    }

    private static void failValidation(final int gameSpeciesCode, final LocalDate beginDate, final LocalDate endDate) {
        final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATE_FORMAT_FINNISH);
        throw new IllegalArgumentException("Incorrect period for law section 10 species: " +
                gameSpeciesCode +
                ", application period: " +
                dateTimeFormatter.print(beginDate) +
                " - " +
                dateTimeFormatter.print(endDate));
    }
}
