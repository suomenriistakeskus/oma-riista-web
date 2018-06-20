package fi.riista.feature.permit.decision.authority;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.document.PermitDecisionTextService;
import fi.riista.security.EntityPermission;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

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

    @Transactional(readOnly = true)
    public PermitDecisionAuthoritiesDTO getAuthorities(final long decisionId) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(decisionId, EntityPermission.READ);

        final PermitDecisionAuthoritiesDTO dto = new PermitDecisionAuthoritiesDTO();
        dto.setId(decisionId);
        dto.setPresenter(PermitDecisionAuthorityDTO.create(decision.getPresenter()));
        dto.setDecisionMaker(PermitDecisionAuthorityDTO.create(decision.getDecisionMaker()));
        return dto;
    }

    @Transactional
    public void updateAuthorities(final long decisionId, final PermitDecisionAuthoritiesDTO dto) {
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

    private void remove(final PermitDecisionAuthority a) {
        if (a != null) {
            permitDecisionAuthorityRepository.delete(a);
        }
    }

    private PermitDecisionAuthority create(final PermitDecision decision, final PermitDecisionAuthorityDTO authority) {
        if (authority == null) {
            return null;
        }
        final PermitDecisionAuthority entity = new PermitDecisionAuthority();
        entity.setPermitDecision(decision);
        entity.setFirstName(authority.getFirstName());
        entity.setLastName(authority.getLastName());
        entity.setTitle(authority.getTitle());
        entity.setPhoneNumber(authority.getPhoneNumber());
        entity.setEmail(authority.getEmail());
        return permitDecisionAuthorityRepository.save(entity);
    }
}
