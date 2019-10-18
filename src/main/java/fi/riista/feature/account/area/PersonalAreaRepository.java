package fi.riista.feature.account.area;

import fi.riista.feature.common.repository.BaseRepository;

import java.util.Optional;

public interface PersonalAreaRepository extends BaseRepository<PersonalArea, Long> {

    Optional<PersonalArea> findByExternalId(String externalId);

}
