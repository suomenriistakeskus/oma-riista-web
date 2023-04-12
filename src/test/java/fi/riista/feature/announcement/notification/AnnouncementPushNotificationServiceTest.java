package fi.riista.feature.announcement.notification;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import fi.riista.config.jackson.CustomJacksonObjectMapper;
import fi.riista.feature.announcement.show.MobileAnnouncementDTO;
import fi.riista.feature.announcement.show.MobileAnnouncementSenderDTO;
import fi.riista.feature.push.MobileClientDeviceRepository;
import fi.riista.integration.fcm.FcmMulticastSender;
import fi.riista.util.Locales;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;
import liquibase.repackaged.org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AnnouncementPushNotificationServiceTest extends TestCase {

    @InjectMocks
    private AnnouncementPushNotificationService announcementPushNotificationService;
    @Mock
    private MobileClientDeviceRepository mobileClientDeviceRepositoryMock;
    @Mock
    private FcmMulticastSender fcmMulticastSenderMock;
    @Mock
    private CustomJacksonObjectMapper objectMapperMock;

    private final CustomJacksonObjectMapper objectMapper = new CustomJacksonObjectMapper(true);

    @Captor
    private ArgumentCaptor<MulticastMessage> multicastMessageCaptor;

    @Before
    public void setup() throws JsonProcessingException {
        when(objectMapperMock.writeValueAsString(any())).thenAnswer(invocation -> objectMapper.writeValueAsString(invocation.getArguments()[0]));
    }

    @Test
    public void testTextTruncated() throws FirebaseMessagingException, JsonProcessingException {
        final AnnouncementNotificationDTO dto = createDTO(
                AnnouncementPushNotificationService.MAX_SUBJECT_LENGTH + 1,
                AnnouncementPushNotificationService.MAX_BODY_LENGTH + 1);

        announcementPushNotificationService.asyncSend(dto);

        verify(fcmMulticastSenderMock).send(multicastMessageCaptor.capture());

        final String messageAsString = objectMapper.writeValueAsString(multicastMessageCaptor.getValue());
        final JsonNode multicastMessage = objectMapper.readTree(messageAsString);

        final JsonNode notification = multicastMessage.get("notification");
        final String title = notification.get("title").asText();
        final String body = notification.get("body").asText();

        final JsonNode announcementJson = objectMapper.readTree(multicastMessage.get("data").get("announcement").asText());
        final boolean abbreviated = announcementJson.get("abbreviated").asBoolean();
        final String announcementSubject = announcementJson.get("subject").asText();
        final String announcementBody = announcementJson.get("body").asText();

        assertThat(title.length(), is(equalTo((AnnouncementPushNotificationService.MAX_SUBJECT_LENGTH))));
        assertThat(body.length(), is(equalTo((AnnouncementPushNotificationService.MAX_BODY_LENGTH))));

        assertThat(abbreviated, is(true));
        assertThat(announcementSubject.length(), is(equalTo((AnnouncementPushNotificationService.MAX_SUBJECT_LENGTH))));
        assertThat(announcementBody.length(), is(equalTo((AnnouncementPushNotificationService.MAX_BODY_LENGTH))));
    }

    private static AnnouncementNotificationDTO createDTO(final int subjectLength, final int bodyLength) {
        final MobileAnnouncementSenderDTO sender = new MobileAnnouncementSenderDTO(
                Map.of("aa", "bb"), Map.of("cc", "dd"), "lähettäjä");
        final MobileAnnouncementDTO announcement = new MobileAnnouncementDTO(1L, 1, LocalDateTime.now(), sender,
                stringLengthOf(subjectLength),
                stringLengthOf(bodyLength));
        final AnnouncementNotificationTargets targets = new AnnouncementNotificationTargets(List.of(), List.of("a", "b"));
        return new AnnouncementNotificationDTO(announcement, targets, Locales.FI);
    }

    private static String stringLengthOf(final int length) {
        return StringUtils.rightPad("", length, 'x');
    }
}