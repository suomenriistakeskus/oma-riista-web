package fi.riista.feature.mail.delivery;

import com.google.common.util.concurrent.RateLimiter;
import fi.riista.feature.mail.HasMailMessageFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoopMailDeliveryService implements MailDeliveryService {
    private static final Logger LOG = LoggerFactory.getLogger(NoopMailDeliveryService.class);

    private final RateLimiter rateLimiter;

    public NoopMailDeliveryService() {
        this.rateLimiter = RateLimiter.create(20);
    }

    @Override
    public long getRemainingQuota() {
        return Long.MAX_VALUE;
    }

    @Override
    public void send(final HasMailMessageFields message, final String recipientEmail) {
        rateLimiter.acquire();

        LOG.info("Not sending for recipient: {}", recipientEmail);
    }
}
