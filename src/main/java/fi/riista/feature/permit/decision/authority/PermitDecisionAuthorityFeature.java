package fi.riista.feature.permit.decision.authority;

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
import fi.riista.feature.common.decision.nomination.QNominationDecision;
import fi.riista.feature.organization.QOrganisation;
import fi.riista.feature.organization.rhy.QRiistanhoitoyhdistys;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.QPermitDecision;
import fi.riista.feature.permit.decision.document.PermitDecisionTextService;
import fi.riista.security.EntityPermission;
import fi.riista.util.F;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
public class PermitDecisionAuthorityFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private PermitDecisionAuthorityRepository permitDecisionAuthorityRepository;

    @Resource
    private PermitDecisionTextService permitDecisionTextService;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Transactional(readOnly = true)
    public DecisionAuthoritiesDTO getAuthorities(final long decisionId) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(decisionId, EntityPermission.READ);

        final DecisionAuthoritiesDTO dto = new DecisionAuthoritiesDTO();
        dto.setId(decisionId);
        dto.setPresenter(DecisionAuthorityDTO.create(
                        F.mapNullable(decision.getPresenter(), PermitDecisionAuthority::getAuthorityDetails)));
        dto.setDecisionMaker(DecisionAuthorityDTO.create(
                        F.mapNullable(decision.getDecisionMaker(), PermitDecisionAuthority::getAuthorityDetails)));
        return dto;
    }

    @Transactional
    public void updateAuthorities(final long decisionId, final DecisionAuthoritiesDTO dto) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(decisionId, EntityPermission.UPDATE);
        decision.assertHandler(activeUserService.requireActiveUser());

        remove(decision.getPresenter());
        remove(decision.getDecisionMaker());

        decision.setPresenter(null);
        decision.setDecisionMaker(null);

        decision.setPresenter(create(decision, dto.getPresenter()));
        decision.setDecisionMaker(create(decision, dto.getDecisionMaker()));

        decision.getDocument().setAdditionalInfo(permitDecisionTextService.generateAdditionalInfo(decision));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    public List<DecisionRkaAuthorityDTO> listByPermitDecision(final long decisionId) {
        final QDecisionRkaAuthority RKA_DELIVERY = QDecisionRkaAuthority.decisionRkaAuthority;
        final QPermitDecision DECISION = QPermitDecision.permitDecision;
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

    private void remove(final PermitDecisionAuthority a) {
        if (a != null) {
            permitDecisionAuthorityRepository.delete(a);
        }
    }

    private PermitDecisionAuthority create(final PermitDecision decision, final DecisionAuthorityDTO authority) {
        if (authority == null) {
            return null;
        }
        final PermitDecisionAuthority entity = new PermitDecisionAuthority();
        entity.setPermitDecision(decision);

        final DecisionRkaAuthorityDetails authorityDetails = new DecisionRkaAuthorityDetails();
        authorityDetails.setFirstName(authority.getFirstName());
        authorityDetails.setLastName(authority.getLastName());
        authorityDetails.setTitle(authority.getTitle());
        authorityDetails.setPhoneNumber(authority.getPhoneNumber());
        authorityDetails.setEmail(authority.getEmail());
        entity.setAuthorityDetails(authorityDetails);

        return permitDecisionAuthorityRepository.save(entity);
    }
}
