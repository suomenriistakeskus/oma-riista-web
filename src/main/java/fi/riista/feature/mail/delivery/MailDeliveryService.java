package fi.riista.feature.mail.delivery;

import fi.riista.feature.mail.MailMessageDTO;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public interface MailDeliveryService<T extends Serializable> {
    void send(MailMessageDTO message);

    void sendAll(Map<T, MailMessageDTO> outgoingBatch,
                 Set<T> successfulMessages,
                 Set<T> failedMessages);
}
