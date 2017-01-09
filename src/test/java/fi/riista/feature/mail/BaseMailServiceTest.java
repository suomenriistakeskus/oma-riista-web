package fi.riista.feature.mail;

import fi.riista.feature.mail.delivery.MailDeliveryService;
import fi.riista.feature.mail.queue.OutgoingMailProvider;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public abstract class BaseMailServiceTest {
    @Mock
    protected MailDeliveryService<Long> mailDeliveryService;

    @Mock
    protected OutgoingMailProvider<Long> outgoingMailProvider;

    @InjectMocks
    protected MailServiceImpl mailService;

    @Before
    public void init() {
        mailService.mailDeliveryEnabled = true;
        mailService.fallbackDefaultEmailFromAddress = "root@example.org";
    }

    protected MailMessageDTO.Builder createTestMessageBuilder() {
        return new MailMessageDTO.Builder()
                //.withFrom("sender@example.org")
                .withTo("recipient@example.org")
                .withSubject("Email subject")
                .withBody("Hello world!");
    }
}
