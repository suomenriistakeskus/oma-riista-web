package fi.riista.feature.harvestregistry;

import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;

import static java.util.Objects.requireNonNull;

@Component
public class HarvestRegistryAuditService {

    @Resource
    private RiistanhoitoyhdistysRepository riistanhoitoyhdistysRepository;

    @Resource
    private HarvestRegistryCoordinatorSearchRepository coordinatorSearchRepository;

    @Async
    @Transactional
    public void addSearch(final @Nonnull HarvestRegistryCoordinatorRequestDTO dto) {
        requireNonNull(dto);
        final Riistanhoitoyhdistys rhy = requireNonNull(riistanhoitoyhdistysRepository.getOne(dto.getRhyId()));

        final HarvestRegistryCoordinatorSearch search = new HarvestRegistryCoordinatorSearch();
        search.setSearchReason(dto.getSearchReason());
        search.setRhyCode(rhy.getOfficialCode());
        search.setGameSpeciesCode(dto.getSpecies());
        search.setBeginDate(dto.getBeginDate());
        search.setEndDate(dto.getEndDate());

        coordinatorSearchRepository.save(search);
    }
}
