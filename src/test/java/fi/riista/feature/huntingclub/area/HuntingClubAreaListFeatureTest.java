package fi.riista.feature.huntingclub.area;

import com.google.common.primitives.Ints;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class HuntingClubAreaListFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private HuntingClubAreaListFeature huntingClubAreaListFeature;

    @Test
    public void testListHuntingYears() {
        InitData initData = new InitData().persist();

        authenticate(initData.user);

        assertEquals(Ints.asList(2013, 2014, 2015),
                huntingClubAreaListFeature.listHuntingYears(initData.club.getId()));
    }

    @Test
    public void testListByClubAndYear() {
        InitData initData = new InitData().persist();

        authenticate(initData.user);

        assertSizeAndNames(huntingClubAreaListFeature.listByClubAndYear(
                initData.club.getId(), 2013, true, true), 1, "2013");
        assertSizeAndNames(huntingClubAreaListFeature.listByClubAndYear(
                initData.club.getId(), 2014, true, true), 2, "first", "second");
        assertSizeAndNames(huntingClubAreaListFeature.listByClubAndYear(
                initData.club.getId(), 2015, true, true), 1, "2015");
    }

    @Test
    public void testListActiveOnly() {
        InitData initData = new InitData();
        initData.area2013.setActive(false);
        initData.area2014_First.setActive(false);

        initData.persist();

        authenticate(initData.user);

        assertSizeAndNames(huntingClubAreaListFeature.listByClubAndYear(
                initData.club.getId(), 2013, true, true), 0);
        assertSizeAndNames(huntingClubAreaListFeature.listByClubAndYear(
                initData.club.getId(), 2014, true, true), 1, "second");
        assertSizeAndNames(huntingClubAreaListFeature.listByClubAndYear(
                initData.club.getId(), 2015, true, true), 1, "2015");
    }

    @Test
    public void testListOnlyNonEmptyGeometries() {
        InitData initData = new InitData();
        initData.area2013.setActive(false);
        initData.area2014_First.setActive(false);

        initData.area2014_First.setZone(model().newGISZone(9999));
        initData.area2014_Second.setZone(model().newGISZone(10000));
        initData.area2013.setZone(model().newGISZone(0));

        initData.persist();

        authenticate(initData.user);

        assertSizeAndNames(huntingClubAreaListFeature.listByClubAndYear(
                initData.club.getId(), 2013, true, false), 0);
        assertSizeAndNames(huntingClubAreaListFeature.listByClubAndYear(
                initData.club.getId(), 2014, true, false), 1, "second");
        assertSizeAndNames(huntingClubAreaListFeature.listByClubAndYear(
                initData.club.getId(), 2015, true, false), 0);
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
        private HuntingClubArea area2014_First;
        private HuntingClubArea area2014_Second;
        private HuntingClubArea area2015;

        InitData() {
            club = model().newHuntingClub();
            Person member1 = model().newHuntingClubMember(club, OccupationType.SEURAN_YHDYSHENKILO).getPerson();
            user = createUser(member1);

            area2013 = model().newHuntingClubArea(club, "2013", "2013", 2013);
            area2014_First = model().newHuntingClubArea(club, "first", "first", 2014);
            area2014_Second = model().newHuntingClubArea(club, "second", "second", 2014);
            area2015 = model().newHuntingClubArea(club, "2015", "2015", 2015);
        }

        InitData persist() {
            persistInNewTransaction();
            return this;
        }
    }
}
