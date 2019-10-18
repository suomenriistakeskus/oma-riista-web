package fi.riista.feature.permit.decision.publish;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountRepository;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.PermitNumberUtil;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.PermitDecisionRepository;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmount;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmountRepository;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.Collect;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import fi.riista.util.Locales;
import fi.riista.util.NumberUtils;
import org.joda.time.LocalDate;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class HarvestPermitDecisionSynchronizerTest extends EmbeddedDatabaseTest {

    @Resource
    private HarvestPermitDecisionSynchronizer harvestPermitDecisionSynchronizer;

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @Resource
    private HarvestPermitSpeciesAmountRepository harvestPermitSpeciesAmountRepository;

    @Resource
    private PermitDecisionRepository permitDecisionRepository;

    @Resource
    private PermitDecisionSpeciesAmountRepository permitDecisionSpeciesAmountRepository;

    @Test
    public void testMooselike_Create_Smoke() {
        final HarvestPermitArea permitArea = model().newHarvestPermitArea();
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final HarvestPermitApplication application =
                model().newHarvestPermitApplication(rhy, permitArea, HarvestPermitCategory.MOOSELIKE);
        final PermitDecision decision = model().newPermitDecision(application);
        final GameSpecies species = model().newGameSpeciesMoose();
        final PermitDecisionSpeciesAmount decisionSpeciesAmount = model().newPermitDecisionSpeciesAmount(decision,
                species, 1f);

        runInTransaction(() -> {
            persistInCurrentlyOpenTransaction();

            harvestPermitDecisionSynchronizer.synchronize(decision);
        });

        runInTransaction(() -> {
            final List<HarvestPermit> permitList = harvestPermitRepository.findAll();
            final List<HarvestPermitSpeciesAmount> speciesAmountList = harvestPermitSpeciesAmountRepository.findAll();

            assertThat(permitList, hasSize(1));
            assertThat(speciesAmountList, hasSize(1));

            final HarvestPermit harvestPermit = permitList.get(0);
            final HarvestPermitSpeciesAmount speciesAmount = speciesAmountList.get(0);

            assertDecisionEquals(decision, harvestPermit, decision.getDecisionYear());
            assertSpeciesEquals(decisionSpeciesAmount, speciesAmount);
        });
    }

    @Test
    public void testBird_Create_Smoke() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final GameSpecies species = model().newGameSpecies(GameSpecies.BIRD_PERMIT_SPECIES.iterator().next());

        final HarvestPermitApplication application =
                model().newHarvestPermitApplication(rhy, null, HarvestPermitCategory.BIRD);
        final HarvestPermitApplicationSpeciesAmount applicationSpeciesAmount =
                model().newHarvestPermitApplicationSpeciesAmount(application, species, 1f, 1);
        application.getSpeciesAmounts().add(applicationSpeciesAmount);

        final PermitDecision decision = model().newPermitDecision(application);
        final PermitDecisionSpeciesAmount decisionSpeciesAmount =
                model().newPermitDecisionSpeciesAmount(decision, species, 1f);

        runInTransaction(() -> {
            persistInCurrentlyOpenTransaction();

            harvestPermitDecisionSynchronizer.synchronize(decision);
        });

        runInTransaction(() -> {
            final List<HarvestPermit> permitList = harvestPermitRepository.findAll();
            final List<HarvestPermitSpeciesAmount> speciesAmountList = harvestPermitSpeciesAmountRepository.findAll();

            assertThat(permitList, hasSize(1));
            assertThat(speciesAmountList, hasSize(1));

            final HarvestPermit harvestPermit = permitList.get(0);
            final HarvestPermitSpeciesAmount speciesAmount = speciesAmountList.get(0);

            assertDecisionEquals(decision, harvestPermit, decision.getDecisionYear());
            assertSpeciesEquals(decisionSpeciesAmount, speciesAmount);
        });
    }

    @Test
    public void testMooselike_Update_Smoke() {
        final HarvestPermitArea permitArea = model().newHarvestPermitArea();
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final HarvestPermitApplication application =
                model().newHarvestPermitApplication(rhy, permitArea, HarvestPermitCategory.MOOSELIKE);

        final PermitDecision decision = model().newPermitDecision(application);
        decision.setDecisionYear(2019);

        final GameSpecies species1 = model().newGameSpecies(GameSpecies.OFFICIAL_CODE_MOOSE);
        final GameSpecies species2 = model().newGameSpecies(GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER);
        final GameSpecies species3 = model().newGameSpecies(GameSpecies.OFFICIAL_CODE_WILD_FOREST_REINDEER);
        final GameSpecies species4 = model().newGameSpecies(GameSpecies.OFFICIAL_CODE_FALLOW_DEER);

        final PermitDecisionSpeciesAmount decisionSpa1 =
                model().newPermitDecisionSpeciesAmount(decision, species1, 1f);
        decisionSpa1.setBeginDate(new LocalDate(2019, 9, 1));
        decisionSpa1.setEndDate(new LocalDate(2019, 12, 31));

        final PermitDecisionSpeciesAmount decisionSpa2 =
                model().newPermitDecisionSpeciesAmount(decision, species2, 2f);
        decisionSpa2.setBeginDate(new LocalDate(2019, 9, 1));
        decisionSpa2.setEndDate(new LocalDate(2020, 2, 15));

        final PermitDecisionSpeciesAmount decisionSpa3 =
                model().newPermitDecisionSpeciesAmount(decision, species3, 3f);
        decisionSpa3.setBeginDate(new LocalDate(2019, 1, 1));
        decisionSpa3.setEndDate(new LocalDate(2019, 12, 31));

        final PermitDecisionSpeciesAmount decisionSpa4 =
                model().newPermitDecisionSpeciesAmount(decision, species4, 0);
        decisionSpa4.setBeginDate(new LocalDate(2019, 9, 1));
        decisionSpa4.setEndDate(new LocalDate(2019, 12, 31));

        persistInNewTransaction();

        // CREATE
        runInTransaction(() -> {
            harvestPermitDecisionSynchronizer.synchronize(permitDecisionRepository.getOne(decision.getId()));
        });

        // CHECK
        runInTransaction(() -> {
            assertThat(harvestPermitRepository.findAll(), hasSize(1));
            assertThat(harvestPermitSpeciesAmountRepository.findAll(), hasSize(3));
        });

        // UPDATE
        runInTransaction(() -> {
            final PermitDecision d = permitDecisionRepository.getOne(decision.getId());
            d.setLocale(Locales.SV);
            permitDecisionSpeciesAmountRepository.getOne(decisionSpa1.getId()).setAmount(0f);
            permitDecisionSpeciesAmountRepository.getOne(decisionSpa2.getId()).setAmount(20f);
            permitDecisionSpeciesAmountRepository.getOne(decisionSpa3.getId()).setAmount(30f);
            permitDecisionSpeciesAmountRepository.getOne(decisionSpa4.getId()).setAmount(40f);
        });

        // SYNCHRONIZE
        runInTransaction(() -> {
            harvestPermitDecisionSynchronizer.synchronize(permitDecisionRepository.getOne(decision.getId()));
        });

        runInTransaction(() -> {
            final List<HarvestPermit> permitList = harvestPermitRepository.findAll();
            final List<HarvestPermitSpeciesAmount> speciesAmountList = harvestPermitSpeciesAmountRepository.findAll();

            assertThat(permitList, hasSize(1));
            assertThat(speciesAmountList, hasSize(3));

            final HarvestPermit harvestPermit = permitList.get(0);
            assertDecisionEquals(permitDecisionRepository.getOne(decision.getId()), harvestPermit, 2019);

            final Map<Integer, HarvestPermitSpeciesAmount> speciesAmountIndex =
                    F.index(speciesAmountList, spa -> spa.getGameSpecies().getOfficialCode());

            final HarvestPermitSpeciesAmount spa1 = speciesAmountIndex.get(GameSpecies.OFFICIAL_CODE_MOOSE);
            final HarvestPermitSpeciesAmount spa2 = speciesAmountIndex.get(GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER);
            final HarvestPermitSpeciesAmount spa3 =
                    speciesAmountIndex.get(GameSpecies.OFFICIAL_CODE_WILD_FOREST_REINDEER);
            final HarvestPermitSpeciesAmount spa4 = speciesAmountIndex.get(GameSpecies.OFFICIAL_CODE_FALLOW_DEER);

            assertNull(spa1);
            assertSpeciesEquals(permitDecisionSpeciesAmountRepository.getOne(decisionSpa2.getId()), spa2);
            assertSpeciesEquals(permitDecisionSpeciesAmountRepository.getOne(decisionSpa3.getId()), spa3);
            assertSpeciesEquals(permitDecisionSpeciesAmountRepository.getOne(decisionSpa4.getId()), spa4);
        });
    }

    @Test
    public void testBird_Update_Smoke() {
        final int firstSpeciesCode = 26287;
        final int secondSpeciesCode = 26291;

        final GameSpecies species1 = model().newGameSpecies(firstSpeciesCode);
        final GameSpecies species2 = model().newGameSpecies(secondSpeciesCode);

        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final HarvestPermitApplication application =
                model().newHarvestPermitApplication(rhy, null, HarvestPermitCategory.BIRD);

        final HarvestPermitApplicationSpeciesAmount applicationSpecies1 =
                model().newHarvestPermitApplicationSpeciesAmount(application, species1);
        applicationSpecies1.setValidityYears(2);

        final HarvestPermitApplicationSpeciesAmount applicationSpecies2 =
                model().newHarvestPermitApplicationSpeciesAmount(application, species2);
        applicationSpecies2.setValidityYears(2);

        application.setSpeciesAmounts(Arrays.asList(applicationSpecies1, applicationSpecies2));

        final PermitDecision decision = model().newPermitDecision(application);
        decision.setDecisionYear(2019);
        decision.setValidityYears(2);

        final String firstPermitNumber = PermitNumberUtil.createPermitNumber(2019, 2, decision.getDecisionNumber());
        final String secondPermitNumber = PermitNumberUtil.createPermitNumber(2020, 2, decision.getDecisionNumber());

        final PermitDecisionSpeciesAmount decisionSpa1 =
                model().newPermitDecisionSpeciesAmount(decision, species1, 10f);
        decisionSpa1.setBeginDate(new LocalDate(2019, 1, 1));
        decisionSpa1.setEndDate(new LocalDate(2019, 12, 31));

        final PermitDecisionSpeciesAmount decisionSpa2 =
                model().newPermitDecisionSpeciesAmount(decision, species1, 20f);
        decisionSpa2.setBeginDate(new LocalDate(2020, 1, 1));
        decisionSpa2.setEndDate(new LocalDate(2020, 12, 31));

        final PermitDecisionSpeciesAmount decisionSpa3 =
                model().newPermitDecisionSpeciesAmount(decision, species2, 30f);
        decisionSpa3.setBeginDate(new LocalDate(2019, 1, 1));
        decisionSpa3.setEndDate(new LocalDate(2019, 12, 31));

        final PermitDecisionSpeciesAmount decisionSpa4 =
                model().newPermitDecisionSpeciesAmount(decision, species2, 40f);
        decisionSpa4.setBeginDate(new LocalDate(2020, 1, 1));
        decisionSpa4.setEndDate(new LocalDate(2020, 12, 31));

        persistInNewTransaction();

        // CREATE
        runInTransaction(() -> {
            harvestPermitDecisionSynchronizer.synchronize(permitDecisionRepository.getOne(decision.getId()));
        });

        // CHECK
        runInTransaction(() -> {
            final List<HarvestPermit> permitList = harvestPermitRepository.findAll();
            assertThat(permitList, hasSize(2));
            assertThat(F.mapNonNullsToSet(permitList, HarvestPermit::getPermitNumber),
                    containsInAnyOrder(firstPermitNumber, secondPermitNumber));
            assertThat(harvestPermitSpeciesAmountRepository.findAll(), hasSize(4));
        });

        // UPDATE
        runInTransaction(() -> {
            final PermitDecision d = permitDecisionRepository.getOne(decision.getId());
            d.setLocale(Locales.SV);

            permitDecisionSpeciesAmountRepository.getOne(decisionSpa1.getId()).setAmount(1f);
            permitDecisionSpeciesAmountRepository.getOne(decisionSpa2.getId()).setAmount(2f);
            permitDecisionSpeciesAmountRepository.getOne(decisionSpa3.getId()).setAmount(3f);
            permitDecisionSpeciesAmountRepository.getOne(decisionSpa4.getId()).setAmount(4f);
        });

        // SYNCHRONIZE
        runInTransaction(() -> {
            harvestPermitDecisionSynchronizer.synchronize(permitDecisionRepository.getOne(decision.getId()));
        });

        runInTransaction(() -> {
            final List<HarvestPermit> permitList = harvestPermitRepository.findAll();
            final List<HarvestPermitSpeciesAmount> speciesAmountList = harvestPermitSpeciesAmountRepository.findAll();

            assertThat(permitList, hasSize(2));
            assertThat(speciesAmountList, hasSize(4));

            final Map<String, HarvestPermit> permitIndex = F.index(permitList, HarvestPermit::getPermitNumber);
            final HarvestPermit firstPermit = permitIndex.get(firstPermitNumber);
            final HarvestPermit secondPermit = permitIndex.get(secondPermitNumber);

            assertNotNull(firstPermit);
            assertNotNull(secondPermit);

            final PermitDecision d = permitDecisionRepository.getOne(decision.getId());
            assertDecisionEquals(d, firstPermit, 2019);
            assertDecisionEquals(d, secondPermit, 2020);

            final Map<String, List<HarvestPermitSpeciesAmount>> speciesByPermitIndex =
                    speciesAmountList.stream().collect(Collect.nullSafeGroupingBy(spa -> spa.getHarvestPermit().getPermitNumber()));

            assertThat(speciesByPermitIndex, hasKey(firstPermitNumber));
            assertThat(speciesByPermitIndex, hasKey(secondPermitNumber));

            final Map<Integer, HarvestPermitSpeciesAmount> firstPermitSpecies =
                    F.index(speciesByPermitIndex.get(firstPermitNumber), spa -> spa.getGameSpecies().getOfficialCode());

            final Map<Integer, HarvestPermitSpeciesAmount> secondPermitSpecies =
                    F.index(speciesByPermitIndex.get(secondPermitNumber),
                            spa -> spa.getGameSpecies().getOfficialCode());

            final HarvestPermitSpeciesAmount spa1 = firstPermitSpecies.get(firstSpeciesCode);
            final HarvestPermitSpeciesAmount spa2 = secondPermitSpecies.get(firstSpeciesCode);
            final HarvestPermitSpeciesAmount spa3 = firstPermitSpecies.get(secondSpeciesCode);
            final HarvestPermitSpeciesAmount spa4 = secondPermitSpecies.get(secondSpeciesCode);

            assertSpeciesEquals(permitDecisionSpeciesAmountRepository.getOne(decisionSpa1.getId()), spa1);
            assertSpeciesEquals(permitDecisionSpeciesAmountRepository.getOne(decisionSpa2.getId()), spa2);
            assertSpeciesEquals(permitDecisionSpeciesAmountRepository.getOne(decisionSpa3.getId()), spa3);
            assertSpeciesEquals(permitDecisionSpeciesAmountRepository.getOne(decisionSpa4.getId()), spa4);
        });

    }

    // Multiyear bird permit

    @Test
    public void testBird_multiYearApplication() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final GameSpecies species = model().newGameSpecies(GameSpecies.BIRD_PERMIT_SPECIES.iterator().next());

        final HarvestPermitApplication application =
                model().newHarvestPermitApplication(rhy, null, HarvestPermitCategory.BIRD);
        final HarvestPermitApplicationSpeciesAmount applicationSpeciesAmount =
                model().newHarvestPermitApplicationSpeciesAmount(application, species, 1f, 5);
        application.getSpeciesAmounts().add(applicationSpeciesAmount);

        final PermitDecision decision = model().newPermitDecision(application);
        final Map<Integer, PermitDecisionSpeciesAmount> decisionSpecies = IntStream.range(0, 5)
                .mapToObj(i -> {
                    final PermitDecisionSpeciesAmount spa =
                            model().newPermitDecisionSpeciesAmount(decision, species, 1f);
                    spa.setBeginDate(spa.getBeginDate().plusYears(i));
                    spa.setEndDate(spa.getEndDate().plusYears(i));
                    return spa;
                }).collect(Collect.indexingBy(PermitDecisionSpeciesAmount::getPermitYear));

        runInTransaction(() -> {
            persistInCurrentlyOpenTransaction();
            harvestPermitDecisionSynchronizer.synchronize(decision);
        });

        runInTransaction(() -> {
            final List<HarvestPermit> permitList = harvestPermitRepository.findAll();
            final List<HarvestPermitSpeciesAmount> speciesAmountList = harvestPermitSpeciesAmountRepository.findAll();

            assertThat(permitList, hasSize(5));
            assertThat(speciesAmountList, hasSize(5));

            permitList.forEach(p -> {
                final String expectedPermitNumber = PermitNumberUtil.createPermitNumber(p.getPermitYear(),
                        decision.getValidityYears(),
                        decision.getDecisionNumber());
                assertDecisionEquals(decision, p, p.getPermitYear());
            });

            speciesAmountList.forEach(spa -> {
                assertSpeciesEquals(decisionSpecies.get(spa.resolveHuntingYear()), spa);
            });

        });
    }

    @Test
    public void testBird_restrictedMultiYearApplication() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final GameSpecies species = model().newGameSpecies(GameSpecies.BIRD_PERMIT_SPECIES.iterator().next());

        // Two year application
        final HarvestPermitApplication application =
                model().newHarvestPermitApplication(rhy, null, HarvestPermitCategory.BIRD);
        final HarvestPermitApplicationSpeciesAmount applicationSpeciesAmount =
                model().newHarvestPermitApplicationSpeciesAmount(application, species, 1f, 2);
        application.getSpeciesAmounts().add(applicationSpeciesAmount);

        // Decision only for one year
        final PermitDecision decision = model().newPermitDecision(application);
        decision.setGrantStatus(PermitDecision.GrantStatus.RESTRICTED);
        final PermitDecisionSpeciesAmount decisionSpaApproved = model().newPermitDecisionSpeciesAmount(decision,
                species, 1f);
        LocalDate begin = DateUtil.today();
        final LocalDate end = begin.plusMonths(2);
        decisionSpaApproved.setBeginDate(begin);
        decisionSpaApproved.setEndDate(end);
        final PermitDecisionSpeciesAmount decisionSpaRejected = model().newPermitDecisionSpeciesAmount(decision,
                species, 0f);
        decisionSpaRejected.setBeginDate(begin.plusYears(1));
        decisionSpaRejected.setEndDate(end.plusYears(1));

        runInTransaction(() -> {
            persistInCurrentlyOpenTransaction();
            harvestPermitDecisionSynchronizer.synchronize(decision);
        });

        runInTransaction(() -> {
            final List<HarvestPermit> permitList = harvestPermitRepository.findAll();
            final List<HarvestPermitSpeciesAmount> speciesAmountList = harvestPermitSpeciesAmountRepository.findAll();

            assertThat(permitList, hasSize(1));
            assertThat(speciesAmountList, hasSize(1));

            final HarvestPermit permit = permitList.get(0);
            final HarvestPermitSpeciesAmount spa = speciesAmountList.get(0);

            // Permit and spa created for first year
            assertDecisionEquals(decision, permit, permit.getPermitYear());
            assertSpeciesEquals(decisionSpaApproved, spa);
        });
    }

    // Rejected applications

    @Test
    public void testMooselike_rejectedCreatesEmptyPermit() {
        final HarvestPermitArea permitArea = model().newHarvestPermitArea();
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final HarvestPermitApplication application =
                model().newHarvestPermitApplication(rhy, permitArea, HarvestPermitCategory.MOOSELIKE);
        final PermitDecision decision = model().newPermitDecision(application);
        decision.setGrantStatus(PermitDecision.GrantStatus.REJECTED);
        final GameSpecies species = model().newGameSpeciesMoose();
        final PermitDecisionSpeciesAmount decisionSpeciesAmount = model().newPermitDecisionSpeciesAmount(decision,
                species, 0f);

        runInTransaction(() -> {
            persistInCurrentlyOpenTransaction();

            harvestPermitDecisionSynchronizer.synchronize(decision);
        });

        runInTransaction(() -> {
            final List<HarvestPermit> permitList = harvestPermitRepository.findAll();
            final List<HarvestPermitSpeciesAmount> speciesAmountList = harvestPermitSpeciesAmountRepository.findAll();

            assertThat(permitList, hasSize(1));
            assertThat(speciesAmountList, hasSize(0));

            final HarvestPermit harvestPermit = permitList.get(0);

            assertDecisionEquals(decision, harvestPermit, decision.getDecisionYear());
        });
    }


    @Test
    public void testBird_rejectedMultiYearApplication() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final GameSpecies species = model().newGameSpecies(GameSpecies.BIRD_PERMIT_SPECIES.iterator().next());

        final HarvestPermitApplication application =
                model().newHarvestPermitApplication(rhy, null, HarvestPermitCategory.BIRD);
        final HarvestPermitApplicationSpeciesAmount applicationSpeciesAmount =
                model().newHarvestPermitApplicationSpeciesAmount(application, species, 1f, 5);
        application.getSpeciesAmounts().add(applicationSpeciesAmount);

        final PermitDecision decision = model().newPermitDecision(application);
        decision.setGrantStatus(PermitDecision.GrantStatus.REJECTED);
        IntStream.range(0, 5).forEach(i -> {
            final PermitDecisionSpeciesAmount spa =
                    model().newPermitDecisionSpeciesAmount(decision, species, 0);
            spa.setBeginDate(spa.getBeginDate().plusYears(i));
            spa.setEndDate(spa.getEndDate().plusYears(i));
        });

        runInTransaction(() -> {
            persistInCurrentlyOpenTransaction();
            harvestPermitDecisionSynchronizer.synchronize(decision);
        });

        runInTransaction(() -> {
            final List<HarvestPermit> permitList = harvestPermitRepository.findAll();
            final List<HarvestPermitSpeciesAmount> speciesAmountList = harvestPermitSpeciesAmountRepository.findAll();

            assertThat(permitList, hasSize(1));
            assertThat(speciesAmountList, hasSize(0));

            final String expectedPermitNumber = PermitNumberUtil.createPermitNumber(decision.getDecisionYear(),
                    decision.getValidityYears(),
                    decision.getDecisionNumber());

            assertEquals(expectedPermitNumber, permitList.get(0).getPermitNumber());

        });
    }

    private static void assertSpeciesEquals(final PermitDecisionSpeciesAmount decisionSpeciesAmount,
                                            final HarvestPermitSpeciesAmount speciesAmount) {
        assertEquals(decisionSpeciesAmount.getAmount(), speciesAmount.getAmount(), 0.1);
        assertEquals(decisionSpeciesAmount.getBeginDate(), speciesAmount.getBeginDate());
        assertEquals(decisionSpeciesAmount.getBeginDate2(), speciesAmount.getBeginDate2());
        assertEquals(decisionSpeciesAmount.getEndDate(), speciesAmount.getEndDate());
        assertEquals(decisionSpeciesAmount.getEndDate2(), speciesAmount.getEndDate2());
        assertEquals(HarvestPermitSpeciesAmountOps.convertRestriction(decisionSpeciesAmount),
                speciesAmount.getRestrictionType());

        if (decisionSpeciesAmount.getRestrictionAmount() != null) {
            assertNotNull(speciesAmount.getRestrictionAmount());
            assertEquals(decisionSpeciesAmount.getRestrictionAmount(), speciesAmount.getRestrictionAmount(), 0.1);
        } else {
            assertNull(speciesAmount.getRestrictionAmount());
        }
    }

    private static void assertDecisionEquals(final PermitDecision decision,
                                             final HarvestPermit harvestPermit,
                                             final int year) {
        final HarvestPermitApplication application = decision.getApplication();
        final HarvestPermitArea permitArea = application.getArea();

        final String expectedPermitNumber = PermitNumberUtil.createPermitNumber(year, decision.getValidityYears(),
                decision.getDecisionNumber());

        assertEquals(expectedPermitNumber, harvestPermit.getPermitNumber());
        assertEquals(decision.getPermitHolder(), harvestPermit.getPermitHolder());
        assertEquals(decision.getPermitTypeCode(), harvestPermit.getPermitTypeCode());
        assertEquals(decision.getDecisionName(), harvestPermit.getPermitType());
        assertEquals(decision.getHuntingClub(), harvestPermit.getHuntingClub());
        assertEquals(decision.getContactPerson(), harvestPermit.getOriginalContactPerson());
        assertEquals(decision.getRhy(), harvestPermit.getRhy());
        assertEquals(decision.getHta(), harvestPermit.getMooseArea());
        assertEquals(application.getRelatedRhys(), harvestPermit.getRelatedRhys());
        assertEquals(application.getPermitPartners(), application.getPermitPartners());

        // TODO:
        assertNull(harvestPermit.getOriginalPermit());

        if (permitArea != null) {
            assertNotNull(harvestPermit.getPermitAreaSize());
            assertEquals(NumberUtils.squareMetersToHectares(permitArea.getZone().getComputedAreaSize()),
                    harvestPermit.getPermitAreaSize().longValue());
        } else {
            assertNull(harvestPermit.getPermitAreaSize());
        }
    }
}
