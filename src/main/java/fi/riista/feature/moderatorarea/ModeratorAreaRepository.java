package fi.riista.feature.moderatorarea;

import fi.riista.feature.common.repository.BaseRepository;

import java.util.Optional;

public interface ModeratorAreaRepository extends BaseRepository<ModeratorArea, Long> {

    Optional<ModeratorArea> findByExternalId(String externalId);

}
