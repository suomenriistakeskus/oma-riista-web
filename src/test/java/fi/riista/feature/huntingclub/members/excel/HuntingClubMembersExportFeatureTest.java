package fi.riista.feature.huntingclub.members.excel;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.entity.Municipality;
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.occupation.Occupation;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;

public class HuntingClubMembersExportFeatureTest extends EmbeddedDatabaseTest implements HuntingGroupFixtureMixin {

    @Resource
    private HuntingClubMembersExportFeature feature;

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
        withMooseHuntingGroupFixture(fixture -> onSavedAndAuthenticated(userCreator.apply(fixture), tx(() -> {
            assert fixture != null
                    && fixture.club != null
                    && fixture.club.getId() != null;
            feature.export(fixture.club.getId());
        })));
    }


    @Test
    public void testExportClubMembers() {
        withMooseHuntingGroupFixture(fixture -> onSavedAndAuthenticated(createNewModerator(), tx(() ->
                assertExport(fixture, 4, false))));
    }

    @Test
    public void testExportClubMembersShareAll() {
        withMooseHuntingGroupFixture(fixture -> {
            setupContactInfo(fixture.groupMemberClubOccupation, fixture.groupMember);
            setupContactInfo(fixture.groupLeaderClubOccupation, fixture.groupLeader);
            setupContactInfo(fixture.clubMemberOccupation, fixture.clubMember);
            setupContactInfo(fixture.clubContactOccupation, fixture.clubContact);

            onSavedAndAuthenticated(createNewModerator(), tx(() -> assertExport(fixture, 4, true)));
        });
    }


    private void setupContactInfo(final Occupation occupation, final Person person) {
        occupation.setContactInfoShare(ContactInfoShare.ALL_MEMBERS);
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

    private void assertExport(final HuntingGroupFixture fixture, final int expectedRowCount, final boolean contactInfoIsShared) {
        assert fixture != null
                && fixture.club != null
                && fixture.club.getId() != null;

        final List<HuntingClubMemberRowDTO> rows = feature.exportDataForTest(fixture.club.getId());

        assertThat(rows, hasSize(expectedRowCount));

        final Matcher<String> emptyMatcher = isEmptyOrNullString();
        final Matcher<String> notEmptyMatcher = not(emptyMatcher);

        for (final HuntingClubMemberRowDTO row : rows) {
            assertThat(row.getClubName().getAnyTranslation(), notEmptyMatcher);
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
            assertThat(row.getHomeMunicipalityName().getAnyTranslation(), contactInfoMatcher);
        }
    }
}
