package fi.riista.integration.habides.export.derogations;

import com.google.common.collect.ImmutableList;
import com.querydsl.sql.SQLQueryFactory;
import fi.riista.feature.account.user.SystemUserPrivilege;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.bird.BirdPermitApplication;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.methods.ForbiddenMethodType;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmount;
import fi.riista.sql.SQRhy;
import fi.riista.test.EmbeddedDatabaseTest;
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

public class HabidesExportFeatureTest extends EmbeddedDatabaseTest {

    private static final int BIRD_1 = 10000;
    private static final int BIRD_2 = 10001;
    private static final int BIRD_3 = 10002;

    private static final List<ForbiddenMethodType> NO_FORBIDDEN_METHODS = Collections.emptyList();
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

        final GameSpecies bird = newBird(BIRD_1, "Avis periculum");
        final GameSpecies bird2 = newBird(BIRD_2, "Avis error");
        final HarvestPermit harvestPermit1 = newHarvestPermit(rhy1, bird, 10, bird2, 13,
                "Area 51", true, true, true, ALL_FORBIDDEN_METHODS);
        addHarvest(harvestPermit1, bird, 1, true);
        addHarvest(harvestPermit1, bird, 1, true);
        addHarvest(harvestPermit1, bird, 1, false);

        final HarvestPermit harvestPermit2 = newHarvestPermit(rhy2, bird, 5, null, 0,
                "Area 52", false, false, true, NO_FORBIDDEN_METHODS);
        addHarvest(harvestPermit2, bird, 1, true);

        newHarvestPermit(rhy1, bird, 13, bird2, 13, "ERROR", false,
                false, false, NO_FORBIDDEN_METHODS);
    }

    @After
    public void tearDown() {
        clearRhyTable();
    }

    @Test
    public void adminExportsBirdsReportSuccessfully() throws Exception {
        final String expected = expectedBirdReport(BIRD_1);

        onSavedAndAuthenticated(
                createNewAdmin(),
                () -> Assert.assertEquals(expected, exportBirdsReport(BIRD_1)));
    }

    @Test
    public void moderatorWithHabidesPrivilegeExportsBirdsReportSuccessfully() throws Exception {
        final String expected = expectedBirdReport(BIRD_2);

        onSavedAndAuthenticated(
                createNewModerator(SystemUserPrivilege.EXPORT_HABIDES_REPORTS),
                () -> Assert.assertEquals(expected, exportBirdsReport(BIRD_2)));
    }

    @Test(expected = AccessDeniedException.class)
    public void moderatorWithoutHabidesPrivilegeExportBirdsReportFails() {
        onSavedAndAuthenticated(createNewModerator(), () -> exportBirdsReport(BIRD_1));
    }

    @Test(expected = AccessDeniedException.class)
    public void userExportBirdsReportFails() {
        onSavedAndAuthenticated(createNewUser(), () -> exportBirdsReport(BIRD_1));
    }

    @Test
    public void numberOfDatabaseQueriesIsNonLinear() {
            final Riistanhoitoyhdistys rhy = newRhy("003", "Testikeskus Kolme", "T003");
            final GameSpecies bird = newBird(BIRD_3, "Avis velocitas");

            for (int i=0; i<100; i++) {
                final HarvestPermit harvestPermit = newHarvestPermit(rhy, bird, 1, null, 0,
                        "Area 53", false, false, true, ALL_FORBIDDEN_METHODS);
                addHarvest(harvestPermit, bird, 1, true);
                addHarvest(harvestPermit, bird, 2, true);
                addHarvest(harvestPermit, bird, 3, true);
            }

            assertMaxQueryCount(8, () -> {
            onSavedAndAuthenticated(createNewAdmin(), () -> exportBirdsReport(BIRD_3));
        });
    }


    /*

        ===============================

        H E L P E R   F U N C T I O N S

        ===============================

     */

    private String exportBirdsReport(final int birdCode) {
        return feature.exportReportForBirdsAsXml(
                new LocalDate(2019, 1, 1),
                new LocalDate(2019, 12, 31),
                birdCode);
    }

    /*

        Generate test data

     */

    private GameSpecies newBird(final int code, final String name) {
        final GameSpecies bird = model().newGameSpecies(code);
        bird.setScientificName(name);
        return bird;
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

    private HarvestPermit newHarvestPermit(
            final Riistanhoitoyhdistys rhy,
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
        final HarvestPermitApplication harvestPermitApplication = model().newHarvestPermitApplication(rhy, harvestPermitArea, bird);
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
        final HarvestPermit harvestPermit = model().newHarvestPermit(rhy);
        harvestPermit.setPermitDecision(permitDecision);

        final HarvestPermitSpeciesAmount harvestPermitSpeciesAmount = model().newHarvestPermitSpeciesAmount(harvestPermit, decisionSpeciesAmount);
        harvestPermitSpeciesAmount.setBeginDate(new LocalDate(2019, 1, 1));
        harvestPermitSpeciesAmount.setEndDate(new LocalDate(2019, 6, 30));
        if (hasTwoPeriods) {
            harvestPermitSpeciesAmount.setBeginDate2(new LocalDate(2019, 9, 1));
            harvestPermitSpeciesAmount.setEndDate2(new LocalDate(2019, 12, 31));
        }

        if (bird2 != null) {
            final PermitDecisionSpeciesAmount decisionSpeciesAmount2 = model().newPermitDecisionSpeciesAmount(permitDecision, bird2, amount2);
            final HarvestPermitSpeciesAmount harvestPermitSpeciesAmount2 = model().newHarvestPermitSpeciesAmount(harvestPermit, decisionSpeciesAmount2);
            harvestPermitSpeciesAmount2.setBeginDate(new LocalDate(2019, 1, 1));
            harvestPermitSpeciesAmount2.setEndDate(new LocalDate(2019, 6, 30));
        }

        return harvestPermit;
    }

    private void addHarvest(final HarvestPermit harvestPermit, final GameSpecies bird, final int amount, final boolean isAccepted) {
        final Person reporter = model().newPerson();
        final Harvest harvest = model().newHarvest(harvestPermit, bird);
        if (isAccepted) {
            harvest.setHarvestReportState(HarvestReportState.APPROVED);
        } else {
            harvest.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);
        }
        harvest.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.ACCEPTED);
        harvest.setHarvestReportDate(new DateTime(2019, 6, 1, 0, 0));
        harvest.setHarvestReportAuthor(reporter);
        harvest.setAmount(amount);
    }

    /*

        Results verification

     */

    private static String expectedBirdReport(final int birdCode) throws Exception{
        return FileUtils.readFileToString(
                new File("src/test/java/fi/riista/integration/habides/export/derogations/expected_" + birdCode + "_report.xml"),
                "UTF-8");
    }

}
