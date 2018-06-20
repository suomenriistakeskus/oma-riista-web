package fi.riista.feature.huntingclub.group;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.huntingclub.hunting.ClubHuntingFinishedException;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;

public class HuntingClubGroupCrudFeature_HuntingFinishedTest extends EmbeddedDatabaseTest {

    @Resource
    private HuntingClubGroupCrudFeature huntingClubGroupCrudFeature;

    @Resource
    private HuntingClubGroupRepository huntingClubGroupRepository;

    @Resource
    private HuntingClubGroupDTOTransformer huntingClubGroupDTOTransformer;

    private void withGroupWithFinishedHunting(final Consumer<HuntingClubGroup> consumer) {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();

        final HarvestPermit permit = model().newHarvestPermit(rhy);
        final HarvestPermitSpeciesAmount speciesAmount =
                model().newHarvestPermitSpeciesAmount(permit, model().newDeerSubjectToClubHunting());

        final HuntingClub club = model().newHuntingClub(rhy);
        permit.getPermitPartners().add(club);
        final HuntingClubGroup group = model().newHuntingClubGroup(club, speciesAmount);

        model().newBasicHuntingSummary(speciesAmount, club, true);

        consumer.accept(group);
    }

    private void callUpdateWithChanges(final HuntingClubGroup group, final Consumer<HuntingClubGroupDTO> consumer) {
        withPerson(person -> {
            model().newOccupation(group.getParentOrganisation(), person, OccupationType.SEURAN_JASEN);
            model().newHuntingClubGroupMember(person, group, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

            onSavedAndAuthenticated(createUser(person), () -> {
                final HuntingClubGroupDTO dto = huntingClubGroupDTOTransformer.apply(group);
                consumer.accept(dto);
                huntingClubGroupCrudFeature.update(dto);
            });
        });
    }

    @Test
    public void testUpdate_nameChangeAllowed() {
        withGroupWithFinishedHunting(group -> {
            final String newNameFinnish = group.getNameFinnish() + "CHANGED";
            final String newNameSwedish = group.getNameSwedish() + "CHANGED";

            callUpdateWithChanges(group, dto -> {
                dto.setNameFI(newNameFinnish);
                dto.setNameSV(newNameSwedish);
            });

            runInTransaction(() -> {
                final HuntingClubGroup updated = huntingClubGroupRepository.getOne(group.getId());
                assertEquals(newNameFinnish, updated.getNameFinnish());
                assertEquals(newNameSwedish, updated.getNameSwedish());
            });
        });
    }

    @Test(expected = ClubHuntingFinishedException.class)
    public void testUpdate_speciesChangeForbidden() {
        withGroupWithFinishedHunting(group -> {
            final GameSpecies newSpecies = model().newGameSpeciesMoose();

            callUpdateWithChanges(group, dto -> dto.setGameSpeciesCode(newSpecies.getOfficialCode()));
        });
    }

    @Test(expected = ClubHuntingFinishedException.class)
    public void testUpdate_huntingYearChangeForbidden() {
        withGroupWithFinishedHunting(group -> {
            callUpdateWithChanges(group, dto -> dto.setHuntingYear(group.getHuntingYear() + 1));
        });
    }

    @Test(expected = ClubHuntingFinishedException.class)
    public void testUpdateHuntingGroup_whenHuntingFinished_forHuntingAreaChange() {
        withGroupWithFinishedHunting(group -> {
            final HuntingClub club = (HuntingClub) group.getParentOrganisation();

            final HuntingClubArea currentArea = model().newHuntingClubArea(club);
            final HuntingClubArea newArea = model().newHuntingClubArea(club);
            group.setHuntingArea(currentArea);

            callUpdateWithChanges(group, dto -> dto.setHuntingAreaId(newArea.getId()));
        });
    }

    @Test(expected = ClubHuntingFinishedException.class)
    public void testUpdateHuntingGroup_whenHuntingFinished_forPermitChange() {
        withGroupWithFinishedHunting(group -> {
            final HuntingClub club = (HuntingClub) group.getParentOrganisation();
            final Riistanhoitoyhdistys rhy = (Riistanhoitoyhdistys) club.getParentOrganisation();

            final HarvestPermit newPermit = model().newHarvestPermit(rhy);
            model().newHarvestPermitSpeciesAmount(newPermit, group.getSpecies());

            callUpdateWithChanges(group, dto -> dto.setPermit(HuntingClubGroupDTO.PermitDTO.create(newPermit)));
        });
    }
}
