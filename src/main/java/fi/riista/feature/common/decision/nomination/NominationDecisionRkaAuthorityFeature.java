package fi.riista.feature.common.decision.nomination;

import com.querydsl.jpa.JPQLQueryFactory;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQuery;
import fi.riista.feature.common.decision.authority.rka.DecisionRkaAuthorityDTO;
import fi.riista.feature.common.decision.authority.rka.QDecisionRkaAuthority;
import fi.riista.feature.organization.QOrganisation;
import fi.riista.feature.organization.rhy.QRiistanhoitoyhdistys;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
public class NominationDecisionRkaAuthorityFeature {

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    public List<DecisionRkaAuthorityDTO> listByDecision(final long decisionId) {
        final QDecisionRkaAuthority RKA_DELIVERY = QDecisionRkaAuthority.decisionRkaAuthority;
        final QNominationDecision DECISION = QNominationDecision.nominationDecision;
        final QRiistanhoitoyhdistys RHY = QRiistanhoitoyhdistys.riistanhoitoyhdistys;
        final QOrganisation RKA = QOrganisation.organisation;
        final SQLQuery<Long> subQuery = SQLExpressions.select(RKA.id).from(DECISION)
                .join(DECISION.rhy, RHY)
                .join(RHY.parentOrganisation, RKA)
                .where(DECISION.id.eq(decisionId));

        return jpqlQueryFactory.select(RKA_DELIVERY).from(RKA_DELIVERY)
                .where(RKA_DELIVERY.rka.id.eq(subQuery))
                .fetch()
                .stream().map(DecisionRkaAuthorityDTO::new)
                .collect(toList());
    }
}
