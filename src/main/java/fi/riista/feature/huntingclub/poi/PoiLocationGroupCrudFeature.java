package fi.riista.feature.huntingclub.poi;

import fi.riista.feature.AbstractCrudFeature;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.area.HuntingClubAreaRepository;
import fi.riista.security.EntityPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;

@Service
public class PoiLocationGroupCrudFeature extends AbstractCrudFeature<Long, PoiLocationGroup, PoiLocationGroupDTO> {

    @Resource
    private PoiLocationGroupRepository repository;

    @Resource
    private PointOfInterestService poiService;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private PoiLocationService poiLocationService;

    @Resource
    private HuntingClubAreaRepository huntingClubAreaRepository;

    @Resource
    private PoiLocationGroupTransformer transformer;

    @Transactional(readOnly = true)
    public List<PoiLocationGroupDTO> list(final long clubId) {
        final HuntingClub club = requireEntityService.requireHuntingClub(clubId, EntityPermission.READ);
        final List<PoiLocationGroup> poiLocationGroups = poiService.listForClub(club);
        return transformer.transform(poiLocationGroups);
    }

    @Override
    protected JpaRepository<PoiLocationGroup, Long> getRepository() {
        return repository;
    }

    @Override
    protected void updateEntity(final PoiLocationGroup entity, final PoiLocationGroupDTO dto) {
        if (entity.isNew()) {
            final HuntingClub club = requireEntityService.requireHuntingClub(dto.getClubId(), EntityPermission.READ);
            final PoiIdAllocation idAllocation = poiService.getOrCreate(club);

            entity.setPoiIdAllocation(idAllocation);
            entity.setType(dto.getType());

            entity.setVisibleId(idAllocation.getNextId());
        }

        entity.setDescription(dto.getDescription());
        entity.forceRevisionUpdate();
    }

    @Override
    protected void afterUpdate(final PoiLocationGroup entity, final PoiLocationGroupDTO dto) {
        poiLocationService.updateLocations(entity, dto.getLocations());
    }

    @Transactional
    @Override
    protected void delete(final PoiLocationGroup entity) {
        poiLocationService.deleteByPoi(entity);
        huntingClubAreaRepository.removeConnectionsToPoi(entity.getId());
        super.delete(entity);
    }

    @Override
    protected PoiLocationGroupDTO toDTO(@Nonnull final PoiLocationGroup entity) {
        return transformer.apply(entity);
    }
}
