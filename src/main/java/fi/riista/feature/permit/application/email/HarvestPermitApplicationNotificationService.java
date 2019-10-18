package fi.riista.feature.permit.application.email;

import fi.riista.feature.mail.MailMessageDTO;
import fi.riista.feature.mail.MailService;
import fi.riista.feature.permit.PermitClientUriFactory;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.net.URI;

@Service
public class HarvestPermitApplicationNotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(HarvestPermitApplicationNotificationService.class);

    @Resource
    private HarvestPermitApplicationNotificationEmailFactory harvestPermitApplicationNotificationEmailFactory;

    @Resource
    private PermitClientUriFactory permitClientUriFactory;

    @Resource
    private MailService mailService;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = Exception.class)
    public void sendNotification(final HarvestPermitApplication application) {
        final URI emailLink = permitClientUriFactory.getAbsoluteAnonymousApplicationUri(application.getUuid());
        final HarvestPermitApplicationNotificationDTO dto = HarvestPermitApplicationNotificationDTO.create(application, emailLink);

        if (StringUtils.isBlank(dto.getContactPersonEmail())) {
            LOG.warn("Contact person email not found for applicationId={}", application.getId());
            return;
        }

        final HarvestPermitApplicationNotificationEmail email =
                harvestPermitApplicationNotificationEmailFactory.create(dto);

        mailService.send(MailMessageDTO.builder()
                .withFrom(mailService.getDefaultFromAddress())
                .withRecipients(email.getRecipients())
                .withSubject(email.getSubject())
                .appendBody("<html><head><meta charset=\"utf-8\"></head><body>")
                .appendBody(email.getBody())
                .appendBody("</body></html>")
                .build());
    }
}
