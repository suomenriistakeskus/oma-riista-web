package fi.riista.feature.common.decision.nomination.reference;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.common.decision.DecisionStatus;
import fi.riista.feature.common.decision.nomination.NominationDecision;
import fi.riista.feature.common.decision.nomination.NominationDecisionRepository;
import fi.riista.feature.common.decision.nomination.NominationDecisionSearchDTO;
import fi.riista.feature.permit.application.HarvestPermitApplicationRepository;
import fi.riista.security.EntityPermission;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

@Component
public class NominationDecisionReferenceFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private NominationDecisionRepository nominationDecisionRepository;

    @Resource
    private ActiveUserService activeUserService;

    @Transactional(readOnly = true)
    public Slice<NominationDecisionReferenceDTO> searchReferences(final NominationDecisionSearchDTO dto) {

        checkState(activeUserService.isModeratorOrAdmin());

        final Slice<NominationDecision> slice = nominationDecisionRepository.search(dto, dto.asPageRequest());

        return slice.map(NominationDecisionReferenceDTO::create);
    }

    @Transactional(readOnly = true)
    public NominationDecisionReferenceDTO getReference(final long id) {
        final NominationDecision decision = requireEntityService.requireNominationDecision(id, EntityPermission.READ);

        return Optional.ofNullable(decision.getReference()).map(NominationDecisionReferenceDTO::create).orElse(null);
    }

    @Transactional
    public void updateReference(final UpdateNominationDecisionReferenceDTO dto) {
        final NominationDecision decision = requireEntityService.requireNominationDecision(dto.getId(), EntityPermission.UPDATE);
        decision.assertEditableBy(activeUserService.requireActiveUser());

        final NominationDecision reference = nominationDecisionRepository.getOne(dto.getReferenceId());
        decision.setReference(reference);
    }

}
