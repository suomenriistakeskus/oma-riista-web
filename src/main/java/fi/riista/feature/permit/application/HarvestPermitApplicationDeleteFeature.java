package fi.riista.feature.permit.application;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.permit.application.amendment.AmendmentApplicationDataRepository;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachmentRepository;
import fi.riista.feature.permit.application.bird.BirdPermitApplicationRepository;
import fi.riista.feature.permit.application.carnivore.CarnivorePermitApplicationRepository;
import fi.riista.feature.permit.application.deportation.DeportationPermitApplicationRepository;
import fi.riista.feature.permit.application.derogation.reasons.DerogationPermitApplicationReasonRepository;
import fi.riista.feature.permit.application.disability.DisabilityPermitApplication;
import fi.riista.feature.permit.application.disability.DisabilityPermitApplicationRepository;
import fi.riista.feature.permit.application.disability.justification.DisabilityPermitHuntingTypeInfoRepository;
import fi.riista.feature.permit.application.disability.justification.DisabilityPermitVehicleRepository;
import fi.riista.feature.permit.application.dogevent.DogEventApplicationRepository;
import fi.riista.feature.permit.application.dogevent.DogEventDisturbanceContactRepository;
import fi.riista.feature.permit.application.dogevent.DogEventDisturbanceRepository;
import fi.riista.feature.permit.application.dogevent.DogEventUnleashRepository;
import fi.riista.feature.permit.application.gamemanagement.GameManagementPermitApplicationRepository;
import fi.riista.feature.permit.application.geometry.HarvestPermitApplicationGeometryFeature;
import fi.riista.feature.permit.application.importing.ImportingPermitApplicationRepository;
import fi.riista.feature.permit.application.lawsectionten.LawSectionTenPermitApplicationRepository;
import fi.riista.feature.permit.application.mammal.MammalPermitApplicationRepository;
import fi.riista.feature.permit.application.nestremoval.NestRemovalPermitApplicationRepository;
import fi.riista.feature.permit.application.research.ResearchPermitApplicationRepository;
import fi.riista.feature.permit.application.weapontransportation.WeaponTransportationPermitApplication;
import fi.riista.feature.permit.application.weapontransportation.WeaponTransportationPermitApplicationRepository;
import fi.riista.feature.permit.application.weapontransportation.justification.TransportedWeaponRepository;
import fi.riista.feature.permit.application.weapontransportation.justification.WeaponTransportationVehicleRepository;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.storage.FileStorageService;
import fi.riista.security.EntityPermission;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.UUID;

import static java.util.Optional.ofNullable;

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

    @Resource
    private NestRemovalPermitApplicationRepository nestRemovalPermitApplicationRepository;

    @Resource
    private LawSectionTenPermitApplicationRepository lawSectionTenPermitApplicationRepository;

    @Resource
    private WeaponTransportationPermitApplicationRepository weaponTransportationPermitApplicationRepository;

    @Resource
    private TransportedWeaponRepository transportedWeaponRepository;

    @Resource
    private WeaponTransportationVehicleRepository weaponTransportationVehicleRepository;

    @Resource
    private DisabilityPermitApplicationRepository disabilityPermitApplicationRepository;

    @Resource
    private DisabilityPermitVehicleRepository disabilityPermitVehicleRepository;

    @Resource
    private DisabilityPermitHuntingTypeInfoRepository disabilityPermitHuntingTypeInfoRepository;

    @Resource
    private DogEventApplicationRepository dogEventApplicationRepository;

    @Resource
    private DogEventUnleashRepository dogEventUnleashRepository;

    @Resource
    private DogEventDisturbanceRepository dogEventDisturbanceRepository;

    @Resource
    private DogEventDisturbanceContactRepository dogEventDisturbanceContactRepository;

    @Resource
    private DeportationPermitApplicationRepository deportationPermitApplicationRepository;

    @Resource
    private ResearchPermitApplicationRepository researchPermitApplicationRepository;

    @Resource
    private ImportingPermitApplicationRepository importingPermitApplicationRepository;

    @Resource
    private GameManagementPermitApplicationRepository gameManagementPermitApplicationRepository;

    @Resource
    private HarvestPermitApplicationGeometryFeature geometryFeature;

    @Transactional
    public void deleteApplication(final long applicationId) {
        final HarvestPermitApplication application =
                requireEntityService.requireHarvestPermitApplication(applicationId, EntityPermission.DELETE);
        application.assertStatus(HarvestPermitApplication.Status.DRAFT);

        HarvestPermitArea areaToDelete = null;

        switch (application.getHarvestPermitCategory()) {
            case MOOSELIKE:
                areaToDelete = application.getArea();
                break;

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
            case LARGE_CARNIVORE_WOLF_PORONHOITO:
                carnivorePermitApplicationRepository.deleteByHarvestPermitApplication(application);
                break;

            case MAMMAL:
                mammalPermitApplicationRepository.deleteByHarvestPermitApplication(application);
                derogationPermitApplicationReasonRepository.deleteByHarvestPermitApplication(application);
                break;

            case NEST_REMOVAL:
                nestRemovalPermitApplicationRepository.deleteByHarvestPermitApplication(application);
                derogationPermitApplicationReasonRepository.deleteByHarvestPermitApplication(application);
                break;

            case LAW_SECTION_TEN:
                lawSectionTenPermitApplicationRepository.deleteByHarvestPermitApplication(application);
                break;

            case WEAPON_TRANSPORTATION:
                final WeaponTransportationPermitApplication weaponTransportationPermitApplication =
                        weaponTransportationPermitApplicationRepository.findByHarvestPermitApplication(application);
                transportedWeaponRepository.deleteByWeaponTransportationPermitApplication(weaponTransportationPermitApplication);
                weaponTransportationVehicleRepository.deleteByWeaponTransportationPermitApplication(weaponTransportationPermitApplication);
                weaponTransportationPermitApplicationRepository.deleteByHarvestPermitApplication(application);
                break;

            case DISABILITY:
                final DisabilityPermitApplication disabilityPermitApplication =
                        disabilityPermitApplicationRepository.findByHarvestPermitApplication(application);
                disabilityPermitVehicleRepository.deleteByDisabilityPermitApplication(disabilityPermitApplication);
                disabilityPermitHuntingTypeInfoRepository.deleteByDisabilityPermitApplication(disabilityPermitApplication);
                disabilityPermitApplicationRepository.deleteByHarvestPermitApplication(application);
                break;

            case DOG_UNLEASH:
                dogEventUnleashRepository.deleteByHarvestPermitApplication(application);
                dogEventApplicationRepository.deleteByHarvestPermitApplication(application);
                break;

            case DOG_DISTURBANCE:
                dogEventDisturbanceRepository.findAllByHarvestPermitApplication(application)
                        .forEach(dogEventDisturbanceContactRepository::deleteAllByEvent);
                dogEventDisturbanceRepository.deleteAllByHarvestPermitApplication(application);
                dogEventApplicationRepository.deleteByHarvestPermitApplication(application);
                break;

            case DEPORTATION:
                deportationPermitApplicationRepository.deleteByHarvestPermitApplication(application);
                derogationPermitApplicationReasonRepository.deleteByHarvestPermitApplication(application);
                break;

            case RESEARCH:
                researchPermitApplicationRepository.deleteByHarvestPermitApplication(application);
                derogationPermitApplicationReasonRepository.deleteByHarvestPermitApplication(application);
                break;

            case IMPORTING:
                importingPermitApplicationRepository.deleteByHarvestPermitApplication(application);
                break;

            case GAME_MANAGEMENT:
                gameManagementPermitApplicationRepository.deleteByHarvestPermitApplication(application);
                break;

            default:
                throw new IllegalArgumentException("Unknown application type: " + application.getHarvestPermitCategory());
        }

        harvestPermitApplicationSpeciesAmountRepository.deleteAll(application.getSpeciesAmounts());

        application.getAttachments().forEach(attachment -> {
            final UUID fileUuid = attachment.getAttachmentMetadata().getId();
            harvestPermitApplicationAttachmentRepository.delete(attachment);
            fileStorageService.remove(fileUuid);
        });

        harvestPermitApplicationRepository.deleteById(applicationId);

        ofNullable(areaToDelete).ifPresent(geometryFeature::deleteArea);
    }
}
