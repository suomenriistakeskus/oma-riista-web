package fi.riista.feature.mail.delivery;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.RawMessage;
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;
import fi.riista.feature.mail.MailMessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.mail.MailParseException;
import org.springframework.mail.MailPreparationException;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class AmazonMailDeliveryServiceImpl implements MailDeliveryService<Long>, DisposableBean {
    private static final Logger LOG = LoggerFactory.getLogger(AmazonMailDeliveryServiceImpl.class);

    private final AmazonSimpleEmailService amazonSimpleEmailService;

    public AmazonMailDeliveryServiceImpl(final AmazonSimpleEmailService amazonSimpleEmailService) {
        this.amazonSimpleEmailService = amazonSimpleEmailService;
    }

    @Override
    public void destroy() {
        this.amazonSimpleEmailService.shutdown();
    }

    @Override
    public void send(final MailMessageDTO message) {
        sendMimeMessage(message);
    }

    @Override
    public void sendAll(
            final Map<Long, MailMessageDTO> outgoingBatch,
            final Set<Long> successfulMessages,
            final Set<Long> failedMessages) {

        outgoingBatch.forEach((messageId, message) -> {
            try {
                sendMimeMessage(message);
                successfulMessages.add(messageId);

            } catch (Exception e) {
                LOG.error("Failed sending", e);
                failedMessages.add(messageId);
            }
        });
    }

    private void sendMimeMessage(final MailMessageDTO mailMessage) {
        if (mailMessage.getTo().toLowerCase().endsWith("invalid")) {
            LOG.warn("Not sending mail to invalid email: '{}' with subject: '{}' and body:\n{}",
                    mailMessage.getTo(), mailMessage.getSubject(), mailMessage.getBody());
            return;
        }

        LOG.info("Attempting delivery using for outgoing email: {}", mailMessage);

        final MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        mailMessage.prepareMimeMessage(mimeMessage);
        this.amazonSimpleEmailService.sendRawEmail(new SendRawEmailRequest(createRawMessage(mimeMessage)));
    }

    private static RawMessage createRawMessage(final MimeMessage mimeMessage) {
        final ByteArrayOutputStream out;

        try {
            out = new ByteArrayOutputStream();
            mimeMessage.writeTo(out);
        } catch (IOException e) {
            throw new MailPreparationException(e);
        } catch (MessagingException e) {
            throw new MailParseException(e);
        }

        return new RawMessage(ByteBuffer.wrap(out.toByteArray()));
    }
}
