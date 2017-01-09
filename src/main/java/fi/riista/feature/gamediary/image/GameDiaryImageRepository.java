package fi.riista.feature.gamediary.image;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.srva.SrvaEvent;

import java.util.List;

public interface GameDiaryImageRepository extends BaseRepository<GameDiaryImage, Long> {

    List<GameDiaryImage> findByHarvest(Harvest harvest);

    List<GameDiaryImage> findByObservation(Observation observation);

    List<GameDiaryImage> findBySrvaEvent(SrvaEvent eventEntity);

}
