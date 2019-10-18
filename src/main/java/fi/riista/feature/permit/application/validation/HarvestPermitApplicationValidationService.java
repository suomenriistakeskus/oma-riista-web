package fi.riista.feature.permit.application.validation;

import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.amendment.AmendmentApplicationData;
import fi.riista.feature.permit.application.amendment.AmendmentApplicationDataRepository;
import fi.riista.feature.permit.application.bird.BirdPermitApplication;
import fi.riista.feature.permit.application.bird.BirdPermitApplicationRepository;
import fi.riista.feature.permit.application.bird.BirdPermitApplicationValidator;
import fi.riista.feature.permit.application.carnivore.CarnivorePermitApplication;
import fi.riista.feature.permit.application.carnivore.CarnivorePermitApplicationRepository;
import fi.riista.feature.permit.application.carnivore.CarnivorePermitApplicationValidator;
import fi.riista.feature.permit.application.mooselike.MooselikePermitApplicationValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class HarvestPermitApplicationValidationService {

    @Resource
    private AmendmentApplicationDataRepository amendmentApplicationDataRepository;

    @Resource
    private BirdPermitApplicationRepository birdPermitApplicationRepository;

    @Resource
    private CarnivorePermitApplicationRepository carnivorePermitApplicationRepository;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void validateContent(final HarvestPermitApplication application) {
        switch (application.getHarvestPermitCategory()) {
            case MOOSELIKE:
                MooselikePermitApplicationValidator.validateContent(application);
                break;
            case MOOSELIKE_NEW:
                final AmendmentApplicationData data = amendmentApplicationDataRepository.getByApplication(application);
                HarvestPermitAmendmentApplicationValidator.validateContent(application, data);
                break;
            case BIRD:
                final BirdPermitApplication birdApplication =
                        birdPermitApplicationRepository.findByHarvestPermitApplication(application);
                BirdPermitApplicationValidator.validateContent(application, birdApplication);
                break;
            case LARGE_CARNIVORE_WOLF:
            case LARGE_CARNIVORE_LYNX_PORONHOITO:
            case LARGE_CARNIVORE_LYNX:
            case LARGE_CARNIVORE_BEAR:
                final CarnivorePermitApplication carnivoreApplication =
                        carnivorePermitApplicationRepository.findByHarvestPermitApplication(application);
                CarnivorePermitApplicationValidator.validateContent(application, carnivoreApplication);
                break;
            default:
                throw new IllegalArgumentException(
                        "Cannot validate application for type " + application.getHarvestPermitCategory());
        }
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void validateApplicationForSending(final HarvestPermitApplication application) {
        switch (application.getHarvestPermitCategory()) {
            case MOOSELIKE:
                MooselikePermitApplicationValidator.validateForSending(application);
                break;
            case MOOSELIKE_NEW:
                final AmendmentApplicationData data = amendmentApplicationDataRepository.getByApplication(application);
                HarvestPermitAmendmentApplicationValidator.validateForSending(application, data);
                break;
            case BIRD:
                final BirdPermitApplication birdApplication =
                        birdPermitApplicationRepository.findByHarvestPermitApplication(application);
                BirdPermitApplicationValidator.validateForSending(application, birdApplication);
                break;
            case LARGE_CARNIVORE_WOLF:
            case LARGE_CARNIVORE_LYNX_PORONHOITO:
            case LARGE_CARNIVORE_LYNX:
            case LARGE_CARNIVORE_BEAR:
                final CarnivorePermitApplication carnivoreApplication =
                    carnivorePermitApplicationRepository.findByHarvestPermitApplication(application);
                CarnivorePermitApplicationValidator.validateForSending(application, carnivoreApplication);
                break;
            default:
                throw new IllegalArgumentException(
                        "Cannot validate application for type " + application.getHarvestPermitCategory());
        }
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void validateApplicationForAmend(final HarvestPermitApplication application) {
        switch (application.getHarvestPermitCategory()) {
            case MOOSELIKE:
                MooselikePermitApplicationValidator.validateForAmend(application);
                break;
            case MOOSELIKE_NEW:
                final AmendmentApplicationData data = amendmentApplicationDataRepository.getByApplication(application);
                HarvestPermitAmendmentApplicationValidator.validateForAmending(application, data);
                break;
            case BIRD:
                final BirdPermitApplication birdApplication =
                        birdPermitApplicationRepository.findByHarvestPermitApplication(application);
                BirdPermitApplicationValidator.validateForAmend(application, birdApplication);
                break;
            case LARGE_CARNIVORE_WOLF:
            case LARGE_CARNIVORE_LYNX_PORONHOITO:
            case LARGE_CARNIVORE_LYNX:
            case LARGE_CARNIVORE_BEAR:
                final CarnivorePermitApplication carnivoreApplication =
                    carnivorePermitApplicationRepository.findByHarvestPermitApplication(application);
                CarnivorePermitApplicationValidator.validateForAmend(application, carnivoreApplication);
                break;
            default:
                throw new IllegalArgumentException(
                        "Cannot validate application for type " + application.getHarvestPermitCategory());
        }
    }
}
