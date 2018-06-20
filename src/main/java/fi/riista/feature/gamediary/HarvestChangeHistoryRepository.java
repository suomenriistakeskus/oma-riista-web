package fi.riista.feature.gamediary;

import fi.riista.feature.gamediary.harvest.Harvest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HarvestChangeHistoryRepository extends JpaRepository<HarvestChangeHistory, Long> {
    List<HarvestChangeHistory> findByHarvest(Harvest harvest);
}
