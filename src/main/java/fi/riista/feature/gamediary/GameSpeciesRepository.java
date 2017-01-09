package fi.riista.feature.gamediary;

import fi.riista.feature.common.repository.BaseRepository;

import java.util.List;
import java.util.Optional;

public interface GameSpeciesRepository extends BaseRepository<GameSpecies, Long> {

    Optional<GameSpecies> findByOfficialCode(int officialCode);

    List<GameSpecies> findBySrvaOrdinalNotNullOrderBySrvaOrdinal();
}
