package fi.riista.feature.announcement.show;

import javax.annotation.Nonnull;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class MobileAnnouncementSenderDTO {
    private final Map<String, String> organisation;
    private final Map<String, String> title;
    private final String fullName;

    public MobileAnnouncementSenderDTO(final @Nonnull Map<String, String> organisation,
                                       final @Nonnull Map<String, String> title,
                                       final @Nonnull String fullName) {
        this.organisation = requireNonNull(organisation);
        this.title = requireNonNull(title);
        this.fullName = requireNonNull(fullName);
    }

    public String getFullName() {
        return fullName;
    }

    public Map<String, String> getTitle() {
        return title;
    }

    public Map<String, String> getOrganisation() {
        return organisation;
    }
}
