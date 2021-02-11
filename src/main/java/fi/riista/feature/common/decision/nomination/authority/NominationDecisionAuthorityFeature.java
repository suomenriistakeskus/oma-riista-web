package fi.riista.feature.common.decision.nomination.authority;

import com.querydsl.jpa.JPQLQueryFactory;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQuery;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.common.decision.authority.DecisionAuthoritiesDTO;
import fi.riista.feature.common.decision.authority.DecisionAuthorityDTO;
import fi.riista.feature.common.decision.authority.DecisionRkaAuthorityDetails;
import fi.riista.feature.common.decision.authority.rka.DecisionRkaAuthorityDTO;
import fi.riista.feature.common.decision.authority.rka.QDecisionRkaAuthority;
import fi.riista.feature.common.decision.nomination.NominationDecision;
import fi.riista.feature.common.decision.nomination.QNominationDecision;
import fi.riista.feature.common.decision.nomination.document.NominationDecisionTextService;
import fi.riista.feature.organization.QOrganisation;
import fi.riista.feature.organization.rhy.QRiistanhoitoyhdistys;
import fi.riista.security.EntityPermission;
import fi.riista.util.F;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
public class NominationDecisionAuthorityFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private NominationDecisionAuthorityRepository nominationDecisionAuthorityRepository;

    @Resource
    private NominationDecisionTextService nominationDecisionTextService;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Transactional(readOnly = true)
    public DecisionAuthoritiesDTO getAuthorities(final long decisionId) {
        final NominationDecision decision = requireEntityService.requireNominationDecision(decisionId,
                EntityPermission.READ);

        final DecisionAuthoritiesDTO dto = new DecisionAuthoritiesDTO();
        dto.setId(decisionId);
        dto.setPresenter(DecisionAuthorityDTO.create(
                F.mapNullable(decision.getPresenter(), NominationDecisionAuthority::getAuthorityDetails)));
        dto.setDecisionMaker(DecisionAuthorityDTO.create(
                F.mapNullable(decision.getDecisionMaker(), NominationDecisionAuthority::getAuthorityDetails)));
        return dto;
    }

    @Transactional
    public void updateAuthorities(final long decisionId, final DecisionAuthoritiesDTO dto) {
        final NominationDecision decision = requireEntityService.requireNominationDecision(decisionId,
                EntityPermission.UPDATE);
        decision.assertHandler(activeUserService.requireActiveUser());

        remove(decision.getPresenter());
        remove(decision.getDecisionMaker());

        decision.setPresenter(null);
        decision.setDecisionMaker(null);

        decision.setPresenter(create(decision, dto.getPresenter()));
        decision.setDecisionMaker(create(decision, dto.getDecisionMaker()));

        decision.getDocument().setAdditionalInfo(nominationDecisionTextService.generateAdditionalInfo(decision));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    public List<DecisionRkaAuthorityDTO> listByNominationDecision(final long decisionId) {
        final QDecisionRkaAuthority RKA_DELIVERY = QDecisionRkaAuthority.decisionRkaAuthority;
        final QNominationDecision DECISION = QNominationDecision.nominationDecision;
        final QRiistanhoitoyhdistys RHY = QRiistanhoitoyhdistys.riistanhoitoyhdistys;
        final QOrganisation RKA = QOrganisation.organisation;
        final SQLQuery<Long> subQuery = SQLExpressions.select(RKA.id).from(DECISION)
                .join(DECISION.rhy, RHY)
                .join(RHY.parentOrganisation, RKA)
                .where(DECISION.id.eq(decisionId));

        return jpqlQueryFactory.selectFrom(RKA_DELIVERY)
                .where(RKA_DELIVERY.rka.id.eq(subQuery))
                .fetch()
                .stream().map(DecisionRkaAuthorityDTO::new)
                .collect(toList());
    }

    private void remove(final NominationDecisionAuthority a) {
        if (a != null) {
            nominationDecisionAuthorityRepository.delete(a);
        }
    }

    private NominationDecisionAuthority create(final NominationDecision decision,
                                               final DecisionAuthorityDTO authority) {
        if (authority == null) {
            return null;
        }
        final NominationDecisionAuthority entity = new NominationDecisionAuthority();
        entity.setNominationDecision(decision);

        final DecisionRkaAuthorityDetails authorityDetails = new DecisionRkaAuthorityDetails();
        authorityDetails.setFirstName(authority.getFirstName());
        authorityDetails.setLastName(authority.getLastName());
        authorityDetails.setTitle(authority.getTitle());
        authorityDetails.setPhoneNumber(authority.getPhoneNumber());
        authorityDetails.setEmail(authority.getEmail());
        entity.setAuthorityDetails(authorityDetails);

        return nominationDecisionAuthorityRepository.save(entity);
    }
}
