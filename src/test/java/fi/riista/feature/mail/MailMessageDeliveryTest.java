package fi.riista.feature.mail;

import fi.riista.feature.mail.queue.MailMessage;
import fi.riista.util.NumberGenerator;
import fi.riista.util.NumberSequence;
import fi.riista.util.ValueGeneratorMixin;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.LongStream;

import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class MailMessageDeliveryTest implements ValueGeneratorMixin {

    @Mock
    private Consumer<String> recipientConsumer;

    @Override
    public NumberGenerator getNumberGenerator() {
        return NumberSequence.INSTANCE;
    }

    private static Set<MailMessageDelivery.Recipient> createRecipients(final int count) {
        return LongStream.rangeClosed(1, count)
                .mapToObj(n -> new MailMessageDelivery.Recipient(n, "email-" + n + "@riista.fi"))
                .collect(toSet());
    }

    private MailMessage createMailMessage() {
        final MailMessage mailMessage = new MailMessage();
        mailMessage.setBody("body-" + nextPositiveInt());
        mailMessage.setSubject("subject-" + nextPositiveInt());
        mailMessage.setFromEmail("from-" + nextPositiveInt());
        return mailMessage;
    }

    @Test
    public void testSingleRecipient() {
        final MailMessage mailMessage = createMailMessage();

        final Set<MailMessageDelivery.Recipient> recipients = createRecipients(1);
        final MailMessageDelivery delivery = new MailMessageDelivery(mailMessage, recipients);

        assertEquals(mailMessage.getBody(), delivery.getBody());
        assertEquals(mailMessage.getSubject(), delivery.getSubject());
        assertEquals(mailMessage.getFromEmail(), delivery.getFrom());

        final Set<String> toEmails = new HashSet<>();
        final Set<Long> successfulIds = new HashSet<>();
        final Set<Long> failedIds = new HashSet<>();

        delivery.consumeRemainingRecipients(toEmails::add);
        delivery.consumeDeliveredRecipientIds(successfulIds::addAll);
        delivery.consumeFailedRecipientIds(failedIds::addAll);

        final MailMessageDelivery.Recipient firstRecipient = recipients.iterator().next();

        assertEquals(1, toEmails.size());
        assertEquals(1, successfulIds.size());
        assertTrue(failedIds.isEmpty());

        assertTrue(toEmails.contains(firstRecipient.getEmail()));
        assertTrue(successfulIds.contains(firstRecipient.getId()));
    }

    @Test
    public void testMultipleRecipients_Success() {
        final MailMessageDelivery delivery = new MailMessageDelivery(createMailMessage(), createRecipients(5));

        final Set<Long> successfulIds = new HashSet<>();
        final Set<Long> failedIds = new HashSet<>();

        delivery.consumeRemainingRecipients(recipientConsumer);
        delivery.consumeDeliveredRecipientIds(successfulIds::addAll);
        delivery.consumeFailedRecipientIds(failedIds::addAll);

        verify(recipientConsumer, times(5)).accept(ArgumentMatchers.anyString());
        verifyNoMoreInteractions(recipientConsumer);

        assertEquals(5, successfulIds.size());
        assertTrue(failedIds.isEmpty());
    }

    @Test
    public void testMultipleRecipients_Failure() {
        Mockito.doThrow(RuntimeException.class).when(recipientConsumer).accept(anyString());

        final MailMessageDelivery delivery = new MailMessageDelivery(createMailMessage(), createRecipients(5));

        final Set<Long> successfulIds = new HashSet<>();
        final Set<Long> failedIds = new HashSet<>();

        int exceptionsCaught = 0;

        for (int i = 0; i < 5; i++) {
            try {
                delivery.consumeRemainingRecipients(recipientConsumer);
            } catch (RuntimeException e) {
                exceptionsCaught++;
            }
        }
        assertEquals(5, exceptionsCaught);

        delivery.consumeDeliveredRecipientIds(successfulIds::addAll);
        delivery.consumeFailedRecipientIds(failedIds::addAll);

        verify(recipientConsumer, times(5)).accept(ArgumentMatchers.anyString());
        verifyNoMoreInteractions(recipientConsumer);

        assertTrue(successfulIds.isEmpty());
        assertEquals(5, failedIds.size());
    }
}
