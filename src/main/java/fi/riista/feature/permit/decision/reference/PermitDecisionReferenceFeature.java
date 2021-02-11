package fi.riista.feature.permit.decision.reference;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationRepository;
import fi.riista.feature.permit.application.search.HarvestPermitApplicationSearchDTO;
import fi.riista.feature.common.decision.DecisionStatus;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.PermitDecisionRepository;
import fi.riista.feature.permit.decision.QPermitDecision;
import fi.riista.security.EntityPermission;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

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
    public Slice<PermitDecisionReferenceDTO> searchReferences(final HarvestPermitApplicationSearchDTO dto) {
        if (CollectionUtils.isEmpty(dto.getStatus())) {
            dto.setStatus(EnumSet.of(
                    HarvestPermitApplicationSearchDTO.StatusSearch.DRAFT,
                    HarvestPermitApplicationSearchDTO.StatusSearch.LOCKED,
                    HarvestPermitApplicationSearchDTO.StatusSearch.PUBLISHED));
        }

        final PageRequest pageRequest = dto.asPageRequest();
        final Slice<HarvestPermitApplication> applicationSlice = harvestPermitApplicationRepository.search(dto, pageRequest);
        final Slice<PermitDecision> decisionSlice = permitDecisionRepository.findAllAsSlice(
                QPermitDecision.permitDecision.application.in(applicationSlice.getContent()), pageRequest);

        final List<PermitDecisionReferenceDTO> resultContent = decisionSlice.getContent().stream()
                .map(PermitDecisionReferenceDTO::create)
                .collect(toList());

        return new SliceImpl<>(resultContent, pageRequest, applicationSlice.hasNext());
    }

    @Transactional(readOnly = true)
    public PermitDecisionReferenceDTO getReference(final long id) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(id, EntityPermission.READ);

        return Optional.ofNullable(decision.getReference()).map(PermitDecisionReferenceDTO::create).orElse(null);
    }

    @Transactional
    public void updateReference(final UpdatePermitDecisionReferenceDTO dto) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(dto.getId(), EntityPermission.UPDATE);
        decision.assertEditableBy(activeUserService.requireActiveUser());

        final PermitDecision reference = permitDecisionRepository.getOne(dto.getReferenceId());
        decision.setReference(reference);
    }

}
