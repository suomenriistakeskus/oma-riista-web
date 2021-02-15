package fi.riista.feature.permit.application.importing.justification;

import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationAuthorizationService;
import fi.riista.feature.permit.application.importing.ImportingPermitApplication;
import fi.riista.feature.permit.application.importing.ImportingPermitApplicationRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;

import static java.util.Objects.requireNonNull;

@Component
public class ImportingPermitApplicationJustificationFeature {

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Resource
    private ImportingPermitApplicationRepository importingPermitApplicationRepository;

    @Transactional(readOnly = true)
    public ImportingPermitApplicationJustificationDTO getJustification(final long applicationId) {

        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.readApplication(applicationId);

        return new ImportingPermitApplicationJustificationDTO(
                importingPermitApplicationRepository.findByHarvestPermitApplication(application));

    }

    @Transactional
    public void updateJustification(final long applicationId,
                                    @Nonnull final ImportingPermitApplicationJustificationDTO dto) {

        requireNonNull(dto);
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.updateApplication(applicationId);
        final ImportingPermitApplication importingPermitApplication =
                importingPermitApplicationRepository.findByHarvestPermitApplication(application);

        importingPermitApplication.setCountryOfOrigin(dto.getCountryOfOrigin());
        importingPermitApplication.setDetails(dto.getDetails());
        importingPermitApplication.setPurpose(dto.getPurpose());
        importingPermitApplication.setRelease(dto.getRelease());

    }

}
