package fi.riista.feature.permit.decision.publish;

import com.github.jknack.handlebars.Handlebars;
import com.querydsl.core.types.dsl.Expressions;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.mail.MailMessageDTO;
import fi.riista.feature.mail.MailService;
import fi.riista.feature.permit.PermitClientUriFactory;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.revision.PermitDecisionRevisionReceiver;
import fi.riista.feature.permit.decision.revision.PermitDecisionRevisionReceiverRepository;
import fi.riista.feature.permit.decision.revision.QPermitDecisionRevisionReceiver;
import fi.riista.util.DateUtil;
import fi.riista.util.LocalisedString;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;

@Service
public class PermitDecisionNotificationService {

    private static final LocalisedString TEMPLATE = LocalisedString.of(
            "decision_notification_permit_holder", "decision_notification_permit_holder.sv");

    private static final LocalisedString TEMPLATE_OTHERS = LocalisedString.of(
            "decision_notification_others", "decision_notification_others.sv");

    private static final String SUBJECT_CONTACT_PERSON = "Suomen riistakeskuksen päätös noudettavissa Oma riista -palvelusta";
    private static final String SUBJECT_OTHERS = "Suomen riistakeskuksen päätös tiedoksi";

    @Resource
    private MostRelevantHarvestPermitLookupService mostRelevantHarvestPermitLookupService;

    @Resource
    private PermitDecisionRevisionReceiverRepository permitDecisionRevisionReceiverRepository;

    @Resource
    private PermitClientUriFactory permitClientUriFactory;

    @Resource
    private Handlebars handlebars;

    @Resource
    private MailService mailService;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void emailRevisionReceivers(final long revisionId) {
        findPendingReceivers(revisionId).forEach(receiver -> {
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
        });
    }

    private List<PermitDecisionRevisionReceiver> findPendingReceivers(final Long revisionId) {
        final QPermitDecisionRevisionReceiver RECEIVER = QPermitDecisionRevisionReceiver.permitDecisionRevisionReceiver;

        return permitDecisionRevisionReceiverRepository.findAllAsList(Expressions.allOf(
                RECEIVER.decisionRevision.id.eq(revisionId),
                RECEIVER.cancelled.isFalse(),
                RECEIVER.email.isNotNull(),
                RECEIVER.sentDate.isNull()));
    }

    private void sendToContactPerson(final PermitDecisionRevisionReceiver receiver) {
        final PermitDecision decision = receiver.getDecisionRevision().getPermitDecision();
        final HarvestPermit harvestPermit = mostRelevantHarvestPermitLookupService.lookupMostRelevant(decision);

        if (harvestPermit != null) {
            final URI emailLink = permitClientUriFactory.getAbsolutePermitDashboardUri(harvestPermit);
            sendEmail(receiver.getEmail(), SUBJECT_CONTACT_PERSON, TEMPLATE, emailLink);

        } else {
            final URI emailLink = permitClientUriFactory.getAbsoluteAnonymousDecisionUri(receiver);
            sendEmail(receiver.getEmail(), SUBJECT_CONTACT_PERSON, TEMPLATE_OTHERS, emailLink);
        }
    }

    private void sendToOtherReceiver(final PermitDecisionRevisionReceiver receiver) {
        final URI emailLink = permitClientUriFactory.getAbsoluteAnonymousDecisionUri(receiver);
        sendEmail(receiver.getEmail(), SUBJECT_OTHERS, TEMPLATE_OTHERS, emailLink);
    }

    private void sendEmail(final String email,
                           final String subject,
                           final LocalisedString template,
                           final URI link) {
        final Map<String, Object> model = singletonMap("url", link.toString());

        mailService.send(MailMessageDTO.builder()
                .withFrom(mailService.getDefaultFromAddress())
                .withRecipients(singletonList(email))
                .withSubject(subject)
                .appendBody("<html><head><meta charset=\"utf-8\"></head><body>")
                .appendHandlebarsBody(handlebars, template.getFinnish(), model)
                .appendBody("<hr/>")
                .appendHandlebarsBody(handlebars, template.getSwedish(), model)
                .appendBody("</body></html>")
                .build());
    }
}
