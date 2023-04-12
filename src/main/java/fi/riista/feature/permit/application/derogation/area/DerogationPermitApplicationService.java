package fi.riista.feature.permit.application.derogation.area;

import com.google.common.base.Preconditions;
import fi.riista.feature.gis.GISQueryService;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationAuthorizationService;
import fi.riista.feature.permit.application.bird.BirdPermitApplication;
import fi.riista.feature.permit.application.bird.BirdPermitApplicationRepository;
import fi.riista.feature.permit.application.carnivore.CarnivorePermitApplication;
import fi.riista.feature.permit.application.carnivore.CarnivorePermitApplicationRepository;
import fi.riista.feature.permit.application.deportation.DeportationPermitApplication;
import fi.riista.feature.permit.application.deportation.DeportationPermitApplicationRepository;
import fi.riista.feature.permit.application.dogevent.DogEventApplication;
import fi.riista.feature.permit.application.dogevent.DogEventApplicationRepository;
import fi.riista.feature.permit.application.gamemanagement.GameManagementPermitApplication;
import fi.riista.feature.permit.application.gamemanagement.GameManagementPermitApplicationRepository;
import fi.riista.feature.permit.application.lawsectionten.LawSectionTenPermitApplication;
import fi.riista.feature.permit.application.lawsectionten.LawSectionTenPermitApplicationRepository;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

@Service
public class DerogationPermitApplicationService {

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Resource
    private GISQueryService gisQueryService;

    @Resource
    private CarnivorePermitApplicationRepository carnivorePermitApplicationRepository;

    @Resource
    private MammalPermitApplicationRepository mammalPermitApplicationRepository;

    @Resource
    private BirdPermitApplicationRepository birdPermitApplicationRepository;

    @Resource
    private NestRemovalPermitApplicationRepository nestRemovalPermitApplicationRepository;

    @Resource
    private LawSectionTenPermitApplicationRepository lawSectionTenPermitApplicationRepository;

    @Resource
    private WeaponTransportationPermitApplicationRepository weaponTransportationPermitApplicationRepository;

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

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public DerogationPermitApplicationAreaInfo findForRead(final long applicationId) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.readApplication(applicationId);

        final HarvestPermitCategory category = application.getHarvestPermitCategory();
        switch (category) {
            case LARGE_CARNIVORE_BEAR:
            case LARGE_CARNIVORE_LYNX:
            case LARGE_CARNIVORE_LYNX_PORONHOITO:
            case LARGE_CARNIVORE_WOLF:
            case LARGE_CARNIVORE_WOLF_PORONHOITO:
                return carnivorePermitApplicationRepository.findByHarvestPermitApplication(application);
            case MAMMAL:
                return mammalPermitApplicationRepository.findByHarvestPermitApplication(application);
            case BIRD:
                return birdPermitApplicationRepository.findByHarvestPermitApplication(application);
            case NEST_REMOVAL:
                return nestRemovalPermitApplicationRepository.findByHarvestPermitApplication(application);
            case LAW_SECTION_TEN:
            case EUROPEAN_BEAVER:
            case PARTRIDGE:
                return lawSectionTenPermitApplicationRepository.findByHarvestPermitApplication(application);
            case WEAPON_TRANSPORTATION:
                return weaponTransportationPermitApplicationRepository.findByHarvestPermitApplication(application);
            case DOG_UNLEASH:
            case DOG_DISTURBANCE:
                return dogEventApplicationRepository.findByHarvestPermitApplication(application);
            case DEPORTATION:
                return deportationPermitApplicationRepository.findByHarvestPermitApplication(application);
            case RESEARCH:
                return researchPermitApplicationRepository.findByHarvestPermitApplication(application);
            case IMPORTING:
                return importingPermitApplicationRepository.findByHarvestPermitApplication(application);
            case GAME_MANAGEMENT:
                return gameManagementPermitApplicationRepository.findByHarvestPermitApplication(application);
            default:
                throw new UnsupportedOperationException("Unsupported category: " + category);
        }
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void update(final long applicationId,
                       final @NotNull DerogationPermitApplicationAreaDTO dto) {

        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.updateApplication(applicationId);

        application.setRhy(gisQueryService.findRhyByLocation(dto.getGeoLocation()));

        final HarvestPermitCategory category = application.getHarvestPermitCategory();
        Preconditions.checkArgument(isAreaSizeValid(category, dto.getAreaSize()), "Area size is invalid");

        switch (category) {
            case LARGE_CARNIVORE_BEAR:
            case LARGE_CARNIVORE_LYNX:
            case LARGE_CARNIVORE_LYNX_PORONHOITO:
            case LARGE_CARNIVORE_WOLF:
            case LARGE_CARNIVORE_WOLF_PORONHOITO: {
                final CarnivorePermitApplication existing =
                        carnivorePermitApplicationRepository.findByHarvestPermitApplication(application);
                Preconditions.checkState(existing != null, "Carnivore application not found.");
                updateFields(existing, dto);
                carnivorePermitApplicationRepository.save(existing);
                break;
            }
            case MAMMAL: {
                final MammalPermitApplication existing =
                        mammalPermitApplicationRepository.findByHarvestPermitApplication(application);
                Preconditions.checkState(existing != null, "Mammal application not found.");
                updateFields(existing, dto);
                mammalPermitApplicationRepository.save(existing);
                break;
            }
            case BIRD: {
                final BirdPermitApplication existing =
                        birdPermitApplicationRepository.findByHarvestPermitApplication(application);
                Preconditions.checkState(existing != null, "Bird application not found.");
                updateFields(existing, dto);
                break;
            }
            case NEST_REMOVAL: {
                final NestRemovalPermitApplication existing =
                        nestRemovalPermitApplicationRepository.findByHarvestPermitApplication(application);
                Preconditions.checkState(existing != null, "Nest removal application not found.");
                updateFields(existing, dto);
                nestRemovalPermitApplicationRepository.save(existing);
                break;
            }
            case LAW_SECTION_TEN:
            case EUROPEAN_BEAVER:
            case PARTRIDGE: {
                final LawSectionTenPermitApplication existing =
                        lawSectionTenPermitApplicationRepository.findByHarvestPermitApplication(application);
                Preconditions.checkState(existing != null, "Law section 10 application not found.");
                updateFields(existing, dto);
                lawSectionTenPermitApplicationRepository.save(existing);
                break;
            }
            case WEAPON_TRANSPORTATION: {
                final WeaponTransportationPermitApplication existing =
                        weaponTransportationPermitApplicationRepository.findByHarvestPermitApplication(application);
                Preconditions.checkState(existing != null, "Weapon transportation application not found.");
                updateFields(existing, dto);
                weaponTransportationPermitApplicationRepository.save(existing);
                break;
            }
            case IMPORTING: {
                final ImportingPermitApplication existing =
                        importingPermitApplicationRepository.findByHarvestPermitApplication(application);
                Preconditions.checkState(existing != null, "Weapon transportation application not found.");
                updateFields(existing, dto);
                importingPermitApplicationRepository.save(existing);
                break;

            }
            case DOG_UNLEASH:
            case DOG_DISTURBANCE: {
                final DogEventApplication existing =
                        dogEventApplicationRepository.findByHarvestPermitApplication(application);
                Preconditions.checkState(existing != null, "Hunting dog training application not found.");
                updateFields(existing, dto);
                dogEventApplicationRepository.save(existing);
                break;
            }
            case DEPORTATION: {
                final DeportationPermitApplication existing =
                        deportationPermitApplicationRepository.findByHarvestPermitApplication(application);
                Preconditions.checkState(existing != null, "Deportation application not found.");
                updateFields(existing, dto);
                deportationPermitApplicationRepository.save(existing);
                break;
            }
            case RESEARCH: {
                final ResearchPermitApplication existing =
                        researchPermitApplicationRepository.findByHarvestPermitApplication(application);
                Preconditions.checkState(existing != null, "Research application not found.");
                updateFields(existing, dto);
                researchPermitApplicationRepository.save(existing);
                break;
            }
            case GAME_MANAGEMENT: {
                final GameManagementPermitApplication existing =
                        gameManagementPermitApplicationRepository.findByHarvestPermitApplication(application);
                Preconditions.checkState(existing != null, "Game management application not found.");
                updateFields(existing, dto);
                gameManagementPermitApplicationRepository.save(existing);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unsupported category: " + category);
        }
    }

    private static void updateFields(final @NotNull DerogationPermitApplicationAreaInfo entity,
                                     final @NotNull DerogationPermitApplicationAreaDTO dto) {
        entity.setAreaSize(dto.getAreaSize());
        entity.setGeoLocation(dto.getGeoLocation());
        entity.setAreaDescription(dto.getAreaDescription());
    }

    private boolean isAreaSizeValid(final HarvestPermitCategory category, final Integer areaSize) {
        return !category.isAreaSizeRequired() || areaSize != null;
    }
}
