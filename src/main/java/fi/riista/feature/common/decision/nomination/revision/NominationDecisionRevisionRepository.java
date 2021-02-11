package fi.riista.feature.common.decision.nomination.revision;

import com.querydsl.core.types.dsl.BooleanExpression;
import fi.riista.feature.common.decision.nomination.NominationDecision;
import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.util.F;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Set;

public interface NominationDecisionRevisionRepository extends BaseRepository<NominationDecisionRevision, Long> {

    List<NominationDecisionRevision> findByNominationDecision(final NominationDecision decision);

    default Set<Long> findRevisionsToPublish(final DateTime now){
        final QNominationDecisionRevision REV = QNominationDecisionRevision.nominationDecisionRevision;

        final BooleanExpression pred = REV.cancelled.isFalse()
                .and(REV.publishDate.isNull())
                .and(REV.scheduledPublishDate.loe(now));

        return F.getUniqueIds(findAllAsList(pred));
    }
}
