package fi.riista.feature.common.decision.nomination;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.api.decision.nomination.NominationDecisionHandlingStatisticsDTO;
import fi.riista.feature.account.user.QSystemUser;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.decision.DecisionStatus;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.QOrganisation;
import fi.riista.feature.organization.rhy.QRiistanhoitoyhdistys;
import io.vavr.Tuple;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fi.riista.util.Collect.tuplesToMap;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;

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

    @Override
    @Transactional(readOnly = true)
    public List<NominationDecision> search(final NominationDecisionSearchDTO dto) {
        if (dto.getDecisionNumber() != null) {
            return findByDecisionNumber(dto.getDecisionNumber());
        }

        return NominationDecisionSearchQueryBuilder.from(jpqlQueryFactory, dto).list();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NominationDecisionHandlingStatisticsDTO> getStatistics(final int year) {
        final QNominationDecision DECISION = QNominationDecision.nominationDecision;
        final QRiistanhoitoyhdistys RHY = QRiistanhoitoyhdistys.riistanhoitoyhdistys;
        final QOrganisation RKA = QOrganisation.organisation;

        final NumberExpression<Integer> k =
                countingCase(DECISION.status.eq(DecisionStatus.DRAFT), "K");
        final NumberExpression<Integer> v =
                countingCase(DECISION.status.ne(DecisionStatus.DRAFT), "V");

        final Map<String, Map<String, Integer>> statusMap = jpqlQueryFactory.select(RKA.officialCode, k, v)
                .from(DECISION)
                .join(DECISION.rhy, RHY)
                .join(RHY.parentOrganisation, RKA)
                .where(DECISION.decisionYear.eq(year))
                .groupBy(RKA.id, RKA.officialCode, RKA.nameFinnish, RKA.nameSwedish)
                .orderBy(RKA.officialCode.asc())
                .fetch().stream().map(t -> {
                    final Map<String, Integer> hkv = new HashMap<>();
                    hkv.put("K", t.get(k));
                    hkv.put("V", t.get(v));
                    final String rkaCode = t.get(RKA.officialCode);
                    return Tuple.of(rkaCode, hkv);
                }).collect(tuplesToMap());
        return jpqlQueryFactory
                .selectFrom(RKA)
                .where(RKA.organisationType.eq(OrganisationType.RKA))
                .orderBy(RKA.officialCode.asc())
                .fetch()
                .stream()
                .map(rka -> {
                    final OrganisationNameDTO rkaDto = OrganisationNameDTO.createWithOfficialCode(rka);
                    final Map<String, Integer> rkaStatus = statusMap.getOrDefault(rka.getOfficialCode(), emptyMap());
                    return new NominationDecisionHandlingStatisticsDTO(rkaDto, rkaStatus.getOrDefault("K", 0),
                            rkaStatus.getOrDefault("V", 0));
                })
                .collect(toList());
    }

    private List<NominationDecision> findByDecisionNumber(final int decisionNumber) {
        final QNominationDecision DECISION = QNominationDecision.nominationDecision;

        return jpqlQueryFactory.selectFrom(DECISION)
                .where(DECISION.decisionNumber.eq(decisionNumber))
                .fetch();
    }

    private static NumberExpression<Integer> countingCase(final BooleanExpression predicate, final String alias) {
        return new CaseBuilder().when(predicate).then(1).otherwise(0).sum().as(alias);
    }
}
