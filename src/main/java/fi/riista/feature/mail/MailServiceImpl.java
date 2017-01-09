package fi.riista.feature.mail;

import fi.riista.feature.mail.delivery.MailDeliveryService;
import fi.riista.feature.mail.queue.OutgoingMailProvider;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class MailServiceImpl implements MailService {

    private static final Logger LOG = LoggerFactory.getLogger(MailServiceImpl.class);

    @Resource
    OutgoingMailProvider<Long> outgoingMailProvider;

    @Resource
    MailDeliveryService<Long> mailDeliveryService;

    @Value("${mail.enabled}")
    boolean mailDeliveryEnabled;

    @Value("${mail.address.from}")
    String fallbackDefaultEmailFromAddress;

    @Override
    public MailMessageDTO send(final MailMessageDTO.Builder builder) {
        return sendInternal(builder, false, null);
    }

    @Override
    public MailMessageDTO sendLater(final MailMessageDTO.Builder builder,
                                    final DateTime sendAfterTime) {
        return sendInternal(builder, false, sendAfterTime);
    }

    @Override
    public MailMessageDTO sendImmediate(final MailMessageDTO.Builder builder) {
        return sendInternal(builder, true, null);
    }

    private MailMessageDTO sendInternal(final MailMessageDTO.Builder builder,
                                        final boolean immediate,
                                        final DateTime sendAfterTime) {
        final MailMessageDTO messageDTO = builder
                .withDefaultFrom(fallbackDefaultEmailFromAddress)
                .build();

        if (mailDeliveryEnabled) {
            if (immediate) {
                mailDeliveryService.send(messageDTO);
            } else {
                outgoingMailProvider.scheduleForDelivery(messageDTO, Optional.ofNullable(sendAfterTime));
            }
        } else {
            LOG.warn("Mail message was not sent or persisted because mail is disabled. " +
                    "Message was from: '{}' to: '{}' with subject: '{}' and body:\n{}",
                    messageDTO.getFrom(), messageDTO.getTo(), messageDTO.getSubject(), messageDTO.getBody());
        }

        return messageDTO;
    }

    @Override
    public void processOutgoingMail() {
        final Map<Long, MailMessageDTO> outgoingBatch = outgoingMailProvider.getOutgoingBatch();

        if (outgoingBatch.isEmpty()) {
            return;
        }

        final Set<Long> failed = new HashSet<>();
        final Set<Long> successful = new HashSet<>();

        try {
            mailDeliveryService.sendAll(outgoingBatch, successful, failed);

        } catch (Exception ex) {
            LOG.error("Sending mail batch failed", ex);
        }

        outgoingMailProvider.storeDeliveryStatus(successful, failed);
    }
}
