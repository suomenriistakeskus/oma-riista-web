package fi.riista.feature.mail.delivery;

import fi.riista.feature.mail.MailMessageDTO;

import java.util.Map;
import java.util.Set;

public class NoopMailDeviceryService implements MailDeliveryService<Long> {
    @Override
    public void send(final MailMessageDTO message) {
    }

    @Override
    public void sendAll(final Map<Long, MailMessageDTO> outgoingBatch, final Set<Long> successfulMessages, final Set<Long> failedMessages) {
    }
}
