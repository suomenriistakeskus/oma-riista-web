package fi.riista.feature.huntingclub.area;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.huntingclub.HuntingClub;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface HuntingClubAreaRepository extends BaseRepository<HuntingClubArea, Long> {
    @Query("select distinct o.huntingYear FROM #{#entityName} o WHERE o.club= ?1")
    List<Integer> listHuntingYears(HuntingClub club);

    Optional<HuntingClubArea> findByExternalId(String externalId);
}
