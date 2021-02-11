package fi.riista.feature.common.decision.nomination;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.common.decision.AppealStatus;
import fi.riista.feature.common.decision.DecisionStatus;
import fi.riista.feature.common.decision.nomination.revision.QNominationDecisionRevision;
import fi.riista.feature.common.repository.BaseRepositoryImpl;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.rhy.QRiistanhoitoyhdistys;
import fi.riista.util.F;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class NominationDecisionSearchQueryBuilder {

    private static final QNominationDecision DECISION = QNominationDecision.nominationDecision;

    private final JPQLQueryFactory queryFactory;
    private final String rhyOfficialCode;
    private final Integer year;
    private final Long handlerId;
    private final Set<NominationDecisionSearchDTO.StatusSearch> statuses;
    private final Set<NominationDecision.NominationDecisionType> decisionTypes;
    private final Set<AppealStatus> appealStatuses;
    private final Set<OccupationType> occupationTypes;


    public static NominationDecisionSearchQueryBuilder from(final JPQLQueryFactory queryFactory,
                                                            final NominationDecisionSearchDTO dto) {
        return new NominationDecisionSearchQueryBuilder(queryFactory, dto);
    }

    private NominationDecisionSearchQueryBuilder(final JPQLQueryFactory queryFactory,
                                                 final NominationDecisionSearchDTO dto) {
        this.queryFactory = queryFactory;
        this.year = dto.getYear();
        this.rhyOfficialCode = dto.getRhyOfficialCode();
        this.appealStatuses = dto.getAppealStatuses();
        this.decisionTypes = dto.getDecisionTypes();
        this.occupationTypes = dto.getOccupationTypes();
        this.handlerId = dto.getHandlerId();
        this.statuses = dto.getStatuses();

    }

    private JPQLQuery<NominationDecision> build() {

        final JPQLQuery<NominationDecision> query = queryFactory.selectFrom(DECISION);

        if (rhyOfficialCode != null) {
            final QRiistanhoitoyhdistys RHY = QRiistanhoitoyhdistys.riistanhoitoyhdistys;
            query.join(DECISION.rhy, RHY);
            query.where(DECISION.rhy.officialCode.eq(rhyOfficialCode));
        }

        if (year != null) {
            query.where(DECISION.decisionYear.eq(year));
        }

        if (!F.isNullOrEmpty(statuses)) {
            final BooleanBuilder b = new BooleanBuilder();
            for (NominationDecisionSearchDTO.StatusSearch s : statuses) {
                switch (s) {
                    case DRAFT:
                        b.or(DECISION.status.eq(DecisionStatus.DRAFT));
                        break;
                    case LOCKED:
                        b.or(DECISION.status.eq(DecisionStatus.LOCKED));
                        break;
                    case PUBLISHED:
                        b.or(DECISION.status.eq(DecisionStatus.PUBLISHED));
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown status: " + s);
                }
            }
            query.where(b.getValue());
        }

        if (!F.isNullOrEmpty(decisionTypes)) {
            query.where(DECISION.decisionType.in(decisionTypes));
        }

        if (!F.isNullOrEmpty(appealStatuses)) {
            query.where(DECISION.appealStatus.in(appealStatuses));
        }

        if (!F.isNullOrEmpty(occupationTypes)) {
            query.where(DECISION.occupationType.in(occupationTypes));
        }

        final QNominationDecisionRevision REV = QNominationDecisionRevision.nominationDecisionRevision;

        if (handlerId != null) {
            final BooleanExpression currentHandler = DECISION.handler.id.eq(handlerId);
            final BooleanExpression revisionCreatedByUser = JPAExpressions.selectOne()
                    .from(REV)
                    .where(REV.nominationDecision.eq(DECISION))
                    .where(REV.auditFields.createdByUserId.eq(handlerId))
                    .exists();

            return query.where(currentHandler.or(revisionCreatedByUser));
        }

        return query;
    }

    public Slice<NominationDecision> slice(final Pageable pageRequest) {
        Objects.requireNonNull(pageRequest);

        final JPQLQuery<NominationDecision> query = build()
                .orderBy(DECISION.decisionYear.desc(), DECISION.decisionNumber.desc())
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize() + 1);

        return BaseRepositoryImpl.toSlice(query.fetch(), pageRequest);
    }

    public List<NominationDecision> list() {

        return build()
                .orderBy(DECISION.decisionYear.desc(), DECISION.decisionNumber.desc())
                .fetch();
    }
}
