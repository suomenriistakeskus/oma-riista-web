package fi.riista.feature.huntingclub.permit.endofhunting.moosesummary;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.huntingclub.HuntingClub;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MooseHuntingSummaryRepository
        extends BaseRepository<MooseHuntingSummary, Long> {

    @Query("select s from #{#entityName} s" +
            " join s.club c" +
            " join s.harvestPermit hp" +
            " where c.id = ?1 and hp.id = ?2")
    Optional<MooseHuntingSummary> findByClubIdAndPermitId(final long clubId, final long permitId);

    List<MooseHuntingSummary> findByHarvestPermit(HarvestPermit harvestPermit);

    List<MooseHuntingSummary> findAllByClub(HuntingClub club);
}
