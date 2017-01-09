package fi.riista.feature.mail.queue;

import fi.riista.feature.mail.MailMessageDTO;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface OutgoingMailProvider<T extends Serializable> {
    /**
     * Store messages for delivery.
     *
     * @param messageDTO contains email headers and body.
     * @param scheduledTime is an optional time for the first scheduled delivery attempt.
     */
    void scheduleForDelivery(MailMessageDTO messageDTO, Optional<DateTime> scheduledTime);

    /**
     * Find outgoing message which are:
     *
     * a) not marked as delivered
     * b) are scheduled to be sent before current time (not too early)
     * c) have not failed delivery (too many delivery attempts)
     */
    Map<T, MailMessageDTO> getOutgoingBatch();

    /**
     * Store delivery status for a batch of messages.
     *
     * @param successful List of primary keys for successfully delivered messages.
     * @param failed List of primary keys for messages which have failed the delivery attempt.
     */
    void storeDeliveryStatus(Set<T> successful, Set<T> failed);
}
