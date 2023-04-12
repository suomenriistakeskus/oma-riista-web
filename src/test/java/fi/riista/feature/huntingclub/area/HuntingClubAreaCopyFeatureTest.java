package fi.riista.feature.huntingclub.area;

import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.copy.HuntingClubAreaCopyDTO;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.HuntingClubGroupRepository;
import fi.riista.feature.huntingclub.poi.PoiIdAllocation;
import fi.riista.feature.huntingclub.poi.PoiLocationGroup;
import fi.riista.feature.huntingclub.poi.PointOfInterestType;
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
    public void testCopySameYearExcludingGroupsAndPOIs() {
        testCopy(0, false, false);
    }

    @Test
    public void testCopySameYearExcludingGroupsAndIncludingPOIs() {
        testCopy(0, false, true);
    }

    @Test
    public void testCopySameYearIncludingGroupsAndExcludingPOIs() {
        testCopy(0, true, false);
    }

    @Test
    public void testCopyNextYearExcludingGroupsAndExcludingPOIs() {
        testCopy(1, false, false);
    }

    @Test
    public void testCopyNextYearExcludingGroupsAndIncludingPOIs() {
        testCopy(1, false, true);
    }

    @Test
    public void testCopyNextYearIncludingGroupsAndExcludingPOIs() {
        testCopy(1, true, false);
    }

    private void testCopy(final int yearDelta, final boolean copyGroups, final boolean copyPOIs) {
        withPerson(person -> {
            final HuntingClub club = model().newHuntingClub();
            final HuntingClubArea area = model().newHuntingClubArea(club);
            final HuntingClubGroup group = model().newHuntingClubGroup(club);
            group.setHuntingYear(area.getHuntingYear());
            group.setHuntingArea(area);

            final PoiIdAllocation POIIdAllocation = model().newPoiIdAllocation(club);
            final PoiLocationGroup POILocationGroup = model().newPoiLocationGroup(POIIdAllocation, PointOfInterestType.OTHER);
            model().newPoiLocation(POILocationGroup);

            area.getPoiLocationGroups().add(POILocationGroup);

            final int newYear = group.getHuntingYear() + yearDelta;

            model().newOccupation(club, person, OccupationType.SEURAN_YHDYSHENKILO);
            model().newOccupation(group, model().newPerson(), OccupationType.RYHMAN_JASEN);
            model().newOccupation(group, model().newPerson(), OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

            onSavedAndAuthenticated(createUser(person), () -> {
                HuntingClubAreaCopyDTO dto = new HuntingClubAreaCopyDTO();
                dto.setId(area.getId());
                dto.setHuntingYear(newYear);
                dto.setCopyGroups(copyGroups);
                dto.setCopyPOIs(copyPOIs);

                final HuntingClubAreaDTO savedDto = huntingClubAreaCopyFeature.copy(dto);
                assertAreaEquals(area.getId(), savedDto.getId());
                assertGroups(club, copyGroups ? 2 : 1);
                assertPOIs(savedDto.getId(), copyPOIs ? 1 : 0);
            });
        });
    }

    private void assertAreaEquals(final Long originalId, final Long copyId) {
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

    private void assertGroups(final HuntingClub club, final int expectedCount) {
        runInTransaction(() -> assertEquals(expectedCount, groupRepository.findByParentOrganisation(club).size()));
    }

    private void assertPOIs(final Long areaId, final int expectedCount) {
        runInTransaction(() -> assertEquals(expectedCount, huntingClubAreaRepository.listPois(areaId).size()));
    }
}
