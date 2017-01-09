package fi.riista.feature.harvestpermit.season;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.gamediary.GameSpecies;

public interface MooselikePriceRepository extends BaseRepository<MooselikePrice, Long> {

    MooselikePrice getByHuntingYearAndGameSpecies(int huntingYear, GameSpecies gameSpecies);
}
