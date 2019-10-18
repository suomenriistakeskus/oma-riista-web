package fi.riista.feature.permit.application.create;

import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationBasicDetailsDTO;
import fi.riista.feature.permit.application.HarvestPermitApplicationRepository;
import fi.riista.feature.permit.application.bird.BirdPermitApplication;
import fi.riista.feature.permit.application.bird.BirdPermitApplicationRepository;
import fi.riista.feature.permit.application.carnivore.CarnivorePermitApplication;
import fi.riista.feature.permit.application.carnivore.CarnivorePermitApplicationRepository;
import fi.riista.feature.permit.area.HarvestPermitAreaRepository;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.Locales;
import org.junit.Test;

import javax.annotation.Resource;

import static fi.riista.feature.harvestpermit.HarvestPermitCategory.BIRD;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.LARGE_CARNIVORE_BEAR;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.LARGE_CARNIVORE_LYNX;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.LARGE_CARNIVORE_LYNX_PORONHOITO;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.LARGE_CARNIVORE_WOLF;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.MOOSELIKE;
import static fi.riista.test.Asserts.assertEmpty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class HarvestPermitApplicationCreateFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private HarvestPermitApplicationCreateFeature feature;

    @Resource
    private HarvestPermitApplicationRepository applicationRepository;

    @Resource
    private CarnivorePermitApplicationRepository carnivorePermitApplicationRepository;

    @Resource
    private BirdPermitApplicationRepository birdPermitApplicationRepository;

    @Resource
    private HarvestPermitAreaRepository areaRepository;

    @Test
    public void testCreateMooselike() {
        final HarvestPermitApplicationCreateDTO dto = new HarvestPermitApplicationCreateDTO();
        dto.setCategory(MOOSELIKE);
        dto.setHuntingYear(2019);
        dto.setApplicationName("moose 2019");

        onSavedAndAuthenticated(createUserWithPerson("applicant"), () -> {
            final HarvestPermitApplicationBasicDetailsDTO harvestPermitApplicationBasicDetailsDTO = feature.create(dto, Locales.FI);

            assertEquals(2019, harvestPermitApplicationBasicDetailsDTO.getHuntingYear());
            assertNotNull(harvestPermitApplicationBasicDetailsDTO.getId());

            final HarvestPermitApplication application = applicationRepository.findOne(harvestPermitApplicationBasicDetailsDTO.getId());
            assertEquals(MOOSELIKE, application.getHarvestPermitCategory());

            assertNotNull(application.getArea());
            assertEmpty(birdPermitApplicationRepository.findAll());
            assertEmpty(carnivorePermitApplicationRepository.findAll());
        });
    }

    @Test
    public void createCarnivore_bird() {
        final HarvestPermitApplicationCreateDTO dto = new HarvestPermitApplicationCreateDTO();
        dto.setCategory(BIRD);
        dto.setHuntingYear(2019);
        dto.setApplicationName("bird 2019");

        onSavedAndAuthenticated(createUserWithPerson("applicant"), () -> {
            final HarvestPermitApplicationBasicDetailsDTO harvestPermitApplicationBasicDetailsDTO = feature.create(dto, Locales.FI);

            assertEquals(2019, harvestPermitApplicationBasicDetailsDTO.getHuntingYear());
            assertNotNull(harvestPermitApplicationBasicDetailsDTO.getId());

            final HarvestPermitApplication application = applicationRepository.findOne(harvestPermitApplicationBasicDetailsDTO.getId());
            assertEquals(dto.getCategory(), application.getHarvestPermitCategory());
            final BirdPermitApplication birdPermitApplication = birdPermitApplicationRepository.findByHarvestPermitApplication(application);
            assertNotNull(birdPermitApplication);
        });
    }

    @Test
    public void createCarnivore_bear() {
        final HarvestPermitApplicationCreateDTO dto = new HarvestPermitApplicationCreateDTO();
        dto.setCategory(LARGE_CARNIVORE_BEAR);
        dto.setHuntingYear(2019);
        dto.setApplicationName("bear 2019");

        createAndAssertForCarnivore(dto);
    }

    @Test
    public void createCarnivore_lynx() {
        final HarvestPermitApplicationCreateDTO dto = new HarvestPermitApplicationCreateDTO();
        dto.setCategory(LARGE_CARNIVORE_LYNX);
        dto.setHuntingYear(2019);
        dto.setApplicationName("lynx 2019");

        createAndAssertForCarnivore(dto);
    }

    @Test
    public void createCarnivore_lynxPoronhoito() {
        final HarvestPermitApplicationCreateDTO dto = new HarvestPermitApplicationCreateDTO();
        dto.setCategory(LARGE_CARNIVORE_LYNX_PORONHOITO);
        dto.setHuntingYear(2019);
        dto.setApplicationName("lynx poronhoito 2019");

        createAndAssertForCarnivore(dto);
    }

    @Test
    public void createCarnivore_wolf() {
        final HarvestPermitApplicationCreateDTO dto = new HarvestPermitApplicationCreateDTO();
        dto.setCategory(LARGE_CARNIVORE_WOLF);
        dto.setHuntingYear(2019);
        dto.setApplicationName("wolf 2019");

        createAndAssertForCarnivore(dto);
    }

    private void createAndAssertForCarnivore(final HarvestPermitApplicationCreateDTO dto) {
        onSavedAndAuthenticated(createUserWithPerson("applicant"), () -> {
            final HarvestPermitApplicationBasicDetailsDTO harvestPermitApplicationBasicDetailsDTO = feature.create(dto, Locales.FI);

            assertEquals(2019, harvestPermitApplicationBasicDetailsDTO.getHuntingYear());
            assertNotNull(harvestPermitApplicationBasicDetailsDTO.getId());

            final HarvestPermitApplication application = applicationRepository.findOne(harvestPermitApplicationBasicDetailsDTO.getId());
            assertEquals(dto.getCategory(), application.getHarvestPermitCategory());
            final CarnivorePermitApplication carnivorePermitApplication = carnivorePermitApplicationRepository.findByHarvestPermitApplication(application);
            assertNotNull(carnivorePermitApplication);

            assertEmpty(birdPermitApplicationRepository.findAll());
        });
    }

}
