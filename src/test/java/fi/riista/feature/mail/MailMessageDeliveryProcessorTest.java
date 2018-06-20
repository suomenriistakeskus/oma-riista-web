package fi.riista.feature.mail;

import fi.riista.feature.mail.delivery.MailDeliveryService;
import fi.riista.feature.mail.queue.MailDeliveryQueue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MailMessageDeliveryProcessorTest {

    private static final long MINIMUM_QUOTA = 100;

    @Mock
    private MailDeliveryService deliveryService;

    @Mock
    private MailDeliveryQueue queue;

    private static MailMessageDelivery createDelivery() {
        return createDelivery("to@riista.fi");
    }

    private static MailMessageDelivery createDelivery(String... toEmail) {
        final MailMessageDTO msg = MailMessageDTO.builder()
                .withFrom("from@riista.fi")
                .withRecipients(Arrays.stream(toEmail).collect(Collectors.toList()))
                .withSubject("subject")
                .appendBody("body")
                .build();

        return new MailMessageDelivery(msg);
    }

    @Before
    public void init() {
        when(deliveryService.getRemainingQuota()).thenReturn(Long.MAX_VALUE);
    }

    private void startDelivery() {
        new MailMessageDeliveryProcessor(deliveryService, queue, MINIMUM_QUOTA).startDelivery();
    }

    @Test
    public void testNothingToDelivery() {
        startDelivery();

        verify(deliveryService, times(1)).getRemainingQuota();
        verify(queue, times(1)).nextDelivery();

        verifyNoMoreInteractions(queue);
        verifyNoMoreInteractions(deliveryService);
    }

    @Test
    public void testSingleRecipient() {
        final MailMessageDelivery delivery = createDelivery();

        when(queue.nextDelivery()).thenReturn(Optional.of(delivery)).thenReturn(Optional.empty());

        startDelivery();

        verify(queue, times(2)).nextDelivery();
        verify(queue, times(1)).storeDeliveryStatus(ArgumentMatchers.eq(delivery));
        verifyNoMoreInteractions(queue);

        verify(deliveryService, times(1)).getRemainingQuota();
        verify(deliveryService, times(1))
                .send(ArgumentMatchers.eq(delivery), ArgumentMatchers.eq("to@riista.fi"));
        verifyNoMoreInteractions(deliveryService);
    }

    @Test
    public void testMultipleRecipients() {
        final MailMessageDelivery delivery = createDelivery("first@riista.fi", "second@riista.fi");

        when(queue.nextDelivery()).thenReturn(Optional.of(delivery)).thenReturn(Optional.empty());

        startDelivery();

        verify(queue, times(2)).nextDelivery();
        verify(queue, times(1)).storeDeliveryStatus(ArgumentMatchers.eq(delivery));
        verifyNoMoreInteractions(queue);

        verify(deliveryService, times(1)).getRemainingQuota();
        verify(deliveryService, times(1))
                .send(ArgumentMatchers.eq(delivery), ArgumentMatchers.eq("first@riista.fi"));
        verify(deliveryService, times(1))
                .send(ArgumentMatchers.eq(delivery), ArgumentMatchers.eq("second@riista.fi"));
        verifyNoMoreInteractions(deliveryService);
    }

    @Test
    public void testMultipleDeliveries() {
        final MailMessageDelivery firstDelivery = createDelivery("first@riista.fi", "second@riista.fi");
        final MailMessageDelivery secondDelivery = createDelivery("third@riista.fi", "fourth@riista.fi");

        when(queue.nextDelivery())
                .thenReturn(Optional.of(firstDelivery))
                .thenReturn(Optional.of(secondDelivery))
                .thenReturn(Optional.empty());

        startDelivery();

        verify(queue, times(3)).nextDelivery();
        verify(queue, times(1)).storeDeliveryStatus(ArgumentMatchers.eq(firstDelivery));
        verify(queue, times(1)).storeDeliveryStatus(ArgumentMatchers.eq(secondDelivery));
        verifyNoMoreInteractions(queue);

        verify(deliveryService, times(1)).getRemainingQuota();
        verify(deliveryService, times(1))
                .send(ArgumentMatchers.eq(firstDelivery), ArgumentMatchers.eq("first@riista.fi"));
        verify(deliveryService, times(1))
                .send(ArgumentMatchers.eq(firstDelivery), ArgumentMatchers.eq("second@riista.fi"));
        verify(deliveryService, times(1))
                .send(ArgumentMatchers.eq(secondDelivery), ArgumentMatchers.eq("third@riista.fi"));
        verify(deliveryService, times(1))
                .send(ArgumentMatchers.eq(secondDelivery), ArgumentMatchers.eq("fourth@riista.fi"));
        verifyNoMoreInteractions(deliveryService);
    }

    @Test
    public void testDeliveryFailure() {
        final MailMessageDelivery delivery = createDelivery();

        when(queue.nextDelivery()).thenReturn(Optional.of(delivery)).thenReturn(Optional.empty());
        doThrow(RuntimeException.class).when(deliveryService)
                .send(ArgumentMatchers.any(HasMailMessageFields.class), ArgumentMatchers.anyString());

        startDelivery();

        verify(queue, times(1)).nextDelivery();
        verify(queue, times(1)).storeDeliveryStatus(ArgumentMatchers.eq(delivery));
        verifyNoMoreInteractions(queue);

        verify(deliveryService, times(1)).getRemainingQuota();
        verify(deliveryService, times(1))
                .send(ArgumentMatchers.eq(delivery), ArgumentMatchers.eq("to@riista.fi"));
        verifyNoMoreInteractions(deliveryService);
    }

    @Test
    public void testInvalidRecipients() {
        final MailMessageDelivery delivery = createDelivery(
                // invalid suffix
                "to@invalid",
                // invalid suffix with sub domain
                "to@riista.invalid",
                /// missing domain
                "to",
                // valid
                "to@riista.fi");

        when(queue.nextDelivery()).thenReturn(Optional.of(delivery)).thenReturn(Optional.empty());

        startDelivery();

        verify(queue, times(2)).nextDelivery();
        verify(queue, times(1)).storeDeliveryStatus(ArgumentMatchers.eq(delivery));
        verifyNoMoreInteractions(queue);

        verify(deliveryService, times(1)).getRemainingQuota();
        verify(deliveryService, times(1))
                .send(ArgumentMatchers.eq(delivery), ArgumentMatchers.eq("to@riista.fi"));
        verifyNoMoreInteractions(deliveryService);
    }

    @Test
    public void testQuotaExceeded() {
        when(deliveryService.getRemainingQuota()).thenReturn(MINIMUM_QUOTA - 1);

        startDelivery();

        verifyNoMoreInteractions(queue);
        verify(deliveryService, times(1)).getRemainingQuota();
        verifyNoMoreInteractions(deliveryService);
    }
}
