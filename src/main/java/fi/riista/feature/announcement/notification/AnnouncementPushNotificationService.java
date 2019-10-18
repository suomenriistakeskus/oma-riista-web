package fi.riista.feature.announcement.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
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
import fi.riista.feature.push.MobileClientDevice;
import fi.riista.feature.push.MobileClientDeviceRepository;
import fi.riista.util.F;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.apache.commons.lang.StringUtils.abbreviate;

@Service
public class AnnouncementPushNotificationService {
    private static final Logger LOG = LoggerFactory.getLogger(AnnouncementPushNotificationService.class);

    private static final int MAX_BATCH_SIZE = 1000;

    // Maximum notification message payload size is 2kB
    // Maximum data payload size is 4kB
    private static final int MAX_BODY_LENGTH = 500;
    private static final int MAX_SUBJECT_LENGTH = 200;

    private static final String KEY_DATA_ANNOUNCEMENT = "announcement";

    @Resource
    private FcmClient fcmClient;

    @Resource
    private MobileClientDeviceRepository mobileClientDeviceRepository;

    @Resource
    private CustomJacksonObjectMapper objectMapper;

    @Async
    @Transactional(noRollbackFor = RuntimeException.class)
    public void asyncSend(final AnnouncementNotificationDTO dto) {
        if (dto.getTargets().getPushTokens().isEmpty()) {
            return;
        }

        LOG.info("Sending push notification to {} devices", dto.getTargets().getPushTokens().size());

        final FcmMessageOptions messageOptions = new FcmMessageOptionsBuilder()
                .setTimeToLive(Duration.ofDays(7))
                .build();

        final NotificationPayload notificationPayload = new NotificationPayloadBuilder()
                .setTitle(abbreviate(dto.getAnnouncement().getSubject(), MAX_SUBJECT_LENGTH))
                .setBody(abbreviate(dto.getAnnouncement().getBody(), MAX_BODY_LENGTH))
                .build();

        final String dataPayload;
        try {
            dataPayload = objectMapper.writeValueAsString(dto.getAnnouncement());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        final Map<String, String> data = singletonMap(KEY_DATA_ANNOUNCEMENT, dataPayload);

        for (final List<String> pushTokenList : Lists.partition(dto.getTargets().getPushTokens(), MAX_BATCH_SIZE)) {
            final DataMulticastMessage request = new DataMulticastMessage(
                    messageOptions, pushTokenList, data, notificationPayload);

            final FcmMessageResponse response = fcmClient.send(request);

            LOG.info("{} errors {} success", response.getNumberOfFailure(), response.getNumberOfSuccess());

            purgeInvalidPushTokens(parseInvalidPushTokens(pushTokenList, response));
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
