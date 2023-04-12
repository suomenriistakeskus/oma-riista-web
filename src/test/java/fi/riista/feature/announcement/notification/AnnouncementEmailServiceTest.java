package fi.riista.feature.announcement.notification;

import fi.riista.feature.announcement.show.MobileAnnouncementDTO;
import fi.riista.feature.announcement.show.MobileAnnouncementSenderDTO;
import fi.riista.feature.mail.queue.MailMessage;
import fi.riista.feature.mail.queue.MailMessageRecipient;
import fi.riista.feature.mail.queue.MailMessageRecipientRepository;
import fi.riista.feature.mail.queue.MailMessageRepository;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.Locales;
import fi.riista.util.LocalisedString;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static fi.riista.test.Asserts.assertThat;
import static fi.riista.util.DateUtil.now;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

public class AnnouncementEmailServiceTest extends EmbeddedDatabaseTest {

    @Resource
    private AnnouncementEmailService service;

    @Resource
    private MailMessageRepository messageRepository;

    @Resource
    private MailMessageRecipientRepository recipientRepository;

    @Test
    public void testSend_smoke() {

        final String email = "test@riista.fi";
        final AnnouncementNotificationDTO dto = createDto(email);

        runInTransaction(() -> {
            service.send(dto);

            final List<MailMessage> messages = messageRepository.findAll();
            assertThat(messages, hasSize(1));

            final List<MailMessageRecipient> recipients = recipientRepository.findAll();
            assertThat(recipients, hasSize(1));

            final MailMessageRecipient mailMessageRecipient = recipients.get(0);
            assertThat(mailMessageRecipient.getEmail(), equalTo(email));
        });
    }

    @Test
    public void testSend_emailWithComplaint() {

        final String email = "test@riista.fi";

        model().newMailMessageComplaint(email);
        persistInNewTransaction();

        final AnnouncementNotificationDTO dto = createDto(email);

        runInTransaction(() -> {
            service.send(dto);

            final List<MailMessage> messages = messageRepository.findAll();
            assertThat(messages, hasSize(0));
        });
    }

    @Test
    public void testSend_emailWithComplaint_multipleRecipients() {

        final String email = "test@riista.fi";

        model().newMailMessageComplaint(email);
        persistInNewTransaction();

        final AnnouncementNotificationDTO dto = createDto(email, "valid@riista.fi");

        runInTransaction(() -> {
            service.send(dto);

            final List<MailMessage> messages = messageRepository.findAll();
            assertThat(messages, hasSize(1));

            final List<MailMessageRecipient> recipients = recipientRepository.findAll();
            assertThat(recipients, hasSize(1));

            final MailMessageRecipient mailMessageRecipient = recipients.get(0);
            assertThat(mailMessageRecipient.getEmail(), equalTo("valid@riista.fi"));
        });
    }

    private AnnouncementNotificationDTO createDto(final String... emails) {
        final MobileAnnouncementSenderDTO senderDTO =
                new MobileAnnouncementSenderDTO(ls("organisation").asMap(), ls("organisationType").asMap(), "Sender");
        final AnnouncementNotificationTargets targets =
                new AnnouncementNotificationTargets(Arrays.asList(emails), Collections.emptyList());
        final MobileAnnouncementDTO announcementDTO =
                new MobileAnnouncementDTO(0L, 1, now().toLocalDateTime(), senderDTO, "subject", "body");

        return new AnnouncementNotificationDTO(announcementDTO, targets, Locales.FI);
    }

    private LocalisedString ls(final String s) {
        return LocalisedString.of(s, s);
    }
}
