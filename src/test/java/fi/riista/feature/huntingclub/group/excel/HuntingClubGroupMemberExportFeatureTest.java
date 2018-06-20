package fi.riista.feature.huntingclub.group.excel;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.entity.Municipality;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.ContactInfoShare;
import fi.riista.feature.organization.person.Person;
import fi.riista.test.EmbeddedDatabaseTest;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.util.List;
import java.util.function.Function;

import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class HuntingClubGroupMemberExportFeatureTest extends EmbeddedDatabaseTest implements HuntingGroupFixtureMixin {

    @Resource
    private HuntingClubGroupMemberExportFeature feature;

    private String homeMunicipalityCode;

    @Before
    public void initMunicipality() {
        // Workaround to fix persistence of municipality field
        final Municipality municipality = model().newMunicipality();
        homeMunicipalityCode = municipality.getOfficialCode();
        persistInNewTransaction();
    }

    @Test(expected = AccessDeniedException.class)
    public void testAuthorization_groupMember() {
        testAuthorization(f -> createNewUser("user", f.groupMember));
    }

    @Test(expected = AccessDeniedException.class)
    public void testAuthorization_groupLeader() {
        testAuthorization(f -> createNewUser("user", f.groupLeader));
    }

    @Test(expected = AccessDeniedException.class)
    public void testAuthorization_clubMember() {
        testAuthorization(f -> createNewUser("user", f.clubMember));
    }

    @Test
    public void testAuthorization_clubContact() {
        testAuthorization(f -> createNewUser("user", f.clubContact));
    }

    @Test
    public void testAuthorization_clubModerator() {
        testAuthorization(f -> createNewModerator());
    }

    private void testAuthorization(final Function<HuntingGroupFixture, SystemUser> userCreator) {
        withMooseHuntingGroupFixture(fixture -> {
            onSavedAndAuthenticated(userCreator.apply(fixture), tx(() -> {
                feature.export(fixture.club.getId(), fixture.group.getHuntingYear(), null);
            }));
        });
    }

    @Test
    public void testNoSpeciesGiven() {
        withMooseHuntingGroupFixture(fixture -> {
            onSavedAndAuthenticated(createNewModerator(), tx(() -> assertExport(fixture, null, 2, false)));
        });
    }

    @Test
    public void testSpeciesGiven() {
        withMooseHuntingGroupFixture(fixture -> onSavedAndAuthenticated(createNewModerator(), tx(() -> {
            assertExport(fixture, fixture.group.getSpecies().getOfficialCode(), 2, false);
        })));
    }

    @Test
    public void testNonMatchingSpeciesGiven() {
        withMooseHuntingGroupFixture(fixture -> {
            final GameSpecies nonMatchingSpecies = model().newGameSpeciesNotSubjectToClubHunting();

            onSavedAndAuthenticated(createNewModerator(), tx(() -> {
                assertExport(fixture, nonMatchingSpecies.getOfficialCode(), 0, false);
            }));
        });
    }

    @Test
    public void testInvitedNotExported() {
        withMooseHuntingGroupFixture(fixture -> withPerson(invitedPerson -> {
            model().newHuntingClubInvitation(invitedPerson, fixture.club, OccupationType.SEURAN_JASEN);
            model().newHuntingClubGroupMember(invitedPerson, fixture.group);

            onSavedAndAuthenticated(createNewModerator(), tx(() -> {
                assertExport(fixture, null, 2, false);
            }));
        }));
    }

    @Test
    public void testContactShareNone() {
        withMooseHuntingGroupFixture(fixture -> {

            setupContactInfo(fixture.groupMemberOccupation, fixture.groupMember, null);
            setupContactInfo(fixture.groupLeaderOccupation, fixture.groupLeader, null);

            onSavedAndAuthenticated(createNewModerator(), tx(() -> assertExport(fixture, null, 2, false)));
        });
    }

    @Test
    public void testContactShareOnlyOfficials() {
        withMooseHuntingGroupFixture(fixture -> {

            setupContactInfo(fixture.groupMemberOccupation, fixture.groupMember, ContactInfoShare.ONLY_OFFICIALS);
            setupContactInfo(fixture.groupLeaderOccupation, fixture.groupLeader, ContactInfoShare.ONLY_OFFICIALS);

            onSavedAndAuthenticated(createNewModerator(), tx(() -> assertExport(fixture, null, 2, true)));
        });
    }

    @Test
    public void testContactShareAllMember() {
        withMooseHuntingGroupFixture(fixture -> {

            setupContactInfo(fixture.groupMemberOccupation, fixture.groupMember, ContactInfoShare.ALL_MEMBERS);
            setupContactInfo(fixture.groupLeaderOccupation, fixture.groupLeader, ContactInfoShare.ALL_MEMBERS);

            onSavedAndAuthenticated(createNewModerator(), tx(() -> assertExport(fixture, null, 2, true)));
        });
    }

    private void setupContactInfo(Occupation occupation, Person person, ContactInfoShare share) {
        occupation.setContactInfoShare(share);
        if (person.getAddress() == null) {
            person.setMrAddress(new Address());
        }
        person.getAddress().setStreetAddress("street");
        person.getAddress().setPostalCode("postalcode");
        person.getAddress().setCity("city");
        person.getAddress().setCountry("country");
        person.setPhoneNumber("020202");
        person.setEmail("x@invalid");
        person.setHomeMunicipalityCode(homeMunicipalityCode);
    }

    private void assertExport(HuntingGroupFixture fixture, Integer speciesCode, int expectedRowCount, boolean contactInfoIsShared) {
        final List<HuntingClubGroupMemberRowDTO> rows = feature.exportDataForTest(fixture.club.getId(), fixture.group.getHuntingYear(), speciesCode);

        assertEquals(expectedRowCount, rows.size());

        final Matcher<String> emptyMatcher = isEmptyOrNullString();
        final Matcher<String> notEmptyMatcher = not(emptyMatcher);

        for (HuntingClubGroupMemberRowDTO row : rows) {
            assertThat(row.getClubName(), notEmptyMatcher);
            assertThat(row.getSpeciesName(), notEmptyMatcher);
            assertThat(row.getGroupName(), notEmptyMatcher);
            assertThat(row.getLastName(), notEmptyMatcher);
            assertThat(row.getFirstName(), notEmptyMatcher);
            assertThat(row.getHunterNumber(), notEmptyMatcher);

            final Matcher<String> contactInfoMatcher = contactInfoIsShared ? notEmptyMatcher : emptyMatcher;

            assertThat(row.getStreetAddress(), contactInfoMatcher);
            assertThat(row.getPostalCode(), contactInfoMatcher);
            assertThat(row.getCity(), contactInfoMatcher);
            assertThat(row.getCountry(), contactInfoMatcher);

            assertThat(row.getPhoneNumber(), contactInfoMatcher);
            assertThat(row.getEmail(), contactInfoMatcher);
            assertThat(row.getHomeMunicipalityName(), contactInfoMatcher);
        }
    }
}
