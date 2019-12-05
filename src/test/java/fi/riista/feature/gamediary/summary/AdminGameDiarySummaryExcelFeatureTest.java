package fi.riista.feature.gamediary.summary;

import com.querydsl.core.BooleanBuilder;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.F;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BEAR;
import static fi.riista.util.DateUtil.today;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;

public class AdminGameDiarySummaryExcelFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private AdminGameDiarySummaryExcelFeature excelFeature;

    private Riistanhoitoyhdistys rhy;
    private HuntingClub club;
    private HuntingClubGroup group;
    private GroupHuntingDay huntingDay;
    private Person hunter;
    private SystemUser moderator;
    private Harvest huntingDayHarvest;
    private Harvest moderatedHuntingDayHarvest;
    private Harvest mooseHarvest;
    private GameSpecies bearSpecies;
    private Harvest bearHarvest;
    private Harvest approvedBearHarvest;
    private GameSpecies mooseSpecies;


    @Before
    public void setup() {
        rhy = model().newRiistanhoitoyhdistys();
        club = model().newHuntingClub(rhy);
        mooseSpecies = model().newGameSpeciesMoose();
        group = model().newHuntingClubGroup(club, mooseSpecies);
        huntingDay = model().newGroupHuntingDay(group, today());
        hunter = model().newPerson();
        moderator = createNewModerator();
        huntingDayHarvest = model().newHarvest(hunter, huntingDay);
        huntingDayHarvest.updateHuntingDayOfGroup(huntingDay, hunter);
        huntingDayHarvest.setRhy(rhy);

        moderatedHuntingDayHarvest = model().newHarvest(hunter, huntingDay);
        moderatedHuntingDayHarvest.updateHuntingDayOfGroup(huntingDay, null);
        moderatedHuntingDayHarvest.setModeratorOverride(true);
        moderatedHuntingDayHarvest.setRhy(rhy);

        mooseHarvest = model().newHarvest(mooseSpecies, hunter);
        mooseHarvest.setRhy(rhy);
        bearSpecies = model().newGameSpecies(OFFICIAL_CODE_BEAR);
        bearHarvest = model().newHarvest(bearSpecies, hunter);
        bearHarvest.setHarvestReportDate(today().toDateTimeAtStartOfDay());
        bearHarvest.setHarvestReportAuthor(hunter);
        bearHarvest.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);
        bearHarvest.setRhy(rhy);
        approvedBearHarvest = model().newHarvest(bearSpecies, hunter);
        approvedBearHarvest.setHarvestReportDate(today().toDateTimeAtStartOfDay());
        approvedBearHarvest.setHarvestReportAuthor(hunter);
        approvedBearHarvest.setHarvestReportState(HarvestReportState.APPROVED);
        approvedBearHarvest.setRhy(rhy);
    }

    @Test
    public void testOnlyOfficial_moose() {

        runAsModeratorInTransaction(() -> {
            final BooleanBuilder harvestPredicate =
                    excelFeature.createHarvestPredicate(null, mooseSpecies,
                            OrganisationType.RK, null, false, true);

            final List<HarvestDTO> harvestDTOS = excelFeature.loadHarvest(harvestPredicate);
            assertThat(harvestDTOS, hasSize(2));
            assertThat(F.getUniqueIds(harvestDTOS), containsInAnyOrder(huntingDayHarvest.getId(),
                    moderatedHuntingDayHarvest.getId()));

        });
    }

    @Test
    public void testAll_moose() {

        runAsModeratorInTransaction(() -> {
            final BooleanBuilder harvestPredicate =
                    excelFeature.createHarvestPredicate(null, mooseSpecies,
                            OrganisationType.RK, null, false, false);

            final List<HarvestDTO> harvestDTOS = excelFeature.loadHarvest(harvestPredicate);
            assertThat(harvestDTOS, hasSize(3));
            assertThat(F.getUniqueIds(harvestDTOS), containsInAnyOrder(huntingDayHarvest.getId(), moderatedHuntingDayHarvest.getId(),
                    mooseHarvest.getId()));
        });
    }

    @Test
    public void testOnlyOfficial_bear() {

        runAsModeratorInTransaction(() -> {
            final BooleanBuilder harvestPredicate =
                    excelFeature.createHarvestPredicate(null, bearSpecies,
                            OrganisationType.RK, null, false, true);

            final List<HarvestDTO> harvestDTOS = excelFeature.loadHarvest(harvestPredicate);
            assertThat(harvestDTOS, hasSize(1));
            final HarvestDTO dto = harvestDTOS.get(0);

            assertEquals(approvedBearHarvest.getId(), dto.getId());
        });
    }

    @Test
    public void testOnlyHarvestReport_bear() {

        runAsModeratorInTransaction(() -> {
            final BooleanBuilder harvestPredicate =
                    excelFeature.createHarvestPredicate(null, null,
                            OrganisationType.RK, null, true, false);

            final List<HarvestDTO> harvestDTOS = excelFeature.loadHarvest(harvestPredicate);
            assertThat(harvestDTOS, hasSize(2));
            harvestDTOS.forEach(dto -> assertEquals(OFFICIAL_CODE_BEAR, dto.getGameSpeciesCode()));
            assertThat(F.getUniqueIds(harvestDTOS), containsInAnyOrder(approvedBearHarvest.getId(),
                    bearHarvest.getId()));
        });
    }

    private void runAsModeratorInTransaction(final Runnable runnable) {
        onSavedAndAuthenticated(moderator, () -> {
            runInTransaction(runnable);
        });
    }
}
