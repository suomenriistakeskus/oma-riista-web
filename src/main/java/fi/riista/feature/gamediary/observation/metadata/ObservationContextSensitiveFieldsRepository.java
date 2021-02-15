package fi.riista.feature.gamediary.observation.metadata;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.gamediary.observation.ObservationCategory;
import fi.riista.feature.gamediary.observation.ObservationType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ObservationContextSensitiveFieldsRepository
        extends BaseRepository<ObservationContextSensitiveFields, Long> {

    @Query("SELECT o FROM #{#entityName} o" +
            " INNER JOIN o.species s" +
            " WHERE s.officialCode = :gameSpeciesCode" +
            " AND o.metadataVersion = :metadataVersion")
    List<ObservationContextSensitiveFields> findAll(@Param("gameSpeciesCode") int gameSpeciesCode,
                                                    @Param("metadataVersion") int metadataVersion);

    @Query("SELECT o FROM #{#entityName} o" +
            " INNER JOIN o.species s" +
            " WHERE s.officialCode = :gameSpeciesCode" +
            " AND o.observationCategory = :observationCategory" +
            " AND o.observationType = :observationType" +
            " AND o.metadataVersion = :metadataVersion")
    Optional<ObservationContextSensitiveFields> findOne(@Param("gameSpeciesCode") int gameSpeciesCode,
                                                        @Param("observationCategory") ObservationCategory observationCategory,
                                                        @Param("observationType") ObservationType observationType,
                                                        @Param("metadataVersion") int metadataVersion);
}
