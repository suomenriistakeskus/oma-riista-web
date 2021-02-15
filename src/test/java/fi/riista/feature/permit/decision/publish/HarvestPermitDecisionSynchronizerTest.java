package fi.riista.feature.permit.decision.publish;

import fi.riista.feature.common.decision.GrantStatus;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountRepository;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.DocumentNumberUtil;
import fi.riista.feature.permit.PermitTypeCode;
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
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BEAN_GOOSE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WIGEON;
import static fi.riista.feature.permit.decision.PermitDecision.DecisionType.CANCEL_ANNUAL_RENEWAL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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

    private Riistanhoitoyhdistys rhy;
    private GameSpecies birdSpecies;

    @Before
    public void setup() {
        rhy = model().newRiistanhoitoyhdistys();
        birdSpecies = model().newGameSpecies(OFFICIAL_CODE_BEAN_GOOSE);
    }

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
        final HarvestPermitApplication application = createApplicationWithAmounts(1);

        final PermitDecision decision = model().newPermitDecision(application);
        final PermitDecisionSpeciesAmount decisionSpeciesAmount =
                model().newPermitDecisionSpeciesAmount(decision, birdSpecies, 1f);

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
            permitDecisionSpeciesAmountRepository.getOne(decisionSpa1.getId()).setSpecimenAmount(0f);
            permitDecisionSpeciesAmountRepository.getOne(decisionSpa2.getId()).setSpecimenAmount(20f);
            permitDecisionSpeciesAmountRepository.getOne(decisionSpa3.getId()).setSpecimenAmount(30f);
            permitDecisionSpeciesAmountRepository.getOne(decisionSpa4.getId()).setSpecimenAmount(40f);
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
        final int secondSpeciesCode = OFFICIAL_CODE_WIGEON;

        final GameSpecies birdSpecies2 = model().newGameSpecies(secondSpeciesCode);


        final HarvestPermitApplication application =
                model().newHarvestPermitApplication(rhy, null, HarvestPermitCategory.BIRD);

        final HarvestPermitApplicationSpeciesAmount applicationSpecies1 =
                model().newHarvestPermitApplicationSpeciesAmount(application, birdSpecies);
        applicationSpecies1.setValidityYears(2);

        final HarvestPermitApplicationSpeciesAmount applicationSpecies2 =
                model().newHarvestPermitApplicationSpeciesAmount(application, birdSpecies2);
        applicationSpecies2.setValidityYears(2);

        application.setSpeciesAmounts(Arrays.asList(applicationSpecies1, applicationSpecies2));

        final PermitDecision decision = model().newPermitDecision(application);
        decision.setDecisionYear(2019);
        decision.setValidityYears(2);

        final String firstPermitNumber = DocumentNumberUtil.createDocumentNumber(2019, 2, decision.getDecisionNumber());
        final String secondPermitNumber = DocumentNumberUtil.createDocumentNumber(2020, 2, decision.getDecisionNumber());

        final PermitDecisionSpeciesAmount decisionSpa1 =
                model().newPermitDecisionSpeciesAmount(decision, birdSpecies, 10f);
        decisionSpa1.setBeginDate(new LocalDate(2019, 1, 1));
        decisionSpa1.setEndDate(new LocalDate(2019, 12, 31));

        final PermitDecisionSpeciesAmount decisionSpa2 =
                model().newPermitDecisionSpeciesAmount(decision, birdSpecies, 20f);
        decisionSpa2.setBeginDate(new LocalDate(2020, 1, 1));
        decisionSpa2.setEndDate(new LocalDate(2020, 12, 31));

        final PermitDecisionSpeciesAmount decisionSpa3 =
                model().newPermitDecisionSpeciesAmount(decision, birdSpecies2, 30f);
        decisionSpa3.setBeginDate(new LocalDate(2019, 1, 1));
        decisionSpa3.setEndDate(new LocalDate(2019, 12, 31));

        final PermitDecisionSpeciesAmount decisionSpa4 =
                model().newPermitDecisionSpeciesAmount(decision, birdSpecies2, 40f);
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

            permitDecisionSpeciesAmountRepository.getOne(decisionSpa1.getId()).setSpecimenAmount(1f);
            permitDecisionSpeciesAmountRepository.getOne(decisionSpa2.getId()).setSpecimenAmount(2f);
            permitDecisionSpeciesAmountRepository.getOne(decisionSpa3.getId()).setSpecimenAmount(3f);
            permitDecisionSpeciesAmountRepository.getOne(decisionSpa4.getId()).setSpecimenAmount(4f);
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

            final HarvestPermitSpeciesAmount spa1 = firstPermitSpecies.get(birdSpecies.getOfficialCode());
            final HarvestPermitSpeciesAmount spa2 = secondPermitSpecies.get(birdSpecies.getOfficialCode());
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
        final HarvestPermitApplication application = createApplicationWithAmounts(5);

        final PermitDecision decision = model().newPermitDecision(application);
        final Map<Integer, PermitDecisionSpeciesAmount> decisionSpecies = IntStream.range(0, 5)
                .mapToObj(i -> {
                    final PermitDecisionSpeciesAmount spa =
                            model().newPermitDecisionSpeciesAmount(decision, birdSpecies, 1f);
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
                final String expectedPermitNumber = DocumentNumberUtil.createDocumentNumber(p.getPermitYear(),
                        decision.getValidityYears(),
                        decision.getDecisionNumber());
                assertDecisionEquals(decision, p, p.getPermitYear());
                assertEquals(expectedPermitNumber, p.getPermitNumber());
            });

            speciesAmountList.forEach(spa -> {
                assertSpeciesEquals(decisionSpecies.get(spa.getBeginDate().getYear()), spa);
            });

        });
    }

    @Test
    public void testBird_restrictedMultiYearApplication() {
        // Two year application
        final HarvestPermitApplication application = createApplicationWithAmounts(2);

        // Decision only for one year
        final PermitDecision decision = model().newPermitDecision(application);
        decision.setGrantStatus(GrantStatus.RESTRICTED);
        final PermitDecisionSpeciesAmount decisionSpaApproved = model().newPermitDecisionSpeciesAmount(decision,
                birdSpecies, 1f);
        LocalDate begin = DateUtil.today();
        final LocalDate end = begin.plusMonths(2);
        decisionSpaApproved.setBeginDate(begin);
        decisionSpaApproved.setEndDate(end);
        final PermitDecisionSpeciesAmount decisionSpaRejected = model().newPermitDecisionSpeciesAmount(decision,
                birdSpecies, 0f);
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

        final HarvestPermitApplication application =
                model().newHarvestPermitApplication(rhy, permitArea, HarvestPermitCategory.MOOSELIKE);
        final PermitDecision decision = model().newPermitDecision(application);
        decision.setGrantStatus(GrantStatus.REJECTED);
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
        final HarvestPermitApplication application = createApplicationWithAmounts(5);

        final PermitDecision decision = model().newPermitDecision(application);
        decision.setGrantStatus(GrantStatus.REJECTED);
        IntStream.range(0, 5).forEach(i -> {
            final PermitDecisionSpeciesAmount spa =
                    model().newPermitDecisionSpeciesAmount(decision, birdSpecies, 0);
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

            final String expectedPermitNumber = DocumentNumberUtil.createDocumentNumber(decision.getDecisionYear(),
                    decision.getValidityYears(),
                    decision.getDecisionNumber());

            assertEquals(expectedPermitNumber, permitList.get(0).getPermitNumber());

        });
    }

    // Updates for previously published decisions

    @Test
    public void test_updateRejectedApplication() {
        final HarvestPermitApplication application = createApplicationWithAmounts(5);

        final PermitDecision decision = model().newPermitDecision(application);
        decision.setGrantStatus(GrantStatus.REJECTED);
        IntStream.range(0, 5).forEach(i -> {
            final PermitDecisionSpeciesAmount spa =
                    model().newPermitDecisionSpeciesAmount(decision, birdSpecies, 0);
            spa.setBeginDate(spa.getBeginDate().plusYears(i));
            spa.setEndDate(spa.getEndDate().plusYears(i));
        });

        // Run twice to simulate publishing of new revision
        for (int i = 0; i<2; ++i){
            runInTransaction(() -> {
                persistInCurrentlyOpenTransaction();
                harvestPermitDecisionSynchronizer.synchronize(decision);
            });

            runInTransaction(() -> {
                final List<HarvestPermit> permitList = harvestPermitRepository.findAll();
                final List<HarvestPermitSpeciesAmount> speciesAmountList = harvestPermitSpeciesAmountRepository.findAll();

                assertThat(permitList, hasSize(1));
                assertThat(speciesAmountList, hasSize(0));

                final String expectedPermitNumber = DocumentNumberUtil.createDocumentNumber(decision.getDecisionYear(),
                        decision.getValidityYears(),
                        decision.getDecisionNumber());

                assertEquals(expectedPermitNumber, permitList.get(0).getPermitNumber());

            });
        }

    }

    @Test
    public void testBird_updateMultiYearApplication_removeYears() {
        final HarvestPermitApplication application = createApplicationWithAmounts(5);

        final PermitDecision decision = model().newPermitDecision(application);

        IntStream.range(0, 5).forEach(i-> {
            model().newHarvestPermit(rhy, DocumentNumberUtil.createDocumentNumber(decision.getDecisionYear()+i, 5, decision.getDecisionNumber()), PermitTypeCode.FOWL_AND_UNPROTECTED_BIRD, decision);
        });

        final Map<Integer, PermitDecisionSpeciesAmount> decisionSpecies = IntStream.range(0, 5)
                .mapToObj(i -> {
                    final float amount = i > 1 ? 0f : 1f;
                    final PermitDecisionSpeciesAmount spa =
                            model().newPermitDecisionSpeciesAmount(decision, birdSpecies, amount);
                    spa.setBeginDate(spa.getBeginDate().plusYears(i));
                    spa.setEndDate(spa.getEndDate().plusYears(i));
                    return spa;
                }).collect(Collect.indexingBy(PermitDecisionSpeciesAmount::getPermitYear));

        runInTransaction(() -> {
            persistInCurrentlyOpenTransaction();
            final List<HarvestPermit> permitList = harvestPermitRepository.findAll();
            assertThat(permitList, hasSize(5));

            harvestPermitDecisionSynchronizer.synchronize(decision);
        });

        runInTransaction(() -> {
            final List<HarvestPermit> permitList = harvestPermitRepository.findAll();
            final List<HarvestPermitSpeciesAmount> speciesAmountList = harvestPermitSpeciesAmountRepository.findAll();

            assertThat(permitList, hasSize(2));
            assertThat(speciesAmountList, hasSize(2));

            permitList.forEach(p -> {
                assertThat(p.getPermitYear(), is(lessThanOrEqualTo(decision.getDecisionYear()+1)));
                final String expectedPermitNumber = DocumentNumberUtil.createDocumentNumber(p.getPermitYear(),
                        decision.getValidityYears(),
                        decision.getDecisionNumber());
                assertDecisionEquals(decision, p, p.getPermitYear());
                assertEquals(expectedPermitNumber, p.getPermitNumber());
            });

            speciesAmountList.forEach(spa -> {
                assertSpeciesEquals(decisionSpecies.get(spa.getBeginDate().getYear()), spa);
            });

        });

    }

    // Cancel annual renewal

    @Test
    public void testAnnualRenewalCancellation() {
        final HarvestPermitApplication application = createApplicationWithAmounts(0);
        final PermitDecision decision = model().newPermitDecision(application);
        decision.setPermitTypeCode(PermitTypeCode.ANNUAL_UNPROTECTED_BIRD);
        final PermitDecisionSpeciesAmount decisionSpeciesAmount =
                model().newPermitDecisionSpeciesAmount(decision, birdSpecies, 1f);

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
            final HarvestPermitSpeciesAmount speciesAmount = speciesAmountList.get(0);

            assertThat(permit.getPermitYear(), is(lessThanOrEqualTo(decision.getDecisionYear() + 1)));
            final String expectedPermitNumber = DocumentNumberUtil.createDocumentNumber(permit.getPermitYear(),
                    decision.getValidityYears(),
                    decision.getDecisionNumber());
            assertDecisionEquals(decision, permit, permit.getPermitYear());
            assertEquals(expectedPermitNumber, permit.getPermitNumber());

            assertSpeciesEquals(decisionSpeciesAmount, speciesAmount);

        });

        // Cancel and synchronize
        runInTransaction(() -> {
            decision.setDecisionType(CANCEL_ANNUAL_RENEWAL);
            harvestPermitDecisionSynchronizer.synchronize(decision);
        });

        // Previous permit should still exist
        runInTransaction(() -> {
            final List<HarvestPermit> permitList = harvestPermitRepository.findAll();
            final List<HarvestPermitSpeciesAmount> speciesAmountList = harvestPermitSpeciesAmountRepository.findAll();

            assertThat(permitList, hasSize(1));
            assertThat(speciesAmountList, hasSize(1));

            final HarvestPermit permit = permitList.get(0);
            final HarvestPermitSpeciesAmount speciesAmount = speciesAmountList.get(0);

            final String expectedPermitNumber = DocumentNumberUtil.createDocumentNumber(permit.getPermitYear(),
                    decision.getValidityYears(),
                    decision.getDecisionNumber());
            assertEquals(expectedPermitNumber, permit.getPermitNumber());
            assertSpeciesEquals(decisionSpeciesAmount, speciesAmount);

        });
    }

    private HarvestPermitApplication createApplicationWithAmounts(final int validityYears) {
        final HarvestPermitApplication application =
                model().newHarvestPermitApplication(rhy, null, HarvestPermitCategory.BIRD);
        final HarvestPermitApplicationSpeciesAmount applicationSpeciesAmount =
                model().newHarvestPermitApplicationSpeciesAmount(application, birdSpecies, 1f, validityYears);
        application.getSpeciesAmounts().add(applicationSpeciesAmount);
        return application;
    }

    private static void assertSpeciesEquals(final PermitDecisionSpeciesAmount decisionSpeciesAmount,
                                            final HarvestPermitSpeciesAmount speciesAmount) {
        assertEquals(decisionSpeciesAmount.getSpecimenAmount(), speciesAmount.getSpecimenAmount(), 0.1);
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

        final String expectedPermitNumber = DocumentNumberUtil.createDocumentNumber(year, decision.getValidityYears(),
                decision.getDecisionNumber());

        assertEquals(expectedPermitNumber, harvestPermit.getPermitNumber());
        assertEquals(decision.getPermitHolder(), harvestPermit.getPermitHolder());
        assertEquals(decision.getDecisionName(), harvestPermit.getPermitType());
        assertEquals(decision.getPermitTypeCode(), harvestPermit.getPermitTypeCode());
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
