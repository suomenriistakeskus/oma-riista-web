package fi.riista.feature.harvestregistry;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.gamediary.harvest.Harvest;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface HarvestRegistryItemRepository extends BaseRepository<HarvestRegistryItem, Long>, HarvestRegistryItemRepositoryCustom{

    @Query(value = "SELECT item FROM HarvestRegistryItem item WHERE item.harvest IN ?1")
    List<HarvestRegistryItem> findByHarvestId(Collection<Harvest> harvests);

    @Modifying
    @Query(value = "DELETE FROM HarvestRegistryItem item WHERE item.harvest  IN ?1")
    void deleteByHarvestId(Collection<Harvest> harvestharvestsIds);
}
