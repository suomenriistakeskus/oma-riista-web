package fi.riista.feature.mail.queue;

import com.google.common.collect.Maps;
import fi.riista.feature.mail.MailMessageDTO;
import fi.riista.util.F;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class DatabaseMailProviderImpl implements OutgoingMailProvider<Long> {

    @Resource
    MailMessageRepository mailMessageRepository;

    // How many times to retry after mail delivery error?
    @Value("${mail.batch.maximum.failures}")
    int maxSendFailures;

    // How many mail messages are sent in one batch operation?
    @Value("${mail.batch.size}")
    int batchSize;

    @Override
    @Transactional
    public void scheduleForDelivery(final MailMessageDTO messageDTO,
                                    final Optional<DateTime> scheduledTime) {

        final MailMessage mailMessage = new MailMessage();

        mailMessage.setBody(messageDTO.getBody());
        mailMessage.setSubject(messageDTO.getSubject());
        mailMessage.setFromEmail(messageDTO.getFrom());
        mailMessage.setToEmail(messageDTO.getTo());

        mailMessage.setSubmitTime(DateTime.now());
        mailMessage.setScheduledTime(scheduledTime.orElse(DateTime.now()));
        mailMessage.setDeliveryTime(null);

        mailMessage.setFailureCounter(0);

        mailMessageRepository.save(mailMessage);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, MailMessageDTO> getOutgoingBatch() {
        final List<MailMessage> unsentMessages = mailMessageRepository.findUnsentMessages(
                maxSendFailures, DateTime.now(), new PageRequest(0, batchSize));

        final Map<Long, MailMessage> messageById = F.indexById(unsentMessages);

        return Maps.transformValues(messageById, mailMessage -> new MailMessageDTO.Builder()
                .withFrom(mailMessage.getFromEmail())
                .withTo(mailMessage.getToEmail())
                .withSubject(mailMessage.getSubject())
                .withBody(mailMessage.getBody())
                .build());
    }

    @Override
    @Transactional
    public void storeDeliveryStatus(final Set<Long> successful, final Set<Long> failed) {
        for (final Long primaryKey : successful) {
            requireMailMessage(primaryKey).markAsDelivered();
        }

        for (final Long primaryKey : failed) {
            requireMailMessage(primaryKey).incrementFailureCounter();
        }
    }

    private MailMessage requireMailMessage(final Long primaryKey) {
        final MailMessage mailMessage = mailMessageRepository.findOne(primaryKey);

        if (mailMessage == null) {
            throw new IllegalArgumentException("No mailMessage with id=" + primaryKey);
        }

        return mailMessage;
    }
}
