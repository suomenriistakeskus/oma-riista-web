package fi.riista.feature.permit.application.bird.cause;

import fi.riista.feature.permit.application.bird.BirdPermitApplication;
import fi.riista.feature.permit.application.bird.BirdPermitApplicationRepository;
import fi.riista.feature.permit.application.bird.BirdPermitApplicationService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.Optional;

@Component
public class BirdPermitApplicationCauseFeature {

    @Resource
    private BirdPermitApplicationService birdPermitApplicationService;

    @Resource
    private BirdPermitApplicationRepository birdPermitApplicationRepository;

    @Transactional(readOnly = true)
    public BirdPermitApplicationCauseDTO getCauseInfo(final long applicationId) {
        final BirdPermitApplication birdApplication =
                birdPermitApplicationService.findForRead(applicationId);

        return Optional.ofNullable(birdApplication)
                .map(BirdPermitApplication::getCause)
                .map(BirdPermitApplicationCauseDTO::createFrom)
                .orElseGet(BirdPermitApplicationCauseDTO::new);
    }

    @Transactional
    public void updateCauseInfo(final long applicationId,
                                final @NotNull BirdPermitApplicationCauseDTO cause) {
        final BirdPermitApplication birdApplication =
                birdPermitApplicationService.findOrCreateForUpdate(applicationId);
        birdApplication.setCause(cause.toEntity());
        birdPermitApplicationRepository.save(birdApplication);
    }

}
