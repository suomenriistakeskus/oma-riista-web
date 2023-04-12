package fi.riista.feature.harvestregistry.external;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;

import static java.util.Objects.requireNonNull;

@Component
public class HarvestRegistryExternalAuditService {

    @Resource
    private HarvestRegistryExternalRequestRepository harvestRegistryExternalRequestRepository;

    @Resource
    private HarvestRegistryExternalRequestSpeciesRepository harvestRegistryExternalRequestSpeciesRepository;

    @Async
    @Transactional
    public void logRequest(final @Nonnull HarvestRegistryExternalRequestDTO dto) {
        requireNonNull(dto);
        final HarvestRegistryExternalRequest externalRequest = new HarvestRegistryExternalRequest();
        externalRequest.setReason(dto.getReason());
        externalRequest.setRemoteUser(dto.getRemoteUser());
        externalRequest.setRemoteAddress(dto.getRemoteAddress());
        externalRequest.setBeginDate(dto.getBeginDate());
        externalRequest.setEndDate(dto.getEndDate());
        externalRequest.setAllSpecies(dto.isAllSpecies());
        externalRequest.setMunicipalityCode(dto.getMunicipalityCode());
        externalRequest.setRkaCode(dto.getRkaCode());
        externalRequest.setRhyCode(dto.getRhyCode());
        externalRequest.setShooterHunterNumber(dto.getShooterHunterNumber());
        externalRequest.setPage(dto.getPage());
        externalRequest.setPageSize(dto.getPageSize());

        final HarvestRegistryExternalRequest savedExternalRequest = harvestRegistryExternalRequestRepository.save(externalRequest);
        dto.getSpecies().forEach((species) -> {
            final HarvestRegistryExternalRequestSpecies harvestRegistryExternalRequestSpecies = new HarvestRegistryExternalRequestSpecies();
            harvestRegistryExternalRequestSpecies.setSpecies(species);
            harvestRegistryExternalRequestSpecies.setRequest(savedExternalRequest);
            harvestRegistryExternalRequestSpeciesRepository.save(harvestRegistryExternalRequestSpecies);
        });
    }
}
