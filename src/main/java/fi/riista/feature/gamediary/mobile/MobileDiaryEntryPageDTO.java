package fi.riista.feature.gamediary.mobile;

import fi.riista.feature.gamediary.GameDiaryEntryDTO;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class MobileDiaryEntryPageDTO<TYPE extends GameDiaryEntryDTO> {

    private final List<TYPE> content;

    // Newest modification time in content list, null if list is empty
    private final LocalDateTime latestEntry;

    private final boolean hasMore;

    public MobileDiaryEntryPageDTO(@Nonnull final List<TYPE> content,
                                   @Nullable final LocalDateTime latestEntry,
                                   final boolean hasMore) {
        this.content = content;
        this.latestEntry = latestEntry;
        this.hasMore = hasMore;
    }

    public List<TYPE> getContent() {
        return content;
    }

    public LocalDateTime getLatestEntry() {
        return latestEntry;
    }

    public boolean isHasMore() {
        return hasMore;
    }
}
