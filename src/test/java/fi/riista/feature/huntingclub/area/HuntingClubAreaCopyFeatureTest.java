package fi.riista.feature.huntingclub.area;

import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.copy.HuntingClubAreaCopyDTO;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.HuntingClubGroupRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Test;

import javax.annotation.Resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HuntingClubAreaCopyFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private HuntingClubAreaCopyFeature huntingClubAreaCopyFeature;

    @Resource
    private HuntingClubAreaRepository huntingClubAreaRepository;

    @Resource
    private HuntingClubGroupRepository groupRepository;

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

                final HuntingClubAreaDTO savedDto = huntingClubAreaCopyFeature.copy(dto);
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
