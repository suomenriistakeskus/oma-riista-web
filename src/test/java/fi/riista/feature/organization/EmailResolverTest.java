package fi.riista.feature.organization;

import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static fi.riista.feature.organization.occupation.OccupationType.RYHMAN_METSASTYKSENJOHTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_JASEN;
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_YHDYSHENKILO;
import static fi.riista.feature.organization.occupation.OccupationType.TOIMINNANOHJAAJA;
import static org.junit.Assert.assertEquals;

public class EmailResolverTest extends EmbeddedDatabaseTest {

    @Resource
    private EmailResolver emailResolver;

    @Transactional
    @Test
    public void testFindEmailsOfOccupiedPersons() {
        withRhy(rhy -> withPerson(person1 -> withPerson(person2 -> withPerson(person3 -> {

            final HuntingClub club = model().newHuntingClub(rhy);

            model().newOccupation(club, person1, SEURAN_YHDYSHENKILO);
            model().newOccupation(club, person2, SEURAN_YHDYSHENKILO);

            person1.setEmail("person1@club.com");
            person2.setEmail("PERSON2@CLUB.COM");
            person3.setEmail("person3@club.com");

            // Email of person3 should not be picked
            model().newOccupation(model().newHuntingClub(rhy), person3, SEURAN_YHDYSHENKILO);
            model().newOccupation(club, person3, SEURAN_JASEN);
            model().newOccupation(rhy, person3, TOIMINNANOHJAAJA);
            model().newOccupation(model().newHuntingClubGroup(club), person3, RYHMAN_METSASTYKSENJOHTAJA);

            persistInCurrentlyOpenTransaction();

            final Set<String> expected = newHashSet(person1.getEmail(), person2.getEmail().toLowerCase());

            assertEquals(expected, emailResolver.findEmailsOfOccupiedPersons(club, SEURAN_YHDYSHENKILO));
        }))));
    }
}
