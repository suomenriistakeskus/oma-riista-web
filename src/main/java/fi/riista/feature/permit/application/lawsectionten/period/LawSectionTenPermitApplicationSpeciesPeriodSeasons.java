package fi.riista.feature.permit.application.lawsectionten.period;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import org.joda.time.LocalDate;

import java.util.Objects;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_EUROPEAN_BEAVER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_PARTRIDGE;
import static fi.riista.util.DateUtil.today;

public class LawSectionTenPermitApplicationSpeciesPeriodSeasons {

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
                return null;
        }

        return new LawSectionTenPermitApplicationSpeciesPeriodSeasons(currentSeason, nextSeason);

    }

    final private RangeSet<LocalDate> currentSeason;
    final private RangeSet<LocalDate> nextSeason;

    public LawSectionTenPermitApplicationSpeciesPeriodSeasons(final RangeSet<LocalDate> currentSeason,
                                                              final RangeSet<LocalDate> nextSeason) {
        Objects.requireNonNull(currentSeason);
        Objects.requireNonNull(nextSeason);

        this.currentSeason = currentSeason;
        this.nextSeason = nextSeason;
    }

    public boolean contains(final LocalDate beginDate, final LocalDate endDate) {
        return (currentSeason.contains(beginDate) && currentSeason.contains(endDate)) ||
                (nextSeason.contains(beginDate) && nextSeason.contains(endDate));
    }

    public RangeSet<LocalDate> getCurrentSeason() {
        return currentSeason;
    }

    public RangeSet<LocalDate> getNextSeason() {
        return nextSeason;
    }
}
