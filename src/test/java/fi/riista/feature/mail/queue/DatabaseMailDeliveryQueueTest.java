package fi.riista.feature.mail.queue;

import fi.riista.config.properties.MailProperties;
import fi.riista.feature.mail.MailMessageDTO;
import fi.riista.feature.mail.MailMessageDelivery;
import fi.riista.feature.mail.bounce.MailMessageBounce;
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

import static fi.riista.util.DateUtil.now;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;


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
            assertThat(mailMessages, hasSize(1));

            final MailMessage msg = mailMessages.get(0);
            assertThat(msg.getBody(), equalTo(dto.getBody()));
            assertThat(msg.getSubject(), equalTo(dto.getSubject()));
            assertThat(msg.getFromEmail(), equalTo(dto.getFrom()));
            assertThat(msg.getScheduledTime(), is(lessThan(now())));

            final List<MailMessageRecipient> recipients = mailMessageRecipientRepository.findAll();
            assertThat(recipients, hasSize(2));
            assertThat(F.mapNonNullsToList(recipients, MailMessageRecipient::getEmail),
                    containsInAnyOrder("first@riista.fi", "second@riista.fi"));

            for (MailMessageRecipient recipient : recipients) {
                assertThat(recipient.getMailMessage(), equalTo(msg));
                assertThat(recipient.getDeliveryTime(), is(nullValue()));
                assertThat(recipient.getFailureCounter(), equalTo(0));
            }
        });
    }

    @Test
    public void testScheduleForDelivery_invalidEmailAddress() {
        final String bouncedMailAddress = "test@riista.fi";

        model().newMailMessageBounce(bouncedMailAddress);
        persistInNewTransaction();

        final MailMessageDTO dto = scheduleDelivery(mailMessage(bouncedMailAddress, "valid@riista.fi"));

        runInTransaction(() -> {
            final List<MailMessage> mailMessages = mailMessageRepository.findAll();
            assertThat(mailMessages, hasSize(1));

            final MailMessage msg = mailMessages.get(0);
            assertThat(msg.getBody(), equalTo(dto.getBody()));
            assertThat(msg.getSubject(), equalTo(dto.getSubject()));
            assertThat(msg.getFromEmail(), equalTo(dto.getFrom()));
            assertThat(msg.getScheduledTime(), is(lessThan(now())));

            final List<MailMessageRecipient> recipients = mailMessageRecipientRepository.findAll();
            assertThat(recipients, hasSize(1));
            final MailMessageRecipient recipient = recipients.get(0);

            assertThat(recipient.getEmail(), equalTo("valid@riista.fi"));
            assertThat(recipient.getMailMessage(), equalTo(msg));
            assertThat(recipient.getDeliveryTime(), is(nullValue()));
            assertThat(recipient.getFailureCounter(), equalTo(0));
        });
    }

    @Test
    public void testScheduleForDelivery_temporaryBounce() {
        final String bouncedMailAddress = "test@riista.fi";

        // Should not filter out bounce with non-permanent type
        model().newMailMessageBounce(bouncedMailAddress,
                MailMessageBounce.BounceType.Transient, MailMessageBounce.BounceSubType.MailboxFull);

        persistInNewTransaction();

        final MailMessageDTO dto = scheduleDelivery(mailMessage(bouncedMailAddress));

        runInTransaction(() -> {
            final List<MailMessage> mailMessages = mailMessageRepository.findAll();
            assertThat(mailMessages, hasSize(1));

            final MailMessage msg = mailMessages.get(0);
            assertThat(msg.getBody(), equalTo(dto.getBody()));
            assertThat(msg.getSubject(), equalTo(dto.getSubject()));
            assertThat(msg.getFromEmail(), equalTo(dto.getFrom()));
            assertThat(msg.getScheduledTime(), is(lessThan(now())));

            final List<MailMessageRecipient> recipients = mailMessageRecipientRepository.findAll();
            assertThat(recipients, hasSize(1));
            final MailMessageRecipient recipient = recipients.get(0);

            assertThat(recipient.getEmail(), equalTo(bouncedMailAddress));
            assertThat(recipient.getMailMessage(), equalTo(msg));
            assertThat(recipient.getDeliveryTime(), is(nullValue()));
            assertThat(recipient.getFailureCounter(), equalTo(0));
        });
    }

    @Test
    public void testScheduleForDelivery_onlyInvalidAddresses() {
        final String bouncedMailAddress = "test@riista.fi";

        model().newMailMessageBounce(bouncedMailAddress);
        persistInNewTransaction();

        final MailMessageDTO dto = scheduleDelivery(mailMessage(bouncedMailAddress));

        runInTransaction(() -> {
            final List<MailMessage> mailMessages = mailMessageRepository.findAll();
            assertThat(mailMessages, hasSize(1));

            final MailMessage msg = mailMessages.get(0);
            assertThat(msg.getBody(), equalTo(dto.getBody()));
            assertThat(msg.getSubject(), equalTo(dto.getSubject()));
            assertThat(msg.getFromEmail(), equalTo(dto.getFrom()));
            assertThat(msg.getScheduledTime(), is(lessThan(now())));

            final List<MailMessageRecipient> recipients = mailMessageRecipientRepository.findAll();
            assertThat(recipients, is(emptyList()));
        });
    }

    @Test
    public void testGetNextDelivery() {
        final MailMessageDTO dto = scheduleDelivery(mailMessage());

        final Optional<MailMessageDelivery> deliveryOptional = deliveryQueue.nextDelivery();

        assertThat(deliveryOptional.isPresent(), is(true));

        final MailMessageDelivery delivery = deliveryOptional.get();

        assertThat(delivery.getBody(), equalTo(dto.getBody()));
        assertThat(delivery.getSubject(), equalTo(dto.getSubject()));
        assertThat(delivery.getFrom(), equalTo(dto.getFrom()));

        final HashSet<String> recipients = new HashSet<>();
        delivery.consumeRemainingRecipients(recipients::add);

        assertThat(recipients, hasSize(2));
        assertThat(recipients, Matchers.containsInAnyOrder("first@riista.fi", "second@riista.fi"));
    }

    @Test
    public void testStoreDeliveryStatus_Success() {
        scheduleDelivery(mailMessage());

        final Optional<MailMessageDelivery> deliveryOptional = deliveryQueue.nextDelivery();
        assertThat(deliveryOptional.isPresent(), is(true));

        final MailMessageDelivery delivery = deliveryOptional.get();
        delivery.consumeRemainingRecipients(email -> {
        });

        deliveryQueue.storeDeliveryStatus(delivery);

        // No more deliveries pending after success
        assertThat(deliveryQueue.nextDelivery().isPresent(), is(false));

        runInTransaction(() -> {
            final List<MailMessageRecipient> recipients = mailMessageRecipientRepository.findAll();
            assertThat(recipients, hasSize(2));

            for (MailMessageRecipient recipient : recipients) {
                assertThat(recipient.getDeliveryTime(), is(notNullValue()));
                assertThat(recipient.getDeliveryTime(), is(lessThan(now())));
                assertThat(recipient.getFailureCounter(), equalTo(0));
            }
        });
    }

    @Test
    public void testStoreDeliveryStatus_Failure() {
        scheduleDelivery(mailMessage());

        final Optional<MailMessageDelivery> deliveryOptional = deliveryQueue.nextDelivery();
        assertThat(deliveryOptional.isPresent(), is(true));

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
        assertThat(deliveryQueue.nextDelivery().isPresent(), is(true));

        runInTransaction(() -> {
            final List<MailMessageRecipient> recipients = mailMessageRecipientRepository.findAll();
            assertThat(recipients, hasSize(2));

            for (MailMessageRecipient recipient : recipients) {
                assertThat(recipient.getDeliveryTime(), is(nullValue()));
                assertThat(recipient.getFailureCounter(), equalTo(1));
            }
        });
    }

    @Test
    public void testStoreDeliveryStatus_TooManyFailures() {
        scheduleDelivery(mailMessage("to@riista.fi"));

        for (int i = 0; i < MailProperties.MAX_RECIPIENT_SEND_FAILURES; i++) {
            final Optional<MailMessageDelivery> deliveryOptional = deliveryQueue.nextDelivery();
            assertThat(deliveryOptional.isPresent(), is(true));

            final MailMessageDelivery delivery = deliveryOptional.get();

            final Set<String> recipientEmails = new HashSet<>();
            try {
                delivery.consumeRemainingRecipients(email -> {
                    recipientEmails.add(email);
                    throw new TestException();
                });
            } catch (TestException ignore) {
            }

            assertThat(recipientEmails, equalTo(Collections.singleton("to@riista.fi")));

            deliveryQueue.storeDeliveryStatus(delivery);
        }

        // Maximum delivery count reached
        final Optional<MailMessageDelivery> delivery = deliveryQueue.nextDelivery();
        assertThat(delivery.isPresent(), is(false));

        runInTransaction(() -> {
            final List<MailMessage> messageList = mailMessageRepository.findAll();
            assertThat(messageList, hasSize(1));

            for (MailMessage msg : messageList) {
                assertThat(msg.isDelivered(), is(true));
            }

            final List<MailMessageRecipient> recipients = mailMessageRecipientRepository.findAll();
            assertThat(recipients, hasSize(1));

            for (MailMessageRecipient recipient : recipients) {
                assertThat(recipient.getDeliveryTime(), is(nullValue()));
                assertThat(recipient.getFailureCounter(), equalTo(MailProperties.MAX_RECIPIENT_SEND_FAILURES));
            }
        });
    }
}
