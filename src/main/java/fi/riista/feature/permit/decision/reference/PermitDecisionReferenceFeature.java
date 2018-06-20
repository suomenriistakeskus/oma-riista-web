package fi.riista.feature.permit.decision.reference;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationRepository;
import fi.riista.feature.permit.application.search.HarvestPermitApplicationSearchDTO;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.PermitDecisionRepository;
import fi.riista.feature.permit.decision.QPermitDecision;
import fi.riista.security.EntityPermission;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.EnumSet;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
public class PermitDecisionReferenceFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private PermitDecisionRepository permitDecisionRepository;

    @Resource
    private HarvestPermitApplicationRepository harvestPermitApplicationRepository;

    @Resource
    private ActiveUserService activeUserService;

    @Transactional(readOnly = true)
    public List<PermitDecisionReferenceDTO> searchReferences(final HarvestPermitApplicationSearchDTO dto) {
        if (CollectionUtils.isEmpty(dto.getStatus())) {
            dto.setStatus(EnumSet.of(
                    HarvestPermitApplicationSearchDTO.StatusSearch.DRAFT,
                    HarvestPermitApplicationSearchDTO.StatusSearch.LOCKED,
                    HarvestPermitApplicationSearchDTO.StatusSearch.PUBLISHED));
        }
        final List<HarvestPermitApplication> applications = harvestPermitApplicationRepository.search(dto);
        return permitDecisionRepository.findAllAsStream(QPermitDecision.permitDecision.application.in(applications))
                .map(PermitDecisionReferenceDTO::create)
                .collect(toList());
    }

    @Transactional(readOnly = true)
    public PermitDecisionReferenceDTO getReference(final long id) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(id, EntityPermission.READ);
        final PermitDecision reference = decision.getReference();
        if (reference == null) {
            throw new NotFoundException();
        }
        return PermitDecisionReferenceDTO.create(reference);
    }

    @Transactional
    public void updateReference(final UpdatePermitDecisionReferenceDTO dto) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(dto.getId(), EntityPermission.UPDATE);
        decision.assertStatus(PermitDecision.Status.DRAFT);
        decision.assertHandler(activeUserService.requireActiveUser());

        final PermitDecision reference = permitDecisionRepository.getOne(dto.getReferenceId());
        decision.setReference(reference);
    }

}
