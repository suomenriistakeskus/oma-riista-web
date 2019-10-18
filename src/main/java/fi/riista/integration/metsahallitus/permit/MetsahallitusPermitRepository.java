package fi.riista.integration.metsahallitus.permit;

import fi.riista.feature.common.repository.BaseRepository;

import java.util.Collection;
import java.util.List;

public interface MetsahallitusPermitRepository extends BaseRepository<MetsahallitusPermit, Long>, MetsahallitusPermitRepositoryCustom {
    List<MetsahallitusPermit> findByPermitIdentifierIn(Collection<String> list);
}
