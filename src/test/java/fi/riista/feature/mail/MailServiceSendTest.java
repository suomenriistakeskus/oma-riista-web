package fi.riista.feature.mail;

import org.joda.time.DateTime;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class MailServiceSendTest extends BaseMailServiceTest {
    @Test
    public void testSendImmediate() {
        // WHEN
        final MailMessageDTO.Builder builder = createTestMessageBuilder();
        final MailMessageDTO messageDTO = mailService.sendImmediate(builder);

        // THEN
        verify(mailDeliveryService, times(1)).send(eq(messageDTO));
        verifyNoMoreInteractions(mailDeliveryService);
        verifyNoMoreInteractions(outgoingMailProvider);
    }

    @Test
    public void testSendImmediateWithoutFrom() {
        // WHEN
        final MailMessageDTO.Builder builder = createTestMessageBuilder();
        final MailMessageDTO messageDTO = mailService.sendImmediate(builder);

        // THEN
        assertEquals("root@example.org", messageDTO.getFrom());
    }

    public void testSendImmediateWithFrom() {
        // WHEN
        final MailMessageDTO.Builder builder = createTestMessageBuilder()
                .withFrom("other@example.org");
        final MailMessageDTO messageDTO = mailService.sendImmediate(builder);

        // THEN
        assertEquals("other@example.org", messageDTO.getFrom());
    }

    @Test
    public void testSend() {
        // WHEN
        final MailMessageDTO.Builder builder = createTestMessageBuilder();
        final MailMessageDTO messageDTO = mailService.send(builder);

        // THEN
        verify(outgoingMailProvider, times(1)).scheduleForDelivery(eq(messageDTO), any(Optional.class));
        verifyNoMoreInteractions(mailDeliveryService);
        verifyNoMoreInteractions(outgoingMailProvider);
    }

    @Test
    public void testSendLater() {
        // GIVEN
        final DateTime sendAfterTime = DateTime.now().plusDays(1);

        // WHEN
        final MailMessageDTO.Builder builder = createTestMessageBuilder();
        final MailMessageDTO messageDTO = mailService.sendLater(builder, sendAfterTime);

        // THEN
        verify(outgoingMailProvider, times(1)).scheduleForDelivery(eq(messageDTO), eq(Optional.of(sendAfterTime)));
        verifyNoMoreInteractions(mailDeliveryService);
        verifyNoMoreInteractions(outgoingMailProvider);
    }

    @Test
    public void testWhenDeliveryDisabled() {
        // GIVEN
        mailService.mailDeliveryEnabled = false;

        // WHEN
        mailService.send(createTestMessageBuilder());
        mailService.sendImmediate(createTestMessageBuilder());
        mailService.sendLater(createTestMessageBuilder(), DateTime.now().minusDays(1));

        // THEN
        verifyNoMoreInteractions(mailDeliveryService);
        verifyNoMoreInteractions(outgoingMailProvider);
    }
}
