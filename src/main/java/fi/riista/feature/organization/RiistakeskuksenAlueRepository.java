package fi.riista.feature.organization;

import fi.riista.feature.common.repository.BaseRepository;

public interface RiistakeskuksenAlueRepository extends BaseRepository<RiistakeskuksenAlue, Long> {
    RiistakeskuksenAlue findByOfficialCode(String officialCode);
}
