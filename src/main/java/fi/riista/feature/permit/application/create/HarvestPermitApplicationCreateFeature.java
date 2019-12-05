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
import fi.riista.feature.permit.application.mammal.MammalPermitApplication;
import fi.riista.feature.permit.application.mammal.MammalPermitApplicationRepository;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.permit.area.HarvestPermitAreaRepository;
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
            case LARGE_CARNIVORE_LYNX_PORONHOITO:
            case LARGE_CARNIVORE_LYNX:
            case LARGE_CARNIVORE_BEAR:
                carnivorePermitApplicationRepository.save(CarnivorePermitApplication.create(application));
                break;
            case MAMMAL:
                mammalPermitApplicationRepository.save(MammalPermitApplication.create(application));
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
