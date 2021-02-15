package fi.riista.feature.permit.decision.publish;

import com.github.jknack.handlebars.Handlebars;
import com.querydsl.core.types.dsl.Expressions;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.harvestpermit.QHarvestPermit;
import fi.riista.feature.mail.MailMessageDTO;
import fi.riista.feature.mail.MailService;
import fi.riista.feature.permit.PermitClientUriFactory;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.feature.common.decision.GrantStatus;
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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;

@Service
public class PermitDecisionNotificationService {

    private enum DecisionCategory {
        NOT_APPLICABLE,
        REJECTION,
        APPROVAL,
        RENEWAL,
        CANCEL_RENEWAL
    }

    private static final LocalisedString TEMPLATE = LocalisedString.of(
            "decision_notification_permit_holder", "decision_notification_permit_holder.sv");

    private static final LocalisedString TEMPLATE_RENEWED_PERMIT = LocalisedString.of(
            "decision_notification_renewal", "decision_notification_renewal.sv");

    private static final LocalisedString TEMPLATE_CANCEL_RENEWAL_PERMIT = LocalisedString.of(
            "decision_notification_cancel_renewal", "decision_notification_cancel_renewal.sv");

    private static final LocalisedString TEMPLATE_OTHERS = LocalisedString.of(
            "decision_notification_others", "decision_notification_others.sv");

    /*package*/ static final String SUBJECT_CONTACT_PERSON = "Suomen riistakeskuksen päätös noudettavissa Oma riista " +
            "-palvelusta";
    /*package*/ static final String SUBJECT_OTHERS = "Suomen riistakeskuksen päätös tiedoksi";

    @Resource
    private MostRelevantHarvestPermitLookupService mostRelevantHarvestPermitLookupService;

    @Resource
    private PermitDecisionRevisionReceiverRepository permitDecisionRevisionReceiverRepository;

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @Resource
    private PermitClientUriFactory permitClientUriFactory;

    @Resource
    private Handlebars handlebars;

    @Resource
    private MailService mailService;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void emailRevisionReceivers(final PermitDecision decision, final long revisionId) {
        final DecisionCategory decisionCategory = deduceDecisionCategory(decision);
        final List<PermitDecisionRevisionReceiver> pendingReceivers = findPendingReceivers(revisionId);

        pendingReceivers.forEach(receiver -> {
            switch (receiver.getReceiverType()) {
                case CONTACT_PERSON:
                    sendToContactPerson(receiver, decisionCategory);
                    break;
                case OTHER:
                    if (decisionCategory != DecisionCategory.RENEWAL) {
                        sendToOtherReceiver(receiver);
                    }
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

    private void sendToContactPerson(final PermitDecisionRevisionReceiver receiver,
                                     final DecisionCategory decisionCategory) {
        switch (decisionCategory) {
            case APPROVAL:
            case REJECTION: {
                doSendToContactPersonForPermit(receiver, TEMPLATE);
                break;
            }
            case RENEWAL: {
                doSendToContactPersonForPermit(receiver, TEMPLATE_RENEWED_PERMIT);
                break;
            }
            case CANCEL_RENEWAL: {
                final URI emailLink =
                        permitClientUriFactory.getAbsoluteAnonymousDecisionDownloadPageUri(receiver.getUuid());
                sendEmail(receiver.getEmail(), SUBJECT_CONTACT_PERSON, TEMPLATE_CANCEL_RENEWAL_PERMIT, emailLink);
                break;
            }
            case NOT_APPLICABLE: {
                final URI emailLink =
                        permitClientUriFactory.getAbsoluteAnonymousDecisionDownloadPageUri(receiver.getUuid());
                sendEmail(receiver.getEmail(), SUBJECT_CONTACT_PERSON, TEMPLATE_OTHERS, emailLink);
                break;
            }
        }
    }

    private void doSendToContactPersonForPermit(final PermitDecisionRevisionReceiver receiver,
                                                final LocalisedString template) {
        final HarvestPermit harvestPermit =
                mostRelevantHarvestPermitLookupService.lookupMostRelevant(receiver.getDecisionRevision().getPermitDecision());
        checkState(harvestPermit != null);
        final URI emailLink = permitClientUriFactory.getAbsolutePermitDashboardUri(harvestPermit);
        sendEmail(receiver.getEmail(), SUBJECT_CONTACT_PERSON, template, emailLink);
    }

    private void sendToOtherReceiver(final PermitDecisionRevisionReceiver receiver) {
        final URI emailLink = permitClientUriFactory.getAbsoluteAnonymousDecisionDownloadPageUri(receiver.getUuid());

        sendEmail(receiver.getEmail(), SUBJECT_OTHERS, TEMPLATE_OTHERS, emailLink);
    }


    private void sendEmail(final String email,
                           final String subject,
                           final LocalisedString template,
                           final URI decisionLink) {
        final Map<String, Object> model = singletonMap("url", decisionLink.toString());

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

    private boolean isRenewalDecision(final PermitDecision decision) {
        checkArgument(decision.getDecisionType() == PermitDecision.DecisionType.HARVEST_PERMIT);
        checkArgument(decision.getGrantStatus() != GrantStatus.REJECTED);

        final QHarvestPermit PERMIT = QHarvestPermit.harvestPermit;
        return harvestPermitRepository.count(
                PERMIT.permitDecision.eq(decision)) > 1;
    }

    private DecisionCategory deduceDecisionCategory(final PermitDecision decision) {
        if (decision.getDecisionType() == PermitDecision.DecisionType.HARVEST_PERMIT) {
            if (decision.getGrantStatus() == GrantStatus.REJECTED) {
                return DecisionCategory.REJECTION;
            }
            if (decision.getPermitTypeCode().equals(PermitTypeCode.ANNUAL_UNPROTECTED_BIRD) && isRenewalDecision(decision)) {
                return DecisionCategory.RENEWAL;
            }
            return DecisionCategory.APPROVAL;
        }
        return DecisionCategory.NOT_APPLICABLE;
    }

}
