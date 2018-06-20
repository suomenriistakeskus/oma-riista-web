package fi.riista.feature.permit.decision.job;

import com.querydsl.core.types.dsl.BooleanExpression;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.harvestpermit.HarvestPermitDecisionSynchronizer;
import fi.riista.feature.permit.decision.notification.PermitDecisionNotificationService;
import fi.riista.feature.permit.decision.revision.PermitDecisionRevision;
import fi.riista.feature.permit.decision.revision.PermitDecisionRevisionReceiver;
import fi.riista.feature.permit.decision.revision.PermitDecisionRevisionReceiverRepository;
import fi.riista.feature.permit.decision.revision.PermitDecisionRevisionRepository;
import fi.riista.feature.permit.decision.revision.QPermitDecisionRevision;
import fi.riista.feature.permit.decision.revision.QPermitDecisionRevisionReceiver;
import fi.riista.feature.permit.invoice.PermitInvoiceService;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Set;

@Component
public class PermitDecisionPublishingFeature {

    @Resource
    private HarvestPermitDecisionSynchronizer harvestPermitDecisionSynchronizer;

    @Resource
    private PermitDecisionRevisionRepository permitDecisionRevisionRepository;

    @Resource
    private PermitDecisionRevisionReceiverRepository permitDecisionRevisionReceiverRepository;

    @Resource
    private PermitDecisionNotificationService permitDecisionNotificationService;

    @Resource
    private PermitInvoiceService permitInvoiceService;

    @Transactional(readOnly = true)
    public Set<Long> findDecisionRevisionsToGenerateHarvestPermits() {
        final QPermitDecisionRevision REV = QPermitDecisionRevision.permitDecisionRevision;
        final BooleanExpression pred = REV.cancelled.isFalse()
                .and(REV.publishDate.isNull())
                .and(REV.scheduledPublishDate.loe(DateUtil.now()));
        final List<PermitDecisionRevision> revisionToPublish = permitDecisionRevisionRepository.findAllAsList(pred);
        return F.getUniqueIds(revisionToPublish);
    }

    @Transactional(rollbackFor = IOException.class)
    public void publishRevision(final long revisionId) throws IOException {
        final PermitDecisionRevision revision = permitDecisionRevisionRepository.getOne(revisionId);
        final PermitDecision decision = revision.getPermitDecision();

        harvestPermitDecisionSynchronizer.synchronize(decision);

        revision.setPublishDate(DateUtil.now());
        decision.setStatusPublished();

        permitInvoiceService.synchronizeProcessingInvoice(decision);

        emailRevisionReceivers(revisionId);
    }

    private void emailRevisionReceivers(final Long revisionId) {
        final QPermitDecisionRevisionReceiver RECEIVER = QPermitDecisionRevisionReceiver.permitDecisionRevisionReceiver;
        final BooleanExpression pred = RECEIVER.decisionRevision.id.eq(revisionId)
                .and(RECEIVER.cancelled.isFalse())
                .and(RECEIVER.email.isNotNull())
                .and(RECEIVER.sentDate.isNull());
        final List<PermitDecisionRevisionReceiver> receivers = permitDecisionRevisionReceiverRepository.findAllAsList(pred);
        receivers.forEach(receiver -> permitDecisionNotificationService.sendNotification(receiver));
    }
}
