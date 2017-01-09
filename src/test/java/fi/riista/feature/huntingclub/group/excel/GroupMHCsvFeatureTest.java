package fi.riista.feature.huntingclub.group.excel;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.person.ContactInfoShare;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class GroupMHCsvFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private GroupMHCsvFeature feature;

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
        withMooseHuntingGroupFixture(fixture -> onSavedAndAuthenticated(userCreator.apply(fixture), tx(() ->
                feature.export(fixture.club.getId(), fixture.group.getHuntingYear(), null))));
    }

    @Test
    public void testNoSpeciesGiven() {
        withMooseHuntingGroupFixture(fixture -> onSavedAndAuthenticated(createNewModerator(), tx(() -> {
            assertExport(fixture, null, 2, false);
        })));
    }

    @Test
    public void testSpeciesGiven() {
        withMooseHuntingGroupFixture(fixture -> onSavedAndAuthenticated(createNewModerator(), tx(() -> {
            assertExport(fixture, fixture.group.getSpecies().getOfficialCode(), 2, false);
        })));
    }

    @Test
    public void testNonMatchingSpeciesGiven() {
        final GameSpecies nonMatchingSpecies = model().newGameSpecies();
        withMooseHuntingGroupFixture(fixture -> onSavedAndAuthenticated(createNewModerator(), tx(() -> {
            assertExport(fixture, nonMatchingSpecies.getOfficialCode(), 0, false);
        })));
    }

    @Test
    public void testInvitedNotExported() {
        withMooseHuntingGroupFixture(fixture -> {
            final Person invitedPerson = model().newPerson();
            model().newHuntingClubInvitation(invitedPerson, fixture.club, OccupationType.SEURAN_JASEN);
            model().newHuntingClubGroupMember(invitedPerson, fixture.group);
            onSavedAndAuthenticated(createNewModerator(), tx(() -> {
                assertExport(fixture, null, 2, false);
            }));
        });
    }

    @Test
    public void testContactShareNone() {
        withMooseHuntingGroupFixture(fixture -> {
            setupContactInfo(fixture.groupMemberOccupation, fixture.groupMember, null);
            setupContactInfo(fixture.groupLeaderOccupation, fixture.groupLeader, null);
            onSavedAndAuthenticated(createNewModerator(), tx(() -> {
                assertExport(fixture, null, 2, false);
            }));
        });
    }

    @Test
    public void testContactShareOnlyOfficials() {
        withMooseHuntingGroupFixture(fixture -> {
            setupContactInfo(fixture.groupMemberOccupation, fixture.groupMember, ContactInfoShare.ONLY_OFFICIALS);
            setupContactInfo(fixture.groupLeaderOccupation, fixture.groupLeader, ContactInfoShare.ONLY_OFFICIALS);
            onSavedAndAuthenticated(createNewModerator(), tx(() -> {
                assertExport(fixture, null, 2, true);
            }));
        });
    }

    @Test
    public void testContactShareAllMember() {
        withMooseHuntingGroupFixture(fixture -> {
            setupContactInfo(fixture.groupMemberOccupation, fixture.groupMember, ContactInfoShare.ALL_MEMBERS);
            setupContactInfo(fixture.groupLeaderOccupation, fixture.groupLeader, ContactInfoShare.ALL_MEMBERS);
            onSavedAndAuthenticated(createNewModerator(), tx(() -> {
                assertExport(fixture, null, 2, true);
            }));
        });
    }

    private static void setupContactInfo(Occupation occupation, Person person, ContactInfoShare share) {
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
    }

    private void assertExport(HuntingGroupFixture fixture, Integer speciesCode, int expectedRowCount, boolean contactInfoIsShared) {
        final GroupMHCsvView csv = feature.export(fixture.club.getId(), fixture.group.getHuntingYear(), speciesCode);

        assertTrue(Arrays.equals(GroupMHCsvView.HEADER, csv.getHeaderRow()));
        List<String[]> rows = csv.getRows();
        assertEquals(expectedRowCount, rows.size());
        for (int rowCounter = 0; rowCounter < rows.size(); rowCounter++) {
            final String[] row = rows.get(rowCounter);

            assertEquals(GroupMHCsvView.HEADER.length, row.length);
            if (rowCounter > 0) {
                for (int column = 8; column < row.length; column++) {
                    String errMsg = "column:" + column + " value:'" + row[column] + "'";
                    if (contactInfoIsShared) {
                        assertNotNull(errMsg, row[column]);
                        assertNotEquals(errMsg, "", row[column]);
                    } else {
                        assertEquals(errMsg, "", row[column]);
                    }
                }
            }
        }
    }


}
