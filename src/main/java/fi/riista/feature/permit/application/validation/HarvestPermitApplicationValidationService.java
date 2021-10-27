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
import fi.riista.feature.permit.application.derogation.reasons.DerogationPermitApplicationReason;
import fi.riista.feature.permit.application.derogation.reasons.DerogationPermitApplicationReasonRepository;
import fi.riista.feature.permit.application.deportation.DeportationPermitApplication;
import fi.riista.feature.permit.application.deportation.DeportationPermitApplicationRepository;
import fi.riista.feature.permit.application.deportation.DeportationPermitApplicationValidator;
import fi.riista.feature.permit.application.disability.DisabilityPermitApplication;
import fi.riista.feature.permit.application.disability.DisabilityPermitApplicationRepository;
import fi.riista.feature.permit.application.disability.DisabilityPermitApplicationValidator;
import fi.riista.feature.permit.application.disability.justification.DisabilityPermitHuntingTypeInfo;
import fi.riista.feature.permit.application.disability.justification.DisabilityPermitHuntingTypeInfoRepository;
import fi.riista.feature.permit.application.disability.justification.DisabilityPermitVehicle;
import fi.riista.feature.permit.application.disability.justification.DisabilityPermitVehicleRepository;
import fi.riista.feature.permit.application.dogevent.DogEventApplication;
import fi.riista.feature.permit.application.dogevent.DogEventApplicationRepository;
import fi.riista.feature.permit.application.dogevent.DogEventApplicationValidator;
import fi.riista.feature.permit.application.dogevent.DogEventDisturbance;
import fi.riista.feature.permit.application.dogevent.DogEventDisturbanceContact;
import fi.riista.feature.permit.application.dogevent.DogEventDisturbanceContactRepository;
import fi.riista.feature.permit.application.dogevent.DogEventDisturbanceRepository;
import fi.riista.feature.permit.application.dogevent.DogEventUnleash;
import fi.riista.feature.permit.application.dogevent.DogEventUnleashRepository;
import fi.riista.feature.permit.application.gamemanagement.GameManagementPermitApplication;
import fi.riista.feature.permit.application.gamemanagement.GameManagementPermitApplicationRepository;
import fi.riista.feature.permit.application.gamemanagement.GameManagementPermitApplicationValidator;
import fi.riista.feature.permit.application.lawsectionten.LawSectionTenPermitApplication;
import fi.riista.feature.permit.application.importing.ImportingPermitApplication;
import fi.riista.feature.permit.application.importing.ImportingPermitApplicationRepository;
import fi.riista.feature.permit.application.importing.ImportingPermitApplicationValidator;
import fi.riista.feature.permit.application.lawsectionten.LawSectionTenPermitApplication;
import fi.riista.feature.permit.application.lawsectionten.LawSectionTenPermitApplicationRepository;
import fi.riista.feature.permit.application.lawsectionten.LawSectionTenPermitApplicationValidator;
import fi.riista.feature.permit.application.mammal.MammalPermitApplication;
import fi.riista.feature.permit.application.mammal.MammalPermitApplicationRepository;
import fi.riista.feature.permit.application.mammal.MammalPermitApplicationValidator;
import fi.riista.feature.permit.application.mooselike.MooselikePermitApplicationValidator;
import fi.riista.feature.permit.application.nestremoval.NestRemovalPermitApplication;
import fi.riista.feature.permit.application.nestremoval.NestRemovalPermitApplicationRepository;
import fi.riista.feature.permit.application.nestremoval.NestRemovalPermitApplicationValidator;
import fi.riista.feature.permit.application.research.ResearchPermitApplication;
import fi.riista.feature.permit.application.research.ResearchPermitApplicationRepository;
import fi.riista.feature.permit.application.research.ResearchPermitApplicationValidator;
import fi.riista.feature.permit.application.weapontransportation.WeaponTransportationPermitApplication;
import fi.riista.feature.permit.application.weapontransportation.WeaponTransportationPermitApplicationRepository;
import fi.riista.feature.permit.application.weapontransportation.WeaponTransportationPermitApplicationValidator;
import fi.riista.feature.permit.application.weapontransportation.justification.TransportedWeapon;
import fi.riista.feature.permit.application.weapontransportation.justification.TransportedWeaponRepository;
import fi.riista.feature.permit.application.weapontransportation.justification.WeaponTransportationVehicle;
import fi.riista.feature.permit.application.weapontransportation.justification.WeaponTransportationVehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import static fi.riista.feature.permit.application.dogevent.DogEventType.DOG_TEST;
import static fi.riista.feature.permit.application.dogevent.DogEventType.DOG_TRAINING;

@Service
public class HarvestPermitApplicationValidationService {

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
    DogEventDisturbanceContactRepository dogEventDisturbanceContactRepository;

    @Resource
    private DeportationPermitApplicationRepository deportationPermitApplicationRepository;

    @Resource
    private ResearchPermitApplicationRepository researchPermitApplicationRepository;

    @Resource
    private ImportingPermitApplicationRepository importingPermitApplicationRepository;

    @Resource
    private GameManagementPermitApplicationRepository gameManagementPermitApplicationRepository;

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
            case LARGE_CARNIVORE_WOLF_PORONHOITO:
            case LARGE_CARNIVORE_LYNX_PORONHOITO:
            case LARGE_CARNIVORE_LYNX:
            case LARGE_CARNIVORE_BEAR:
                final CarnivorePermitApplication carnivoreApplication =
                        carnivorePermitApplicationRepository.findByHarvestPermitApplication(application);
                CarnivorePermitApplicationValidator.validateContent(application, carnivoreApplication);
                break;
            case MAMMAL: {
                final MammalPermitApplication mammalPermitApplication =
                        mammalPermitApplicationRepository.findByHarvestPermitApplication(application);
                final List<DerogationPermitApplicationReason> derogationReasons =
                        derogationPermitApplicationReasonRepository.findByHarvestPermitApplication(application);
                MammalPermitApplicationValidator.validateContent(application, mammalPermitApplication,
                        derogationReasons);
                break;
            }
            case NEST_REMOVAL:
                final NestRemovalPermitApplication nestRemovalPermitApplication =
                        nestRemovalPermitApplicationRepository.findByHarvestPermitApplication(application);
                final List<DerogationPermitApplicationReason> beaverDamDerogationReasons =
                        derogationPermitApplicationReasonRepository.findByHarvestPermitApplication(application);
                NestRemovalPermitApplicationValidator.validateContent(application, nestRemovalPermitApplication, beaverDamDerogationReasons);
                break;
            case LAW_SECTION_TEN:
                final LawSectionTenPermitApplication lawSectionTenPermitApplication =
                        lawSectionTenPermitApplicationRepository.findByHarvestPermitApplication(application);
                LawSectionTenPermitApplicationValidator.validateContent(application, lawSectionTenPermitApplication);
                break;
            case WEAPON_TRANSPORTATION: {
                final WeaponTransportationPermitApplication transportApplication =
                        weaponTransportationPermitApplicationRepository.findByHarvestPermitApplication(application);
                final List<TransportedWeapon> weaponInformation =
                        transportedWeaponRepository.findByWeaponTransportationPermitApplicationOrderById(transportApplication);
                final List<WeaponTransportationVehicle> vehicles =
                        weaponTransportationVehicleRepository.findByWeaponTransportationPermitApplicationOrderById(transportApplication);
                WeaponTransportationPermitApplicationValidator.validateContent(application, transportApplication, weaponInformation, vehicles);
                break;
            }
            case DISABILITY: {
                final DisabilityPermitApplication disabilityPermitApplication =
                        disabilityPermitApplicationRepository.findByHarvestPermitApplication(application);
                final List<DisabilityPermitVehicle> vehicles =
                        disabilityPermitVehicleRepository.findByDisabilityPermitApplicationOrderById(disabilityPermitApplication);
                final List<DisabilityPermitHuntingTypeInfo> huntingTypeInfos =
                        disabilityPermitHuntingTypeInfoRepository.findByDisabilityPermitApplicationOrderById(disabilityPermitApplication);
                DisabilityPermitApplicationValidator.validateContent(application, disabilityPermitApplication, vehicles, huntingTypeInfos);
                break;
            }
            case DOG_UNLEASH:
                final DogEventApplication dogUnleashApplication =
                        dogEventApplicationRepository.findByHarvestPermitApplication(application);
                final List<DogEventUnleash> unleashEvents = dogEventUnleashRepository.findAllByHarvestPermitApplication(application);
                DogEventApplicationValidator.validateContent(application, dogUnleashApplication, unleashEvents);
                break;
            case DOG_DISTURBANCE:
                final DogEventApplication dogDisturbanceApplication =
                        dogEventApplicationRepository.findByHarvestPermitApplication(application);
                final DogEventDisturbance trainingEvent =
                        dogEventDisturbanceRepository.findByHarvestPermitApplicationAndEventType(application, DOG_TRAINING);
                final List<DogEventDisturbanceContact> trainingContacts =
                        dogEventDisturbanceContactRepository.findAllByEvent(trainingEvent);
                final DogEventDisturbance testEvent =
                        dogEventDisturbanceRepository.findByHarvestPermitApplicationAndEventType(application, DOG_TEST);
                final List<DogEventDisturbanceContact> testContacts =
                        dogEventDisturbanceContactRepository.findAllByEvent(testEvent);
                DogEventApplicationValidator.validateContent(application, dogDisturbanceApplication,
                                                             trainingEvent, trainingContacts,
                                                             testEvent, testContacts);
                break;
            case DEPORTATION: {
                final DeportationPermitApplication deportationPermitApplication =
                        deportationPermitApplicationRepository.findByHarvestPermitApplication(application);
                final List<DerogationPermitApplicationReason> derogationReasons =
                        derogationPermitApplicationReasonRepository.findByHarvestPermitApplication(application);
                DeportationPermitApplicationValidator.validateContent(application, deportationPermitApplication,
                        derogationReasons);
                break;
            }
            case RESEARCH: {
                final ResearchPermitApplication researchPermitApplication =
                        researchPermitApplicationRepository.findByHarvestPermitApplication(application);
                final List<DerogationPermitApplicationReason> derogationReasons =
                        derogationPermitApplicationReasonRepository.findByHarvestPermitApplication(application);
                ResearchPermitApplicationValidator.validateContent(application, researchPermitApplication,
                        derogationReasons);
                break;
            }
            case IMPORTING: {
                final ImportingPermitApplication importingPermitApplication =
                        importingPermitApplicationRepository.findByHarvestPermitApplication(application);
                ImportingPermitApplicationValidator.validateContent(application, importingPermitApplication);
                break;
            }
            case GAME_MANAGEMENT: {
                final GameManagementPermitApplication gameManagementPermitApplication =
                        gameManagementPermitApplicationRepository.findByHarvestPermitApplication(application);
                GameManagementPermitApplicationValidator.validateContent(application, gameManagementPermitApplication);
                break;
            }
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
            case LARGE_CARNIVORE_WOLF_PORONHOITO:
            case LARGE_CARNIVORE_LYNX_PORONHOITO:
            case LARGE_CARNIVORE_LYNX:
            case LARGE_CARNIVORE_BEAR:
                final CarnivorePermitApplication carnivoreApplication =
                    carnivorePermitApplicationRepository.findByHarvestPermitApplication(application);
                CarnivorePermitApplicationValidator.validateForSending(application, carnivoreApplication);
                break;
            case MAMMAL: {
                final MammalPermitApplication mammalPermitApplication =
                        mammalPermitApplicationRepository.findByHarvestPermitApplication(application);
                final List<DerogationPermitApplicationReason> derogationReasons =
                        derogationPermitApplicationReasonRepository.findByHarvestPermitApplication(application);
                MammalPermitApplicationValidator.validateForSending(application, mammalPermitApplication,
                        derogationReasons);
                break;
            }
            case NEST_REMOVAL:
                final NestRemovalPermitApplication nestRemovalPermitApplication =
                        nestRemovalPermitApplicationRepository.findByHarvestPermitApplication(application);
                final List<DerogationPermitApplicationReason> beaverDamDerogationReasons =
                        derogationPermitApplicationReasonRepository.findByHarvestPermitApplication(application);
                NestRemovalPermitApplicationValidator.validateForSending(application, nestRemovalPermitApplication, beaverDamDerogationReasons);
                break;
            case LAW_SECTION_TEN:
                final LawSectionTenPermitApplication lawSectionTenPermitApplication =
                        lawSectionTenPermitApplicationRepository.findByHarvestPermitApplication(application);
                LawSectionTenPermitApplicationValidator.validateForSending(application, lawSectionTenPermitApplication);
                break;
            case WEAPON_TRANSPORTATION: {
                final WeaponTransportationPermitApplication transportApplication =
                        weaponTransportationPermitApplicationRepository.findByHarvestPermitApplication(application);
                final List<TransportedWeapon> weaponInformation =
                        transportedWeaponRepository.findByWeaponTransportationPermitApplicationOrderById(transportApplication);
                final List<WeaponTransportationVehicle> vehicles =
                        weaponTransportationVehicleRepository.findByWeaponTransportationPermitApplicationOrderById(transportApplication);
                WeaponTransportationPermitApplicationValidator.validateForSending(application, transportApplication, weaponInformation, vehicles);
                break;
            }
            case DISABILITY: {
                final DisabilityPermitApplication disabilityPermitApplication =
                        disabilityPermitApplicationRepository.findByHarvestPermitApplication(application);
                final List<DisabilityPermitVehicle> vehicles =
                        disabilityPermitVehicleRepository.findByDisabilityPermitApplicationOrderById(disabilityPermitApplication);
                final List<DisabilityPermitHuntingTypeInfo> huntingTypeInfos =
                        disabilityPermitHuntingTypeInfoRepository.findByDisabilityPermitApplicationOrderById(disabilityPermitApplication);
                DisabilityPermitApplicationValidator.validateForSending(application, disabilityPermitApplication, vehicles, huntingTypeInfos);
                break;
            }
            case DOG_UNLEASH:
                final DogEventApplication dogUnleashApplication =
                        dogEventApplicationRepository.findByHarvestPermitApplication(application);
                final List<DogEventUnleash> unleashEvents = dogEventUnleashRepository.findAllByHarvestPermitApplication(application);
                DogEventApplicationValidator.validateForSending(application, dogUnleashApplication, unleashEvents);
                break;
            case DOG_DISTURBANCE:
                final DogEventApplication dogDisturbanceApplication =
                        dogEventApplicationRepository.findByHarvestPermitApplication(application);
                final DogEventDisturbance trainingEvent =
                        dogEventDisturbanceRepository.findByHarvestPermitApplicationAndEventType(application, DOG_TRAINING);
                final List<DogEventDisturbanceContact> trainingContacts =
                        dogEventDisturbanceContactRepository.findAllByEvent(trainingEvent);
                final DogEventDisturbance testEvent =
                        dogEventDisturbanceRepository.findByHarvestPermitApplicationAndEventType(application, DOG_TEST);
                final List<DogEventDisturbanceContact> testContacts =
                        dogEventDisturbanceContactRepository.findAllByEvent(testEvent);

                DogEventApplicationValidator.validateForSending(application, dogDisturbanceApplication,
                                                                trainingEvent, trainingContacts,
                                                                testEvent, testContacts);
                break;
            case DEPORTATION: {
                final DeportationPermitApplication deportationPermitApplication =
                        deportationPermitApplicationRepository.findByHarvestPermitApplication(application);
                final List<DerogationPermitApplicationReason> derogationReasons =
                        derogationPermitApplicationReasonRepository.findByHarvestPermitApplication(application);
                DeportationPermitApplicationValidator.validateForSending(application, deportationPermitApplication,
                        derogationReasons);
                break;
            }
            case RESEARCH: {
                final ResearchPermitApplication researchPermitApplication =
                        researchPermitApplicationRepository.findByHarvestPermitApplication(application);
                final List<DerogationPermitApplicationReason> derogationReasons =
                        derogationPermitApplicationReasonRepository.findByHarvestPermitApplication(application);
                ResearchPermitApplicationValidator.validateForSending(application, researchPermitApplication,
                        derogationReasons);
                break;
            }
            case IMPORTING: {
                final ImportingPermitApplication importingPermitApplication =
                        importingPermitApplicationRepository.findByHarvestPermitApplication(application);
                ImportingPermitApplicationValidator.validateForSending(application, importingPermitApplication);
                break;
            }
            case GAME_MANAGEMENT: {
                final GameManagementPermitApplication gameManagementPermitApplication =
                        gameManagementPermitApplicationRepository.findByHarvestPermitApplication(application);
                GameManagementPermitApplicationValidator.validateForSending(application, gameManagementPermitApplication);
                break;
            }
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
            case LARGE_CARNIVORE_WOLF_PORONHOITO:
            case LARGE_CARNIVORE_LYNX_PORONHOITO:
            case LARGE_CARNIVORE_LYNX:
            case LARGE_CARNIVORE_BEAR:
                final CarnivorePermitApplication carnivoreApplication =
                    carnivorePermitApplicationRepository.findByHarvestPermitApplication(application);
                CarnivorePermitApplicationValidator.validateForAmend(application, carnivoreApplication);
                break;
            case MAMMAL: {
                final MammalPermitApplication mammalPermitApplication =
                        mammalPermitApplicationRepository.findByHarvestPermitApplication(application);
                final List<DerogationPermitApplicationReason> derogationReasons =
                        derogationPermitApplicationReasonRepository.findByHarvestPermitApplication(application);
                MammalPermitApplicationValidator.validateForAmend(application, mammalPermitApplication,
                        derogationReasons);
                break;
            }
            case NEST_REMOVAL:
                final NestRemovalPermitApplication nestRemovalPermitApplication =
                        nestRemovalPermitApplicationRepository.findByHarvestPermitApplication(application);
                final List<DerogationPermitApplicationReason> nestRemovalDerogationReasons =
                        derogationPermitApplicationReasonRepository.findByHarvestPermitApplication(application);
                NestRemovalPermitApplicationValidator.validateForAmend(application, nestRemovalPermitApplication, nestRemovalDerogationReasons);
                break;
            case LAW_SECTION_TEN:
                final LawSectionTenPermitApplication lawSectionTenPermitApplication =
                        lawSectionTenPermitApplicationRepository.findByHarvestPermitApplication(application);
                LawSectionTenPermitApplicationValidator.validateForAmend(application, lawSectionTenPermitApplication);
                break;
            case WEAPON_TRANSPORTATION: {
                final WeaponTransportationPermitApplication transportApplication =
                        weaponTransportationPermitApplicationRepository.findByHarvestPermitApplication(application);
                final List<TransportedWeapon> weaponInformation =
                        transportedWeaponRepository.findByWeaponTransportationPermitApplicationOrderById(transportApplication);
                final List<WeaponTransportationVehicle> vehicles =
                        weaponTransportationVehicleRepository.findByWeaponTransportationPermitApplicationOrderById(transportApplication);
                WeaponTransportationPermitApplicationValidator.validateForAmend(application, transportApplication, weaponInformation, vehicles);
                break;
            }
            case DISABILITY: {
                final DisabilityPermitApplication disabilityPermitApplication =
                        disabilityPermitApplicationRepository.findByHarvestPermitApplication(application);
                final List<DisabilityPermitVehicle> vehicles =
                        disabilityPermitVehicleRepository.findByDisabilityPermitApplicationOrderById(disabilityPermitApplication);
                final List<DisabilityPermitHuntingTypeInfo> huntingTypeInfos =
                        disabilityPermitHuntingTypeInfoRepository.findByDisabilityPermitApplicationOrderById(disabilityPermitApplication);
                DisabilityPermitApplicationValidator.validateForAmend(application, disabilityPermitApplication, vehicles, huntingTypeInfos);
                break;
            }
            case IMPORTING: {
                final ImportingPermitApplication importingPermitApplication =
                        importingPermitApplicationRepository.findByHarvestPermitApplication(application);
                ImportingPermitApplicationValidator.validateForAmend(application, importingPermitApplication);
                break;
            }
            case DOG_UNLEASH:
                final DogEventApplication dogUnleashApplication =
                        dogEventApplicationRepository.findByHarvestPermitApplication(application);
                final List<DogEventUnleash> unleashEvents = dogEventUnleashRepository.findAllByHarvestPermitApplication(application);
                DogEventApplicationValidator.validateForAmend(application, dogUnleashApplication, unleashEvents);
                break;
            case DOG_DISTURBANCE:
                final DogEventApplication dogDisturbanceApplication =
                        dogEventApplicationRepository.findByHarvestPermitApplication(application);
                final DogEventDisturbance trainingEvent =
                        dogEventDisturbanceRepository.findByHarvestPermitApplicationAndEventType(application, DOG_TRAINING);
                final List<DogEventDisturbanceContact> trainingContacts =
                        dogEventDisturbanceContactRepository.findAllByEvent(trainingEvent);
                final DogEventDisturbance testEvent =
                        dogEventDisturbanceRepository.findByHarvestPermitApplicationAndEventType(application, DOG_TEST);
                final List<DogEventDisturbanceContact> testContacts =
                        dogEventDisturbanceContactRepository.findAllByEvent(testEvent);
                DogEventApplicationValidator.validateForAmend(application, dogDisturbanceApplication,
                                                              trainingEvent, trainingContacts,
                                                              testEvent, testContacts);
                break;
            case DEPORTATION: {
                final DeportationPermitApplication deportationPermitApplication =
                        deportationPermitApplicationRepository.findByHarvestPermitApplication(application);
                final List<DerogationPermitApplicationReason> derogationReasons =
                        derogationPermitApplicationReasonRepository.findByHarvestPermitApplication(application);
                DeportationPermitApplicationValidator.validateForAmend(application, deportationPermitApplication,
                        derogationReasons);
                break;
            }
            case RESEARCH: {
                final ResearchPermitApplication researchPermitApplication =
                        researchPermitApplicationRepository.findByHarvestPermitApplication(application);
                final List<DerogationPermitApplicationReason> derogationReasons =
                        derogationPermitApplicationReasonRepository.findByHarvestPermitApplication(application);
                ResearchPermitApplicationValidator.validateForAmend(application, researchPermitApplication,
                        derogationReasons);
                break;
            }
            case GAME_MANAGEMENT: {
                final GameManagementPermitApplication gameManagementPermitApplication =
                        gameManagementPermitApplicationRepository.findByHarvestPermitApplication(application);
                GameManagementPermitApplicationValidator.validateForAmend(application, gameManagementPermitApplication);
                break;
            }
            default:
                throw new IllegalArgumentException(
                        "Cannot validate application for type " + application.getHarvestPermitCategory());
        }
    }
}
