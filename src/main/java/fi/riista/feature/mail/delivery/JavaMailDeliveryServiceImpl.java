package fi.riista.feature.mail.delivery;

import fi.riista.feature.mail.MailMessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class JavaMailDeliveryServiceImpl implements MailDeliveryService<Long> {
    private static final Logger LOG = LoggerFactory.getLogger(JavaMailDeliveryServiceImpl.class);

    private String protocol = "smtp";
    private String host;
    private int port = 25;
    private String username;
    private String password;
    private Properties javaMailProperties = new Properties();

    private Session session;

    public synchronized Session getSession() {
        if (this.session == null) {
            this.session = Session.getInstance(this.javaMailProperties);
        }
        return this.session;
    }

    @Override
    public void send(final MailMessageDTO message) {
        final Transport transport = createTransport();

        try {
            doSend(transport, message);

        } catch (MessagingException ex) {
            throw new RuntimeException(ex);

        } finally {
            destroyTransport(transport);
        }
    }

    @Override
    public void sendAll(
            final Map<Long, MailMessageDTO> outgoingBatch,
            final Set<Long> successfulMessages,
            final Set<Long> failedMessages) {

        final Transport transport = createTransport();

        try {
            for (final Map.Entry<Long, MailMessageDTO> entry : outgoingBatch.entrySet()) {
                final Long messageId = entry.getKey();
                final MailMessageDTO message = entry.getValue();

                try {
                    doSend(transport, message);
                    successfulMessages.add(messageId);
                } catch (MessagingException ex) {
                    failedMessages.add(messageId);
                }
            }
        } finally {
            destroyTransport(transport);
        }
    }

    private Transport createTransport() {
        final Transport transport;

        try {
            transport = getSession().getTransport(this.protocol);
            transport.connect(this.host, this.port, this.username, this.password);

        } catch (MessagingException ex) {
            throw new RuntimeException(ex);
        }

        return transport;
    }

    private static void destroyTransport(Transport transport) {
        try {
            if (transport != null) {
                transport.close();
            }

        } catch (MessagingException ex) {
            LOG.error("Failed to close server connection after message sending", ex);
        }
    }

    private void doSend(Transport transport, MailMessageDTO message) throws MessagingException {
        if (message.getTo().toLowerCase().endsWith("invalid")) {
            LOG.warn("Not sending mail to invalid email: '{}' with subject: '{}' and body:\n{}",
                    message.getTo(), message.getSubject(), message.getBody());
            return;
        }

        LOG.info("Attempting delivery using for outgoing email: {}", message);

        final MimeMessage mimeMessage = new MimeMessage(getSession());
        message.prepareMimeMessage(mimeMessage);
        mimeMessage.saveChanges();
        transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void setJavaMailProperties(Properties javaMailProperties) {
        this.javaMailProperties = javaMailProperties;
    }
}
