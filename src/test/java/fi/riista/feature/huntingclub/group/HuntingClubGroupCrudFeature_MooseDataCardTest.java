package fi.riista.feature.huntingclub.group;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.function.BiConsumer;

import static org.junit.Assert.assertFalse;

public class HuntingClubGroupCrudFeature_MooseDataCardTest extends EmbeddedDatabaseTest {

    @Resource
    private HuntingClubGroupCrudFeature feature;

    @Resource
    private HuntingClubGroupRepository repo;

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
            final HarvestPermit groupHarvestPermit = existingGroup.getHarvestPermit();
            final GameSpecies groupGameSpecies = existingGroup.getSpecies();

            callCreate(club, groupHarvestPermit, groupGameSpecies);
        });
    }

    @Test
    public void testCreate_forDeer_whenMooseDataCardExists() {
        withClubAndGroupCreatedFromMooseDataCard((club, existingGroup) -> {
            final HarvestPermit groupHarvestPermit = existingGroup.getHarvestPermit();
            final GameSpecies groupGameSpecies = model().newGameSpecies(GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER);
            model().newHarvestPermitSpeciesAmount(groupHarvestPermit, groupGameSpecies);

            callCreate(club, groupHarvestPermit, groupGameSpecies);
        });
    }

    @Test(expected = CannotModifyMooseDataCardHuntingGroupException.class)
    public void testUpdate_asClubContact() {
        withClubAndGroupCreatedFromMooseDataCard((club, group) -> {
            withPerson(person -> {
                model().newOccupation(group.getParentOrganisation(), person, OccupationType.SEURAN_YHDYSHENKILO);

                onSavedAndAuthenticated(createUser(person), () -> {
                    feature.update(HuntingClubGroupDTO.create(group, group.getSpecies(), group.getHarvestPermit()));
                });
            });
        });
    }

    @Test(expected = CannotModifyMooseDataCardHuntingGroupException.class)
    public void testUpdate_asGroupLeader() {
        withClubAndGroupCreatedFromMooseDataCard((club, group) -> {
            withPerson(person -> {
                model().newOccupation(group.getParentOrganisation(), person, OccupationType.SEURAN_JASEN);
                model().newHuntingClubGroupMember(person, group, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

                onSavedAndAuthenticated(createUser(person), () -> {
                    feature.update(HuntingClubGroupDTO.create(group, group.getSpecies(), group.getHarvestPermit()));
                });
            });
        });
    }

    @Test(expected = CannotModifyMooseDataCardHuntingGroupException.class)
    public void testDelete_asModerator() {
        final HuntingClubGroup group = model().newHuntingClubGroup();
        group.setFromMooseDataCard(true);

        onSavedAndAuthenticated(createNewModerator(), () -> feature.delete(group.getId()));

        assertFalse(repo.exists(group.getId()));
    }

    @Test(expected = CannotModifyMooseDataCardHuntingGroupException.class)
    public void testDelete_asClubContact() {
        final HuntingClubGroup group = model().newHuntingClubGroup();
        group.setFromMooseDataCard(true);

        withPerson(person -> {
            model().newOccupation(group.getParentOrganisation(), person, OccupationType.SEURAN_YHDYSHENKILO);

            onSavedAndAuthenticated(createUser(person), () -> feature.delete(group.getId()));
        });

        assertFalse(repo.exists(group.getId()));
    }
}
