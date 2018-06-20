package fi.riista.feature.mail;

import com.google.common.base.Preconditions;
import fi.riista.feature.mail.delivery.MailDeliveryService;
import fi.riista.feature.mail.queue.MailDeliveryQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

class MailMessageDeliveryProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(MailMessageDeliveryProcessor.class);

    private final MailDeliveryService deliveryService;
    private final MailDeliveryQueue deliveryQueue;
    private final long minimumQuota;
    private long remainingQuota;

    public MailMessageDeliveryProcessor(final MailDeliveryService deliveryService,
                                        final MailDeliveryQueue deliveryQueue,
                                        final long minimumQuota) {
        Preconditions.checkArgument(minimumQuota > 0);
        this.deliveryService = Objects.requireNonNull(deliveryService);
        this.deliveryQueue = Objects.requireNonNull(deliveryQueue);
        this.minimumQuota = Objects.requireNonNull(minimumQuota);
        this.remainingQuota = deliveryService.getRemainingQuota();
    }

    /**
     * Process messages and recipients until:
     *
     * a) no more deliveries remaining from the queue
     * b) mail delivery fails for recipient
     * c) remaining send quota is too small
     *
     * NOTE: Ignores invalid recipients silently.
     */
    public void startDelivery() {
        do {
            if (remainingQuotaTooSmall()) {
                LOG.error("Send quota exceeded: {}", remainingQuota);
                break;
            }
        } while (nextDelivery());
    }

    private boolean remainingQuotaTooSmall() {
        return remainingQuota < minimumQuota;
    }

    private boolean nextDelivery() {
        final Optional<MailMessageDelivery> deliveryOptional = deliveryQueue.nextDelivery();

        if (!deliveryOptional.isPresent()) {
            return false;
        }

        final MailMessageDelivery delivery = deliveryOptional.get();

        try {
            delivery.consumeRemainingRecipients(email -> nextRecipient(delivery, email));
            return true;

        } catch (Exception e) {
            LOG.error("Mail delivery failed", e);
            return false;

        } finally {
            deliveryQueue.storeDeliveryStatus(delivery);
        }
    }

    private void nextRecipient(final MailMessageDelivery delivery, final String email) {
        if (isValidEmail(email)) {
            deliveryService.send(delivery, email);
            remainingQuota--;

        } else {
            LOG.warn("Not sending to invalid recipient: {}", email);
        }
    }

    private static boolean isValidEmail(final String email) {
        return !email.endsWith("invalid") && email.contains("@");
    }
}
