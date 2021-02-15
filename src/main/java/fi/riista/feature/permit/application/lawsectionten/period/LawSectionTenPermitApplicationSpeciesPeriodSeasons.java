package fi.riista.feature.permit.application.lawsectionten.period;

import com.google.common.collect.RangeSet;
import org.joda.time.LocalDate;

import java.util.Objects;

public class LawSectionTenPermitApplicationSpeciesPeriodSeasons {

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
