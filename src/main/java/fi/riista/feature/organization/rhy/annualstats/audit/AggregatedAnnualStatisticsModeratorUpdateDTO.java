package fi.riista.feature.organization.rhy.annualstats.audit;

import fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import java.util.SortedMap;

import static java.util.Objects.requireNonNull;

public class AggregatedAnnualStatisticsModeratorUpdateDTO {

    private final long rhyId;
    private final int year;
    private final SortedMap<AnnualStatisticGroup, DateTime> dataGroups;

    public AggregatedAnnualStatisticsModeratorUpdateDTO(final Long rhyId,
                                                        final Integer year,
                                                        @Nonnull final SortedMap<AnnualStatisticGroup, DateTime> dataGroups) {

        this.rhyId = requireNonNull(rhyId, "rhyId is null");
        this.year = requireNonNull(year, "year is null");
        this.dataGroups = requireNonNull(dataGroups, "dataGroups is null");
    }

    public long getRhyId() {
        return rhyId;
    }

    public int getYear() {
        return year;
    }

    public SortedMap<AnnualStatisticGroup, DateTime> getDataGroups() {
        return dataGroups;
    }
}
