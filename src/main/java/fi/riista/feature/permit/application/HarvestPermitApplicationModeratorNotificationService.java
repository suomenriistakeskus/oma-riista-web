package fi.riista.feature.permit.application;

import fi.riista.feature.RuntimeEnvironmentUtil;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.mail.MailMessageDTO;
import fi.riista.feature.mail.MailService;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;

@Component
public class HarvestPermitApplicationModeratorNotificationService {
    private static final Logger LOG = LoggerFactory.getLogger(HarvestPermitApplicationModeratorNotificationService.class);

    @Resource
    private MailService mailService;

    @Resource
    private RuntimeEnvironmentUtil runtimeEnvironmentUtil;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = Exception.class)
    public void sendModeratorNotification(final HarvestPermitApplication application) {
        if (!runtimeEnvironmentUtil.isProductionEnvironment()) {
            LOG.warn("Moderator notification is sent only in production, application id={}", application.getId());
            return;
        }

        if (application.getHarvestPermitCategory().isMooselike()) {
            LOG.warn("No moderator notification for mooselike application id={}", application.getId());
            return;
        }

        mailService.send(MailMessageDTO.builder()
                .withFrom(mailService.getDefaultFromAddress())
                .withSubject(String.format("Uusi hakemus %d", application.getApplicationNumber()))
                .withRecipients(Collections.singleton("lupahallinto.kirjaamo@riista.fi"))
                .appendBody(formatMessageBody(application)).build());
    }

    private String formatMessageBody(final HarvestPermitApplication application) {
        final HarvestPermitCategory harvestPermitCategory = application.getHarvestPermitCategory();
        final Riistanhoitoyhdistys rhy = application.getRhy();
        final Organisation rka = rhy.getParentOrganisation();

        return "<html><head><meta charset=\"utf-8\"></head><body>" +
                String.format("<p>Uusi hakemus %d on jätetty käsiteltäväksi.</p>", application.getApplicationNumber()) +
                String.format("<p>Hakemuksen tyyppi: %s</p>", harvestPermitCategory.getApplicationName().getFinnish()) +
                String.format("<p>Hakemuksen alue: %s</p>", rka.getNameFinnish()) +
                "</body></html>";
    }
}
