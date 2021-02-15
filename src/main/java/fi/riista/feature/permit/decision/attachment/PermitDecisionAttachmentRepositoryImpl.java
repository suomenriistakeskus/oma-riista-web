package fi.riista.feature.permit.decision.attachment;

import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.permit.decision.revision.PermitDecisionRevision;
import fi.riista.feature.permit.decision.revision.QPermitDecisionRevisionAttachment;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Repository
public class PermitDecisionAttachmentRepositoryImpl implements PermitDecisionAttachmentRepositoryCustom {

    private static final QPermitDecisionAttachment ATTACHMENT = QPermitDecisionAttachment.permitDecisionAttachment;
    private static final QPermitDecisionRevisionAttachment REVISION_ATTACHMENT =
            QPermitDecisionRevisionAttachment.permitDecisionRevisionAttachment;

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Override
    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<PermitDecisionAttachment> findListedAttachmentsByPermitDecisionRevision(final PermitDecisionRevision revision) {
        return jpqlQueryFactory.select(ATTACHMENT)
                .from(REVISION_ATTACHMENT)
                .innerJoin(REVISION_ATTACHMENT.decisionAttachment, ATTACHMENT)
                .where(REVISION_ATTACHMENT.decisionRevision.eq(revision))
                .where(REVISION_ATTACHMENT.orderingNumber.isNotNull())
                .orderBy(REVISION_ATTACHMENT.orderingNumber.asc())
                .fetch();
    }
}
