package fi.riista.feature.common.decision.nomination.attachment;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.common.decision.nomination.NominationDecision;
import fi.riista.feature.common.decision.nomination.QNominationDecision;
import fi.riista.feature.common.decision.nomination.revision.NominationDecisionRevision;
import fi.riista.feature.common.decision.nomination.revision.QNominationDecisionRevisionAttachment;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Repository
public class NominationDecisionAttachmentRepositoryImpl implements NominationDecisionAttachmentRepositoryCustom{

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<NominationDecisionAttachment> findOrderedByNominationDecision(final NominationDecision decision) {

        return fetch(decision, false);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<NominationDecisionAttachment> findAllByNominationDecision(final NominationDecision decision) {

        return fetch(decision, true);
    }

    private List<NominationDecisionAttachment> fetch(final NominationDecision decision, final boolean all) {
        final QNominationDecisionAttachment ATTACHMENT = QNominationDecisionAttachment.nominationDecisionAttachment;
        final QNominationDecision DECISION = QNominationDecision.nominationDecision;

        final BooleanBuilder predicate = new BooleanBuilder(ATTACHMENT.lifecycleFields.deletionTime.isNull());
        if (!all) {
            predicate.and(ATTACHMENT.orderingNumber.isNotNull());
        }

        return jpqlQueryFactory.selectFrom(ATTACHMENT)
                .innerJoin(ATTACHMENT.nominationDecision, DECISION)
                .where(DECISION.eq(decision))
                .where(predicate)
                .orderBy(ATTACHMENT.orderingNumber.asc().nullsLast())
                .fetch();
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<NominationDecisionAttachment> findListedAttachmentsByNominationDecisionRevision(final NominationDecisionRevision revision) {
        final QNominationDecisionAttachment ATTACHMENT = QNominationDecisionAttachment.nominationDecisionAttachment;
        final QNominationDecisionRevisionAttachment REVISION_ATTACHMENT =
                QNominationDecisionRevisionAttachment.nominationDecisionRevisionAttachment;

        return jpqlQueryFactory.select(ATTACHMENT)
                .from(REVISION_ATTACHMENT)
                .innerJoin(REVISION_ATTACHMENT.decisionAttachment, ATTACHMENT)
                .where(REVISION_ATTACHMENT.decisionRevision.eq(revision))
                .where(REVISION_ATTACHMENT.orderingNumber.isNotNull())
                .orderBy(REVISION_ATTACHMENT.orderingNumber.asc())
                .fetch();
    }
}
