package fi.riista.feature.moderatorarea;

import fi.riista.feature.AbstractCrudFeature;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.RiistakeskuksenAlueRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.security.SecureRandom;

@Component
public class ModeratorAreaCrudFeature extends AbstractCrudFeature<Long, ModeratorArea, ModeratorAreaDTO> {

    @Resource
    private ModeratorAreaDTOTransformer moderatorAreaDTOTransformer;

    @Resource
    private ModeratorAreaRepository moderatorAreaRepository;

    @Resource
    private RiistakeskuksenAlueRepository riistakeskuksenAlueRepository;

    @Resource
    private SecureRandom secureRandom;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private GISZoneRepository zoneRepository;

    @Override
    protected void updateEntity(final ModeratorArea entity, final ModeratorAreaDTO dto) {
        if (entity.isNew()) {
            entity.generateAndStoreExternalId(secureRandom);
            entity.setModerator(activeUserService.requireActiveUser());
            entity.setZone(zoneRepository.save(new GISZone()));
        }

        final RiistakeskuksenAlue rka = riistakeskuksenAlueRepository.findByOfficialCode(dto.getRkaCode());

        entity.setName(dto.getName());
        entity.setYear(dto.getYear());
        entity.setRka(rka);
    }

    @Override
    protected ModeratorAreaDTO toDTO(@Nonnull final ModeratorArea entity) {
        return moderatorAreaDTOTransformer.apply(entity);
    }

    @Override
    protected JpaRepository<ModeratorArea, Long> getRepository() {
        return moderatorAreaRepository;
    }
}
