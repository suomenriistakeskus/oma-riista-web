package fi.riista.feature.mail.queue;

import fi.riista.feature.mail.MailMessageDTO;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseMailProviderTest {

    private DatabaseMailProviderImpl mailProvider;

    @Mock
    private MailMessageRepository mailMessageRepository;

    @Before
    public void init() {
        mailProvider = new DatabaseMailProviderImpl();
        mailProvider.mailMessageRepository = mailMessageRepository;
        mailProvider.batchSize = 2;
        mailProvider.maxSendFailures = 2;
    }

    @Test
    public void testScheduleWithoutDate() {
        // GIVEN
        final MailMessageDTO.Builder builder = createTestMessageBuilder();

        // WHEN
        mailProvider.scheduleForDelivery(builder.build(), Optional.<DateTime> empty());

        // THEN
        verify(mailMessageRepository, times(1)).save(any(MailMessage.class));
    }

    @Test
    public void testGetOutgoingBatch() {
        // GIVEN
        final MailMessage mailMessage = new MailMessage();

        mailMessage.setId(284L);
        mailMessage.setFromEmail("from@example.org");
        mailMessage.setToEmail("to@example.org");
        mailMessage.setSubject("Test subject");
        mailMessage.setBody("Test body");

        // WHEN
        when(mailMessageRepository.findUnsentMessages(anyInt(), any(DateTime.class), any(Pageable.class)))
                .thenReturn(Arrays.asList(mailMessage));

        final Map<Long,MailMessageDTO> batch = mailProvider.getOutgoingBatch();

        // THEN
        verify(mailMessageRepository, times(1)).findUnsentMessages(eq(2), any(DateTime.class), any(Pageable.class));
        verifyNoMoreInteractions(mailMessageRepository);

        assertThat(batch, hasKey(284L));
        assertThat(batch.get(284L).getFrom(), equalTo("from@example.org"));
        assertThat(batch.get(284L).getTo(), equalTo("to@example.org"));
        assertThat(batch.get(284L).getSubject(), equalTo("Test subject"));
        assertThat(batch.get(284L).getBody(), equalTo("Test body"));
    }

    protected MailMessageDTO.Builder createTestMessageBuilder() {
        return new MailMessageDTO.Builder()
                .withFrom("sender@example.org")
                .withTo("recipient@example.org")
                .withSubject("Email subject")
                .withBody("Hello world!");
    }
}
