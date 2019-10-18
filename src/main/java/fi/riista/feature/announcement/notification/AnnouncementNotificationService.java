package fi.riista.feature.announcement.notification;

import fi.riista.feature.announcement.Announcement;
import fi.riista.feature.announcement.show.MobileAnnouncementDTO;
import fi.riista.feature.announcement.show.MobileAnnouncementDTOTransformer;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class AnnouncementNotificationService {

    @Resource
    private AnnouncementSubscriberPersonResolver announcementSubscriberPersonResolver;

    @Resource
    private AnnouncementEmailService announcementEmailService;

    @Resource
    private AnnouncementPushNotificationService announcementPushNotificationService;

    @Resource
    private MobileAnnouncementDTOTransformer mobileAnnouncementDTOTransformer;

    @Transactional(noRollbackFor = RuntimeException.class)
    public void sendNotifications(final Announcement announcement,
                                  final boolean sendEmail) {
        final AnnouncementNotificationTargets targets =
                announcementSubscriberPersonResolver.collectTargets(announcement, sendEmail);
        final MobileAnnouncementDTO announcementDTO = mobileAnnouncementDTOTransformer.apply(announcement);
        final AnnouncementNotificationDTO dto = new AnnouncementNotificationDTO(
                announcementDTO, targets, LocaleContextHolder.getLocale());

        announcementEmailService.asyncSend(dto);
        announcementPushNotificationService.asyncSend(dto);
    }

}
