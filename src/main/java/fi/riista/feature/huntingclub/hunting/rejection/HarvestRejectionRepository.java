package fi.riista.feature.huntingclub.hunting.rejection;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface HarvestRejectionRepository extends BaseRepository<HarvestRejection, Long> {
    @Modifying
    @Query("DELETE FROM HarvestRejection r WHERE r.group = ?1 and r.harvest = ?2")
    void deleteByGroup(HuntingClubGroup group, Harvest harvest);

    Optional<HarvestRejection> findByGroupAndHarvest(HuntingClubGroup group, Harvest harvest);

    List<HarvestRejection> findByGroup(HuntingClubGroup group);
}
