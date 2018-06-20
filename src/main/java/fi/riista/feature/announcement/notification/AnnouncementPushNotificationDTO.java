package fi.riista.feature.announcement.notification;

import com.google.common.collect.Lists;
import fi.riista.feature.announcement.show.MobileAnnouncementDTO;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AnnouncementPushNotificationDTO {
    private static final int MAX_BATCH_SIZE = 1000;

    private final MobileAnnouncementDTO announcement;
    private final List<String> pushTokenIds;

    public AnnouncementPushNotificationDTO(final MobileAnnouncementDTO announcement, final List<String> pushTokenIds) {
        this.announcement = Objects.requireNonNull(announcement);
        this.pushTokenIds = Objects.requireNonNull(pushTokenIds);
    }

    public List<AnnouncementPushNotificationDTO> createBatches() {
        return Lists.partition(pushTokenIds, MAX_BATCH_SIZE).stream()
                .map(subList -> new AnnouncementPushNotificationDTO(this.announcement, subList))
                .collect(Collectors.toList());
    }

    public MobileAnnouncementDTO getAnnouncement() {
        return announcement;
    }

    public List<String> getPushTokenIds() {
        return pushTokenIds;
    }
}
