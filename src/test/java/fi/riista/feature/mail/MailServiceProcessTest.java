package fi.riista.feature.mail;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class MailServiceProcessTest extends BaseMailServiceTest {
    @Captor
    private ArgumentCaptor<Set<Long>> successful;

    @Captor
    private ArgumentCaptor<Set<Long>> failed;

    @Captor
    private ArgumentCaptor<Map<Long, MailMessageDTO>> outgoingBatch;

    @Test
    public void testProcessEmptyBatch() {
        // GIVEN
        when(outgoingMailProvider.getOutgoingBatch()).thenReturn(Collections.<Long, MailMessageDTO> emptyMap());

        // WHEN
        mailService.processOutgoingMail();

        // THEN
        verifyNoMoreInteractions(mailDeliveryService);
    }

    @Test
    public void testProcessSingleItemSuccessfully() {
        // GIVEN
        final MailMessageDTO messageDTO = createTestMessageBuilder().withFrom("from@example.org").build();
        final ImmutableMap<Long, MailMessageDTO> batch = ImmutableMap.of(256L, messageDTO);

        when(outgoingMailProvider.getOutgoingBatch()).thenReturn(batch);

        doAnswer(invocationOnMock -> {
            @SuppressWarnings("unchecked")
            Set<Serializable> successful = (Set<Serializable>) invocationOnMock.getArguments()[1];
            successful.add(256L);
            return null;
        }).when(mailDeliveryService).sendAll(any(Map.class), any(Set.class), any(Set.class));

        // WHEN
        mailService.processOutgoingMail();

        // THEN
        verify(mailDeliveryService, times(1)).sendAll(outgoingBatch.capture(), any(Set.class), any(Set.class));
        verify(outgoingMailProvider, times(1)).getOutgoingBatch();
        verify(outgoingMailProvider, times(1)).storeDeliveryStatus(
                successful.capture(), failed.capture());

        assertTrue(outgoingBatch.getValue().containsKey(256L));
        assertTrue(outgoingBatch.getValue().containsValue(messageDTO));
        assertTrue(successful.getValue().contains(256L));
        assertEquals(1, successful.getValue().size());
        assertTrue(failed.getValue().isEmpty());

        verifyNoMoreInteractions(mailDeliveryService);
        verifyNoMoreInteractions(outgoingMailProvider);
    }

    @Test
    public void testProcessSingleItemFailure() {
        // GIVEN
        final MailMessageDTO messageDTO = createTestMessageBuilder().withFrom("from@example.org").build();
        final ImmutableMap<Long, MailMessageDTO> batch = ImmutableMap.of(256L, messageDTO);

        // WHEN
        when(outgoingMailProvider.getOutgoingBatch()).thenReturn(batch);

        doAnswer(invocationOnMock -> {
            @SuppressWarnings("unchecked")
            Set<Serializable> failed = (Set<Serializable>) invocationOnMock.getArguments()[2];
            failed.add(256L);
            return null;
        }).when(mailDeliveryService).sendAll(any(Map.class), any(Set.class), any(Set.class));

        mailService.processOutgoingMail();

        // THEN

        verify(mailDeliveryService, times(1)).sendAll(outgoingBatch.capture(), any(Set.class), any(Set.class));
        verify(outgoingMailProvider, times(1)).getOutgoingBatch();
        verify(outgoingMailProvider, times(1)).storeDeliveryStatus(
                successful.capture(), failed.capture());

        assertTrue(successful.getValue().isEmpty());
        assertTrue(failed.getValue().contains(256L));
        assertEquals(1, failed.getValue().size());

        verifyNoMoreInteractions(mailDeliveryService);
        verifyNoMoreInteractions(outgoingMailProvider);
    }
}
