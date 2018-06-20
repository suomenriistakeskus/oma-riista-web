package fi.riista.feature.mail.queue;

import fi.riista.feature.mail.MailMessageDTO;
import fi.riista.feature.mail.MailMessageDelivery;

import java.util.Optional;

public class SimpleMailDeliveryQueue implements MailDeliveryQueue {
    private MailMessageDTO nextMessage = null;

    @Override
    public void scheduleForDelivery(final MailMessageDTO messageDTO) {
        this.nextMessage = messageDTO;
    }

    @Override
    public Optional<MailMessageDelivery> nextDelivery() {
        if (this.nextMessage == null) {
            return Optional.empty();
        }

        final MailMessageDelivery deliveryDTO = new MailMessageDelivery(this.nextMessage);
        this.nextMessage = null;
        return Optional.of(deliveryDTO);
    }

    @Override
    public void storeDeliveryStatus(final MailMessageDelivery batch) {

    }
}
