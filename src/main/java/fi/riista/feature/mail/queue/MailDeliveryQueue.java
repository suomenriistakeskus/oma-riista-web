package fi.riista.feature.mail.queue;

import fi.riista.feature.mail.MailMessageDTO;
import fi.riista.feature.mail.MailMessageDelivery;

import java.util.Optional;

public interface MailDeliveryQueue {
    /**
     * Store messages for delivery.
     */
    void scheduleForDelivery(MailMessageDTO messageDTO);

    /**
     * Find outgoing message which are:
     *
     * a) not marked as delivered
     * b) is scheduled to be sent before current time (not too early)
     * c) has remaining undelivered recipients
     * d) recipients have not failed delivery (too many delivery attempts)
     */
    Optional<MailMessageDelivery> nextDelivery();

    /**
     * Store delivery status for a batch of messages.
     */
    void storeDeliveryStatus(MailMessageDelivery batch);
}
