package fi.riista.feature.announcement.notification;

import static com.google.firebase.messaging.MessagingErrorCode.INVALID_ARGUMENT;
import static com.google.firebase.messaging.MessagingErrorCode.UNREGISTERED;
import static java.util.Collections.singletonMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import fi.riista.config.jackson.CustomJacksonObjectMapper;
import fi.riista.feature.announcement.show.MobileAnnouncementDTO;
import fi.riista.feature.push.MobileClientDevice;
import fi.riista.feature.push.MobileClientDeviceRepository;
import fi.riista.integration.fcm.FcmMulticastSender;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import javax.annotation.Resource;
import org.joda.time.Days;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnnouncementPushNotificationService {
    private static final Logger LOG = LoggerFactory.getLogger(AnnouncementPushNotificationService.class);

    private static final int MAX_BATCH_SIZE = 500;

    // Maximum notification message payload size is 2kB
    // Maximum data payload size is 4kB
    static final int MAX_BODY_LENGTH = 500;
    static final int MAX_SUBJECT_LENGTH = 200;
    private static int NOTIFICATION_TTL_DAYS = 7;

    private static final String KEY_DATA_ANNOUNCEMENT = "announcement";
    private static final String APNS_EXPIRATION_HEADER = "apns-expiration";
    private static final List<MessagingErrorCode> INVALID_TOKEN_CODES = ImmutableList.of(UNREGISTERED, INVALID_ARGUMENT);

    @Resource
    private MobileClientDeviceRepository mobileClientDeviceRepository;

    @Resource
    private FcmMulticastSender fcmMulticastSender;

    @Resource
    private CustomJacksonObjectMapper objectMapper;

    @Async
    @Transactional(noRollbackFor = RuntimeException.class)
    public void asyncSend(final AnnouncementNotificationDTO dto) {
        if (dto.getTargets().getPushTokens().isEmpty()) {
            return;
        }

        LOG.info("Sending push notification to {} devices", dto.getTargets().getPushTokens().size());

        final AndroidConfig androidConfig = AndroidConfig.builder()
                .setTtl(Days.days(NOTIFICATION_TTL_DAYS).toStandardSeconds().getSeconds())
                .build();

        // Apple push notification service config
        // Expiration shall be presented as 'UNIX epoch expressed in seconds (UTC)'
        final String apnsExpiration = String.valueOf(DateUtil.now().plusDays(NOTIFICATION_TTL_DAYS).getMillis() / 1000);
        final ApnsConfig apnsConfig = ApnsConfig.builder()
                .setAps(Aps.builder().build())
                .putHeader(APNS_EXPIRATION_HEADER, apnsExpiration)
                .build();

        final MobileAnnouncementDTO announcementAbbreviated = dto.getAnnouncement().copyAbbreviated(MAX_SUBJECT_LENGTH, MAX_BODY_LENGTH);
        final Notification notification = Notification.builder()
                .setTitle(announcementAbbreviated.getSubject())
                .setBody(announcementAbbreviated.getBody())
                .build();

        final String dataPayload;
        try {
            dataPayload = objectMapper.writeValueAsString(announcementAbbreviated);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        final Map<String, String> data = singletonMap(KEY_DATA_ANNOUNCEMENT, dataPayload);

        for (final List<String> pushTokenList : Lists.partition(dto.getTargets().getPushTokens(), MAX_BATCH_SIZE)) {
            final MulticastMessage message = MulticastMessage.builder()
                    .setAndroidConfig(androidConfig)
                    .setApnsConfig(apnsConfig)
                    .putAllData(data)
                    .setNotification(notification)
                    .addAllTokens(pushTokenList)
                    .build();
            try {
                fcmMulticastSender.send(message).ifPresent(response -> {
                    LOG.info("{} errors {} success", response.getFailureCount(), response.getSuccessCount());
                    purgeInvalidPushTokens(parseInvalidPushTokens(pushTokenList, response));

                });
            } catch (FirebaseMessagingException e) {
                LOG.warn("Sending FCM message failed.", e);
            }
        }
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
                                                       final BatchResponse response) {
        final List<String> invalidTokenIds = new LinkedList<>();
        final ListIterator<String> requestIterator = pushTokens.listIterator();
        final ListIterator<SendResponse> responseIterator = response.getResponses().listIterator();

        // Response contains response items in same order as requested
        while (requestIterator.hasNext() && responseIterator.hasNext()) {
            final String canonicalId = requestIterator.next();
            final SendResponse resultItem = responseIterator.next();

            if (!resultItem.isSuccessful()) {
                if (INVALID_TOKEN_CODES.contains(resultItem.getException().getMessagingErrorCode())) {
                    invalidTokenIds.add(canonicalId);
                } else if (resultItem.getException().getMessagingErrorCode() != null) {
                    LOG.error("Got unexpected errorCode {}", resultItem.getException().getMessagingErrorCode());
                }
            }
        }

        if (requestIterator.hasNext() || responseIterator.hasNext()) {
            LOG.warn("Mismatch for request and response size {} != {}",
                    response.getResponses().size(), pushTokens.size());
        }

        return invalidTokenIds;
    }
}
