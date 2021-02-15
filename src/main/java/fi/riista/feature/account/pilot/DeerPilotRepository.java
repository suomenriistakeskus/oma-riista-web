package fi.riista.feature.account.pilot;

import fi.riista.feature.common.repository.BaseRepository;

import java.util.Optional;

public interface DeerPilotRepository extends BaseRepository<DeerPilot, Long>, DeerPilotRepositoryCustom {

    Optional<DeerPilot> findByHarvestPermitId(final long harvestPermitId);

}
