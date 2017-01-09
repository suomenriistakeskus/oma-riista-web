package fi.riista.feature.mail.queue;

import fi.riista.feature.mail.MailMessageDTO;
import org.joda.time.DateTime;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class NoopMailProviderImpl implements OutgoingMailProvider<Long> {
    @Override
    public void scheduleForDelivery(final MailMessageDTO messageDTO, final Optional<DateTime> scheduledTime) {
    }

    @Override
    public Map<Long, MailMessageDTO> getOutgoingBatch() {
        return null;
    }

    @Override
    public void storeDeliveryStatus(final Set<Long> successful, final Set<Long> failed) {

    }
}
