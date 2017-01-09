package fi.riista.feature.announcement;

import fi.riista.feature.common.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;

public interface AnnouncementSubscriberRepository extends BaseRepository<AnnouncementSubscriber, Long> {
    List<AnnouncementSubscriber> findByAnnouncement(Announcement announcement);

    @Modifying
    void deleteByAnnouncement(Announcement announcement);
}
