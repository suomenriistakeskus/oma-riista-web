package fi.riista.feature.huntingclub.members.invitation;

import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertTrue;

public class HuntingClubMemberInvitationFeatureTest extends EmbeddedDatabaseTest {

    private HuntingClub club;
    private Person clubContact;
    private Person member;

    @Resource
    private HuntingClubMemberInvitationFeature feature;

    @Before
    public void setup() {
        club = model().newHuntingClub();
        final Occupation contactOccupation = model().newHuntingClubMember(club, OccupationType.SEURAN_YHDYSHENKILO);
        final Occupation memberOccupation = model().newHuntingClubMember(club, OccupationType.SEURAN_JASEN);
        clubContact = contactOccupation.getPerson();
        member = memberOccupation.getPerson();
    }

    @Test
    public void testInvalidHunterNumber_smoke() {
        final Person invitedPerson = model().newPerson();

        onSavedAndAuthenticated(createNewUser("contact", clubContact), () -> {
            final Set<String> invalidHunterNumbers =
                    feature.findInvalidHunterNumbers(club.getId(), singleton(invitedPerson.getHunterNumber()));

            assertThat(invalidHunterNumbers, hasSize(0));
        });
    }

    @Test
    public void testInvalidHunterNumber_nonExistingHunterNumber() {
        final String hunterNumber = hunterNumber();

        onSavedAndAuthenticated(createNewUser("contact", clubContact), () -> {
            final Set<String> invalidHunterNumbers =
                    feature.findInvalidHunterNumbers(club.getId(), singleton(hunterNumber));

            assertThat(invalidHunterNumbers, hasSize(1));
            assertTrue(invalidHunterNumbers.contains(hunterNumber));
        });
    }

    @Test
    public void testInvalidHunterNumber_emptyList() {

        onSavedAndAuthenticated(createNewUser("contact", clubContact), () -> {
            final Set<String> invalidHunterNumbers = feature.findInvalidHunterNumbers(club.getId(),
                    emptySet());
            assertThat(invalidHunterNumbers, hasSize(0));
        });
    }

}
