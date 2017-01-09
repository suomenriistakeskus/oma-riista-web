package fi.riista.feature.huntingclub.area;

import com.google.common.collect.Lists;
import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.huntingclub.copy.HuntingClubAreaCopyDTO;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.HuntingClubGroupRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HuntingClubAreaCrudFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private HuntingClubAreaCrudFeature crudFeature;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testUpdateDetachedArea_CanUpdateAllFields() {
        HuntingClub club = model().newHuntingClub();
        Person contactPerson = model().newHuntingClubMember(club, OccupationType.SEURAN_YHDYSHENKILO).getPerson();
        SystemUser user = createUser(contactPerson);
        HuntingClubArea area = model().newHuntingClubArea(club, "FI", "SV", 2015);

        onSavedAndAuthenticated(user, () -> crudFeature.update(HuntingClubAreaDTO.create(area, club, null)));
    }

    @Test
    public void testUpdateAttachedArea_CanNotUpdateHuntingYear() {
        HuntingClub club = model().newHuntingClub();
        Person contactPerson = model().newHuntingClubMember(club, OccupationType.SEURAN_YHDYSHENKILO).getPerson();
        SystemUser user = createUser(contactPerson);
        HuntingClubArea area = model().newHuntingClubArea(club, "FI", "SV", 2015);
        HuntingClubGroup group = model().newHuntingClubGroupWithArea(area);

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("huntingYear cannot be changed");

        onSavedAndAuthenticated(user, () -> {
            final HuntingClubAreaDTO dto = HuntingClubAreaDTO.create(area, club, null);
            dto.setHuntingYear(area.getHuntingYear() + 1);
            crudFeature.update(dto);
        });
    }

    @Test
    public void testUpdateAttachedArea_CanNotUpdateActiveStatus() {
        HuntingClub club = model().newHuntingClub();
        Person contactPerson = model().newHuntingClubMember(club, OccupationType.SEURAN_YHDYSHENKILO).getPerson();
        SystemUser user = createUser(contactPerson);
        HuntingClubArea area = model().newHuntingClubArea(club, "FI", "SV", 2015);
        HuntingClubGroup group = model().newHuntingClubGroupWithArea(area);

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("area cannot be deactivated");

        onSavedAndAuthenticated(user, () -> {
            final HuntingClubAreaDTO dto = HuntingClubAreaDTO.create(area, club, null);
            dto.setActive(false);
            crudFeature.update(dto);
        });
    }

    @Test
    public void testUpdateAttachedArea_CanUpdateName() {
        HuntingClub club = model().newHuntingClub();
        Person contactPerson = model().newHuntingClubMember(club, OccupationType.SEURAN_YHDYSHENKILO).getPerson();
        SystemUser user = createUser(contactPerson);
        HuntingClubArea area = model().newHuntingClubArea(club, "FI", "SV", 2015);
        HuntingClubGroup group = model().newHuntingClubGroupWithArea(area);

        onSavedAndAuthenticated(user, () -> {
            final HuntingClubAreaDTO dto = HuntingClubAreaDTO.create(area, club, null);
            dto.setNameFI("FI2");
            dto.setNameSV("SV2");
            crudFeature.update(dto);
        });
    }

    @Resource
    private HuntingClubAreaRepository huntingClubAreaRepository;

    @Resource
    private HuntingClubGroupRepository groupRepository;

    @Test
    public void testListHuntingYears() {
        InitData initData = new InitData().persist();

        authenticate(initData.user);

        assertEquals(Lists.newArrayList(2013, 2014, 2015), crudFeature.listHuntingYears(initData.club.getId()));
    }

    @Test
    public void testListByClubAndYear() {
        InitData initData = new InitData().persist();

        authenticate(initData.user);

        assertSizeAndNames(crudFeature.listByClubAndYear(initData.club.getId(), 2013, true), 1, "2013");
        assertSizeAndNames(crudFeature.listByClubAndYear(initData.club.getId(), 2014, true), 2, "first", "second");
        assertSizeAndNames(crudFeature.listByClubAndYear(initData.club.getId(), 2015, true), 1, "2015");
    }

    @Test
    public void testListActiveOnly() {
        InitData initData = new InitData();
        initData.area2013.setActive(false);
        initData.areaFirst.setActive(false);

        initData.persist();

        authenticate(initData.user);

        assertSizeAndNames(crudFeature.listByClubAndYear(initData.club.getId(), 2013, true), 0);
        assertSizeAndNames(crudFeature.listByClubAndYear(initData.club.getId(), 2014, true), 1, "second");
        assertSizeAndNames(crudFeature.listByClubAndYear(initData.club.getId(), 2015, true), 1, "2015");
    }

    private static void assertSizeAndNames(List<HuntingClubAreaDTO> dtos, int count, String... names) {
        assertEquals(count, dtos.size());
        assertEquals("Give equal number of names as there should dtos", count, names.length);
        for (int i = 0; i < names.length; i++) {
            assertEquals(names[i], dtos.get(i).getNameFI());
        }
    }

    private class InitData {
        private HuntingClub club;
        private SystemUser user;
        private HuntingClubArea area2013;
        private HuntingClubArea areaFirst;
        private HuntingClubArea areaSecond;
        private HuntingClubArea area2015;

        InitData() {
            club = model().newHuntingClub();
            Person member1 = model().newHuntingClubMember(club, OccupationType.SEURAN_YHDYSHENKILO).getPerson();
            user = createUser(member1);

            area2013 = model().newHuntingClubArea(club, "2013", "2013", 2013);
            areaFirst = model().newHuntingClubArea(club, "first", "first", 2014);
            areaSecond = model().newHuntingClubArea(club, "second", "second", 2014);
            area2015 = model().newHuntingClubArea(club, "2015", "2015", 2015);
        }

        InitData persist() {
            persistInNewTransaction();
            return this;
        }
    }

    @Test
    public void testCopySameYearExcludingGroups() {
        testCopy(0, false);
    }

    @Test
    public void testCopySameYearIncludingGroups() {
        testCopy(0, true);
    }

    @Test
    public void testCopyNextYearExcludingGroups() {
        testCopy(1, false);
    }

    @Test
    public void testCopyNextYearIncludingGroups() {
        testCopy(1, true);
    }

    private void testCopy(int yearDelta, boolean copyGroups) {
        withPerson(person -> {
            final HuntingClub club = model().newHuntingClub();
            final HuntingClubArea area = model().newHuntingClubArea(club);
            final HuntingClubGroup group = model().newHuntingClubGroup(club);
            group.setHuntingYear(area.getHuntingYear());
            group.setHuntingArea(area);

            final int newYear = group.getHuntingYear() + yearDelta;

            model().newOccupation(club, person, OccupationType.SEURAN_YHDYSHENKILO);
            model().newOccupation(group, model().newPerson(), OccupationType.RYHMAN_JASEN);
            model().newOccupation(group, model().newPerson(), OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

            onSavedAndAuthenticated(createUser(person), () -> {
                HuntingClubAreaCopyDTO dto = new HuntingClubAreaCopyDTO();
                dto.setId(area.getId());
                dto.setHuntingYear(newYear);
                dto.setCopyGroups(copyGroups);

                final HuntingClubAreaDTO savedDto = crudFeature.copy(dto);
                assertAreaEquals(area.getId(), savedDto.getId());
                assertGroups(club, copyGroups ? 2 : 1);
            });
        });
    }

    private void assertAreaEquals(Long originalId, Long copyId) {
        runInTransaction(() -> {
            final HuntingClubArea original = huntingClubAreaRepository.getOne(originalId);
            final HuntingClubArea copy = huntingClubAreaRepository.getOne(copyId);

            if (original.getHuntingYear() == copy.getHuntingYear()) {
                assertFalse(copy.getNameFinnish().equals(original.getNameFinnish()));
                assertFalse(copy.getNameSwedish().equals(original.getNameSwedish()));
                assertTrue(copy.getNameFinnish().startsWith(original.getNameFinnish()));
                assertTrue(copy.getNameSwedish().startsWith(original.getNameSwedish()));
            } else {
                assertEquals(original.getNameFinnish(), copy.getNameFinnish());
                assertEquals(original.getNameSwedish(), copy.getNameSwedish());
            }

            assertEquals(original.getClub(), copy.getClub());

            // For some reason structure of zone_palsta table is different between
            // h2 and pg, so we can't actually insert to zone_palsta and then copy them.
        });
    }

    private void assertGroups(HuntingClub club, int expectedCount) {
        runInTransaction(() -> assertEquals(expectedCount, groupRepository.findByParentOrganisation(club).size()));
    }
}
