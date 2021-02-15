package fi.riista.feature.permit.application.lawsectionten.period;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_EUROPEAN_BEAVER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_PARTRIDGE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_RINGED_SEAL;
import static fi.riista.util.DateUtil.DATE_FORMAT_FINNISH;
import static fi.riista.util.DateUtil.today;

public class LawSectionTenPermitApplicationSpeciesPeriodValidator {

    public static LawSectionTenPermitApplicationSpeciesPeriodSeasons getPermitSeasons(final int gameSpeciesCode) {

        final LocalDate today = today();
        final int currentYear = today.getYear();

        final RangeSet<LocalDate> currentSeason = TreeRangeSet.create();
        final RangeSet<LocalDate> nextSeason = TreeRangeSet.create();

        switch (gameSpeciesCode) {
            case OFFICIAL_CODE_EUROPEAN_BEAVER:
                if (today.isBefore(new LocalDate(currentYear, 5, 1))) {
                    currentSeason.add(Range.closed(
                            new LocalDate(currentYear - 1, 8, 20),
                            new LocalDate(currentYear, 4, 30)));
                    nextSeason.add(Range.closed(
                            new LocalDate(currentYear, 8, 20),
                            new LocalDate(currentYear + 1, 4, 30)));
                } else if (today.isBefore(new LocalDate(currentYear, 8, 20))) {
                    nextSeason.add(Range.closed(
                            new LocalDate(currentYear, 8, 20),
                            new LocalDate(currentYear + 1, 4, 30)));
                } else {
                    currentSeason.add(Range.closed(
                            new LocalDate(currentYear, 8, 20),
                            new LocalDate(currentYear + 1, 4, 30)));
                    nextSeason.add(Range.closed(
                            new LocalDate(currentYear + 1, 8, 20),
                            new LocalDate(currentYear + 2, 4, 30)));
                }
                break;
            case OFFICIAL_CODE_RINGED_SEAL:
                if (today.isBefore(new LocalDate(currentYear, 8, 1))) {
                    currentSeason.add(Range.closed(
                            new LocalDate(currentYear - 1, 8, 1),
                            new LocalDate(currentYear - 1, 12, 31)));
                    currentSeason.add(Range.closed(
                            new LocalDate(currentYear, 4, 16),
                            new LocalDate(currentYear, 7, 31)));
                    nextSeason.add(Range.closed(
                            new LocalDate(currentYear, 1, 8),
                            new LocalDate(currentYear, 12, 31)));
                    nextSeason.add(Range.closed(
                            new LocalDate(currentYear + 1, 4, 16),
                            new LocalDate(currentYear + 1, 7, 31)));
                } else {
                    currentSeason.add(Range.closed(
                            new LocalDate(currentYear, 8, 1),
                            new LocalDate(currentYear, 12, 31)));
                    currentSeason.add(Range.closed(
                            new LocalDate(currentYear + 1, 4, 16),
                            new LocalDate(currentYear + 1, 7, 31)));
                    nextSeason.add(Range.closed(
                            new LocalDate(currentYear + 1, 8, 1),
                            new LocalDate(currentYear + 1, 12, 31)));
                    nextSeason.add(Range.closed(
                            new LocalDate(currentYear + 2, 4, 16),
                            new LocalDate(currentYear + 2, 7, 31)));
                }
                break;
            case OFFICIAL_CODE_PARTRIDGE:
                if (today.isBefore(new LocalDate(currentYear, 9, 1))) {
                    nextSeason.add(Range.closed(
                            new LocalDate(currentYear, 9, 1),
                            new LocalDate(currentYear, 12, 31)));
                } else {
                    currentSeason.add(Range.closed(
                            new LocalDate(currentYear, 9, 1),
                            new LocalDate(currentYear, 12, 31)));
                    nextSeason.add(Range.closed(
                            new LocalDate(currentYear + 1, 9, 1),
                            new LocalDate(currentYear + 1, 12, 31)));
                }
                break;
            default:
                throw new IllegalArgumentException("Incorrect species for law section 10 period");
        }

        return new LawSectionTenPermitApplicationSpeciesPeriodSeasons(currentSeason, nextSeason);

    }

    public static void validatePeriod(final int gameSpeciesCode,
                                      final LocalDate beginDate,
                                      final LocalDate endDate) {

        if (beginDate == null || endDate == null || beginDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Illegal period");
        }

        final LawSectionTenPermitApplicationSpeciesPeriodSeasons seasons = getPermitSeasons(gameSpeciesCode);

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
