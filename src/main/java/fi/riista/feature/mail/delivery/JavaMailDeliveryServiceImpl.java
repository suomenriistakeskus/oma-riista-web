package fi.riista.feature.mail.delivery;

import com.google.common.util.concurrent.RateLimiter;
import fi.riista.feature.mail.HasMailMessageFields;
import org.nlab.smtp.pool.SmtpConnectionPool;
import org.nlab.smtp.transport.connection.ClosableSmtpConnection;

import javax.mail.internet.MimeMessage;

public class JavaMailDeliveryServiceImpl implements MailDeliveryService {
    private static final double MAX_DELIVERY_PER_SECOND = 10;

    private final SmtpConnectionPool connectionPool;
    private final RateLimiter rateLimiter;

    public JavaMailDeliveryServiceImpl(SmtpConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
        this.rateLimiter = RateLimiter.create(MAX_DELIVERY_PER_SECOND);
    }

    @Override
    public long getRemainingQuota() {
        return Long.MAX_VALUE;
    }

    @Override
    public void send(final HasMailMessageFields message, final String recipient) {
        rateLimiter.acquire();

        try (final ClosableSmtpConnection transport = connectionPool.borrowObject()) {
            final MimeMessage mimeMessage = new MimeMessage(transport.getSession());
            message.prepareMimeMessage(mimeMessage, recipient);
            transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());

        } catch (RuntimeException e) {
            throw e;

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
