package fi.riista.feature.huntingclub.group;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitLockedByDateService;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.F;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;

import static fi.riista.util.DateUtil.today;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class HuntingClubGroupCrudFeatureTest extends EmbeddedDatabaseTest implements HuntingGroupFixtureMixin {

    @Resource
    private HuntingClubGroupCrudFeature feature;

    @Resource
    private HuntingClubGroupRepository repo;

    @Resource
    private HuntingClubGroupDTOTransformer transformer;

    @Resource
    private HarvestPermitLockedByDateService harvestPermitLockedByDateService;

    @Before
    public void disablePermitLockByDate() {
        harvestPermitLockedByDateService.disableLockingForTests();
    }

    @After
    public void enablePermitLockByDate() {
        harvestPermitLockedByDateService.normalLocking();
    }

    @Test
    public void testUpdateHuntingGroup() {
        withMooseHuntingGroupFixture(fixture -> {
            final GameSpecies newSpecies = model().newDeerSubjectToClubHunting();
            final HuntingClubArea newArea = model().newHuntingClubArea(fixture.club);
            final HarvestPermit newPermit = model().newHarvestPermit(fixture.rhy);
            model().newHarvestPermitSpeciesAmount(newPermit, newSpecies, fixture.group.getHuntingYear() + 1);

            onSavedAndAuthenticated(createUser(fixture.clubContact), () -> {
                final HuntingClubGroupDTO dto = transformer.apply(fixture.group);

                dto.setNameFI(fixture.group.getNameFinnish() + "CHANGED");
                dto.setNameSV(fixture.group.getNameSwedish() + "CHANGED");
                dto.setHuntingYear(fixture.group.getHuntingYear() + 1);
                dto.setGameSpeciesCode(newSpecies.getOfficialCode());
                dto.setHuntingAreaId(newArea.getId());
                dto.setPermit(HuntingClubGroupDTO.PermitDTO.create(newPermit));

                feature.update(dto);

                runInTransaction(() -> {
                    final HuntingClubGroup updated = repo.getOne(fixture.group.getId());

                    assertEquals(dto.getNameFI(), updated.getNameFinnish());
                    assertEquals(dto.getNameSV(), updated.getNameSwedish());
                    assertEquals(dto.getHuntingYear(), updated.getHuntingYear());
                    assertEquals(dto.getHuntingAreaId(), F.getId(updated.getHuntingArea()));

                    assertNotNull(updated.getSpecies());
                    assertEquals(dto.getGameSpeciesCode(), updated.getSpecies().getOfficialCode());

                    assertNotNull(updated.getHarvestPermit());
                    assertEquals(dto.getPermit().getPermitNumber(), updated.getHarvestPermit().getPermitNumber());
                });
            });
        });
    }

    @Test
    public void testDelete_asModerator() {
        final HuntingClubGroup group = model().newHuntingClubGroup();

        onSavedAndAuthenticated(createNewModerator(), () -> {
            feature.delete(group.getId());
            assertFalse(repo.exists(group.getId()));
        });
    }

    @Test
    public void testDelete_asClubContact() {
        withPerson(person -> {
            final HuntingClubGroup group = model().newHuntingClubGroup();
            model().newOccupation(group.getParentOrganisation(), person, OccupationType.SEURAN_YHDYSHENKILO);

            onSavedAndAuthenticated(createUser(person), () -> {
                feature.delete(group.getId());
                assertFalse(repo.exists(group.getId()));
            });
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testDelete_asGroupLeader() {
        withPerson(person -> {
            final HuntingClubGroup group = model().newHuntingClubGroup();
            model().newOccupation(group.getParentOrganisation(), person, OccupationType.SEURAN_JASEN);
            model().newHuntingClubGroupMember(person, group, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

            onSavedAndAuthenticated(createUser(person), () -> feature.delete(group.getId()));
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testDelete_asGroupMember() {
        withPerson(person -> {
            final HuntingClubGroup group = model().newHuntingClubGroup();
            model().newOccupation(group.getParentOrganisation(), person, OccupationType.SEURAN_JASEN);
            model().newHuntingClubGroupMember(person, group, OccupationType.RYHMAN_JASEN);

            onSavedAndAuthenticated(createUser(person), () -> feature.delete(group.getId()));
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testDelete_asClubMember() {
        withPerson(person -> {
            final HuntingClubGroup group = model().newHuntingClubGroup();
            model().newOccupation(group.getParentOrganisation(), person, OccupationType.SEURAN_JASEN);

            onSavedAndAuthenticated(createUser(person), () -> feature.delete(group.getId()));
        });
    }

    @Test
    public void testDeleteWithDeactiveOccupations() {
        withPerson(person -> {
            final HuntingClubGroup group = model().newHuntingClubGroup();
            model().newOccupation(group.getParentOrganisation(), person, OccupationType.SEURAN_YHDYSHENKILO);

            final Occupation o1 = model().newHuntingClubGroupMember(person, group, OccupationType.RYHMAN_JASEN);
            o1.softDelete();

            final Occupation o2 = model().newHuntingClubGroupMember(person, group, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
            o2.setBeginDate(today().minusDays(2));
            o2.setEndDate(today().minusDays(1));

            onSavedAndAuthenticated(createUser(person), () -> feature.delete(group.getId()));
        });
    }
}
