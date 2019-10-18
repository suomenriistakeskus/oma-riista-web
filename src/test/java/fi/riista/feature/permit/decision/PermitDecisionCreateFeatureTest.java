package fi.riista.feature.permit.decision;

import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.feature.permit.application.DeliveryAddress;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.bird.BirdPermitApplication;
import fi.riista.feature.permit.application.bird.cause.BirdPermitApplicationCause;
import fi.riista.feature.permit.application.bird.forbidden.BirdPermitApplicationForbiddenMethods;
import fi.riista.feature.permit.application.carnivore.CarnivorePermitApplication;
import fi.riista.feature.permit.application.carnivore.species.CarnivorePermitSpecies;
import fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReason;
import fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonRepository;
import fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonType;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.ReflectionTestUtils;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

import static fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonType.REASON_AVIATION_SAFETY;
import static fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonType.REASON_CROPS_DAMAMGE;
import static fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonType.REASON_DOMESTIC_PETS;
import static fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonType.REASON_FAUNA;
import static fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonType.REASON_FISHING;
import static fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonType.REASON_FLORA;
import static fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonType.REASON_FOREST_DAMAGE;
import static fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonType.REASON_POPULATION_PRESERVATION;
import static fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonType.REASON_PUBLIC_HEALTH;
import static fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonType.REASON_PUBLIC_SAFETY;
import static fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonType.REASON_RESEARCH;
import static fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonType.REASON_WATER_SYSTEM;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class PermitDecisionCreateFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private PermitDecisionCreateFeature permitDecisionCreateFeature;

    @Resource
    private PermitDecisionDerogationReasonRepository permitDecisionDerogationReasonRepository;

    private Riistanhoitoyhdistys rhy;

    @Before
    public void setup() {
        rhy = model().newRiistanhoitoyhdistys();
        rhy.setEmail("rhy@invalid");
    }


    // MOOSELIKE

    @Test
    public void testMooselike() {
        final HarvestPermitApplication harvestPermitApplication = createMooselikeApplication();

        persistInNewTransaction();

        runInTransaction(() -> {
            final PermitDecision decision = permitDecisionCreateFeature.createDecision(harvestPermitApplication);
            assertEquals(decision.getPermitTypeCode(), PermitTypeCode.MOOSELIKE);
        });
    }

    // BIRD

    @Test
    public void testBird() {
        final BirdPermitApplication birdPermitApplication = createBirdPermitApplication();

        persistInNewTransaction();

        runInTransaction(() -> {
            final PermitDecision decision = permitDecisionCreateFeature
                    .createDecision(birdPermitApplication.getHarvestPermitApplication());
            assertEquals(decision.getPermitTypeCode(), PermitTypeCode.FOWL_AND_UNPROTECTED_BIRD);
        });
    }

    @Test
    public void testBird_legalSectionsCopied() {
        final BirdPermitApplication birdPermitApplication = createBirdPermitApplication();

        final BirdPermitApplicationForbiddenMethods forbiddenMethods = birdPermitApplication.getForbiddenMethods();
        forbiddenMethods.setDeviateSection32("deviate");
        forbiddenMethods.setDeviateSection33("deviate");
        forbiddenMethods.setDeviateSection34("deviate");
        forbiddenMethods.setDeviateSection35("");
        forbiddenMethods.setDeviateSection51(null);

        persistInNewTransaction();

        runInTransaction(() -> {
            final PermitDecision decision = permitDecisionCreateFeature
                    .createDecision(birdPermitApplication.getHarvestPermitApplication());
            assertTrue(decision.isLegalSection32());
            assertTrue(decision.isLegalSection33());
            assertTrue(decision.isLegalSection34());
            assertFalse(decision.isLegalSection35());
            assertFalse(decision.isLegalSection51());
        });
    }

    @Test
    public void testBird_legalSectionsCopied_trapsAndTapeRecorders() {
        final BirdPermitApplication birdPermitApplication = createBirdPermitApplication();

        final BirdPermitApplicationForbiddenMethods forbiddenMethods = birdPermitApplication.getForbiddenMethods();
        forbiddenMethods.setTraps(true);
        forbiddenMethods.setTapeRecorders(true);

        persistInNewTransaction();

        runInTransaction(() -> {
            final PermitDecision decision = permitDecisionCreateFeature
                    .createDecision(birdPermitApplication.getHarvestPermitApplication());
            assertFalse(decision.isLegalSection32());
            assertTrue(decision.isLegalSection33());
            assertTrue(decision.isLegalSection34());
            assertFalse(decision.isLegalSection35());
            assertFalse(decision.isLegalSection51());
        });
    }

    @Test
    public void testBird_allCauseFlagsMappable() {
        final BirdPermitApplication birdPermitApplication = createBirdPermitApplication();

        final BirdPermitApplicationCause birdPermitApplicationCause = new BirdPermitApplicationCause();
        setAllCauses(birdPermitApplicationCause);
        birdPermitApplication.setCause(birdPermitApplicationCause);

        persistInNewTransaction();

        runInTransaction(() -> {
            final PermitDecision decision = permitDecisionCreateFeature
                    .createDecision(birdPermitApplication.getHarvestPermitApplication());

            final List<PermitDecisionDerogationReasonType> collect = permitDecisionDerogationReasonRepository
                    .findByPermitDecision(decision)
                    .stream()
                    .map(PermitDecisionDerogationReason::getReasonType)
                    .collect(toList());

            assertTrue(collect.contains(REASON_PUBLIC_HEALTH));
            assertTrue(collect.contains(REASON_PUBLIC_SAFETY));
            assertTrue(collect.contains(REASON_AVIATION_SAFETY));
            assertTrue(collect.contains(REASON_CROPS_DAMAMGE));
            assertTrue(collect.contains(REASON_DOMESTIC_PETS));
            assertTrue(collect.contains(REASON_FOREST_DAMAGE));
            assertTrue(collect.contains(REASON_FISHING));
            assertTrue(collect.contains(REASON_WATER_SYSTEM));
            assertTrue(collect.contains(REASON_FLORA));
            assertTrue(collect.contains(REASON_FAUNA));
            assertTrue(collect.contains(REASON_RESEARCH));
        });
    }

    // CARNIVORE

    @Test
    public void testCarnivore() {
        final CarnivorePermitApplication carnivorePermitApplication =
                createCarnivorePermitApplication(HarvestPermitCategory.LARGE_CARNIVORE_BEAR);

        persistInNewTransaction();

        runInTransaction(() -> {
            final PermitDecision decision = permitDecisionCreateFeature
                    .createDecision(carnivorePermitApplication.getHarvestPermitApplication());
            assertEquals(decision.getPermitTypeCode(), PermitTypeCode.BEAR_KANNAHOIDOLLINEN);
        });
    }

    @Test
    public void testCarnivore_derogationReasonApplied() {
        final CarnivorePermitApplication carnivorePermitApplication =
                createCarnivorePermitApplication(HarvestPermitCategory.LARGE_CARNIVORE_BEAR);

        persistInNewTransaction();

        runInTransaction(() -> {
            final PermitDecision decision = permitDecisionCreateFeature
                    .createDecision(carnivorePermitApplication.getHarvestPermitApplication());

            final List<PermitDecisionDerogationReasonType> collect = permitDecisionDerogationReasonRepository
                    .findByPermitDecision(decision)
                    .stream()
                    .map(PermitDecisionDerogationReason::getReasonType)
                    .collect(toList());

            assertThat(collect, hasSize(1));
            assertTrue(collect.contains(REASON_POPULATION_PRESERVATION));
        });
    }

    private void setAllCauses(BirdPermitApplicationCause birdPermitApplicationCause) {
        ReflectionTestUtils.methodsOfClass(false)
                .apply(BirdPermitApplicationCause.class)
                .filter(m -> m.getName().startsWith("set")).forEach(m -> {
            try {
                m.invoke(birdPermitApplicationCause, true);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        });
    }

    private HarvestPermitApplication createMooselikeApplication() {
        return model().newHarvestPermitApplication(rhy, model().newHarvestPermitArea(),
                HarvestPermitCategory.MOOSELIKE);
    }

    private BirdPermitApplication createBirdPermitApplication() {
        final HarvestPermitApplication application =
                model().newHarvestPermitApplication(rhy, null, HarvestPermitCategory.BIRD);
        final BirdPermitApplication birdPermitApplication = model().newBirdPermitApplication(application);

        application.setDeliveryAddress(DeliveryAddress.createFromPersonNullable(application.getContactPerson()));
        final HarvestPermitApplicationSpeciesAmount harvestPermitApplicationSpeciesAmount =
                model().newHarvestPermitApplicationSpeciesAmount(application, model().newGameSpecies(), 1.0f, 4);
        application.setSpeciesAmounts(Collections.singletonList(harvestPermitApplicationSpeciesAmount));
        return birdPermitApplication;
    }

    private CarnivorePermitApplication createCarnivorePermitApplication(final HarvestPermitCategory category) {
        final HarvestPermitApplication application =
                model().newHarvestPermitApplication(rhy, null, category);
        final CarnivorePermitApplication carnivorePermitApplication =
                model().newCarnivorePermitApplication(application);

        application.setDeliveryAddress(DeliveryAddress.createFromPersonNullable(application.getContactPerson()));
        final HarvestPermitApplicationSpeciesAmount harvestPermitApplicationSpeciesAmount =
                model().newHarvestPermitApplicationSpeciesAmount(application,
                        model().newGameSpecies(CarnivorePermitSpecies.getSpecies(category)), 1.0f, 1);
        application.setSpeciesAmounts(Collections.singletonList(harvestPermitApplicationSpeciesAmount));
        return carnivorePermitApplication;
    }
}
