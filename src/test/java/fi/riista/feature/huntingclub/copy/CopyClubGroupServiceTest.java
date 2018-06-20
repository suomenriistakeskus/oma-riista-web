package fi.riista.feature.huntingclub.copy;

import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.ContactInfoShare;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CopyClubGroupServiceTest extends EmbeddedDatabaseTest implements HuntingGroupFixtureMixin {

    @Resource
    private CopyClubGroupService service;

    @Resource
    private OccupationRepository occupationRepository;

    @Test
    public void testCopyToSameYear() {
        final HuntingClub club = model().newHuntingClub();
        final HuntingClubArea area = model().newHuntingClubArea(club);
        final HuntingClubGroup group = model().newHuntingClubGroup(club);
        final Occupation occ = model().newHuntingClubGroupMember(group, OccupationType.RYHMAN_JASEN);
        occ.setContactInfoShare(ContactInfoShare.ALL_MEMBERS);

        onSavedAndAuthenticated(createNewModerator(), tx(() -> {
            final HuntingClubGroupCopyDTO dto = new HuntingClubGroupCopyDTO();
            dto.setHuntingYear(group.getHuntingYear());
            dto.setHuntingAreaId(area.getId());

            final HuntingClubGroup newGroup = service.copy(group.getId(), dto);

            assertGroupEquals(group, newGroup);
            assertOccupationExists(newGroup, occ);
        }));
    }

    private static void assertGroupEquals(HuntingClubGroup group, HuntingClubGroup newGroup) {
        assertTrue(newGroup.getNameFinnish().startsWith(group.getNameFinnish()));
        assertTrue(newGroup.getNameSwedish().startsWith(group.getNameSwedish()));
        assertEquals(group.getSpecies(), newGroup.getSpecies());
        assertEquals(group.getHuntingYear(), newGroup.getHuntingYear());
    }

    private void assertOccupationExists(HuntingClubGroup group, Occupation expected) {
        final List<Occupation> occupations = occupationRepository.findByOrganisation(group);
        assertEquals(1, occupations.size());

        final Occupation occ = occupations.get(0);

        assertEquals(expected.getPerson(), occ.getPerson());
        assertNotNull(expected.getContactInfoShare());
        assertEquals(expected.getContactInfoShare(), occ.getContactInfoShare());
    }
}
