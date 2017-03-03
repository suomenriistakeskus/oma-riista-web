package fi.riista.feature.huntingclub.statistics;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class HuntingClubStatisticsFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private HuntingClubStatisticsFeature huntingClubStatisticsFeature;

    private HuntingClub createClubOnly(Riistanhoitoyhdistys rhy) {
        return model().newHuntingClub(rhy);
    }

    private HuntingClub createNotRegisteredWithPermit(Riistanhoitoyhdistys rhy) {
        final HuntingClub huntingClub = createClubOnly(rhy);
        final HarvestPermit harvestPermit = model().newMooselikePermit(rhy);
        harvestPermit.setPermitPartners(Collections.singleton(huntingClub));
        return huntingClub;
    }

    private HuntingClub createRegistered(Riistanhoitoyhdistys rhy) {
        final HuntingClub club = createNotRegisteredWithPermit(rhy);
        model().newOccupation(club, model().newPerson(), OccupationType.SEURAN_YHDYSHENKILO);
        return club;
    }

    private HuntingClub createAreaDefined(Riistanhoitoyhdistys rhy) {
        final HuntingClub club = createRegistered(rhy);
        model().newHuntingClubArea(club);
        return club;
    }

    private HuntingClub createHasMember(Riistanhoitoyhdistys rhy) {
        final HuntingClub club = createAreaDefined(rhy);
        model().newOccupation(club, model().newPerson(), OccupationType.SEURAN_JASEN);
        return club;
    }

    private HuntingClub createHasInvitation(Riistanhoitoyhdistys rhy) {
        final HuntingClub club = createAreaDefined(rhy);
        model().newHuntingClubInvitation(club);
        return club;
    }

    private HuntingClub createHasMemberAndInvitation(Riistanhoitoyhdistys rhy) {
        final HuntingClub club = createAreaDefined(rhy);
        model().newOccupation(club, model().newPerson(), OccupationType.SEURAN_JASEN);
        model().newHuntingClubInvitation(club);
        return club;
    }

    private HuntingClub createHasGroup(Riistanhoitoyhdistys rhy) {
        final HuntingClub club = createAreaDefined(rhy);
        final Person member = model().newPerson();
        model().newOccupation(club, member, OccupationType.SEURAN_JASEN);
        final HuntingClubGroup group = model().newHuntingClubGroup(club);
        model().newOccupation(group, member, OccupationType.RYHMAN_JASEN);
        return club;
    }

    private HuntingClub createHasGroupLeader(Riistanhoitoyhdistys rhy) {
        final HuntingClub club = createAreaDefined(rhy);
        final Person member = model().newPerson();
        model().newOccupation(club, member, OccupationType.SEURAN_JASEN);
        final HuntingClubGroup group = model().newHuntingClubGroup(club);
        model().newOccupation(group, member, OccupationType.RYHMAN_JASEN);
        model().newOccupation(group, member, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
        return club;
    }

    @Test
    public void testSmoke() {
        final RiistakeskuksenAlue rka1 = model().newRiistakeskuksenAlue();
        final Riistanhoitoyhdistys rka1_rhy1 = model().newRiistanhoitoyhdistys(rka1);

        createNotRegisteredWithPermit(rka1_rhy1);
        createRegistered(rka1_rhy1);
        createAreaDefined(rka1_rhy1);
        createHasMember(rka1_rhy1);
        createHasInvitation(rka1_rhy1);
        createHasGroup(rka1_rhy1);
        createHasGroupLeader(rka1_rhy1);

        onSavedAndAuthenticated(createNewAdmin(), () -> {
            final List<HuntingClubStatisticsRow> statistics = huntingClubStatisticsFeature.calculate();
            assertThat(statistics, hasSize(2));

            assertCounts(0, statistics, 7, 1, 1, 2, 1, 1);
            assertCounts(1, statistics, 7, 1, 1, 2, 1, 1);
        });
    }

    @Test
    public void testClubWithoutPermitIgnored() {
        final RiistakeskuksenAlue rka1 = model().newRiistakeskuksenAlue();
        final Riistanhoitoyhdistys rka1_rhy1 = model().newRiistanhoitoyhdistys(rka1);

        createClubOnly(rka1_rhy1);

        onSavedAndAuthenticated(createNewAdmin(), () -> {
            final List<HuntingClubStatisticsRow> statistics = huntingClubStatisticsFeature.calculate();
            assertThat(statistics, hasSize(2));

            assertCounts(0, statistics, 0, 0, 0, 0, 0, 0);
            assertCounts(1, statistics, 0, 0, 0, 0, 0, 0);
        });
    }

    @Test
    public void testMemberAndInvitationAreEqual() {
        final RiistakeskuksenAlue rka1 = model().newRiistakeskuksenAlue();
        final Riistanhoitoyhdistys rka1_rhy1 = model().newRiistanhoitoyhdistys(rka1);

        createHasMemberAndInvitation(rka1_rhy1);

        onSavedAndAuthenticated(createNewAdmin(), () -> {
            final List<HuntingClubStatisticsRow> statistics = huntingClubStatisticsFeature.calculate();
            assertThat(statistics, hasSize(2));

            assertCounts(0, statistics, 1, 0, 0, 1, 0, 0);
        });
    }

    @Test
    public void testIgnoreClubContactPersonAsMember() {
        final RiistakeskuksenAlue rka1 = model().newRiistakeskuksenAlue();
        final Riistanhoitoyhdistys rka1_rhy1 = model().newRiistanhoitoyhdistys(rka1);

        final HuntingClub club = createRegistered(rka1_rhy1);

        final Person person = model().newPerson();
        model().newOccupation(club, person, OccupationType.SEURAN_YHDYSHENKILO);
        model().newOccupation(club, person, OccupationType.SEURAN_JASEN);
        model().newHuntingClubArea(club);

        onSavedAndAuthenticated(createNewAdmin(), () -> {
            final List<HuntingClubStatisticsRow> statistics = huntingClubStatisticsFeature.calculate();
            assertThat(statistics, hasSize(2));

            // Should not count member as registered, becuase same person as contact person
            assertCounts(0, statistics, 1, 0, 1, 0, 0, 0);
        });
    }

    @Test
    public void testAreaMustBeDefinedForNextLevel() {
        final RiistakeskuksenAlue rka1 = model().newRiistakeskuksenAlue();
        final Riistanhoitoyhdistys rka1_rhy1 = model().newRiistanhoitoyhdistys(rka1);

        final HuntingClub club = createRegistered(rka1_rhy1);

        final Person person1 = model().newPerson();
        final Person person2 = model().newPerson();
        model().newOccupation(club, person1, OccupationType.SEURAN_YHDYSHENKILO);
        //model().newHuntingClubArea(club);
        model().newOccupation(club, person2, OccupationType.SEURAN_JASEN);

        onSavedAndAuthenticated(createNewAdmin(), () -> {
            final List<HuntingClubStatisticsRow> statistics = huntingClubStatisticsFeature.calculate();
            assertThat(statistics, hasSize(2));

            // Area is not defined so registered member should not count
            assertCounts(0, statistics, 1, 1, 0, 0, 0, 0);
        });
    }

    @Test
    public void testMultipleRhy() {
        final RiistakeskuksenAlue rka1 = model().newRiistakeskuksenAlue();
        final Riistanhoitoyhdistys rka1_rhy1 = model().newRiistanhoitoyhdistys(rka1);
        final Riistanhoitoyhdistys rka1_rhy2 = model().newRiistanhoitoyhdistys(rka1);

        createNotRegisteredWithPermit(rka1_rhy1);
        createRegistered(rka1_rhy1);
        createAreaDefined(rka1_rhy1);
        createHasMember(rka1_rhy1);
        createHasInvitation(rka1_rhy1);
        createHasGroup(rka1_rhy1);
        createHasGroupLeader(rka1_rhy1);

        createNotRegisteredWithPermit(rka1_rhy2);
        createRegistered(rka1_rhy2);
        createAreaDefined(rka1_rhy2);

        onSavedAndAuthenticated(createNewAdmin(), () -> {
            final List<HuntingClubStatisticsRow> statistics = huntingClubStatisticsFeature.calculate();
            assertThat(statistics, hasSize(2));

            assertCounts(0, statistics, 10, 2, 2, 2, 1, 1);
        });
    }

    @Test
    public void testMultipleRka() {
        final RiistakeskuksenAlue rka1 = model().newRiistakeskuksenAlue("100");
        final RiistakeskuksenAlue rka2 = model().newRiistakeskuksenAlue("200");
        final Riistanhoitoyhdistys rka1_rhy1 = model().newRiistanhoitoyhdistys(rka1);
        final Riistanhoitoyhdistys rka2_rhy1 = model().newRiistanhoitoyhdistys(rka2);

        createNotRegisteredWithPermit(rka1_rhy1);
        createRegistered(rka1_rhy1);
        createAreaDefined(rka1_rhy1);
        createHasMember(rka1_rhy1);
        createHasInvitation(rka1_rhy1);
        createHasGroup(rka1_rhy1);
        createHasGroupLeader(rka1_rhy1);

        createNotRegisteredWithPermit(rka2_rhy1);
        createRegistered(rka2_rhy1);
        createAreaDefined(rka2_rhy1);

        onSavedAndAuthenticated(createNewAdmin(), () -> {
            final List<HuntingClubStatisticsRow> statistics = huntingClubStatisticsFeature.calculate();
            assertThat(statistics, hasSize(3));

            assertCounts(0, statistics, 10, 2, 2, 2, 1, 1);
            assertCounts(1, statistics, 7, 1, 1, 2, 1, 1);
            assertCounts(2, statistics, 3, 1, 1, 0, 0, 0);
        });
    }

    @Test
    public void testByRkaStatistics() {
        final RiistakeskuksenAlue rka1 = model().newRiistakeskuksenAlue("100");
        final RiistakeskuksenAlue rka2 = model().newRiistakeskuksenAlue("200");
        final Riistanhoitoyhdistys rka1_rhy1 = model().newRiistanhoitoyhdistys(rka1);
        final Riistanhoitoyhdistys rka1_rhy2 = model().newRiistanhoitoyhdistys(rka1);
        final Riistanhoitoyhdistys rka1_rhy3 = model().newRiistanhoitoyhdistys(rka1);

        final Riistanhoitoyhdistys rka2_rhy1 = model().newRiistanhoitoyhdistys(rka2);

        createNotRegisteredWithPermit(rka1_rhy1);
        createRegistered(rka1_rhy1);
        createAreaDefined(rka1_rhy1);
        createHasMember(rka1_rhy1);
        createHasInvitation(rka1_rhy1);
        createHasGroup(rka1_rhy1);
        createHasGroupLeader(rka1_rhy1);

        createHasGroup(rka1_rhy2);
        createHasGroup(rka1_rhy2);

        createHasGroupLeader(rka1_rhy3);
        createHasGroupLeader(rka1_rhy3);
        createHasGroupLeader(rka1_rhy3);

        //These should not be counted
        createNotRegisteredWithPermit(rka2_rhy1);
        createRegistered(rka2_rhy1);
        createAreaDefined(rka2_rhy1);

        onSavedAndAuthenticated(createNewAdmin(), () -> {
            final List<HuntingClubStatisticsRow> statistics =
                    huntingClubStatisticsFeature.calculateByRka(rka1.getId(), false);
            assertThat(statistics, hasSize(4));

            assertCounts(0, statistics, 12, 1, 1, 2, 3, 4);
            assertCounts(1, statistics, 7, 1, 1, 2, 1, 1);
            assertCounts(2, statistics, 2, 0, 0, 0, 2, 0);
            assertCounts(3, statistics, 3, 0, 0, 0, 0, 3);
        });
    }

    private static void assertCounts(final int row,
                                     final List<HuntingClubStatisticsRow> statistics,
                                     final int expectedCountAll,
                                     final int expectedCountRegistered,
                                     final int expectedCountAreaDefined,
                                     final int expectedCountMemberInvited,
                                     final int expectedCountGroupCreated,
                                     final int expectedCountGroupLeaderSelected) {
        final HuntingClubStatisticsRow rka1Stats = statistics.get(row);

        assertThat(rka1Stats.getCountAll(), equalTo(expectedCountAll));
        assertThat(rka1Stats.getCountRegistered(), equalTo(expectedCountRegistered));
        assertThat(rka1Stats.getCountAreaDefined(), equalTo(expectedCountAreaDefined));
        assertThat(rka1Stats.getCountMemberInvited(), equalTo(expectedCountMemberInvited));
        assertThat(rka1Stats.getCountGroupCreated(), equalTo(expectedCountGroupCreated));
        assertThat(rka1Stats.getCountGroupLeaderSelect(), equalTo(expectedCountGroupLeaderSelected));
    }
}
