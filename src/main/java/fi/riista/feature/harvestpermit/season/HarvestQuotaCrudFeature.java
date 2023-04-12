package fi.riista.feature.harvestpermit.season;

import fi.riista.feature.AbstractCrudFeature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Resource;

@Service
public class HarvestQuotaCrudFeature extends AbstractCrudFeature<Long, HarvestQuota, HarvestQuotaDTO> {

    @Resource
    private HarvestQuotaRepository repository;

    @Override
    protected JpaRepository<HarvestQuota, Long> getRepository() {
        return repository;
    }

    @Override
    protected void updateEntity(HarvestQuota entity, HarvestQuotaDTO dto) {
        entity.setQuota(dto.getQuota());
    }

    @Override
    protected HarvestQuotaDTO toDTO(@Nonnull HarvestQuota entity) {
        return HarvestQuotaDTO.create(entity);
    }
}
