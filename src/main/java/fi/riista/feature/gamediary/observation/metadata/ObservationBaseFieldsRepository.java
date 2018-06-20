package fi.riista.feature.gamediary.observation.metadata;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.error.NotFoundException;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ObservationBaseFieldsRepository extends BaseRepository<ObservationBaseFields, Long> {

    List<ObservationBaseFields> findByMetadataVersion(int metadataVersion);

    @Query("SELECT o FROM #{#entityName} o" +
            " INNER JOIN o.species s" +
            " WHERE s.officialCode = :gameSpeciesCode" +
            " AND o.metadataVersion = :metadataVersion")
    Optional<ObservationBaseFields> findOne(@Param("gameSpeciesCode") int gameSpeciesCode,
                                            @Param("metadataVersion") int metadataVersion);

    default ObservationBaseFields getOne(final int gameSpeciesCode, final int metadataVersion) {
        return findOne(gameSpeciesCode, metadataVersion).orElseThrow(() -> {
            return new NotFoundException(String.format(
                    "Observation base metadata (v%d) for species with code %d not found",
                    metadataVersion, gameSpeciesCode));
        });
    }
}
