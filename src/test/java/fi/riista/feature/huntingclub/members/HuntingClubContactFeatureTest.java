package fi.riista.feature.huntingclub.members;

import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.HuntingClubSubtype;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.members.rhy.RhyClubOccupationDTO;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.F;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.util.List;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

@RunWith(Theories.class)
public class HuntingClubContactFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private HuntingClubContactFeature huntingClubContactFeature;

    // listContacts

    @Test(expected = AccessDeniedException.class)
    public void testAuthorization_listContacts_user() {
        doTestNormalUser(rhyId -> huntingClubContactFeature.listRhyContacts(rhyId));
    }

    @Test
    public void testAuthorization_listContacts_coordinator() {
        doTestCoordinator(rhyId -> huntingClubContactFeature.listRhyContacts(rhyId));
    }

    @Test
    public void testAuthorization_listContacts_moderator() {
        doTestModerator(rhyId -> huntingClubContactFeature.listRhyContacts(rhyId));
    }

    // listLeaders

    @Test(expected = AccessDeniedException.class)
    public void testAuthorization_listLeaders_user() {
        doTestNormalUser(rhyId -> huntingClubContactFeature.listRhyHuntingLeaders(rhyId, 2015));
    }

    @Test
    public void testAuthorization_listLeaders_coordinator() {
        doTestCoordinator(rhyId -> huntingClubContactFeature.listRhyHuntingLeaders(rhyId, 2015));
    }

    @Test
    public void testAuthorization_listLeaders_moderator() {
        doTestModerator(rhyId -> huntingClubContactFeature.listRhyHuntingLeaders(rhyId, 2015));
    }

    private void doTestNormalUser(Function<Long, List<RhyClubOccupationDTO>> actionToTest) {
        withRhy(rhy -> onSavedAndAuthenticated(createNewUser(), () -> actionToTest.apply(rhy.getId())));
    }

    private void doTestCoordinator(Function<Long, List<RhyClubOccupationDTO>> actionToTest) {
        withRhyAndCoordinator((rhy, coordinator) -> {
            onSavedAndAuthenticated(createUser(coordinator), () -> actionToTest.apply(rhy.getId()));
        });
    }

    private void doTestModerator(Function<Long, List<RhyClubOccupationDTO>> actionToTest) {
        withRhy(rhy -> onSavedAndAuthenticated(createNewModerator(), () -> actionToTest.apply(rhy.getId())));
    }

    @Test
    public void testContactsAreFetchedByClubRHY_LeadersAreFetchedByPermit() {
        withRhy(rhy1 -> withRhy(rhy2 -> {
            final int huntingYear = 2016;

            final HarvestPermit permit1 = model().newHarvestPermit(rhy1);
            final HarvestPermit permit2 = model().newHarvestPermit(rhy2);

            final HuntingClub club1 = model().newHuntingClub(rhy1);
            final HuntingClub club2 = model().newHuntingClub(rhy2);

            final Occupation contact1 = model().newHuntingClubMember(club1, OccupationType.SEURAN_YHDYSHENKILO);
            final Occupation leader1 = createGroupAndLeader(huntingYear, permit1, club1);

            final Occupation contact2 = model().newHuntingClubMember(club2, OccupationType.SEURAN_YHDYSHENKILO);
            // This group's permit points to rhy1
            final Occupation leader2 = createGroupAndLeader(huntingYear, permit1, club2);
            // These two groups permits point to rhy2. There are two groups to make sure contacts are not duplicated
            final Occupation leader2_2 = createGroupAndLeader(huntingYear, permit2, club2);
            final Occupation leader2_3 = createGroupAndLeader(huntingYear, permit2, club2);

            onSavedAndAuthenticated(createNewModerator(), () -> {
                assertOccupations(huntingClubContactFeature.listRhyHuntingLeaders(rhy1.getId(), huntingYear), leader1, leader2);
                assertOccupations(huntingClubContactFeature.listRhyHuntingLeaders(rhy2.getId(), huntingYear), leader2_2, leader2_3);

                assertOccupations(huntingClubContactFeature.listRhyContacts(rhy1.getId()), contact1);
                assertOccupations(huntingClubContactFeature.listRhyContacts(rhy2.getId()), contact2);
            });
        }));
    }

    @Theory
    public void testListRhyHuntingLeaders_clubSubtype(final HuntingClubSubtype subtype) {
        withRhy(rhy -> {
            final int huntingYear = 2021;

            final HarvestPermit permit = model().newHarvestPermit(rhy);
            final HuntingClub club = model().newHuntingClub(rhy);
            club.setSubtype(subtype);
            final Occupation leader = createGroupAndLeader(huntingYear, permit, club);

            onSavedAndAuthenticated(createNewModerator(), () -> {
                final List<RhyClubOccupationDTO> occupations = huntingClubContactFeature.listRhyHuntingLeaders(rhy.getId(), huntingYear);
                assertThat(occupations, hasSize(1));
                assertOccupations(occupations, leader);
                assertThat(occupations.get(0).getClubSubtype(), is(equalTo(club.getSubtype())));
            });
        });
    }

    @Theory
    public void testListRhyContacts_clubSubtype(final HuntingClubSubtype subtype) {
        withRhy(rhy -> {
            final HuntingClub club = model().newHuntingClub(rhy);
            club.setSubtype(subtype);
            final Occupation contact = model().newHuntingClubMember(club, OccupationType.SEURAN_YHDYSHENKILO);

            onSavedAndAuthenticated(createNewModerator(), () -> {
                final List<RhyClubOccupationDTO> occupations = huntingClubContactFeature.listRhyContacts(rhy.getId());
                assertThat(occupations, hasSize(1));
                assertOccupations(occupations, contact);
                assertThat(occupations.get(0).getClubSubtype(), is(equalTo(club.getSubtype())));
            });
        });
    }

    private Occupation createGroupAndLeader(int huntingYear, HarvestPermit permit, HuntingClub club) {
        final HuntingClubGroup group = model().newHuntingClubGroup(club);
        group.updateHarvestPermit(permit);
        group.setHuntingYear(huntingYear);
        return model().newHuntingClubGroupMember(group, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
    }

    private static void assertOccupations(List<RhyClubOccupationDTO> occupations, Occupation... expected) {
        assertEquals(F.getUniqueIds(expected), F.getUniqueIds(occupations));
    }
}
