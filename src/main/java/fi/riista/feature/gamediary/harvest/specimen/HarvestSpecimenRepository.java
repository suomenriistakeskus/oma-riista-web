package fi.riista.feature.gamediary.harvest.specimen;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.gamediary.harvest.Harvest;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface HarvestSpecimenRepository extends BaseRepository<HarvestSpecimen, Long> {

    List<HarvestSpecimen> findByHarvest(Harvest harvest);

    List<HarvestSpecimen> findByHarvest(Harvest harvest, Sort sort);

}
