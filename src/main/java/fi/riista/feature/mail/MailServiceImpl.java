package fi.riista.feature.mail;

import fi.riista.config.properties.MailProperties;
import fi.riista.feature.mail.delivery.MailDeliveryService;
import fi.riista.feature.mail.queue.MailDeliveryQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class MailServiceImpl implements MailService {

    private static final Logger LOG = LoggerFactory.getLogger(MailServiceImpl.class);

    @Resource
    private MailDeliveryQueue mailDeliveryQueue;

    @Resource
    private MailDeliveryService mailDeliveryService;

    @Resource
    private MailProperties mailProperties;

    @Override
    public String getDefaultFromAddress() {
        return mailProperties.getDefaultFromAddress();
    }

    @Override
    public void send(final MailMessageDTO dto) {
        if (!mailProperties.isMailDeliveryEnabled()) {
            LOG.warn("Mail delivery is currently disabled for message with subject '{}'", dto.getSubject());
        }

        mailDeliveryQueue.scheduleForDelivery(dto);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void processOutgoingMail() {
        if (!mailProperties.isMailDeliveryEnabled()) {
            return;
        }

        new MailMessageDeliveryProcessor(
                mailDeliveryService, mailDeliveryQueue, mailProperties.getBatchSize()).startDelivery();
    }
}
