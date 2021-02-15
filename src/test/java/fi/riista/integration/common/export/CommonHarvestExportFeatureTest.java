package fi.riista.integration.common.export;

import fi.riista.config.Constants;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.SystemUserPrivilege;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.specimen.GameAntlersType;
import fi.riista.feature.gamediary.harvest.specimen.GameFitnessClass;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.integration.common.export.harvests.CHAR_Harvest;
import fi.riista.integration.common.export.harvests.CHAR_Harvests;
import fi.riista.integration.common.export.harvests.CHAR_Specimen;
import fi.riista.test.Asserts;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_OTTER;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.ADULT_MALE;
import static fi.riista.integration.common.export.RvrConstants.RVR_SPECIES;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CommonHarvestExportFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private CommonHarvestExportFeature feature;

    private SystemUser apiLukeUser;
    private SystemUser apiRvrUser;
    private GameSpecies mooseSpecies;
    private GameSpecies bearSpecies;
    private RiistakeskuksenAlue rka;
    private Riistanhoitoyhdistys rhy;
    private Person harvestPerson;

    @Before
    public void setUp() {
        apiLukeUser = createNewApiUser(SystemUserPrivilege.EXPORT_LUKE_COMMON);
        apiRvrUser = createNewApiUser(SystemUserPrivilege.EXPORT_RVR_COMMON);
        mooseSpecies = model().newGameSpeciesMoose();
        bearSpecies = model().newGameSpecies(47348);
        rka = model().newRiistakeskuksenAlue();
        rhy = model().newRiistanhoitoyhdistys(rka);
        harvestPerson = model().newPerson();

    }

    @Test(expected = AccessDeniedException.class)
    public void testAccessDeniedLuke() {
        onSavedAndAuthenticated(createNewAdmin(), () -> feature.exportAllHarvests(2018, 1));
    }

    @Test(expected = AccessDeniedException.class)
    public void testAccessDeniedRVR() {
        onSavedAndAuthenticated(createNewAdmin(), () -> feature.exportRVRHarvests(2018, 1));
    }

    @Test(expected = AccessDeniedException.class)
    public void testLukeCannotAccessRVR() {
        authenticate(apiLukeUser);
        feature.exportRVRHarvests(2018, 1);
    }

    @Test(expected = AccessDeniedException.class)
    public void testRVRCannotAccessLuke() {
        authenticate(apiRvrUser);
        feature.exportAllHarvests(2018, 1);
    }

    @Test
    public void testAccessGrantedLuke() {
        persistInNewTransaction();

        authenticate(apiLukeUser);
        Asserts.assertEmpty(feature.exportAllHarvests(2018, 1).getHarvest());
    }

    @Test
    public void testAccessGrantedRVR() {
        persistInNewTransaction();

        authenticate(apiRvrUser);
        Asserts.assertEmpty(feature.exportRVRHarvests(2018, 1).getHarvest());

    }

    @Test
    public void testFindsWithCorrectInterval() {
        withRhy(rhy -> {
            createHarvest(toDateTime(2017, 12, 31));
            createHarvest(toDateTime(2018, 1, 1));
            createHarvest(toDateTime(2018, 1, 31));
            createHarvest(toDateTime(2018, 2, 1));

            onSavedAndAuthenticated(apiLukeUser, () -> {
                final CHAR_Harvests result = feature.exportAllHarvests(2018, 1);
                Assert.assertEquals(2, result.getHarvest().size());
                result.getHarvest()
                        .forEach(harvest -> Assert.assertEquals(1, harvest.getPointOfTime().monthOfYear().get()));
            });
        });
    }


    @Test
    public void testFindsPermitNumber() {
        final DateTime dateTime = toDateTime(2018, 02, 11);
        final HarvestPermit permit = model().newHarvestPermit();
        final Harvest harvest = createHarvest(dateTime);

        harvest.setHarvestPermit(permit);
        harvest.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.PROPOSED);

        onSavedAndAuthenticated(apiLukeUser, () -> {
            final CHAR_Harvests result = feature.exportAllHarvests(dateTime.getYear(), dateTime.getMonthOfYear());
            Assert.assertEquals(1, result.getHarvest().size());
            final CHAR_Harvest h = result.getHarvest().iterator().next();
            Assert.assertEquals(permit.getPermitNumber(), h.getPermitNumber());
        });

    }

    @Test
    public void testFindsPermitNumberForMooselikeHarvest() {
        final DateTime dateTime = toDateTime(2018, 02, 11);

        final GroupHuntingDay huntingDay = createPermitAndHuntingDay(dateTime);
        final HarvestPermit permit = huntingDay.getGroup().getHarvestPermit();

        final Harvest harvest = model().newHarvest(mooseSpecies, harvestPerson, huntingDay);
        harvest.setRhy(rhy);

        onSavedAndAuthenticated(apiLukeUser, () -> {
            final CHAR_Harvests result = feature.exportAllHarvests(dateTime.getYear(), dateTime.getMonthOfYear());
            Assert.assertEquals(1, result.getHarvest().size());
            final CHAR_Harvest h = result.getHarvest().iterator().next();
            Assert.assertEquals(permit.getPermitNumber(), h.getPermitNumber());
        });
    }

    @Test
    public void testOfficialHarvest_withMoosePermit() {
        final DateTime dateTime = toDateTime(2018, 02, 11);
        final GroupHuntingDay huntingDay = createPermitAndHuntingDay(dateTime);

        // Create harvest for moose permit that is accepted to hunting day
        final Harvest mooseHuntingHarvest = model().newHarvest(mooseSpecies, harvestPerson, huntingDay);
        mooseHuntingHarvest.setRhy(rhy);

        onSavedAndAuthenticated(apiLukeUser, () -> {
            final CHAR_Harvests result = feature.exportAllHarvests(dateTime.getYear(), dateTime.getMonthOfYear());
            assertOfficialHarvest(result);
        });
    }

    @Test
    public void testOfficialHarvest_seasonHarvest() {
        final DateTime dateTime = toDateTime(2018, 02, 11);

        //Create bear harvest which is accepted as season harvest
        final Harvest bearHarvest = createBearHarvest(dateTime);
        markHarvestReportApproved(bearHarvest);

        onSavedAndAuthenticated(apiLukeUser, () -> {
            final CHAR_Harvests result = feature.exportAllHarvests(dateTime.getYear(), dateTime.getMonthOfYear());
            assertOfficialHarvest(result);
        });
    }

    @Test
    public void testSpecimenFetchedProperly() {
        final DateTime dateTime = toDateTime(2018, 02, 11);
        final Harvest harvest = createHarvest(dateTime);

        final HarvestSpecimen specimen = model().newHarvestSpecimen(harvest, ADULT_MALE);
        specimen.setWeightEstimated(250.0);
        specimen.setWeightMeasured(null);
        specimen.setNotEdible(false);
        specimen.setFitnessClass(GameFitnessClass.ERINOMAINEN);
        specimen.setAntlersWidth(4);
        specimen.setAntlerPointsLeft(3);
        specimen.setAntlerPointsRight(2);
        specimen.setAntlersType(GameAntlersType.LAPIO);

        onSavedAndAuthenticated(apiLukeUser, () -> {
            final CHAR_Harvests result = feature.exportAllHarvests(dateTime.getYear(), dateTime.getMonthOfYear());
            Assert.assertEquals(1, result.getHarvest().size());

            final List<CHAR_Specimen> resultSpecimenList = result.getSpecimen();
            Assert.assertEquals(1, resultSpecimenList.size());

            final CHAR_Specimen resultSpecimen = resultSpecimenList.iterator().next();
            Assert.assertEquals((long) harvest.getId(), resultSpecimen.getHarvestId());
            Assert.assertEquals(specimen.getGender().name(), resultSpecimen.getGender().name());
            Assert.assertEquals(specimen.getAge().name(), resultSpecimen.getAge().name());

            // Estimated weight should be assigned to weight as described in the schema documentation
            Assert.assertNotNull(resultSpecimen.getWeight());

            Assert.assertEquals(specimen.getWeightEstimated(), resultSpecimen.getWeight(), 0.01);
            Assert.assertEquals(specimen.getWeightEstimated(), resultSpecimen.getWeightEstimated(), 0.01);
            Assert.assertNull(resultSpecimen.getWeightMeasured());
            Assert.assertEquals(specimen.getFitnessClass().name(), resultSpecimen.getFitnessClass().name());
            Assert.assertEquals(specimen.getAntlersType().name(), resultSpecimen.getAntlersType().name());
            Assert.assertEquals(specimen.getAntlersWidth(), resultSpecimen.getAntlersWidth());
            Assert.assertEquals(specimen.getAntlerPointsLeft(), resultSpecimen.getAntlerPointsLeft());
            Assert.assertEquals(specimen.getAntlerPointsRight(), resultSpecimen.getAntlerPointsRight());
            Assert.assertEquals(specimen.getNotEdible(), resultSpecimen.isNotEdible());

            Assert.assertNull(specimen.getWeightMeasured());
        });
    }

    // RVR SPECIFIC

    @Test
    public void testRvrReturnsOnlyOfficialHarvest_bearNotApproved() {
        final DateTime dateTime = toDateTime(2018, 02, 11);

        //Create bear harvest which is accepted as season harvest
        final Harvest bearHarvest = createBearHarvest(dateTime);

        onSavedAndAuthenticated(apiRvrUser, () -> {
            final CHAR_Harvests result = feature.exportRVRHarvests(dateTime.getYear(), dateTime.getMonthOfYear());
            assertTrue(result.getHarvest().isEmpty());
        });
    }

    @Test
    public void testRvrReturnsOnlyOfficialHarvest_bearApproved() {
        final DateTime dateTime = toDateTime(2018, 02, 11);

        //Create bear harvest which is accepted as season harvest
        final Harvest bearHarvest = createBearHarvest(dateTime);
        markHarvestReportApproved(bearHarvest);
        onSavedAndAuthenticated(apiRvrUser, () -> {
            final CHAR_Harvests result = feature.exportRVRHarvests(dateTime.getYear(), dateTime.getMonthOfYear());
            assertOfficialHarvest(result);
        });
    }

    @Test
    public void testRvrReturnsOnlyOfficialHarvest_mooseNotApproved() {
        final DateTime dateTime = toDateTime(2018, 02, 11);

        final GroupHuntingDay huntingDay = createPermitAndHuntingDay(dateTime);

        final Harvest mooseHuntingHarvest = model().newHarvest(mooseSpecies);
        mooseHuntingHarvest.setRhy(rhy);

        onSavedAndAuthenticated(apiRvrUser, () -> {
            final CHAR_Harvests result = feature.exportRVRHarvests(dateTime.getYear(), dateTime.getMonthOfYear());
            assertTrue(result.getHarvest().isEmpty());
        });
    }

    @Test
    public void testRvrReturnsOnlyOfficialHarvest_mooseApproved() {
        final DateTime dateTime = toDateTime(2018, 02, 11);
        final GroupHuntingDay huntingDay = createPermitAndHuntingDay(dateTime);

        // Create harvest for moose permit that is accepted to hunting day
        final Harvest mooseHuntingHarvest = model().newHarvest(mooseSpecies, harvestPerson, huntingDay);
        mooseHuntingHarvest.setRhy(rhy);

        onSavedAndAuthenticated(apiRvrUser, () -> {
            final CHAR_Harvests result = feature.exportRVRHarvests(dateTime.getYear(), dateTime.getMonthOfYear());
            assertOfficialHarvest(result);
        });
    }

    @Test
    public void testRvrExportsOnlyValidSpecies() {

        final DateTime dateTime = toDateTime(2018, 02, 11);

        Arrays.stream(GameSpecies.ALL_GAME_SPECIES_CODES)
                .filter(code -> !RVR_SPECIES.contains(code))
                .forEach(code -> {
                    final Harvest harvest =
                            model().newHarvest(model().newGameSpecies(code),
                                    harvestPerson,
                                    harvestPerson);
                    harvest.setGeoLocation(geoLocation());
                    harvest.setPointOfTime(dateTime);
                    harvest.setRhy(rhy);
                    markHarvestReportApproved(harvest);
                });

        onSavedAndAuthenticated(apiRvrUser, () -> {
            final CHAR_Harvests result = feature.exportRVRHarvests(dateTime.getYear(), dateTime.getMonthOfYear());
            assertTrue(result.getHarvest().isEmpty());

            final String xml = feature.exportRVRHarvestsAsXml(dateTime.getYear(), dateTime.getMonthOfYear());
            assertFalse(xml.contains("<harvest>"));
        });
    }

    @Test
    public void testExportRVRHarvests_otter() {
        final DateTime dateTime = toDateTime(2021, 1, 14);
        final HarvestPermit permit = model().newHarvestPermit();

        final GameSpecies otter = model().newGameSpecies(OFFICIAL_CODE_OTTER);

        final Harvest harvest = model().newHarvest(permit, otter);
        harvest.setPointOfTime(dateTime);
        harvest.setHarvestReportDate(dateTime);
        harvest.setHarvestReportState(HarvestReportState.APPROVED);
        harvest.setHarvestReportAuthor(harvestPerson);
        harvest.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.ACCEPTED);

        onSavedAndAuthenticated(apiRvrUser, () -> {
            final CHAR_Harvests result = feature.exportRVRHarvests(dateTime.getYear(), dateTime.getMonthOfYear());
            assertOfficialHarvest(result);
        });
    }

    private Harvest createBearHarvest(DateTime dateTime) {
        final Harvest bearHarvest =
                model().newHarvest(bearSpecies,
                        harvestPerson,
                        harvestPerson);
        bearHarvest.setGeoLocation(geoLocation());
        bearHarvest.setPointOfTime(dateTime);
        bearHarvest.setRhy(rhy);
        return bearHarvest;
    }

    private static void assertOfficialHarvest(final CHAR_Harvests result) {

        Assert.assertEquals(1, result.getHarvest().size());
        final CHAR_Harvest h = result.getHarvest().iterator().next();
        assertTrue(h.isOfficialHarvest());

    }

    private static DateTime toDateTime(final int year, final int month, final int day) {
        // Search criteria uses inclusive search parameter, so one millisecond is added
        return new DateTime(year, month, day, 0, 0, 0, 1, Constants.DEFAULT_TIMEZONE);
    }

    private Harvest createHarvest(final DateTime date) {
        final Harvest o = model().newHarvest(mooseSpecies, harvestPerson);
        o.setPointOfTime(date);
        o.setRhy(rhy);
        return o;
    }

    private GroupHuntingDay createPermitAndHuntingDay(DateTime dateTime) {
        final HarvestPermit moosePermit = model().newHarvestPermit();
        final HuntingClub club = model().newHuntingClub();
        final HuntingClubGroup group = model().newHuntingClubGroup(club);
        final GroupHuntingDay huntingDay = model().newGroupHuntingDay(group, dateTime.toLocalDate());

        moosePermit.setPermitPartners(Collections.singleton(club));
        group.updateHarvestPermit(moosePermit);
        return huntingDay;
    }

    private void markHarvestReportApproved(Harvest harvest) {
        harvest.setHarvestReportState(HarvestReportState.APPROVED);
        harvest.setHarvestReportAuthor(harvestPerson);
        harvest.setHarvestReportDate(DateUtil.now());
    }
}
