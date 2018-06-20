package fi.riista.feature.huntingclub.area;

import com.google.common.base.Preconditions;
import fi.riista.feature.AbstractCrudFeature;
import fi.riista.feature.gis.metsahallitus.MetsahallitusProperties;
import fi.riista.feature.huntingclub.HuntingClubRepository;
import fi.riista.feature.huntingclub.group.HuntingClubGroupRepository;
import fi.riista.security.EntityPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.security.SecureRandom;
import java.util.Objects;

@Component
public class HuntingClubAreaCrudFeature extends AbstractCrudFeature<Long, HuntingClubArea, HuntingClubAreaDTO> {

    @Resource
    private HuntingClubAreaRepository huntingClubAreaRepository;

    @Resource
    private HuntingClubRepository huntingClubRepository;

    @Resource
    private HuntingClubGroupRepository huntingClubGroupRepository;

    @Resource
    private HuntingClubAreaDTOTransformer huntingClubAreaDTOTransformer;

    @Resource
    private SecureRandom secureRandom;

    @Resource
    private MetsahallitusProperties metsahallitusProperties;

    @Override
    protected JpaRepository<HuntingClubArea, Long> getRepository() {
        return huntingClubAreaRepository;
    }

    @Override
    protected HuntingClubAreaDTO toDTO(@Nonnull final HuntingClubArea entity) {
        return huntingClubAreaDTOTransformer.apply(entity);
    }

    @Override
    protected void updateEntity(final HuntingClubArea entity, final HuntingClubAreaDTO dto) {
        if (entity.isNew()) {
            entity.setClub(huntingClubRepository.getOne(dto.getClubId()));
            entity.setActive(true);

        } else {
            final long attachedGroups = huntingClubGroupRepository.countByHuntingArea(entity);

            if (attachedGroups > 0) {
                Preconditions.checkArgument(Objects.equals(dto.getHuntingYear(), entity.getHuntingYear()),
                        "huntingYear cannot be changed");
            }
        }
        entity.setHuntingYear(dto.getHuntingYear());
        entity.setMetsahallitusYear(HuntingClubArea.calculateMetsahallitusYear(
                dto.getHuntingYear(), metsahallitusProperties.getLatestMetsahallitusYear()));
        entity.setNameFinnish(dto.getNameFI());
        entity.setNameSwedish(dto.getNameSV());

        if (entity.getExternalId() == null) {
            entity.generateAndStoreExternalId(secureRandom);
        }
    }

    @Transactional
    public void setActiveStatus(final long huntingClubAreaId, final boolean active) {
        final HuntingClubArea huntingClubArea = requireEntity(huntingClubAreaId, EntityPermission.UPDATE);

        if (!active) {
            final long attachedGroups = huntingClubGroupRepository.countByHuntingArea(huntingClubArea);

            if (attachedGroups > 0) {
                throw new IllegalStateException("area cannot be deactivated");
            }
        }

        huntingClubArea.setActive(active);
    }
}
