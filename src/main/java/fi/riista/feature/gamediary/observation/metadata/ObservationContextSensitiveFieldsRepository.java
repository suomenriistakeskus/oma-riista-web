package fi.riista.feature.gamediary.observation.metadata;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.gamediary.GameSpecies;

import java.util.List;

public interface ObservationContextSensitiveFieldsRepository
        extends BaseRepository<ObservationContextSensitiveFields, Long> {

    List<ObservationContextSensitiveFields> findBySpeciesAndMetadataVersion(GameSpecies species, int metadataVersion);

}
