package fi.riista.feature.permit.application.search;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.permit.application.HarvestPermitApplicationRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Map;

@Service
public class ModeratorApplicationsTodoFeature {

    @Resource
    private HarvestPermitApplicationRepository harvestPermitApplicationRepository;

    @Resource
    private ActiveUserService activeUserService;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    public ModeratorApplicationsTodoDTO getApplicationsTodoCount() {
        final Long handlerId = activeUserService.requireActiveUserId();
        final Map<Long, Integer> permitsToRenewByHandlerId =
                harvestPermitApplicationRepository.getAnnualPermitsToRenewByHandlerId();

        return new ModeratorApplicationsTodoDTO(
                !permitsToRenewByHandlerId.isEmpty(),
                permitsToRenewByHandlerId.getOrDefault(handlerId, 0));
    }

}
