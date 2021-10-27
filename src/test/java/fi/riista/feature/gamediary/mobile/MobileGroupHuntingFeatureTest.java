package fi.riista.feature.gamediary.mobile;

import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.feature.huntingclub.hunting.MobileGroupHuntingAreaDTO;
import fi.riista.feature.huntingclub.hunting.MobileGroupHuntingStatusDTO;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.F;
import fi.riista.util.MockTimeProvider;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Map;

import static fi.riista.feature.organization.occupation.OccupationType.RYHMAN_JASEN;
import static fi.riista.feature.organization.occupation.OccupationType.RYHMAN_METSASTYKSENJOHTAJA;
import static fi.riista.test.Asserts.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class MobileGroupHuntingFeatureTest extends EmbeddedDatabaseTest implements HuntingGroupFixtureMixin {

    @Resource
    private MobileGroupHuntingFeature feature;

    @After
    public void tearDown() {
        MockTimeProvider.resetMock();
    }

    @Test
    public void testGetHuntingGroups() {
        withMooseHuntingGroupFixture(f ->
                withHuntingGroupFixture(f.species, notApplicableFixture ->
                        onSavedAndAuthenticated(createUser(f.groupLeader), () -> {
                            final MobileGroupHuntingLeaderDTO huntingGroups = feature.getHuntingGroups();
                            assertThat(huntingGroups.getClubs(), hasSize(1));
                            assertThat(huntingGroups.getGroups(), hasSize(1));

                            assertClub(f.club, huntingGroups.getClubs().get(0));

                            final MobileHuntingClubGroupDTO groupDTO = huntingGroups.getGroups().get(0);
                            assertGroup(f.group, groupDTO);

                            assertThat(groupDTO.getClubId(), equalTo(f.club.getId()));
                            assertThat(groupDTO.getPermitNumber(), equalTo(f.permit.getPermitNumber()));
                            assertThat(groupDTO.getSpeciesCode(), equalTo(f.species.getOfficialCode()));
                        })));
    }

    @Test
    public void testGetHuntingGroups_noResultForGroupMembers() {
        withMooseHuntingGroupFixture(f ->
                onSavedAndAuthenticated(createUser(f.groupMember), () -> {
                    final MobileGroupHuntingLeaderDTO huntingGroups = feature.getHuntingGroups();
                    assertThat(huntingGroups.getClubs(), hasSize(0));
                    assertThat(huntingGroups.getGroups(), hasSize(0));
                }));
    }

    @Test
    public void testGetMembers() {
        withMooseHuntingGroupFixture(f ->
                withHuntingGroupFixture(f.species, notApplicableFixture ->
                        onSavedAndAuthenticated(createUser(f.groupLeader), () -> {
                            final Map<OccupationType, MobileHuntingGroupOccupationDTO> members =
                                    F.index(feature.getMembers(f.group.getId()), MobileHuntingGroupOccupationDTO::getOccupationType);
                            assertThat(members.entrySet(), hasSize(2));

                            final MobileHuntingGroupOccupationDTO leader = members.get(RYHMAN_METSASTYKSENJOHTAJA);
                            assertOccupation(f.groupLeader, leader);

                            final MobileHuntingGroupOccupationDTO member = members.get(RYHMAN_JASEN);
                            assertOccupation(f.groupMember, member);
                        })));
    }

    @Ignore("getBounds causes problems with H2, will need to check out how to test this one")
    @Test
    public void testGroupHuntingArea() {
        withMooseHuntingGroupFixture(f ->
                withHuntingGroupFixture(f.species, notApplicableFixture ->
                        onSavedAndAuthenticated(createUser(f.groupLeader), () -> {
                            final MobileGroupHuntingAreaDTO areaDTO = feature.groupHuntingArea(f.group.getId());
                            assertThat(areaDTO.getAreaId(), equalTo(f.clubArea.getId()));
                            assertThat(areaDTO.getExternalId(), equalTo(f.clubArea.getExternalId()));
                        })));
    }

    @Test
    public void testGetGroupStatus_permitDayOpenForHuntingYear() {
        final LocalDate huntingDay = new LocalDate(2021, 3, 31);
        MockTimeProvider.mockTime(huntingDay.toDate().getTime());

        withMooseHuntingGroupFixture(f ->
                withHuntingGroupFixture(f.species, notApplicableFixture ->
                        onSavedAndAuthenticated(createUser(f.groupLeader), () -> {
                            final MobileGroupHuntingStatusDTO status = feature.getGroupStatus(f.group.getId());
                            assertThat(status.isCanCreateHarvest(), is(true));
                            assertThat(status.isCanCreateHuntingDay(), is(true));
                            assertThat(status.isCanCreateObservation(), is(true));
                            assertThat(status.isCanEditDiaryEntry(), is(true));
                            assertThat(status.isCanEditHuntingDay(), is(true));
                        })));
    }

    @Test
    public void testGetGroupStatus_permitDayLockedForHuntingYear() {
        final LocalDate huntingDay = new LocalDate(2021, 4, 1);
        MockTimeProvider.mockTime(huntingDay.toDate().getTime());

        withMooseHuntingGroupFixture(f ->
                withHuntingGroupFixture(f.species, notApplicableFixture ->
                        onSavedAndAuthenticated(createUser(f.groupLeader), () -> {
                            final MobileGroupHuntingStatusDTO status = feature.getGroupStatus(f.group.getId());
                            assertThat(status.isCanCreateHarvest(), is(false));
                            assertThat(status.isCanCreateHuntingDay(), is(false));
                            assertThat(status.isCanCreateObservation(), is(false));
                            assertThat(status.isCanEditDiaryEntry(), is(false));
                            assertThat(status.isCanEditHuntingDay(), is(false));
                        })));
    }

    private void assertOccupation(final Person person, final MobileHuntingGroupOccupationDTO dto) {
        assertThat(dto.getPersonId(), equalTo(person.getId()));
        assertThat(dto.getFirstName(), equalTo(person.getFirstName()));
        assertThat(dto.getLastName(), equalTo(person.getLastName()));
        assertThat(dto.getHunterNumber(), equalTo(person.getHunterNumber()));
    }

    private void assertClub(final HuntingClub club, final MobileHuntingClubDTO clubDTO) {
        assertThat(clubDTO.getId(), equalTo(club.getId()));
        assertThat(clubDTO.getName().get("fi"), equalTo(club.getNameFinnish()));
        assertThat(clubDTO.getName().get("sv"), equalTo(club.getNameSwedish()));
        assertThat(clubDTO.getOfficialCode(), equalTo(club.getOfficialCode()));
    }

    private void assertGroup(final HuntingClubGroup group, final MobileHuntingClubGroupDTO groupDTO) {
        assertThat(groupDTO.getId(), equalTo(group.getId()));
        assertThat(groupDTO.getHuntingYear(), equalTo(group.getHuntingYear()));
        assertThat(groupDTO.getName().get("fi"), equalTo(group.getNameFinnish()));
        assertThat(groupDTO.getName().get("sv"), equalTo(group.getNameSwedish()));

    }
}
