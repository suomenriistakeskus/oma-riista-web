package fi.riista.feature.huntingclub.group;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.moosedatacard.MooseDataCardImport;
import fi.riista.feature.huntingclub.moosedatacard.MooseDataCardImportRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.storage.metadata.PersistentFileMetadataRepository;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.F;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.function.BiConsumer;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class HuntingClubGroupCrudFeature_MooseDataCardTest extends EmbeddedDatabaseTest {

    @Resource
    private HuntingClubGroupCrudFeature feature;

    @Resource
    private HuntingClubGroupRepository repo;

    @Resource
    private MooseDataCardImportRepository mooseDataCardImportRepo;

    @Resource
    private PersistentFileMetadataRepository fileMetadataRepo;

    private void withClubAndGroupCreatedFromMooseDataCard(final BiConsumer<HuntingClub, HuntingClubGroup> consumer) {
        final GameSpecies species = model().newGameSpeciesMoose();
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final HarvestPermit permit = model().newHarvestPermit(rhy);
        final HarvestPermitSpeciesAmount speciesAmount = model().newHarvestPermitSpeciesAmount(permit, species);

        final HuntingClub club = model().newHuntingClub(rhy);
        final HuntingClubGroup group = model().newHuntingClubGroup(club, speciesAmount);
        group.setFromMooseDataCard(true);

        consumer.accept(club, group);
    }

    private void callCreate(final HuntingClub club,
                            final HarvestPermit groupHarvestPermit,
                            final GameSpecies groupGameSpecies) {
        withPerson(person -> {
            model().newOccupation(club, person, OccupationType.SEURAN_YHDYSHENKILO);

            onSavedAndAuthenticated(createUser(person), () -> {
                final HuntingClubGroup transientGroup = model().newHuntingClubGroup(club);
                feature.create(HuntingClubGroupDTO.create(transientGroup, groupGameSpecies, groupHarvestPermit));
            });
        });
    }

    @Test(expected = CannotCreateManagedGroupWhenMooseDataCardGroupExists.class)
    public void testCreate_forMoose_whenMooseDataCardExists() {
        withClubAndGroupCreatedFromMooseDataCard((club, existingGroup) -> {
            callCreate(club, existingGroup.getHarvestPermit(), existingGroup.getSpecies());
        });
    }

    @Test
    public void testCreate_forDeer_whenMooseDataCardExists() {
        withClubAndGroupCreatedFromMooseDataCard((club, existingGroup) -> {
            final HarvestPermit groupHarvestPermit = existingGroup.getHarvestPermit();
            final GameSpecies groupGameSpecies = model().newDeerSubjectToClubHunting();
            model().newHarvestPermitSpeciesAmount(groupHarvestPermit, groupGameSpecies);

            callCreate(club, groupHarvestPermit, groupGameSpecies);
        });
    }

    @Test(expected = CannotModifyMooseDataCardHuntingGroupException.class)
    public void testUpdate_asClubContact() {
        withClubAndGroupCreatedFromMooseDataCard((club, group) -> withPerson(person -> {
            model().newOccupation(club, person, OccupationType.SEURAN_YHDYSHENKILO);

            onSavedAndAuthenticated(createUser(person), () -> {
                feature.update(HuntingClubGroupDTO.create(group, group.getSpecies(), group.getHarvestPermit()));
            });
        }));
    }

    @Test(expected = CannotModifyMooseDataCardHuntingGroupException.class)
    public void testUpdate_asGroupLeader() {
        withClubAndGroupCreatedFromMooseDataCard((club, group) -> withPerson(person -> {

            model().newOccupation(club, person, OccupationType.SEURAN_JASEN);
            model().newHuntingClubGroupMember(person, group, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

            onSavedAndAuthenticated(createUser(person), () -> {
                feature.update(HuntingClubGroupDTO.create(group, group.getSpecies(), group.getHarvestPermit()));
            });
        }));
    }

    @Test
    public void testDelete_asModerator_whenAllImportsDeleted() {
        testDelete_asModerator(true);
    }

    @Test
    public void testDelete_asModerator_whenSomeImportsNotDeleted() {
        testDelete_asModerator(false);
    }

    private void testDelete_asModerator(final boolean allImportsSoftDeleted) {
        final HuntingClub club = model().newHuntingClub();

        final HuntingClubGroup group = model().newHuntingClubGroup(club);
        group.setFromMooseDataCard(true);

        // These are created to verify that related imports and file metadata entities are deleted
        // as well.
        final MooseDataCardImport import1 = model().newMooseDataCardImport(group);
        model().newMooseDataCardImport(group).softDelete();

        if (allImportsSoftDeleted) {
            import1.softDelete();
        }

        final HuntingClubGroup anotherGroup = model().newHuntingClubGroup(club);
        anotherGroup.setFromMooseDataCard(true);

        final MooseDataCardImport anotherGroupImport = model().newMooseDataCardImport(anotherGroup);

        onSavedAndAuthenticated(createNewModerator(), () -> {

            assertEntityCountBeforeModeratorInvokesGroupDelete();

            try {
                feature.delete(group.getId());

                if (!allImportsSoftDeleted) {
                    fail("Deletion of moose data card group should have failed when non-deleted imports exist");
                }
            } catch (final CannotModifyMooseDataCardHuntingGroupException e) {
                if (allImportsSoftDeleted) {
                    fail("Deletion of moose data card group should have succeeded when all imports are deleted");
                }
            }

            if (allImportsSoftDeleted) {
                assertEquals(asList(anotherGroup), repo.findAll());
                assertEquals(asList(anotherGroupImport), mooseDataCardImportRepo.findAll());
                assertEquals(
                        F.getUniqueIds(anotherGroupImport.getXmlFileMetadata(), anotherGroupImport.getPdfFileMetadata()),
                        F.getUniqueIds(fileMetadataRepo.findAll()));
            } else {
                assertEntityCountBeforeModeratorInvokesGroupDelete();
            }
        });
    }

    private void assertEntityCountBeforeModeratorInvokesGroupDelete() {
        assertEquals(2, repo.count());
        assertEquals(3, mooseDataCardImportRepo.count());
        assertEquals(6, fileMetadataRepo.count());
    }

    @Test
    public void testDelete_asClubContact() {
        withPerson(person -> {
            final HuntingClubGroup group = model().newHuntingClubGroup();
            group.setFromMooseDataCard(true);

            model().newOccupation(group.getParentOrganisation(), person, OccupationType.SEURAN_YHDYSHENKILO);

            onSavedAndAuthenticated(createUser(person), () -> {
                try {
                    feature.delete(group.getId());
                    fail("Club contact person should not be able to delete a moose data card group");
                } catch (final CannotModifyMooseDataCardHuntingGroupException e) {
                    // Expected
                }
                assertTrue(repo.existsById(group.getId()));
            });
        });
    }
}
