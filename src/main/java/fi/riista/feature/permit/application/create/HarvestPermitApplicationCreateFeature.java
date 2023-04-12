package fi.riista.feature.permit.application.create;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.feature.permit.application.DeliveryAddress;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationBasicDetailsDTO;
import fi.riista.feature.permit.application.HarvestPermitApplicationRepository;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.feature.permit.application.bird.BirdPermitApplication;
import fi.riista.feature.permit.application.bird.BirdPermitApplicationRepository;
import fi.riista.feature.permit.application.carnivore.CarnivorePermitApplication;
import fi.riista.feature.permit.application.carnivore.CarnivorePermitApplicationRepository;
import fi.riista.feature.permit.application.deportation.DeportationPermitApplication;
import fi.riista.feature.permit.application.deportation.DeportationPermitApplicationRepository;
import fi.riista.feature.permit.application.disability.DisabilityPermitApplication;
import fi.riista.feature.permit.application.disability.DisabilityPermitApplicationRepository;
import fi.riista.feature.permit.application.dogevent.DogEventApplication;
import fi.riista.feature.permit.application.dogevent.DogEventApplicationRepository;
import fi.riista.feature.permit.application.gamemanagement.GameManagementPermitApplication;
import fi.riista.feature.permit.application.gamemanagement.GameManagementPermitApplicationRepository;
import fi.riista.feature.permit.application.importing.ImportingPermitApplication;
import fi.riista.feature.permit.application.importing.ImportingPermitApplicationRepository;
import fi.riista.feature.permit.application.lawsectionten.LawSectionTenPermitApplication;
import fi.riista.feature.permit.application.lawsectionten.LawSectionTenPermitApplicationRepository;
import fi.riista.feature.permit.application.mammal.MammalPermitApplication;
import fi.riista.feature.permit.application.mammal.MammalPermitApplicationRepository;
import fi.riista.feature.permit.application.nestremoval.NestRemovalPermitApplication;
import fi.riista.feature.permit.application.nestremoval.NestRemovalPermitApplicationRepository;
import fi.riista.feature.permit.application.research.ResearchPermitApplication;
import fi.riista.feature.permit.application.research.ResearchPermitApplicationRepository;
import fi.riista.feature.permit.application.weapontransportation.WeaponTransportationPermitApplication;
import fi.riista.feature.permit.application.weapontransportation.WeaponTransportationPermitApplicationRepository;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.permit.area.HarvestPermitAreaRepository;
import fi.riista.security.EntityPermission;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.security.SecureRandom;
import java.util.Locale;


@Component
public class HarvestPermitApplicationCreateFeature {

    @Resource
    private GISZoneRepository gisZoneRepository;

    @Resource
    private HarvestPermitAreaRepository harvestPermitAreaRepository;

    @Resource
    private HarvestPermitApplicationRepository harvestPermitApplicationRepository;

    @Resource
    private BirdPermitApplicationRepository birdPermitApplicationRepository;

    @Resource
    private CarnivorePermitApplicationRepository carnivorePermitApplicationRepository;

    @Resource
    private MammalPermitApplicationRepository mammalPermitApplicationRepository;

    @Resource
    private NestRemovalPermitApplicationRepository nestRemovalPermitApplicationRepository;

    @Resource
    private LawSectionTenPermitApplicationRepository lawSectionTenPermitApplicationRepository;

    @Resource
    private WeaponTransportationPermitApplicationRepository weaponTransportationPermitApplicationRepository;

    @Resource
    private DisabilityPermitApplicationRepository disabilityPermitApplicationRepository;

    @Resource
    private DogEventApplicationRepository dogEventApplicationRepository;

    @Resource
    private DeportationPermitApplicationRepository deportationPermitApplicationRepository;

    @Resource
    private ResearchPermitApplicationRepository researchPermitApplicationRepository;

    @Resource
    private ImportingPermitApplicationRepository importingPermitApplicationRepository;

    @Resource
    private GameManagementPermitApplicationRepository gameManagementPermitApplicationRepository;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private PersonRepository personRepository;

    @Resource
    private SecureRandom secureRandom;

    @Transactional
    public HarvestPermitApplicationBasicDetailsDTO create(final HarvestPermitApplicationCreateDTO dto,
                                                          final Locale locale) {
        final HarvestPermitArea permitArea = createPermitAreaIfRequired(dto);
        final Person contactPerson = resolveContactPerson(dto);

        final HarvestPermitApplication application = new HarvestPermitApplication();
        application.setLocale(locale);
        application.setDecisionLocale(locale); // Default to application locale
        application.setArea(permitArea);
        application.setHarvestPermitCategory(dto.getCategory());
        application.setApplicationName(dto.getApplicationName());
        application.setContactPerson(contactPerson);
        application.setPermitHolder(PermitHolder.createHolderForPerson(contactPerson));
        application.setStatus(HarvestPermitApplication.Status.DRAFT);
        application.setApplicationYear(dto.getHuntingYear());
        application.setDeliveryAddress(DeliveryAddress.createFromPersonNullable(contactPerson));

        activeUserService.assertHasPermission(application, EntityPermission.CREATE);

        harvestPermitApplicationRepository.save(application);

        createApplicationTypeSpecificEntities(application, dto);

        return new HarvestPermitApplicationBasicDetailsDTO(application);
    }


    private void createApplicationTypeSpecificEntities(final HarvestPermitApplication application,
                                                       final HarvestPermitApplicationCreateDTO dto) {
        switch (dto.getCategory()) {
            case BIRD:
                birdPermitApplicationRepository.save(BirdPermitApplication.create(application));
                break;
            case MOOSELIKE:
            case MOOSELIKE_NEW:
                break;
            case LARGE_CARNIVORE_WOLF:
            case LARGE_CARNIVORE_WOLF_PORONHOITO:
            case LARGE_CARNIVORE_LYNX_PORONHOITO:
            case LARGE_CARNIVORE_LYNX:
            case LARGE_CARNIVORE_BEAR:
                carnivorePermitApplicationRepository.save(CarnivorePermitApplication.create(application));
                break;
            case MAMMAL:
                mammalPermitApplicationRepository.save(MammalPermitApplication.create(application));
                break;
            case NEST_REMOVAL:
                nestRemovalPermitApplicationRepository.save(NestRemovalPermitApplication.create(application));
                break;
            case LAW_SECTION_TEN:
            case EUROPEAN_BEAVER:
            case PARTRIDGE:
                lawSectionTenPermitApplicationRepository.save(LawSectionTenPermitApplication.create(application));
                break;
            case WEAPON_TRANSPORTATION:
                weaponTransportationPermitApplicationRepository.save(WeaponTransportationPermitApplication.create(application));
                break;
            case DISABILITY:
                disabilityPermitApplicationRepository.save(DisabilityPermitApplication.create(application));
                break;
            case DOG_UNLEASH:
            case DOG_DISTURBANCE:
                dogEventApplicationRepository.save(DogEventApplication.create(application));
                break;
            case DEPORTATION:
                deportationPermitApplicationRepository.save(new DeportationPermitApplication(application));
                break;
            case RESEARCH:
                researchPermitApplicationRepository.save((new ResearchPermitApplication(application)));
                break;
            case IMPORTING:
                importingPermitApplicationRepository.save(ImportingPermitApplication.create(application));
                break;
            case GAME_MANAGEMENT:
                gameManagementPermitApplicationRepository.save(new GameManagementPermitApplication(application));
                break;
            default:
                throw new IllegalArgumentException("Unsupported permit category:" + dto.getCategory());
        }
    }

    private HarvestPermitArea createPermitAreaIfRequired(final HarvestPermitApplicationCreateDTO dto) {
        if (!dto.getCategory().isMooselike()) {
            return null;
        }

        final HarvestPermitArea permitArea = new HarvestPermitArea();
        permitArea.setHuntingYear(dto.getHuntingYear());
        permitArea.setZone(gisZoneRepository.save(new GISZone()));
        permitArea.generateAndStoreExternalId(secureRandom);

        return harvestPermitAreaRepository.save(permitArea);
    }

    private Person resolveContactPerson(final HarvestPermitApplicationCreateDTO dto) {
        if (activeUserService.isModeratorOrAdmin()) {
            return personRepository.getOne(dto.getPersonId());
        }
        return activeUserService.requireActivePerson();
    }
}
