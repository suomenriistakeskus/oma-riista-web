package fi.riista.feature.common.decision.nomination.revision;

import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.common.decision.nomination.attachment.QNominationDecisionAttachment;
import fi.riista.feature.gis.hta.HirvitalousalueDTO;
import fi.riista.feature.gis.hta.QGISHirvitalousalue;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.types.dsl.Expressions.set;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Repository
public class NominationDecisionRevisionAttachmentRepositoryImpl implements NominationDecisionRevisionAttachmentRepositoryCustom {

    private static final QNominationDecisionRevisionAttachment ATTACHMENT =
            QNominationDecisionRevisionAttachment.nominationDecisionRevisionAttachment;
    private static final QNominationDecisionAttachment DECISION_ATTACHMENT =
            QNominationDecisionAttachment.nominationDecisionAttachment;

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Transactional(readOnly = true)
    public List<NominationDecisionRevisionDTO.AttachmentDTO> findByNominationDecisionRevision(final NominationDecisionRevision revision) {

        return jpqlQueryFactory.select(createDTOProjection())
                .from(ATTACHMENT)
                .join(ATTACHMENT.decisionAttachment, DECISION_ATTACHMENT)
                .where(ATTACHMENT.decisionRevision.eq(revision))
                .fetch();
    }

    @Transactional(readOnly = true)
    public Map<NominationDecisionRevision, Set<NominationDecisionRevisionDTO.AttachmentDTO>> findByNominationDecisionRevisionIn(
            final Collection<NominationDecisionRevision> revisions) {

        if (revisions.isEmpty()) {
            return emptyMap();
        }

        return jpqlQueryFactory
                .from(ATTACHMENT)
                .join(ATTACHMENT.decisionAttachment, DECISION_ATTACHMENT)
                .where(ATTACHMENT.decisionRevision.in(revisions))
                .transform(GroupBy.groupBy(ATTACHMENT.decisionRevision).as(GroupBy.set(createDTOProjection())));
    }

    @Nonnull
    private static ConstructorExpression<NominationDecisionRevisionDTO.AttachmentDTO> createDTOProjection() {
        return Projections.constructor(NominationDecisionRevisionDTO.AttachmentDTO.class,
                ATTACHMENT.id, ATTACHMENT.orderingNumber, DECISION_ATTACHMENT.description);
    }

}
