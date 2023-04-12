package fi.riista.feature.huntingclub.deercensus;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.huntingclub.HuntingClub;

import java.util.List;

public interface DeerCensusRepository extends BaseRepository<DeerCensus, Long> {

    List<DeerCensus> findAllByHuntingClub(HuntingClub huntingClub);
}
