package fi.riista.feature.mail.queue;

import fi.riista.config.properties.MailProperties;
import fi.riista.feature.mail.MailMessageDTO;
import fi.riista.feature.mail.MailMessageDelivery;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.F;
import org.hamcrest.Matchers;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class DatabaseMailDeliveryQueueTest extends EmbeddedDatabaseTest {

    private static class TestException extends RuntimeException {
    }

    @Resource
    private MailDeliveryQueue deliveryQueue;

    @Resource
    private MailMessageRepository mailMessageRepository;

    @Resource
    private MailMessageRecipientRepository mailMessageRecipientRepository;

    private static MailMessageDTO mailMessage(String... toEmail) {
        return MailMessageDTO.builder()
                .withFrom("from@riista.fi")
                .withRecipients(Arrays.stream(toEmail).collect(Collectors.toList()))
                .withSubject("subject")
                .appendBody("body")
                .build();
    }

    private static MailMessageDTO mailMessage() {
        return mailMessage("first@riista.fi", "second@riista.fi");
    }

    private MailMessageDTO scheduleDelivery(final MailMessageDTO dto) {
        deliveryQueue.scheduleForDelivery(dto);
        try {
            Thread.sleep(10);
        } catch (InterruptedException ignore) {
        }
        return dto;
    }

    @Test
    public void testScheduleForDelivery() {
        final MailMessageDTO dto = scheduleDelivery(mailMessage());

        runInTransaction(() -> {
            final List<MailMessage> mailMessages = mailMessageRepository.findAll();
            assertEquals(1, mailMessages.size());

            final MailMessage msg = mailMessages.get(0);
            assertEquals(dto.getBody(), msg.getBody());
            assertEquals(dto.getSubject(), msg.getSubject());
            assertEquals(dto.getFrom(), msg.getFromEmail());
            assertTrue(msg.getScheduledTime().isBeforeNow());

            final List<MailMessageRecipient> recipients = mailMessageRecipientRepository.findAll();
            assertEquals(2, recipients.size());
            assertEquals(Arrays.asList("first@riista.fi", "second@riista.fi"),
                    F.mapNonNullsToList(recipients, MailMessageRecipient::getEmail));

            for (MailMessageRecipient recipient : recipients) {
                assertEquals(msg, recipient.getMailMessage());
                assertNull(recipient.getDeliveryTime());
                assertEquals(0, recipient.getFailureCounter());
            }
        });
    }

    @Test
    public void testGetNextDelivery() {
        final MailMessageDTO dto = scheduleDelivery(mailMessage());

        final Optional<MailMessageDelivery> deliveryOptional = deliveryQueue.nextDelivery();

        assertTrue(deliveryOptional.isPresent());

        final MailMessageDelivery delivery = deliveryOptional.get();

        assertEquals(dto.getBody(), delivery.getBody());
        assertEquals(dto.getSubject(), delivery.getSubject());
        assertEquals(dto.getFrom(), delivery.getFrom());

        final HashSet<String> recipients = new HashSet<>();
        delivery.consumeRemainingRecipients(recipients::add);

        assertEquals(2, recipients.size());
        assertThat(recipients, Matchers.containsInAnyOrder("first@riista.fi", "second@riista.fi"));
    }

    @Test
    public void testStoreDeliveryStatus_Success() {
        scheduleDelivery(mailMessage());

        final Optional<MailMessageDelivery> deliveryOptional = deliveryQueue.nextDelivery();
        assertTrue(deliveryOptional.isPresent());

        final MailMessageDelivery delivery = deliveryOptional.get();
        delivery.consumeRemainingRecipients(email -> {
        });

        deliveryQueue.storeDeliveryStatus(delivery);

        // No more deliveries pending after success
        assertFalse(deliveryQueue.nextDelivery().isPresent());

        runInTransaction(() -> {
            final List<MailMessageRecipient> recipients = mailMessageRecipientRepository.findAll();
            assertEquals(2, recipients.size());

            for (MailMessageRecipient recipient : recipients) {
                assertNotNull(recipient.getDeliveryTime());
                assertTrue(recipient.getDeliveryTime().isBeforeNow());
                assertEquals(0, recipient.getFailureCounter());
            }
        });
    }

    @Test
    public void testStoreDeliveryStatus_Failure() {
        scheduleDelivery(mailMessage());

        final Optional<MailMessageDelivery> deliveryOptional = deliveryQueue.nextDelivery();
        assertTrue(deliveryOptional.isPresent());

        final MailMessageDelivery delivery = deliveryOptional.get();

        // First failure
        try {
            delivery.consumeRemainingRecipients(email -> {
                throw new TestException();
            });
        } catch (TestException ignore) {
        }

        // Second failure
        try {
            delivery.consumeRemainingRecipients(email -> {
                throw new TestException();
            });
        } catch (TestException ignore) {
        }

        deliveryQueue.storeDeliveryStatus(delivery);

        // Delivery is still pending
        assertTrue(deliveryQueue.nextDelivery().isPresent());

        runInTransaction(() -> {
            final List<MailMessageRecipient> recipients = mailMessageRecipientRepository.findAll();
            assertEquals(2, recipients.size());

            for (MailMessageRecipient recipient : recipients) {
                assertNull(recipient.getDeliveryTime());
                assertEquals(1, recipient.getFailureCounter());
            }
        });
    }

    @Test
    public void testStoreDeliveryStatus_TooManyFailures() {
        scheduleDelivery(mailMessage("to@riista.fi"));

        for (int i = 0; i < MailProperties.MAX_RECIPIENT_SEND_FAILURES; i++) {
            final Optional<MailMessageDelivery> deliveryOptional = deliveryQueue.nextDelivery();
            assertTrue(deliveryOptional.isPresent());

            final MailMessageDelivery delivery = deliveryOptional.get();

            final Set<String> recipientEmails = new HashSet<>();
            try {
                delivery.consumeRemainingRecipients(email -> {
                    recipientEmails.add(email);
                    throw new TestException();
                });
            } catch (TestException ignore) {
            }

            assertEquals(Collections.singleton("to@riista.fi"), recipientEmails);

            deliveryQueue.storeDeliveryStatus(delivery);
        }

        // Maximum delivery count reached
        final Optional<MailMessageDelivery> delivery = deliveryQueue.nextDelivery();
        assertFalse(delivery.isPresent());

        runInTransaction(() -> {
            final List<MailMessage> messageList = mailMessageRepository.findAll();
            assertEquals(1, messageList.size());

            for (MailMessage msg : messageList) {
                assertTrue(msg.isDelivered());
            }

            final List<MailMessageRecipient> recipients = mailMessageRecipientRepository.findAll();
            assertEquals(1, recipients.size());

            for (MailMessageRecipient recipient : recipients) {
                assertNull(recipient.getDeliveryTime());
                assertEquals(MailProperties.MAX_RECIPIENT_SEND_FAILURES, recipient.getFailureCounter());
            }
        });
    }
}
