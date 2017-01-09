package fi.riista.feature.huntingclub;

import fi.riista.feature.common.repository.BaseRepository;

public interface HuntingClubRepository extends BaseRepository<HuntingClub, Long> {

    HuntingClub findByOfficialCode(String officialCode);
}
