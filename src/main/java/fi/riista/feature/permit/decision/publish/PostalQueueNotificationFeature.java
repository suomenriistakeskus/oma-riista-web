package fi.riista.feature.permit.decision.publish;

import fi.riista.feature.RuntimeEnvironmentUtil;
import fi.riista.feature.mail.MailMessageDTO;
import fi.riista.feature.mail.MailService;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

@Component
public class PostalQueueNotificationFeature {

    private static final Logger LOG = LoggerFactory.getLogger(PostalQueueNotificationFeature.class);

    @Resource
    private HarvestPermitApplicationRepository harvestPermitApplicationRepository;

    @Resource
    private MailService mailService;

    @Resource
    private RuntimeEnvironmentUtil runtimeEnvironmentUtil;

    @Transactional
    public void sendPostalQueueNotification() {
        if (!runtimeEnvironmentUtil.isProductionEnvironment()) {
            LOG.info("Not production env, exiting.");
            return;
        }
        final List<HarvestPermitApplication> results = harvestPermitApplicationRepository.listPostalQueue();
        final int queueLength = results.size();
        if (queueLength > 0) {
            sendMail(queueLength);
        }
    }

    private void sendMail(final int queueLength) {
        final String text = String.format("Oma riista -palvelussa on postitettavia päätöksiä %d kpl", queueLength);
        mailService.send(MailMessageDTO.builder()
                .withFrom(mailService.getDefaultFromAddress())
                .withSubject(text)
                .withRecipients(Collections.singleton("lupahallinto.kirjaamo@riista.fi"))
                .appendBody(text)
                .appendBody("<br><br>Tämä on Oma riista -palvelun automaattisesti muodostama viesti")
                .build());
    }
}
