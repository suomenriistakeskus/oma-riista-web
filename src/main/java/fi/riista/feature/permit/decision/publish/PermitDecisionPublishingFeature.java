package fi.riista.feature.permit.decision.publish;

import com.querydsl.core.types.dsl.BooleanExpression;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.revision.PermitDecisionRevision;
import fi.riista.feature.permit.decision.revision.PermitDecisionRevisionRepository;
import fi.riista.feature.permit.decision.revision.QPermitDecisionRevision;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Set;

@Component
public class PermitDecisionPublishingFeature {

    @Resource
    private HarvestPermitDecisionSynchronizer harvestPermitDecisionSynchronizer;

    @Resource
    private PermitDecisionRevisionRepository permitDecisionRevisionRepository;

    @Resource
    private PermitDecisionNotificationService permitDecisionNotificationService;

    @Resource
    private PermitDecisionInvoiceSynchronizer permitDecisionInvoiceSynchronizer;

    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @Transactional(readOnly = true)
    public Set<Long> findDecisionRevisionsToPublish() {
        final QPermitDecisionRevision REV = QPermitDecisionRevision.permitDecisionRevision;
        final BooleanExpression pred = REV.cancelled.isFalse()
                .and(REV.publishDate.isNull())
                .and(REV.scheduledPublishDate.loe(DateUtil.now()));

        return F.getUniqueIds(permitDecisionRevisionRepository.findAllAsList(pred));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @Transactional(rollbackFor = IOException.class)
    public void publishRevision(final long revisionId) throws IOException {
        final PermitDecisionRevision revision = permitDecisionRevisionRepository.getOne(revisionId);
        final PermitDecision decision = revision.getPermitDecision();

        harvestPermitDecisionSynchronizer.synchronize(decision);

        revision.setPublishDate(DateUtil.now());
        decision.setStatusPublished();

        permitDecisionInvoiceSynchronizer.synchronizeProcessingInvoice(decision);
        permitDecisionNotificationService.emailRevisionReceivers(decision, revisionId);
    }
}
