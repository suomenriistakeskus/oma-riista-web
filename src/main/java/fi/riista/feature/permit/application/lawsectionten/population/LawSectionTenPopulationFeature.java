package fi.riista.feature.permit.application.lawsectionten.population;

import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationAuthorizationService;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.derogation.population.DerogationPermitApplicationSpeciesPopulationDTO;
import fi.riista.feature.permit.application.derogation.population.DerogationPermitApplicationSpeciesPopulationFeature;
import fi.riista.feature.permit.application.lawsectionten.LawSectionTenPermitApplication;
import fi.riista.feature.permit.application.lawsectionten.LawSectionTenPermitApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Optional.ofNullable;

@Service
public class LawSectionTenPopulationFeature {

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Resource
    private LawSectionTenPermitApplicationRepository lawSectionTenPermitApplicationRepository;

    @Transactional(readOnly = true)
    public LawSectionTenPopulationDTO getSpeciesPopulation(final long applicationId) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.readApplication(applicationId);

        return ofNullable(lawSectionTenPermitApplicationRepository.findByHarvestPermitApplication(application))
                .map(sectionTenApplication -> LawSectionTenPopulationDTO.createFrom(application, sectionTenApplication))
                .orElseThrow(NotFoundException::new);

    }

    @Transactional
    public void saveSpeciesPopulation(final long applicationId,
                                      final LawSectionTenPopulationDTO dto) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.updateApplication(applicationId);

        final LawSectionTenPermitApplication sectionTenApplication =
                lawSectionTenPermitApplicationRepository.findByHarvestPermitApplication(application);

        checkState(sectionTenApplication != null, "Section ten application not found");

        sectionTenApplication.setJustification(dto.getJustification());
        sectionTenApplication.setPopulationDescription(dto.getPopulationDescription());
        sectionTenApplication.setDamagesCaused(dto.getDamagesCaused());
        sectionTenApplication.setTransferredAnimalOrigin(dto.getTransferredAnimalOrigin());
        sectionTenApplication.setTransferredAnimalAmount(dto.getTransferredAnimalAmount());
    }
}
