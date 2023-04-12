package fi.riista.feature.permit.application.carnivore.justification;

import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountRepository;
import fi.riista.feature.permit.application.carnivore.CarnivorePermitApplication;
import fi.riista.feature.permit.application.carnivore.CarnivorePermitApplicationService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;

import static java.util.Objects.requireNonNull;

@Component
public class CarnivorePermitApplicationJustificationFeature {

    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository speciesAmountRepository;

    @Resource
    private CarnivorePermitApplicationService carnivorePermitApplicationService;

    @Transactional(readOnly = true)
    public CarnivorePermitApplicationJustificationDTO getJustification(final long applicationId) {
        final CarnivorePermitApplication carnivoreApplication =
                carnivorePermitApplicationService.findForRead(applicationId);

        return speciesAmountRepository
                .findAtMostOneByHarvestPermitApplication(carnivoreApplication.getHarvestPermitApplication())
                .map(spa -> new CarnivorePermitApplicationJustificationDTO(carnivoreApplication, spa))
                .orElseGet(CarnivorePermitApplicationJustificationDTO::new);
    }

    @Transactional
    public void updateJustification(final long applicationId,
                                    @Nonnull final CarnivorePermitApplicationJustificationDTO dto) {

        requireNonNull(dto);

        final CarnivorePermitApplication carnivoreApplication =
                carnivorePermitApplicationService.findForUpdate(applicationId);

        carnivoreApplication.setAlternativeMeasures(dto.getAlternativeMeasures());

        final HarvestPermitApplicationSpeciesAmount existingSpeciesAmount =
                requireSpeciesAmount(carnivoreApplication.getHarvestPermitApplication());

        existingSpeciesAmount.setPopulationAmount(dto.getPopulationAmount());
    }

    private HarvestPermitApplicationSpeciesAmount requireSpeciesAmount(final HarvestPermitApplication application) {
        return speciesAmountRepository
                .findAtMostOneByHarvestPermitApplication(application)
                .orElseThrow(NotFoundException::new);
    }
}
