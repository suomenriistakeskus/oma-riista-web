package fi.riista.integration.habides.export.derogations;

import com.google.common.collect.ImmutableList;
import com.querydsl.sql.SQLQueryFactory;
import fi.riista.feature.account.user.SystemUserPrivilege;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.nestremoval.HarvestPermitNestLocationType;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.harvestpermit.usage.PermitUsage;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.bird.BirdPermitApplication;
import fi.riista.feature.permit.application.gamemanagement.GameManagementPermitApplication;
import fi.riista.feature.permit.application.mammal.MammalPermitApplication;
import fi.riista.feature.permit.application.nestremoval.NestRemovalPermitApplication;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.methods.ForbiddenMethodType;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmount;
import fi.riista.sql.SQRhy;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.F;
import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BEAR;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_GARGANEY;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_PINTAIL;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_POCHARD;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_ROE_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_SHOVELER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_TUFTED_DUCK;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WIGEON;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WOLVERINE;
import static java.util.Collections.emptyList;
import static org.junit.Assert.fail;

public class HabidesExportFeatureTest extends EmbeddedDatabaseTest {

    private static final int BIRD_1 = OFFICIAL_CODE_WIGEON;
    private static final int BIRD_2 = OFFICIAL_CODE_PINTAIL;
    private static final int BIRD_3 = OFFICIAL_CODE_GARGANEY;
    private static final int BIRD_4 = OFFICIAL_CODE_SHOVELER;
    private static final int BIRD_5 = OFFICIAL_CODE_POCHARD;

    private static final int MAMMAL_1 = OFFICIAL_CODE_ROE_DEER;
    private static final int MAMMAL_2 = OFFICIAL_CODE_BEAR;
    private static final int MAMMAL_3 = OFFICIAL_CODE_WOLVERINE;

    private static final List<ForbiddenMethodType> NO_FORBIDDEN_METHODS = emptyList();
    private static final List<ForbiddenMethodType> ALL_FORBIDDEN_METHODS = ImmutableList.of(
            ForbiddenMethodType.SNARES,
            ForbiddenMethodType.LIVE_ANIMAL_DECOY,
            ForbiddenMethodType.TAPE_RECORDERS,
            ForbiddenMethodType.ELECTRICAL_DEVICE,
            ForbiddenMethodType.ARTIFICIAL_LIGHT,
            ForbiddenMethodType.MIRRORS,
            ForbiddenMethodType.ILLUMINATION_DEVICE,
            ForbiddenMethodType.NIGHT_SHOOTING_DEVICE,
            ForbiddenMethodType.EXPLOSIVES,
            ForbiddenMethodType.NETS,
            ForbiddenMethodType.TRAPS,
            ForbiddenMethodType.POISON,
            ForbiddenMethodType.GASSING,
            ForbiddenMethodType.AUTOMATIC_WEAPON,
            ForbiddenMethodType.LIMES,
            ForbiddenMethodType.HOOKS,
            ForbiddenMethodType.CROSSBOWS,
            ForbiddenMethodType.SPEAR,
            ForbiddenMethodType.BLOWPIPE,
            ForbiddenMethodType.LEGHOLD_TRAP,
            ForbiddenMethodType.CONCEALED_WEAPON,
            ForbiddenMethodType.OTHER_SELECTIVE,
            ForbiddenMethodType.OTHER_NON_SELECTIVE
    );

    @Resource
    private HabidesExportFeature feature;

    @Resource
    private SQLQueryFactory sqlQueryFactory;

    @Before
    public void setUp() {

        final Riistanhoitoyhdistys rhy1 = newRhy("001", "Testikeskus Yksi", "T001");
        final Riistanhoitoyhdistys rhy2 = newRhy("002", "Testikeskus Kaksi", "T002");

        final GameSpecies bird = newSpecies(BIRD_1, "Avis periculum");
        final GameSpecies bird2 = newSpecies(BIRD_2, "Avis error");
        final HarvestPermit harvestPermit1 = newBirdHarvestPermit(rhy1, 2019, bird, 10, bird2, 13,
                "Area 51", true, true, true, ALL_FORBIDDEN_METHODS);
        addHarvest(harvestPermit1, 2019, bird, 1, true);
        addHarvest(harvestPermit1, 2019, bird, 1, true);
        addHarvest(harvestPermit1, 2019, bird, 1, false);

        final HarvestPermit harvestPermit2 = newBirdHarvestPermit(rhy2, 2019, bird, 5, null, 0,
                "Area 52", false, false, true, NO_FORBIDDEN_METHODS);
        addHarvest(harvestPermit2, 2019, bird, 1, true);

        newBirdHarvestPermit(rhy1, 2019, bird, 0, bird2, 0, "ERROR", false,
                false, false, NO_FORBIDDEN_METHODS);

        final GameSpecies bird3 = newSpecies(BIRD_4, "Avis 4");
        final GameSpecies bird4 = newSpecies(BIRD_5, "Avis 5");
        newNestRemovalHarvestPermit(rhy1, 2020, bird3, 2, 10, null,
                bird4, 3, 5, 4, "Area 54",
                true, true, ALL_FORBIDDEN_METHODS);
        newNestRemovalHarvestPermit(rhy2, 2020, bird3, null, 20, null,
                null, null, null, null, "Area 55",
                false, true, NO_FORBIDDEN_METHODS);
        final HarvestPermit harvestPermit3 = newBirdHarvestPermit(rhy2, 2020, bird4, 10, null, 0,
                "Area 56", false, false, true, NO_FORBIDDEN_METHODS);
        addHarvest(harvestPermit3, 2020, bird4, 9, true);

        final GameSpecies mammal1 = newSpecies(MAMMAL_1, "Mammalia normalis");
        final GameSpecies mammal2 = newSpecies(MAMMAL_2, "Mammalia peculiari");
        final HarvestPermit mammalHarvestPermit1 = newMammalHarvestPermit(rhy1, 2020, mammal1, 5,
                mammal2, 6, "Mammal Area", true, true, true, ALL_FORBIDDEN_METHODS);
        addHarvest(mammalHarvestPermit1, 2020, mammal1, 1, true);
        addHarvest(mammalHarvestPermit1, 2020, mammal1, 1, true);
        addHarvest(mammalHarvestPermit1, 2020, mammal1, 1, false);
        addHarvest(mammalHarvestPermit1, 2020, mammal2, 2, true);
        addHarvest(mammalHarvestPermit1, 2020, mammal2, 1, true);

        newNestRemovalHarvestPermit(rhy1, 2020, mammal1, null, null, 4,
                mammal2, null, null, 2, "Mammal Construction Area",
                false, true, NO_FORBIDDEN_METHODS);

        final GameSpecies mammal3 = newSpecies(MAMMAL_3, "Mammalia inanis");
        newMammalHarvestPermit(rhy1, 2020, mammal3, 0,
                null, 0, "Mammal area",
                false, false, true, NO_FORBIDDEN_METHODS);

        final HarvestPermit harvestPermit4 = newBirdHarvestPermit(rhy2, 2019, bird, 2, null, 0,
                "Area 53", false, false, true, Collections.singletonList(ForbiddenMethodType.EXPLOSIVES));
        addHarvest(harvestPermit4, 2019, bird, 2, true);

        final HarvestPermit harvestPermit5 = newBirdHarvestPermit(rhy2, 2019, bird, 3, null, 0,
                "Area 54", false, true, true, NO_FORBIDDEN_METHODS);
        addHarvest(harvestPermit5, 2019, bird, 3, true);

        newGameManagementPermit(rhy1, 2020, bird3, 2, 10, null,
                null, null, "Bird Capture Area", true, true,
                ALL_FORBIDDEN_METHODS);
        newGameManagementPermit(rhy2, 2020, mammal1, 10, null, null,
                null, null, "Mammal Capture Area",
                true, true, ALL_FORBIDDEN_METHODS);
    }

    @After
    public void tearDown() {
        clearRhyTable();
    }

    @Test
    public void adminExportsBirdsReportSuccessfully() throws Exception {
        final String expected = expectedReport(BIRD_1);

        onSavedAndAuthenticated(
                createNewAdmin(),
                () -> Assert.assertEquals(expected, exportReport(BIRD_1, 2019)));
    }

    @Test
    public void moderatorWithHabidesPrivilegeExportsBirdsReportSuccessfully() throws Exception {
        final String expected = expectedReport(BIRD_2);

        onSavedAndAuthenticated(
                createNewModerator(SystemUserPrivilege.EXPORT_HABIDES_REPORTS),
                () -> Assert.assertEquals(expected, exportReport(BIRD_2, 2019)));
    }

    @Test(expected = AccessDeniedException.class)
    public void moderatorWithoutHabidesPrivilegeExportBirdsReportFails() {
        onSavedAndAuthenticated(createNewModerator(), () -> {
            try {
                feature.exportReportAsXml(
                        2019,
                        BIRD_1);
            } catch (DerogationNotFoundException | DraftDecisionsExistException e) {
                fail();
            }
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void userExportBirdsReportFails() {
        onSavedAndAuthenticated(createNewUser(), () -> {
            try {
                feature.exportReportAsXml(
                        2019,
                        BIRD_1);
            } catch (DerogationNotFoundException | DraftDecisionsExistException e) {
                fail();
            }
        });
    }

    @Test
    public void numberOfDatabaseQueriesIsNonLinear() {
            final Riistanhoitoyhdistys rhy = newRhy("003", "Testikeskus Kolme", "T003");
            final GameSpecies bird = newSpecies(BIRD_3, "Avis velocitas");

            for (int i=0; i<100; i++) {
                final HarvestPermit harvestPermit = newBirdHarvestPermit(rhy, 2019, bird, 1, null, 0,
                        "Area 53", false, false, true, ALL_FORBIDDEN_METHODS);
                addHarvest(harvestPermit, 2019, bird, 1, true);
                addHarvest(harvestPermit, 2019, bird, 2, true);
                addHarvest(harvestPermit, 2019, bird, 3, true);
            }

            assertMaxQueryCount(14, () -> {
            onSavedAndAuthenticated(createNewAdmin(), () -> exportReport(BIRD_3, 2019));
        });
    }

    @Test
    public void exportBirdReportSuccessfully_nestRemovalAndGameManagement() throws Exception {
        final String expected = expectedReport(BIRD_4);

        onSavedAndAuthenticated(
                createNewModerator(SystemUserPrivilege.EXPORT_HABIDES_REPORTS),
                () -> Assert.assertEquals(expected,
                        exportReport(
                                BIRD_4,
                                2020)));
    }

    @Test
    public void exportBirdReportSuccessfully_HarvestsNestsEggsAndConstructions() throws Exception {
        final String expected = expectedReport(BIRD_5);

        onSavedAndAuthenticated(
                createNewModerator(SystemUserPrivilege.EXPORT_HABIDES_REPORTS),
                () -> Assert.assertEquals(expected,
                        exportReport(
                                BIRD_5,
                                2020)));
    }

    @Test
    public void exportHabitatsReportSuccessfully() throws Exception {
        final String expected = expectedReport(MAMMAL_1);

        onSavedAndAuthenticated(
                createNewModerator(SystemUserPrivilege.EXPORT_HABIDES_REPORTS),
                () -> Assert.assertEquals(expected,
                        exportReport(
                                MAMMAL_1,
                                2020)));
    }

    @Test
    public void exportHabitatsReportSuccessfully_AnnexIVSpecies() throws Exception {
        final String expected = expectedReport(MAMMAL_2);

        onSavedAndAuthenticated(
                createNewModerator(SystemUserPrivilege.EXPORT_HABIDES_REPORTS),
                () -> Assert.assertEquals(expected,
                        exportReport(
                                MAMMAL_2,
                                2020)));
    }

    @Test
    public void exportHabitatsReportSuccesfully_noPermitSpecies() {
        onSavedAndAuthenticated(
                createNewModerator(SystemUserPrivilege.EXPORT_HABIDES_REPORTS),
                () -> {
                    try {
                        feature.exportReportAsXml(2020, MAMMAL_3);
                    } catch (Exception e) {
                        Assert.assertTrue(e instanceof DerogationNotFoundException);
                    }
                });
    }

    @Test
    public void testExportReportAsXml_permitsWithDraftDecisions() {
        final HarvestPermit permit =
                newBirdHarvestPermit(model().newRiistanhoitoyhdistys(), 2020, newSpecies(OFFICIAL_CODE_TUFTED_DUCK, "new bird"), 5,
                        null, 0, "Area 55", false, false, false, NO_FORBIDDEN_METHODS);

        onSavedAndAuthenticated(createNewModerator(SystemUserPrivilege.EXPORT_HABIDES_REPORTS), () -> {
            try {
                feature.exportReportAsXml(2020, OFFICIAL_CODE_TUFTED_DUCK);
                fail();
            } catch (Exception e) {
                Assert.assertTrue(e instanceof DraftDecisionsExistException);

                final DraftDecisionsExistException draftDecisionsExistException = (DraftDecisionsExistException)e;
                Assert.assertEquals(1, draftDecisionsExistException.getDraftDecisions().size());
                Assert.assertEquals(permit.getPermitNumber(), draftDecisionsExistException.getDraftDecisions().get(0));
            }
        });
    }

    /*

        ===============================

        H E L P E R   F U N C T I O N S

        ===============================

     */

    private String exportReport(final int speciesCode, final int year) {
        try {
            return feature.exportReportAsXml(
                    year,
                    speciesCode);
        } catch (Exception e) {
            fail();
        }

        return "";
    }

    /*

        Generate test data

     */

    private GameSpecies newSpecies(final int code, final String name) {
        final GameSpecies species = model().newGameSpecies(code);
        species.setScientificName(name);
        return species;
    }

    private Riistanhoitoyhdistys newRhy(final String id, final String rkaName, final String nuts2Id) {

        final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();
        rka.setNameFinnish(rkaName);

        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys(rka);
        rhy.setOfficialCode(id);

        final SQRhy RHY = SQRhy.rhy;
        sqlQueryFactory.insert(RHY)
                .set(RHY.id, id)
                .set(RHY.nuts2Id, nuts2Id)
                .execute();

        return rhy;
    }

    private void clearRhyTable() {
        final SQRhy RHY = SQRhy.rhy;
        sqlQueryFactory.delete(RHY)
                .execute();
    }

    private HarvestPermit newBirdHarvestPermit(
            final Riistanhoitoyhdistys rhy,
            final int year,
            final GameSpecies bird,
            final int amount,
            final GameSpecies bird2,
            final int amount2,
            final String areaName,
            final boolean hasTwoPeriods,
            final boolean usedMotorVehicles,
            final boolean isLocked,
            final List<ForbiddenMethodType> forbiddenMethodTypes) {

        final HarvestPermitArea harvestPermitArea = model().newHarvestPermitArea();
        final HarvestPermitApplication harvestPermitApplication =
                model().newHarvestPermitApplication(rhy, harvestPermitArea, bird, HarvestPermitCategory.BIRD);
        final BirdPermitApplication birdPermitApplication = model().newBirdPermitApplication(harvestPermitApplication);
        birdPermitApplication.getProtectedArea().setName(areaName);

        final PermitDecision permitDecision = model().newPermitDecision(rhy, bird);
        permitDecision.setApplication(harvestPermitApplication);
        permitDecision.setLegalSection32(usedMotorVehicles);
        if (!isLocked) {
            permitDecision.setStatusDraft();
        }

        forbiddenMethodTypes.forEach(forbiddenMethodType -> model().newPermitDecisionForbiddenMethod(permitDecision, bird, forbiddenMethodType));

        final PermitDecisionSpeciesAmount decisionSpeciesAmount = model().newPermitDecisionSpeciesAmount(permitDecision, bird, amount);
        final HarvestPermit harvestPermit = model().newHarvestPermit(rhy, permitNumber(year, 1), PermitTypeCode.FOWL_AND_UNPROTECTED_BIRD);
        harvestPermit.setPermitDecision(permitDecision);

        final HarvestPermitSpeciesAmount harvestPermitSpeciesAmount = model().newHarvestPermitSpeciesAmount(harvestPermit, decisionSpeciesAmount);
        harvestPermitSpeciesAmount.setBeginDate(new LocalDate(year, 1, 1));
        harvestPermitSpeciesAmount.setEndDate(new LocalDate(year, 6, 30));

        if (hasTwoPeriods) {
            harvestPermitSpeciesAmount.setBeginDate2(new LocalDate(year, 9, 1));
            harvestPermitSpeciesAmount.setEndDate2(new LocalDate(year, 12, 31));
        }

        if (bird2 != null) {
            final PermitDecisionSpeciesAmount decisionSpeciesAmount2 = model().newPermitDecisionSpeciesAmount(permitDecision, bird2, amount2);
            final HarvestPermitSpeciesAmount harvestPermitSpeciesAmount2 = model().newHarvestPermitSpeciesAmount(harvestPermit, decisionSpeciesAmount2);
            harvestPermitSpeciesAmount2.setBeginDate(new LocalDate(year, 1, 1));
            harvestPermitSpeciesAmount2.setEndDate(new LocalDate(year, 6, 30));
        }

        return harvestPermit;
    }

    private HarvestPermit newMammalHarvestPermit(
            final Riistanhoitoyhdistys rhy,
            final int year,
            final GameSpecies mammal,
            final int amount,
            final GameSpecies mammal2,
            final int amount2,
            final String areaName,
            final boolean hasTwoPeriods,
            final boolean usedMotorVehicles,
            final boolean isLocked,
            final List<ForbiddenMethodType> forbiddenMethodTypes) {

        final HarvestPermitArea harvestPermitArea = model().newHarvestPermitArea();
        final HarvestPermitApplication harvestPermitApplication =
                model().newHarvestPermitApplication(rhy, harvestPermitArea, mammal, HarvestPermitCategory.MAMMAL);
        final MammalPermitApplication mammalPermitApplication = model().newMammalPermitApplication(harvestPermitApplication);
        mammalPermitApplication.setAreaDescription(areaName);

        final PermitDecision permitDecision = model().newPermitDecision(rhy, mammal);
        permitDecision.setApplication(harvestPermitApplication);
        permitDecision.setLegalSection32(usedMotorVehicles);
        if (!isLocked) {
            permitDecision.setStatusDraft();
        }

        forbiddenMethodTypes.forEach(forbiddenMethodType -> model().newPermitDecisionForbiddenMethod(permitDecision, mammal, forbiddenMethodType));

        final PermitDecisionSpeciesAmount decisionSpeciesAmount = model().newPermitDecisionSpeciesAmount(permitDecision, mammal, amount);
        final HarvestPermit harvestPermit = model().newHarvestPermit(rhy, permitNumber(year, 1), PermitTypeCode.MAMMAL_DAMAGE_BASED);
        harvestPermit.setPermitDecision(permitDecision);

        final HarvestPermitSpeciesAmount harvestPermitSpeciesAmount = model().newHarvestPermitSpeciesAmount(harvestPermit, decisionSpeciesAmount);
        harvestPermitSpeciesAmount.setBeginDate(new LocalDate(year, 8, 1));
        harvestPermitSpeciesAmount.setEndDate(new LocalDate(year, 8, 31));

        if (hasTwoPeriods) {
            harvestPermitSpeciesAmount.setBeginDate2(new LocalDate(year, 10, 1));
            harvestPermitSpeciesAmount.setEndDate2(new LocalDate(year, 10, 31));
        }

        if (mammal2 != null) {
            final PermitDecisionSpeciesAmount decisionSpeciesAmount2 = model().newPermitDecisionSpeciesAmount(permitDecision, mammal2, amount2);
            final HarvestPermitSpeciesAmount harvestPermitSpeciesAmount2 = model().newHarvestPermitSpeciesAmount(harvestPermit, decisionSpeciesAmount2);
            harvestPermitSpeciesAmount2.setBeginDate(new LocalDate(year, 9, 1));
            harvestPermitSpeciesAmount2.setEndDate(new LocalDate(year, 9, 30));
        }

        return harvestPermit;
    }

    private HarvestPermit newNestRemovalHarvestPermit(
            final Riistanhoitoyhdistys rhy,
            final int year,
            final GameSpecies species,
            final Integer nestAmount,
            final Integer eggAmount,
            final Integer constructionAmount,
            final GameSpecies species2,
            final Integer nestAmount2,
            final Integer eggAmount2,
            final Integer constructionAmount2,
            final String areaName,
            final boolean usedMotorVehicles,
            final boolean isLocked,
            final List<ForbiddenMethodType> forbiddenMethodTypes) {

        final HarvestPermitArea harvestPermitArea = model().newHarvestPermitArea();
        final HarvestPermitApplication harvestPermitApplication =
                model().newHarvestPermitApplication(rhy, harvestPermitArea, species, HarvestPermitCategory.NEST_REMOVAL);
        final NestRemovalPermitApplication nestRemovalPermitApplication =
                model().newNestRemovalPermitApplication(harvestPermitApplication);
        nestRemovalPermitApplication.setAreaDescription(areaName);

        final PermitDecision permitDecision = model().newPermitDecision(rhy, species);
        permitDecision.setApplication(harvestPermitApplication);
        permitDecision.setLegalSection32(usedMotorVehicles);
        if (!isLocked) {
            permitDecision.setStatusDraft();
        }

        forbiddenMethodTypes.forEach(forbiddenMethodType -> model().newPermitDecisionForbiddenMethod(permitDecision, species, forbiddenMethodType));

        final PermitDecisionSpeciesAmount decisionSpeciesAmount =
                model().newPermitDecisionSpeciesAmount(permitDecision, species, null, nestAmount, eggAmount, constructionAmount);
        final HarvestPermit harvestPermit = model().newHarvestPermit(rhy, permitNumber(year, 1), PermitTypeCode.NEST_REMOVAL_BASED);
        harvestPermit.setPermitDecision(permitDecision);

        final HarvestPermitSpeciesAmount harvestPermitSpeciesAmount = model().newHarvestPermitSpeciesAmount(harvestPermit, decisionSpeciesAmount);
        harvestPermitSpeciesAmount.setBeginDate(new LocalDate(year, 8, 1));
        harvestPermitSpeciesAmount.setEndDate(new LocalDate(year, 8, 30));

        final Integer nestUsage = Optional.ofNullable(nestAmount)
                .map(nests -> nests > 0 ? nests - 1 : nests)
                .orElse(null);
        final Integer eggUsage = Optional.ofNullable(eggAmount)
                .map(eggs -> eggs > 0 ? eggs - 1 : eggs)
                .orElse(null);
        final Integer constructionUsage = Optional.ofNullable(constructionAmount)
                .map(constructions -> constructions > 0 ? constructions - 1 : constructions)
                .orElse(null);
        model().newHarvestPermitNestRemovalUsage(harvestPermitSpeciesAmount, nestUsage, eggUsage, constructionUsage, geoLocation(), HarvestPermitNestLocationType.NEST);

        if (species2 != null) {
            final PermitDecisionSpeciesAmount decisionSpeciesAmount2 =
                    model().newPermitDecisionSpeciesAmount(permitDecision, species2, null, nestAmount2, eggAmount2, constructionAmount2);
            final HarvestPermitSpeciesAmount harvestPermitSpeciesAmount2 = model().newHarvestPermitSpeciesAmount(harvestPermit, decisionSpeciesAmount2);
            harvestPermitSpeciesAmount2.setBeginDate(new LocalDate(year, 9, 1));
            harvestPermitSpeciesAmount2.setEndDate(new LocalDate(year, 9, 30));

            final Integer nestUsage2 = Optional.ofNullable(nestAmount2)
                    .map(nests -> nests > 0 ? nests - 1 : nests)
                    .orElse(null);
            final Integer eggUsage2 = Optional.ofNullable(eggAmount2)
                    .map(eggs -> eggs > 0 ? eggs - 1 : eggs)
                    .orElse(null);
            final Integer constructionUsage2 = Optional.ofNullable(constructionAmount2)
                    .map(constructions -> constructions > 0 ? constructions - 1 : constructions)
                    .orElse(null);
            model().newHarvestPermitNestRemovalUsage(harvestPermitSpeciesAmount2, nestUsage2, eggUsage2, constructionUsage2, geoLocation(), HarvestPermitNestLocationType.NEST);

        }

        return harvestPermit;
    }

    private HarvestPermit newGameManagementPermit(
            final Riistanhoitoyhdistys rhy,
            final int year,
            final GameSpecies species,
            final Integer specimenAmount,
            final Integer eggAmount,
            final GameSpecies species2,
            final Integer specimenAmount2,
            final Integer eggAmount2,
            final String areaName,
            final boolean usedMotorVehicles,
            final boolean isLocked,
            final List<ForbiddenMethodType> forbiddenMethodTypes) {

        final HarvestPermitArea harvestPermitArea = model().newHarvestPermitArea();
        final HarvestPermitApplication harvestPermitApplication =
                model().newHarvestPermitApplication(rhy, harvestPermitArea, species, HarvestPermitCategory.GAME_MANAGEMENT);
        final GameManagementPermitApplication gameManagementPermitApplication =
                model().newGameManagementPermitApplication(harvestPermitApplication);
        gameManagementPermitApplication.setAreaDescription(areaName);

        final PermitDecision permitDecision = model().newPermitDecision(rhy, species);
        permitDecision.setApplication(harvestPermitApplication);
        permitDecision.setLegalSection32(usedMotorVehicles);
        if (!isLocked) {
            permitDecision.setStatusDraft();
        }

        forbiddenMethodTypes.forEach(forbiddenMethodType -> model().newPermitDecisionForbiddenMethod(permitDecision, species, forbiddenMethodType));

        final PermitDecisionSpeciesAmount decisionSpeciesAmount =
                model().newPermitDecisionSpeciesAmount(permitDecision, species, specimenAmount.floatValue(), null, eggAmount, null);
        final HarvestPermit harvestPermit = model().newHarvestPermit(rhy, permitNumber(year, 1), PermitTypeCode.GAME_MANAGEMENT);
        harvestPermit.setPermitDecision(permitDecision);

        final HarvestPermitSpeciesAmount harvestPermitSpeciesAmount = model().newHarvestPermitSpeciesAmount(harvestPermit, decisionSpeciesAmount);
        harvestPermitSpeciesAmount.setBeginDate(new LocalDate(year, 8, 1));
        harvestPermitSpeciesAmount.setEndDate(new LocalDate(year, 8, 30));

        final Integer specimenUsage = Optional.ofNullable(specimenAmount)
                .map(specimen -> specimen > 0 ? specimen - 1 : specimen)
                .orElse(null);
        final Integer eggUsage = Optional.ofNullable(eggAmount)
                .map(eggs -> eggs > 0 ? eggs - 1 : eggs)
                .orElse(null);
        final PermitUsage usage = model().newPermitUsage(harvestPermitSpeciesAmount, specimenUsage, eggUsage);
        model().newPermitUsageLocation(usage);

        if (species2 != null) {
            final PermitDecisionSpeciesAmount decisionSpeciesAmount2 =
                    model().newPermitDecisionSpeciesAmount(permitDecision, species2, F.mapNullable(specimenAmount2, Integer::floatValue), null, eggAmount2, null);
            final HarvestPermitSpeciesAmount harvestPermitSpeciesAmount2 = model().newHarvestPermitSpeciesAmount(harvestPermit, decisionSpeciesAmount2);
            harvestPermitSpeciesAmount2.setBeginDate(new LocalDate(year, 9, 1));
            harvestPermitSpeciesAmount2.setEndDate(new LocalDate(year, 9, 30));

            final Integer specimenUsage2 = Optional.ofNullable(specimenAmount2)
                    .map(specimen -> specimen > 0 ? specimen - 1 : specimen)
                    .orElse(null);
            final Integer eggUsage2 = Optional.ofNullable(eggAmount2)
                    .map(eggs -> eggs > 0 ? eggs - 1 : eggs)
                    .orElse(null);
            final PermitUsage usage2 = model().newPermitUsage(harvestPermitSpeciesAmount, specimenUsage2, eggUsage2);
            model().newPermitUsageLocation(usage2);
        }

        return harvestPermit;
    }

    private void addHarvest(final HarvestPermit harvestPermit, final int year, final GameSpecies species, final int amount, final boolean isAccepted) {
        final Person reporter = model().newPerson();
        final Harvest harvest = model().newHarvest(harvestPermit, species);
        if (isAccepted) {
            harvest.setHarvestReportState(HarvestReportState.APPROVED);
        } else {
            harvest.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);
        }
        harvest.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.ACCEPTED);
        harvest.setHarvestReportDate(new DateTime(year, 6, 1, 0, 0));
        harvest.setHarvestReportAuthor(reporter);
        harvest.setAmount(amount);
    }

    /*

        Results verification

     */

    private static String expectedReport(final int speciesCode) throws Exception{
        return FileUtils.readFileToString(
                new File("src/test/java/fi/riista/integration/habides/export/derogations/expected_" + speciesCode + "_report.xml"),
                "UTF-8");
    }

}
