package fi.riista.feature.gamediary.search;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import fi.riista.feature.gamediary.GameDiaryEntryType;
import fi.riista.util.DateUtil;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import javax.validation.constraints.NotNull;
import java.util.EnumSet;

public class GameDiarySearchDTO {
    private final Interval interval;
    private final boolean includeHarvest;
    private final boolean includeObservation;
    private final boolean includeSrva;
    private final boolean onlyReports;
    private final boolean onlyTodo;
    private final boolean reportedForOthers;

    @JsonCreator
    public GameDiarySearchDTO(@JsonProperty("beginDate") @NotNull final LocalDate beginDate,
                              @JsonProperty("endDate") @NotNull final LocalDate endDate,
                              @JsonProperty("includeHarvest") final boolean includeHarvest,
                              @JsonProperty("includeObservation") final boolean includeObservation,
                              @JsonProperty("includeSrva") final boolean includeSrva,
                              @JsonProperty("onlyReports") final boolean onlyReports,
                              @JsonProperty("onlyTodo") final boolean onlyTodo,
                              @JsonProperty("reportedForOthers") final boolean reportedForOthers) {
        this.interval = beginDate.isAfter(endDate)
                ? DateUtil.createDateInterval(endDate, beginDate)
                : DateUtil.createDateInterval(beginDate, endDate);
        this.includeHarvest = includeHarvest;
        this.includeObservation = includeObservation;
        this.includeSrva = includeSrva;
        this.onlyReports = onlyReports;
        this.onlyTodo = onlyTodo;
        this.reportedForOthers = reportedForOthers;
    }

    // For testing
    GameDiarySearchDTO(final Interval interval,
                       final EnumSet<GameDiaryEntryType> entryTypes,
                       final boolean onlyReports,
                       final boolean onlyTodo,
                       final boolean reportedForOthers) {
        this.interval = interval;
        this.includeHarvest = entryTypes.contains(GameDiaryEntryType.HARVEST);
        this.includeObservation = entryTypes.contains(GameDiaryEntryType.OBSERVATION);
        this.includeSrva = entryTypes.contains(GameDiaryEntryType.SRVA);
        this.onlyReports = onlyReports;
        this.onlyTodo = onlyTodo;
        this.reportedForOthers = reportedForOthers;
    }

    public Interval getInterval() {
        return interval;
    }

    public boolean isIncludeHarvest() {
        return includeHarvest;
    }

    public boolean isIncludeObservation() {
        return includeObservation;
    }

    public boolean isIncludeSrva() {
        return includeSrva;
    }

    public boolean isOnlyReports() {
        return onlyReports;
    }

    public boolean isOnlyTodo() {
        return onlyTodo;
    }

    public boolean isReportedForOthers() {
        return reportedForOthers;
    }
}
