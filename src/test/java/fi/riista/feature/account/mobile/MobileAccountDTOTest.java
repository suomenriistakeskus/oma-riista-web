package fi.riista.feature.account.mobile;

import com.google.common.collect.Sets;
import fi.riista.feature.account.AccountShootingTestDTO;
import fi.riista.feature.account.user.SystemUser;
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
import java.util.TreeSet;

import static fi.riista.util.DateUtil.today;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class MobileAccountDTOTest {

    @Test
    public void testNotHunter() {
        final SystemUser user = createUser();
        final Person person = user.getPerson();

        final SortedSet<Integer> harvestYears = F.newSortedSet(2012, 2014);
        final SortedSet<Integer> observationYears = F.newSortedSet(2011, 2013, 2014);

        final MobileAccountDTO dto = MobileAccountDTO.create(
                user.getUsername(),
                person,
                person.getAddress(),
                person.getRhyMembership(),
                harvestYears,
                observationYears,
                Collections.<MobileOccupationDTO> emptyList(),
                false,
                false,
                null,
                Collections.<AccountShootingTestDTO> emptyList());

        assertCommonFields(user, harvestYears, observationYears, dto);
        assertNotHunter(dto);
    }

    @Test
    public void testHunter() {
        final LocalDate today = today();
        final SystemUser user = createHunter(today.minusMonths(6), today.plusMonths(6));
        final Person person = user.getPerson();

        final SortedSet<Integer> harvestYears = F.newSortedSet(2012, 2014);
        final SortedSet<Integer> observationYears = F.newSortedSet(2011);

        final MobileAccountDTO dto = MobileAccountDTO.create(
                user.getUsername(),
                person,
                person.getAddress(),
                person.getRhyMembership(),
                harvestYears,
                observationYears,
                Collections.<MobileOccupationDTO> emptyList(),
                false,
                false,
                null,
                Collections.<AccountShootingTestDTO> emptyList());

        assertCommonFields(user, harvestYears, observationYears, dto);
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

    private static void assertCommonFields(final SystemUser user,
                                           final SortedSet<Integer> harvestYears,
                                           final SortedSet<Integer> observationYears,
                                           final MobileAccountDTO dto) {

        final DateTime now = DateUtil.now();

        assertEquals(new TreeSet<>(Sets.union(harvestYears, observationYears)), dto.getGameDiaryYears());
        assertEquals(harvestYears, dto.getHarvestYears());
        assertEquals(observationYears, dto.getObservationYears());

        assertNotNull(dto.getTimestamp());
        assertTrue(new Interval(now.minusSeconds(1), now.plusSeconds(1)).contains(dto.getTimestamp()));

        assertEquals(user.getUsername(), dto.getUsername());
        assertEquals(user.getPerson().getFirstName(), dto.getFirstName());
        assertEquals(user.getPerson().getLastName(), dto.getLastName());
    }

    private static void assertNotHunter(final MobileAccountDTO dto) {
        assertNull(dto.getHunterNumber());
        assertNull(dto.getRhy());
        assertFalse(dto.isHuntingCardValidNow());
    }

    private static void assertHunter(final Person person, final boolean isPaid, final MobileAccountDTO dto) {
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
