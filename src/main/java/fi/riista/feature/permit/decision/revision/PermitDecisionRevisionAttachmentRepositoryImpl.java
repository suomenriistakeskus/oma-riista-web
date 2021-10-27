package fi.riista.feature.permit.decision.revision;

import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.permit.decision.QPermitDecision;
import fi.riista.feature.permit.decision.attachment.QPermitDecisionAttachment;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.feature.storage.metadata.QPersistentFileMetadata;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import static fi.riista.feature.common.decision.DecisionStatus.PUBLISHED;
import static fi.riista.feature.permit.PermitTypeCode.CARNIVORE_PERMIT_CODES;

@Repository
public class PermitDecisionRevisionAttachmentRepositoryImpl implements PermitDecisionRevisionAttachmentRepositoryCustom {

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Transactional(readOnly = true)
    public List<PersistentFileMetadata> findLatestPublicDecisionAttachmentsPdf(int decisionNumber) {

        final QPermitDecisionAttachment ATTACHMENT = QPermitDecisionAttachment.permitDecisionAttachment;
        final QPermitDecisionRevisionAttachment REV_ATTACHMENT = QPermitDecisionRevisionAttachment.permitDecisionRevisionAttachment;

        final QPermitDecisionRevision REV = QPermitDecisionRevision.permitDecisionRevision;
        final QPermitDecision DECISION = QPermitDecision.permitDecision;
        final QPersistentFileMetadata METADATA = QPersistentFileMetadata.persistentFileMetadata;

        return jpqlQueryFactory
                .select(METADATA)
                .from(REV_ATTACHMENT)
                .innerJoin(REV_ATTACHMENT.decisionAttachment, ATTACHMENT)
                .innerJoin(REV_ATTACHMENT.decisionRevision, REV)
                .innerJoin(REV.permitDecision, DECISION)
                .innerJoin(ATTACHMENT.attachmentMetadata, METADATA)
                // ordering number declares that attachment is public. If ordering number is null then the attachment is internal use only
                .where(ATTACHMENT.orderingNumber.isNotNull())
                .where(DECISION.decisionNumber.eq(decisionNumber))
                .where(DECISION.permitTypeCode.in(CARNIVORE_PERMIT_CODES))
                .where(DECISION.status.eq(PUBLISHED))
                .orderBy(ATTACHMENT.orderingNumber.asc())
                .fetch();
    }
}
