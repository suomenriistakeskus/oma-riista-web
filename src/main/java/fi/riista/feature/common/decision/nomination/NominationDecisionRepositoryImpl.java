package fi.riista.feature.common.decision.nomination;

import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.account.user.QSystemUser;
import fi.riista.feature.account.user.SystemUser;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Repository
public class NominationDecisionRepositoryImpl implements NominationDecisionRepositoryCustom {

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Override
    @Transactional(readOnly = true)
    public List<SystemUser> listHandlers() {
        final QNominationDecision DECISION = QNominationDecision.nominationDecision;
        final QSystemUser HANDLER = QSystemUser.systemUser;

        return jpqlQueryFactory.select(HANDLER)
                .from(DECISION)
                .join(DECISION.handler, HANDLER)
                .where(DECISION.handler.isNotNull())
                .distinct()
                .fetch();
    }

    @Override
    @Transactional(readOnly = true)
    public Slice<NominationDecision> search(final NominationDecisionSearchDTO dto, final Pageable pageRequest) {
        if (dto.getDecisionNumber() != null) {
            return new SliceImpl<>(findByDecisionNumber(dto.getDecisionNumber()), pageRequest, false);
        }

        return NominationDecisionSearchQueryBuilder.from(jpqlQueryFactory, dto)
                .slice(pageRequest);
    }

    private List<NominationDecision> findByDecisionNumber(final int decisionNumber) {
        final QNominationDecision DECISION = QNominationDecision.nominationDecision;

        return jpqlQueryFactory.selectFrom(DECISION)
                .where(DECISION.decisionNumber.eq(decisionNumber))
                .fetch();
    }
}
