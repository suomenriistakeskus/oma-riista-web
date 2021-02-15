package fi.riista.feature.organization.rhy;

import fi.riista.feature.RequireEntityService;
import fi.riista.security.EntityPermission;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import static java.util.Objects.requireNonNull;

@Component
public class RhySrvaCallRingRotationFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Transactional(readOnly = true)
    public RhySrvaRotationDTO getRotation(final long rhyId) {
        final Riistanhoitoyhdistys riistanhoitoyhdistys =
                requireEntityService.requireRiistanhoitoyhdistys(rhyId, EntityPermission.READ);
        return  RhySrvaRotationDTO.create(riistanhoitoyhdistys);
    }

    @Transactional
    public void updateRotation(final long rhyId, final RhySrvaRotationDTO dto) {
        final Riistanhoitoyhdistys riistanhoitoyhdistys =
                requireEntityService.requireRiistanhoitoyhdistys(rhyId, EntityPermission.UPDATE);
        requireNonNull(dto);
        riistanhoitoyhdistys.setSrvaRotation(dto.getSrvaRotation());
        riistanhoitoyhdistys.setRotationStart(dto.getStartDate());
    }
}
