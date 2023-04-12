package fi.riista.feature.gamediary.mobile;

import com.google.common.collect.ImmutableList;
import org.joda.time.LocalDateTime;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.function.Function.identity;

public class MobileDeletedDiaryEntriesDTO {


    // Newest modification time in content list, null if list is empty
    private final LocalDateTime latestEntry;

    private final List<Long> entryIds;

    public MobileDeletedDiaryEntriesDTO(final LocalDateTime latestEntry, final List<Long> entryIds) {
        this.latestEntry = latestEntry;
        this.entryIds = entryIds;
    }

    public MobileDeletedDiaryEntriesDTO combine(final MobileDeletedDiaryEntriesDTO other) {
        final LocalDateTime timeStamp = Stream.of(this.latestEntry, other.latestEntry)
                .filter(Objects::nonNull)
                .max(Comparator.comparing(identity()))
                .orElse(null);

        final ImmutableList<Long> ids = ImmutableList.<Long>builder()
                .addAll(this.entryIds)
                .addAll(other.getEntryIds())
                .build();

        return new MobileDeletedDiaryEntriesDTO(timeStamp, ids);
    }

    public LocalDateTime getLatestEntry() {
        return latestEntry;
    }

    public List<Long> getEntryIds() {
        return entryIds;
    }
}
