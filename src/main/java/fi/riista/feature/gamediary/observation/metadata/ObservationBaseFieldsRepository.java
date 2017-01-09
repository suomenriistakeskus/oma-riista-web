package fi.riista.feature.gamediary.observation.metadata;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.observation.metadata.ObservationBaseFields;

import java.util.Optional;

public interface ObservationBaseFieldsRepository extends BaseRepository<ObservationBaseFields, Long> {

    Optional<ObservationBaseFields> findBySpeciesAndMetadataVersion(GameSpecies species, int metadataVersion);

}
