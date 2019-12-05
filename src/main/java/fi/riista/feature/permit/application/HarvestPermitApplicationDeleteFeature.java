package fi.riista.feature.permit.application;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.permit.application.amendment.AmendmentApplicationDataRepository;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachmentRepository;
import fi.riista.feature.permit.application.bird.BirdPermitApplicationRepository;
import fi.riista.feature.permit.application.carnivore.CarnivorePermitApplicationRepository;
import fi.riista.feature.permit.application.derogation.reasons.DerogationPermitApplicationReasonRepository;
import fi.riista.feature.permit.application.mammal.MammalPermitApplicationRepository;
import fi.riista.feature.storage.FileStorageService;
import fi.riista.security.EntityPermission;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.UUID;

@Component
public class HarvestPermitApplicationDeleteFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private FileStorageService fileStorageService;

    @Resource
    private HarvestPermitApplicationAttachmentRepository harvestPermitApplicationAttachmentRepository;

    @Resource
    private HarvestPermitApplicationRepository harvestPermitApplicationRepository;

    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository harvestPermitApplicationSpeciesAmountRepository;

    @Resource
    private AmendmentApplicationDataRepository amendmentApplicationDataRepository;

    @Resource
    private BirdPermitApplicationRepository birdPermitApplicationRepository;

    @Resource
    private CarnivorePermitApplicationRepository carnivorePermitApplicationRepository;

    @Resource
    private MammalPermitApplicationRepository mammalPermitApplicationRepository;

    @Resource
    private DerogationPermitApplicationReasonRepository derogationPermitApplicationReasonRepository;

    @Transactional
    public void deleteApplication(final long applicationId) {
        final HarvestPermitApplication application =
                requireEntityService.requireHarvestPermitApplication(applicationId, EntityPermission.DELETE);
        application.assertStatus(HarvestPermitApplication.Status.DRAFT);

        switch (application.getHarvestPermitCategory()) {
            case MOOSELIKE:
                throw new IllegalArgumentException("Can not delete mooselike application");

            case MOOSELIKE_NEW:
                amendmentApplicationDataRepository.deleteByApplication(application);
                break;

            case BIRD:
                birdPermitApplicationRepository.deleteByHarvestPermitApplication(application);
                break;
            case LARGE_CARNIVORE_BEAR:
            case LARGE_CARNIVORE_LYNX:
            case LARGE_CARNIVORE_LYNX_PORONHOITO:
            case LARGE_CARNIVORE_WOLF:
                carnivorePermitApplicationRepository.deleteByHarvestPermitApplication(application);
                break;
            case MAMMAL:
                mammalPermitApplicationRepository.deleteByHarvestPermitApplication(application);
                derogationPermitApplicationReasonRepository.deleteByHarvestPermitApplication(application);
                break;
            default:
                throw new IllegalArgumentException("Unknown application type: " + application.getHarvestPermitCategory());
        }

        harvestPermitApplicationSpeciesAmountRepository.delete(application.getSpeciesAmounts());

        application.getAttachments().forEach(attachment -> {
            final UUID fileUuid = attachment.getAttachmentMetadata().getId();
            harvestPermitApplicationAttachmentRepository.delete(attachment);
            fileStorageService.remove(fileUuid);
        });

        harvestPermitApplicationRepository.delete(applicationId);
    }
}
