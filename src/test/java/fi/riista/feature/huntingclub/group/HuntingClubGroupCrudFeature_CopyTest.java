package fi.riista.feature.huntingclub.group;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.huntingclub.copy.HuntingClubGroupCopyDTO;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HuntingClubGroupCrudFeature_CopyTest extends EmbeddedDatabaseTest {

    @Resource
    private HuntingClubGroupCrudFeature huntingClubGroupCrudFeature;

    @Resource
    private HuntingClubGroupRepository huntingClubGroupRepository;

    @Resource
    private OccupationRepository occupationRepository;

    @Test
    public void testCopySameYear() {
        testCopy(0);
    }

    @Test
    public void testCopyNextYear() {
        testCopy(1);
    }

    private void testCopy(int yearDelta) {
        withMooseHuntingGroupFixture(fixture -> {
            final int newYear = fixture.group.getHuntingYear() + yearDelta;
            final HuntingClubArea newArea = model().newHuntingClubArea(fixture.club);
            newArea.setHuntingYear(newYear);

            onSavedAndAuthenticated(createUser(fixture.clubContact), () -> {
                final HuntingClubGroupCopyDTO dto = new HuntingClubGroupCopyDTO();
                dto.setHuntingYear(newYear);
                dto.setHuntingAreaId(yearDelta == 0 ? fixture.clubArea.getId() : newArea.getId());

                final HuntingClubGroupDTO savedDto = huntingClubGroupCrudFeature.copy(fixture.group.getId(), dto);
                assertGroupEquals(fixture.group.getId(), savedDto.getId());
            });
        });
    }

    private void assertGroupEquals(final long originalId, final long copyId) {
        runInTransaction(() -> {
            final HuntingClubGroup original = huntingClubGroupRepository.getOne(originalId);
            final HuntingClubGroup copy = huntingClubGroupRepository.getOne(copyId);

            if (original.getHuntingYear() == copy.getHuntingYear()) {
                assertFalse(copy.getNameFinnish().equals(original.getNameFinnish()));
                assertFalse(copy.getNameSwedish().equals(original.getNameSwedish()));
                assertTrue(copy.getNameFinnish().startsWith(original.getNameFinnish()));
                assertTrue(copy.getNameSwedish().startsWith(original.getNameSwedish()));
            } else {
                assertEquals(original.getNameFinnish(), copy.getNameFinnish());
                assertEquals(original.getNameSwedish(), copy.getNameSwedish());
            }

            assertOccupations(occupationRepository.findByOrganisation(original), occupationRepository.findByOrganisation(copy));
        });
    }

    private static void assertOccupations(List<Occupation> originals, List<Occupation> copies) {
        assertEquals(originals.size(), copies.size());
        originals.forEach(orig -> {
            assertTrue(copies.stream().anyMatch((copy) ->
                    copy.getOccupationType().equals(orig.getOccupationType()) &&
                            copy.getPerson().getId().equals(orig.getPerson().getId()) &&
                            Objects.equals(copy.getCallOrder(), orig.getCallOrder())
            ));
        });
    }
}
