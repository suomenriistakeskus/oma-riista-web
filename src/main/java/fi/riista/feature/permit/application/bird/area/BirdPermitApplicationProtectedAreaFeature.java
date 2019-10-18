package fi.riista.feature.permit.application.bird.area;

import fi.riista.feature.gis.GISQueryService;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.bird.BirdPermitApplication;
import fi.riista.feature.permit.application.bird.BirdPermitApplicationRepository;
import fi.riista.feature.permit.application.bird.BirdPermitApplicationService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

@Component
public class BirdPermitApplicationProtectedAreaFeature {

    @Resource
    private BirdPermitApplicationService birdPermitApplicationService;

    @Resource
    private BirdPermitApplicationRepository birdPermitApplicationRepository;

    @Resource
    private GISQueryService gisQueryService;

    // PROTECTED AREA

    @Transactional(readOnly = true)
    public BirdPermitApplicationProtectedAreaDTO getProtectedAreaInfo(final long applicationId) {
        final BirdPermitApplication birdApplication = birdPermitApplicationService.findForRead(applicationId);

        return BirdPermitApplicationProtectedAreaDTO.createFrom(birdApplication);
    }

    @Transactional
    public void updateProtectedArea(final long applicationId,
                                    final @NotNull BirdPermitApplicationProtectedAreaDTO protectedArea) {
        final BirdPermitApplication birdPermitApplication =
                birdPermitApplicationService.findOrCreateForUpdate(applicationId);

        final HarvestPermitApplication application = birdPermitApplication.getHarvestPermitApplication();
        application.setRhy(gisQueryService.findRhyByLocation(protectedArea.getGeoLocation()));

        birdPermitApplication.setProtectedArea(protectedArea.toEntity());
        birdPermitApplicationRepository.save(birdPermitApplication);
    }
}
