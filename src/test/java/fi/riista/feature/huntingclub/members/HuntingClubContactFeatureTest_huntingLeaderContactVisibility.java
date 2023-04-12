package fi.riista.feature.huntingclub.members;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.fixture.MooselikePermitFixtureMixin;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.members.rhy.RhyClubOccupationDTO;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.ContactInfoShare;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.F;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.runner.RunWith;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(Theories.class)
public class HuntingClubContactFeatureTest_huntingLeaderContactVisibility
        extends EmbeddedDatabaseTest
        implements MooselikePermitFixtureMixin {

    @Resource
    private HuntingClubContactFeature huntingClubContactFeature;

    // List leaders for permit

    @Test
    public void testListClubHuntingLeaders_contactInfoNotListedByDefault() {
        withRhy(rhy -> {
            final int huntingYear = 2021;
            final GameSpecies moose = model().newGameSpeciesMoose();
            final HarvestPermit permit = model().newMooselikePermit(rhy, huntingYear);
            model().newHarvestPermitSpeciesAmount(permit, moose);
            final HuntingClub club = model().newHuntingClub(rhy);
            createGroupAndLeader(huntingYear, permit, club, moose);

            onSavedAndAuthenticated(createNewModerator(), () -> {
                final PermitHuntingLeaderContactInfoDTO dto = huntingClubContactFeature.listClubHuntingLeaders(
                        permit.getId(), huntingYear, moose.getOfficialCode());

                assertThat(dto.getPartnerLeaders(), is(empty()));
                assertThat(dto.getOtherLeaders(), is(empty()));
            });
        });
    }

    @Test
    public void testListClubHuntingLeaders_contactInfoShown_permitLevel() {
        runPartnerContactShareWith(ContactInfoShare.SAME_PERMIT_LEVEL);
    }

    @Test
    public void testListClubHuntingLeaders_contactInfoShown_rhyLevel() {
        runPartnerContactShareWith(ContactInfoShare.RHY_LEVEL);
    }

    private void runPartnerContactShareWith(final ContactInfoShare share) {
        withRhy(rhy -> {
            final int huntingYear = 2022;
            final GameSpecies moose = model().newGameSpeciesMoose();
            final HarvestPermit permit = model().newMooselikePermit(rhy, huntingYear);

            model().newHarvestPermitSpeciesAmount(permit, moose);
            final HuntingClub club = model().newHuntingClub(rhy);
            final Occupation leader = createGroupAndLeader(huntingYear, permit, club, moose);
            leader.setContactInfoShare(share);

            onSavedAndAuthenticated(createNewModerator(), () -> {
                final PermitHuntingLeaderContactInfoDTO dto = huntingClubContactFeature.listClubHuntingLeaders(
                        permit.getId(), huntingYear, moose.getOfficialCode());

                assertThat(dto.getPartnerLeaders(), hasSize(1));
                assertThat(dto.getOtherLeaders(), is(empty()));
                final HuntingClubContactDetailDTO contactDetailDTO = dto.getPartnerLeaders().get(0);
                assertContactDetailsMatch(club, leader, contactDetailDTO);
            });
        });
    }

    @Test
    public void testListClubHuntingLeaders_nonPartnerContact_nullValue() {
        withRhy(rhy -> {
            final int huntingYear = 2021;
            final GameSpecies moose = model().newGameSpeciesMoose();
            final HarvestPermit permit = model().newMooselikePermit(rhy, huntingYear);
            final HarvestPermit otherPermit = model().newMooselikePermit(rhy, huntingYear);
            final HuntingClub club = model().newHuntingClub(rhy);
            createGroupAndLeader(huntingYear, otherPermit, club, moose);

            onSavedAndAuthenticated(createNewModerator(), () -> {
                final PermitHuntingLeaderContactInfoDTO dto = huntingClubContactFeature.listClubHuntingLeaders(
                        permit.getId(), huntingYear, moose.getOfficialCode());

                assertThat(dto.getPartnerLeaders(), is(empty()));
                assertThat(dto.getOtherLeaders(), is(empty()));

            });
        });
    }

    @Test
    public void testListClubHuntingLeaders_nonPartnerContact_permitLevel() {
        withRhy(rhy -> {
            final int huntingYear = 2021;
            final GameSpecies moose = model().newGameSpeciesMoose();
            final HarvestPermit permit = model().newMooselikePermit(rhy, huntingYear);
            final HarvestPermit otherPermit = model().newMooselikePermit(rhy, huntingYear);
            final HuntingClub club = model().newHuntingClub(rhy);
            final Occupation leader = createGroupAndLeader(huntingYear, otherPermit, club, moose);
            leader.setContactInfoShare(ContactInfoShare.SAME_PERMIT_LEVEL);

            onSavedAndAuthenticated(createNewModerator(), () -> {
                final PermitHuntingLeaderContactInfoDTO dto = huntingClubContactFeature.listClubHuntingLeaders(
                        permit.getId(), huntingYear, moose.getOfficialCode());

                assertThat(dto.getPartnerLeaders(), is(empty()));
                assertThat(dto.getOtherLeaders(), is(empty()));

            });
        });
    }

    @Test
    public void testListClubHuntingLeaders_nonPartnerContact_rhyLevel() {
        withRhy(rhy -> {
            final int huntingYear = 2021;
            final GameSpecies moose = model().newGameSpeciesMoose();
            final HarvestPermit permit = model().newMooselikePermit(rhy, huntingYear);
            final HarvestPermit otherPermit = model().newMooselikePermit(rhy, huntingYear);
            final HuntingClub club = model().newHuntingClub(rhy);
            final Occupation leader = createGroupAndLeader(huntingYear, otherPermit, club, moose);
            leader.setContactInfoShare(ContactInfoShare.RHY_LEVEL);

            onSavedAndAuthenticated(createNewModerator(), () -> {
                final PermitHuntingLeaderContactInfoDTO dto = huntingClubContactFeature.listClubHuntingLeaders(
                        permit.getId(), huntingYear, moose.getOfficialCode());

                assertThat(dto.getPartnerLeaders(), is(empty()));
                assertThat(dto.getOtherLeaders(), hasSize(1));
                assertContactDetailsMatch(club, leader, dto.getOtherLeaders().get(0));
            });
        });
    }

    // Leaders for permit contact person

    @Test(expected = AccessDeniedException.class)
    public void testListClubHuntingLeadersForContactPerson_unauthorized() {
        withRhy(rhy -> {
            final int huntingYear = 2021;
            final GameSpecies moose = model().newGameSpeciesMoose();
            final HarvestPermit permit = model().newMooselikePermit(rhy, huntingYear);
            final HuntingClub club = model().newHuntingClub(rhy);

            final Occupation leader = createGroupAndLeader(huntingYear, permit, club, moose);

            onSavedAndAuthenticated(createUser(leader.getPerson()), () -> {
                huntingClubContactFeature.listClubHuntingLeadersForContactPerson(
                        permit.getId(), huntingYear, moose.getOfficialCode());

                fail("Should have thrown AccessDeniedException");
            });
        });
    }

    @Test
    public void testListClubHuntingLeadersForContactPerson_primaryLeaderContactInfoListedAlways() {
        withRhy(rhy -> {
            final int huntingYear = 2021;
            final GameSpecies moose = model().newGameSpeciesMoose();
            final HarvestPermit permit = model().newMooselikePermit(rhy, huntingYear);
            final HuntingClub club = model().newHuntingClub(rhy);

            final Occupation leader = createGroupAndLeader(huntingYear, permit, club, moose);
            leader.setCallOrder(0);
            leader.setContactInfoShare(null);

            onSavedAndAuthenticated(createUser(permit.getOriginalContactPerson()), () -> {
                final PermitHuntingLeaderContactInfoDTO dto = huntingClubContactFeature.listClubHuntingLeadersForContactPerson(
                        permit.getId(), huntingYear, moose.getOfficialCode());

                assertThat(dto.getPartnerLeaders(), hasSize(1));
                assertThat(dto.getOtherLeaders(), is(empty()));
                assertContactDetailsMatch(club, leader, dto.getPartnerLeaders().get(0));
            });
        });
    }

    @Test
    public void testListClubHuntingLeaders_contactPersonSeesNonPrimaryLeaderContactInfoWhenSet() {
        withRhy(rhy -> {
            final int huntingYear = 2021;
            final GameSpecies moose = model().newGameSpeciesMoose();
            final HarvestPermit permit = model().newMooselikePermit(rhy, huntingYear);
            final HuntingClub club = model().newHuntingClub(rhy);

            final Occupation leader = createGroupAndLeader(huntingYear, permit, club, moose);
            leader.setCallOrder(1);
            leader.setContactInfoShare(ContactInfoShare.SAME_PERMIT_LEVEL);

            // This should not be visible
            final HuntingClub otherClub = model().newHuntingClub(rhy);
            final HarvestPermit otherPermit = model().newMooselikePermit(rhy, huntingYear);
            createGroupAndLeader(huntingYear, otherPermit, otherClub, moose);

            onSavedAndAuthenticated(createUser(permit.getOriginalContactPerson()), () -> {
                final PermitHuntingLeaderContactInfoDTO dto =
                        huntingClubContactFeature.listClubHuntingLeadersForContactPerson(
                                permit.getId(), huntingYear, moose.getOfficialCode());

                assertThat(dto.getPartnerLeaders(), hasSize(1));
                assertThat(dto.getOtherLeaders(), is(empty()));
                assertContactDetailsMatch(club, leader, dto.getPartnerLeaders().get(0));
            });
        });
    }

    @Test
    public void testListClubHuntingLeaders_contactPersonSeesNonPrimaryLeaderContactInfoWhenNotSet() {
        withRhy(rhy -> {
            final int huntingYear = 2021;
            final GameSpecies moose = model().newGameSpeciesMoose();
            final HarvestPermit permit = model().newMooselikePermit(rhy, huntingYear);
            final HuntingClub club = model().newHuntingClub(rhy);
            final Occupation leader = createGroupAndLeader(huntingYear, permit, club, moose);

            // This should not be visible
            final HuntingClub otherClub = model().newHuntingClub(rhy);
            final HarvestPermit otherPermit = model().newMooselikePermit(rhy, huntingYear);
            createGroupAndLeader(huntingYear, otherPermit, otherClub, moose);

            leader.setCallOrder(1);
            leader.setContactInfoShare(null);

            onSavedAndAuthenticated(createUser(permit.getOriginalContactPerson()), () -> {
                final PermitHuntingLeaderContactInfoDTO dto =
                        huntingClubContactFeature.listClubHuntingLeadersForContactPerson(
                                permit.getId(), huntingYear, moose.getOfficialCode());

                assertThat(dto.getPartnerLeaders(), hasSize(1));
                assertThat(dto.getOtherLeaders(), is(empty()));
                assertContactDetailsMatch(club, leader, dto.getPartnerLeaders().get(0));
            });
        });
    }

    private Occupation createGroupAndLeader(int huntingYear, HarvestPermit permit, HuntingClub club, GameSpecies species) {
        final HuntingClubGroup group = model().newHuntingClubGroup(club);
        group.updateHarvestPermit(permit);
        group.setHuntingYear(huntingYear);
        group.setSpecies(species);
        return model().newHuntingClubGroupMember(group, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
    }

    private void assertContactDetailsMatch(HuntingClub club, Occupation leader, HuntingClubContactDetailDTO contactDetailDTO) {
        assertThat(contactDetailDTO.getClub().getNameFI(), equalTo(club.getNameFinnish()));
        assertThat(contactDetailDTO.getFirstName(), equalTo(leader.getPerson().getFirstName()));
        assertThat(contactDetailDTO.getLastName(), equalTo(leader.getPerson().getLastName()));
    }
}
