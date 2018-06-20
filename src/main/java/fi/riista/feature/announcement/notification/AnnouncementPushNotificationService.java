package fi.riista.feature.announcement.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.bytefish.fcmjava.client.FcmClient;
import de.bytefish.fcmjava.model.builders.FcmMessageOptionsBuilder;
import de.bytefish.fcmjava.model.enums.ErrorCodeEnum;
import de.bytefish.fcmjava.model.options.FcmMessageOptions;
import de.bytefish.fcmjava.requests.builders.NotificationPayloadBuilder;
import de.bytefish.fcmjava.requests.data.DataMulticastMessage;
import de.bytefish.fcmjava.requests.notification.NotificationPayload;
import de.bytefish.fcmjava.responses.FcmMessageResponse;
import de.bytefish.fcmjava.responses.FcmMessageResultItem;
import fi.riista.config.jackson.CustomJacksonObjectMapper;
import fi.riista.feature.announcement.Announcement;
import fi.riista.feature.announcement.show.MobileAnnouncementDTO;
import fi.riista.feature.announcement.show.MobileAnnouncementDTOTransformer;
import fi.riista.feature.push.MobileClientDevice;
import fi.riista.feature.push.MobileClientDeviceRepository;
import fi.riista.util.F;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

@Service
public class AnnouncementPushNotificationService {
    private static final Logger LOG = LoggerFactory.getLogger(AnnouncementPushNotificationService.class);

    // Maximum notification message payload size is 2kB
    // Maximum data payload size is 4kB
    private static final int MAX_BODY_LENGTH = 500;
    private static final int MAX_SUBJECT_LENGTH = 200;
    private static final String KEY_DATA_ANNOUNCEMENT = "announcement";

    @Resource
    private FcmClient fcmClient;

    @Resource
    private MobileAnnouncementDTOTransformer mobileAnnouncementDTOTransformer;

    @Resource
    private MobileClientDeviceRepository mobileClientDeviceRepository;

    @Resource
    private CustomJacksonObjectMapper objectMapper;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void sendNotification(final Announcement announcement, final List<String> pushTokenIds) {
        final MobileAnnouncementDTO dto = mobileAnnouncementDTOTransformer.apply(announcement);

        // Limit push notification payload size
        dto.setBody(StringUtils.abbreviate(dto.getBody(), MAX_BODY_LENGTH));
        dto.setSubject(StringUtils.abbreviate(dto.getSubject(), MAX_SUBJECT_LENGTH));

        final AnnouncementPushNotificationDTO notificationDTO = new AnnouncementPushNotificationDTO(dto, pushTokenIds);

        notificationDTO.createBatches().forEach(batch -> {
            final FcmMessageResponse response = fcmClient.send(createMulticastMessage(batch));

            LOG.info("{} errors {} success", response.getNumberOfFailure(), response.getNumberOfSuccess());

            purgeInvalidPushTokens(parseInvalidPushTokens(batch.getPushTokenIds(), response));
        });
    }

    private DataMulticastMessage createMulticastMessage(final AnnouncementPushNotificationDTO dto) {
        final FcmMessageOptions messageOptions = new FcmMessageOptionsBuilder()
                .setTimeToLive(Duration.ofDays(7))
                .build();

        final MobileAnnouncementDTO announcement = dto.getAnnouncement();
        final NotificationPayload notificationPayload = new NotificationPayloadBuilder()
                .setTitle(announcement.getSubject())
                .setBody(announcement.getBody())
                .build();

        final String dataPayload;
        try {
            dataPayload = objectMapper.writeValueAsString(announcement);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        final Map<String, String> data = Collections.singletonMap(KEY_DATA_ANNOUNCEMENT, dataPayload);

        return new DataMulticastMessage(messageOptions, dto.getPushTokenIds(), data, notificationPayload);
    }

    private void purgeInvalidPushTokens(final List<String> invalidTokenIds) {
        for (final String invalidTokenId : invalidTokenIds) {
            final MobileClientDevice device = mobileClientDeviceRepository.findByPushToken(invalidTokenId);

            if (device != null) {
                LOG.warn("Removing invalid pushToken personId={} token={}",
                        F.getId(device.getPerson()), invalidTokenId);
                mobileClientDeviceRepository.delete(device);
            } else {
                LOG.error("Could not find device matching pushToken {}", invalidTokenId);
            }
        }

        mobileClientDeviceRepository.flush();
    }

    private static List<String> parseInvalidPushTokens(final List<String> pushTokens,
                                                       final FcmMessageResponse response) {
        final List<String> invalidTokenIds = new LinkedList<>();
        final ListIterator<String> requestIterator = pushTokens.listIterator();
        final ListIterator<FcmMessageResultItem> responseIterator = response.getResults().listIterator();

        // Response contains response items in same order as requested
        while (requestIterator.hasNext() && responseIterator.hasNext()) {
            final String canonicalId = requestIterator.next();
            final FcmMessageResultItem resultItem = responseIterator.next();

            if (resultItem.getErrorCode() == ErrorCodeEnum.NotRegistered ||
                    resultItem.getErrorCode() == ErrorCodeEnum.InvalidRegistration) {
                invalidTokenIds.add(canonicalId);
            } else if (resultItem.getErrorCode() != null) {
                LOG.error("Got unexpected errorCode {}", resultItem.getErrorCode());
            }
        }

        if (requestIterator.hasNext() || responseIterator.hasNext()) {
            LOG.warn("Mismatch for request and response size {} != {}",
                    response.getResults().size(), pushTokens.size());
        }

        return invalidTokenIds;
    }
}
