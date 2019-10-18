package fi.riista.feature.account.area;

import fi.riista.feature.AbstractCrudFeature;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.gis.zone.GISZoneRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.security.SecureRandom;

@Component
public class PersonalAreaCrudFeature extends AbstractCrudFeature<Long, PersonalArea, PersonalAreaDTO> {

    @Resource
    private PersonalAreaDTOTransformer personalAreaDTOTransformer;

    @Resource
    private PersonalAreaRepository personalAreaRepository;

    @Resource
    private SecureRandom secureRandom;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private GISZoneRepository zoneRepository;

    @Override
    protected void updateEntity(final PersonalArea entity, final PersonalAreaDTO dto) {
        if (entity.isNew()) {
            entity.generateAndStoreExternalId(secureRandom);
            entity.setPerson(activeUserService.requireActivePerson());
            entity.setZone(zoneRepository.save(new GISZone()));
        }

        entity.setName(dto.getName());
    }

    @Override
    protected PersonalAreaDTO toDTO(@Nonnull final PersonalArea entity) {
        return personalAreaDTOTransformer.apply(entity);
    }

    @Override
    protected JpaRepository<PersonalArea, Long> getRepository() {
        return personalAreaRepository;
    }
}
