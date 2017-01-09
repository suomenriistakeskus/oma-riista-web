package fi.riista.feature.gamediary.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.mobile.MobileAccountV1DTO;
import fi.riista.feature.gamediary.mobile.MobileOccupationDTO;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.util.DateUtil;
import fi.riista.util.F;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.junit.Test;

import java.util.Collections;
import java.util.SortedSet;

public class MobileAccountV1DTOTest {

    @Test
    public void testNotHunter() {
        final SystemUser user = createUser();
        final Person person = user.getPerson();
        final SortedSet<Integer> years = F.newSortedSet(2012, 2014);

        final MobileAccountV1DTO dto = MobileAccountV1DTO.create(
                user.getUsername(),
                person,
                person.getAddress(),
                person.getRhyMembership(),
                years,
                Collections.<MobileOccupationDTO> emptyList());

        assertCommonFields(user, years, dto);
        assertNotHunter(dto);
    }

    @Test
    public void testHunter() {
        final SystemUser user = createHunter(DateUtil.today().minusMonths(6), DateUtil.today().plusMonths(6));
        final Person person = user.getPerson();
        final SortedSet<Integer> years = F.newSortedSet(2012, 2014);

        final MobileAccountV1DTO dto = MobileAccountV1DTO.create(
                user.getUsername(),
                person,
                person.getAddress(),
                person.getRhyMembership(),
                years,
                Collections.<MobileOccupationDTO> emptyList());

        assertCommonFields(user, years, dto);
        assertHunter(person, true, dto);
    }

    private static SystemUser createHunter(final LocalDate feePaidBegin, final LocalDate feePaidEnd) {
        final SystemUser u = createUser();
        final Person p = u.getPerson();
        p.setHunterNumber("11111111");
        p.setHuntingCardStart(feePaidBegin);
        p.setHuntingCardEnd(feePaidEnd);

        final Riistanhoitoyhdistys rhy = new Riistanhoitoyhdistys();
        rhy.setNameFinnish("nameFI");
        rhy.setNameSwedish("nameSV");
        rhy.setOfficialCode("555");
        p.setRhyMembership(rhy);

        return u;
    }

    private static void assertCommonFields(
            final SystemUser user, final SortedSet<Integer> years, final MobileAccountV1DTO dto) {

        final DateTime now = DateUtil.now();

        assertEquals(years, dto.getGameDiaryYears());

        assertNotNull(dto.getTimestamp());
        assertTrue(new Interval(now.minusSeconds(1), now.plusSeconds(1)).contains(dto.getTimestamp()));

        assertEquals(user.getUsername(), dto.getUsername());
        assertEquals(user.getPerson().getFirstName(), dto.getFirstName());
        assertEquals(user.getPerson().getLastName(), dto.getLastName());
    }

    private static void assertNotHunter(final MobileAccountV1DTO dto) {
        assertNull(dto.getHunterNumber());
        assertNull(dto.getRhy());
        assertFalse(dto.isHuntingCardValidNow());
    }

    private static void assertHunter(final Person person, final boolean isPaid, final MobileAccountV1DTO dto) {
        final Riistanhoitoyhdistys rhy = person.getRhyMembership();
        assertEquals(rhy.getNameFinnish(), dto.getRhy().getName().get("fi"));
        assertEquals(rhy.getNameSwedish(), dto.getRhy().getName().get("sv"));
        assertEquals(rhy.getOfficialCode(), dto.getRhy().getOfficialCode());

        assertEquals(person.getHunterNumber(), dto.getHunterNumber());
        assertEquals(isPaid, dto.isHuntingCardValidNow());
    }

    private static SystemUser createUser() {
        final SystemUser u = new SystemUser();
        u.setUsername("username");
        final Person person = new Person();
        person.setFirstName("firstName");
        person.setLastName("lastName");
        u.setPerson(person);
        return u;
    }
}
