package fi.riista.feature.mail.delivery;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.GetSendQuotaResult;
import com.amazonaws.services.simpleemail.model.RawMessage;
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;
import com.google.common.base.Suppliers;
import com.google.common.util.concurrent.RateLimiter;
import fi.riista.feature.mail.HasMailMessageFields;
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
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class AmazonMailDeliveryServiceImpl implements MailDeliveryService, DisposableBean {
    private static final Logger LOG = LoggerFactory.getLogger(AmazonMailDeliveryServiceImpl.class);

    private static final double FALLBACK_SEND_RATE = 1;
    private static final double FALLBACK_REMAINING_QUOTA = 1000;

    private final Session session = Session.getInstance(new Properties());
    private final AmazonSimpleEmailService amazonSimpleEmailService;
    private final RateLimiter rateLimiter;
    private final Supplier<Double> rateLimitCache;
    private final Supplier<Double> sendQuotaCache;

    public AmazonMailDeliveryServiceImpl(final AmazonSimpleEmailService amazonSimpleEmailService) {
        this.amazonSimpleEmailService = amazonSimpleEmailService;
        this.rateLimiter = RateLimiter.create(FALLBACK_SEND_RATE);
        this.rateLimitCache = Suppliers.memoizeWithExpiration(this::getMaxSendRate, 1, TimeUnit.HOURS);
        this.sendQuotaCache = Suppliers.memoizeWithExpiration(this::getRemainingSendQuota, 5, TimeUnit.MINUTES);
    }

    private Double getMaxSendRate() {
        try {
            return amazonSimpleEmailService.getSendQuota().getMaxSendRate() - 1.0;

        } catch (Exception e) {
            LOG.error("Could not determine maxSendRate", e);

            return FALLBACK_SEND_RATE;
        }
    }

    private double getRemainingSendQuota() {
        try {
            final GetSendQuotaResult sendQuota = amazonSimpleEmailService.getSendQuota();
            final Double sentLast24Hours = sendQuota.getSentLast24Hours();
            final Double max24HourSend = sendQuota.getMax24HourSend();

            if (max24HourSend <= 0) {
                // Unlimited quota = -1
                return Integer.MAX_VALUE;
            }

            final double remainingQuota = max24HourSend - sentLast24Hours;

            LOG.info("Remaining send quota: {}", remainingQuota);

            return remainingQuota > 0 ? remainingQuota : FALLBACK_REMAINING_QUOTA;

        } catch (Exception e) {
            LOG.error("Could not determine remaining send quota", e);

            return FALLBACK_REMAINING_QUOTA;
        }
    }

    @Override
    public void destroy() {
        this.amazonSimpleEmailService.shutdown();
    }

    @Override
    public long getRemainingQuota() {
        return Math.round(sendQuotaCache.get());
    }

    @Override
    public void send(final HasMailMessageFields message, final String recipient) {
        final Double expectedRate = rateLimitCache.get();

        if (Math.abs(rateLimiter.getRate() - expectedRate) > 0.1) {
            LOG.info("Adjusting send rate to {}", expectedRate);
            rateLimiter.setRate(expectedRate);
        }

        rateLimiter.acquire();

        this.amazonSimpleEmailService.sendRawEmail(
                new SendRawEmailRequest(createRawMessage(createMimeMessage(message, recipient))));
    }

    private MimeMessage createMimeMessage(final HasMailMessageFields msg, final String recipient) {
        final MimeMessage mimeMessage = new MimeMessage(session);
        msg.prepareMimeMessage(mimeMessage, recipient);
        return mimeMessage;
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
