package fi.riista.feature.permit.decision.notification;

import com.github.jknack.handlebars.Handlebars;
import com.google.common.collect.Maps;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.mail.MailMessageDTO;
import fi.riista.feature.mail.MailService;
import fi.riista.feature.permit.PermitClientUriFactory;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.revision.PermitDecisionRevisionReceiver;
import fi.riista.util.DateUtil;
import fi.riista.util.LocalisedString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class PermitDecisionNotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(PermitDecisionNotificationService.class);

    private static final LocalisedString TEMPLATE = LocalisedString.of(
            "decision_notification_permit_holder", "decision_notification_permit_holder.sv");

    private static final LocalisedString TEMPLATE_OTHERS = LocalisedString.of(
            "decision_notification_others", "decision_notification_others.sv");

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @Resource
    private PermitClientUriFactory permitClientUriFactory;

    @Resource
    private Handlebars handlebars;

    @Resource
    private MailService mailService;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void sendNotification(final PermitDecisionRevisionReceiver receiver) {
        if (receiver.isCancelled()) {
            return;
        }
        switch (receiver.getReceiverType()) {
            case CONTACT_PERSON:
                sendToContactPerson(receiver);
                break;
            case OTHER:
                sendToOtherReceiver(receiver);
                break;
            default:
                throw new IllegalStateException("Unknown receiverType:" + receiver.getReceiverType());
        }
        receiver.setSentDate(DateUtil.now());
    }

    private void sendToContactPerson(final PermitDecisionRevisionReceiver receiver) {
        final PermitDecision decision = receiver.getDecisionRevision().getPermitDecision();

        final List<HarvestPermit> existingPermits = harvestPermitRepository.findByPermitDecision(decision);
        if (existingPermits.size() != 1) {
            LOG.error(String.format("Invalid HarvestPermit count %d for decisionId %d",
                    existingPermits.size(), decision.getId()));
            return;
        }
        final HarvestPermit harvestPermit = existingPermits.get(0);
        final Map<String, Object> model = Maps.newHashMap();
        model.put("url", permitClientUriFactory.getAbsolutePermitDashboardUri(harvestPermit).toString());

        final String emailSubject = "Suomen riistakeskuksen päätös noudettavissa Oma riista -palvelusta";

        final MailMessageDTO msg = MailMessageDTO.builder()
                .withFrom(mailService.getDefaultFromAddress())
                .withSubject(emailSubject)
                .withRecipients(Collections.singletonList(receiver.getEmail()))
                .appendBody("<html><head><meta charset=\"utf-8\"></head><body>")
                .appendHandlebarsBody(handlebars, TEMPLATE.getFinnish(), model)
                .appendBody("<hr/>")
                .appendHandlebarsBody(handlebars, TEMPLATE.getSwedish(), model)
                .appendBody("</body></html>")
                .build();
        mailService.send(msg);
    }

    private void sendToOtherReceiver(final PermitDecisionRevisionReceiver receiver) {

        final Map<String, Object> model = Maps.newHashMap();
        model.put("url", permitClientUriFactory.getAbsoluteAnonymousDecisionUri(receiver).toString());

        final String emailSubject = "Suomen riistakeskuksen päätös tiedoksi";

        final MailMessageDTO msg = MailMessageDTO.builder()
                .withFrom(mailService.getDefaultFromAddress())
                .withSubject(emailSubject)
                .withRecipients(Collections.singletonList(receiver.getEmail()))
                .appendBody("<html><head><meta charset=\"utf-8\"></head><body>")
                .appendHandlebarsBody(handlebars, TEMPLATE_OTHERS.getFinnish(), model)
                .appendBody("<hr/>")
                .appendHandlebarsBody(handlebars, TEMPLATE_OTHERS.getSwedish(), model)
                .appendBody("</body></html>")
                .build();
        mailService.send(msg);
    }
}
