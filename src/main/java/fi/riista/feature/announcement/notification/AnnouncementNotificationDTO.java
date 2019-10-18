package fi.riista.feature.announcement.notification;

import fi.riista.feature.announcement.show.MobileAnnouncementDTO;

import javax.annotation.Nonnull;
import java.util.Locale;

import static java.util.Objects.requireNonNull;

public class AnnouncementNotificationDTO {
    private final MobileAnnouncementDTO announcement;
    private final AnnouncementNotificationTargets targets;
    private final Locale locale;

    public AnnouncementNotificationDTO(final @Nonnull MobileAnnouncementDTO announcement,
                                       final @Nonnull AnnouncementNotificationTargets targets,
                                       final @Nonnull Locale locale) {
        this.announcement = requireNonNull(announcement);
        this.targets = requireNonNull(targets);
        this.locale = requireNonNull(locale);
    }

    public MobileAnnouncementDTO getAnnouncement() {
        return announcement;
    }

    public AnnouncementNotificationTargets getTargets() {
        return targets;
    }

    public Locale getLocale() {
        return locale;
    }
}
