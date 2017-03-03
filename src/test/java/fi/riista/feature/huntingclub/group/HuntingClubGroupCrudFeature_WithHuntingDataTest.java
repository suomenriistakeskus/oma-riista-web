package fi.riista.feature.huntingclub.group;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.util.DateUtil;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.function.Consumer;

import static fi.riista.util.DateUtil.today;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class HuntingClubGroupCrudFeature_WithHuntingDataTest extends EmbeddedDatabaseTest {

    @Resource
    private HuntingClubGroupCrudFeature huntingClubGroupCrudFeature;

    @Resource
    private HuntingClubGroupRepository huntingClubGroupRepository;

    @Resource
    private HuntingClubGroupDTOTransformer huntingClubGroupDTOTransformer;

    private void withMooseGroupWithHuntingData(final Consumer<Fixture> consumer) {
        final Fixture f = new Fixture();
        f.rhy = model().newRiistanhoitoyhdistys();
        f.club = model().newHuntingClub(f.rhy);

        f.gameSpecies = model().newGameSpeciesMoose();
        f.permit = model().newHarvestPermit(f.rhy);
        f.permit.getPermitPartners().add(f.club);
        f.speciesAmount = model().newHarvestPermitSpeciesAmount(f.permit, f.gameSpecies);

        f.group = model().newHuntingClubGroup(f.club, f.speciesAmount);
        model().newGroupHuntingDay(f.group, today());

        consumer.accept(f);
    }

    private static class Fixture {
        Riistanhoitoyhdistys rhy;
        HuntingClub club;
        GameSpecies gameSpecies;
        HuntingClubGroup group;
        HarvestPermit permit;
        HarvestPermitSpeciesAmount speciesAmount;
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
        withMooseGroupWithHuntingData(f -> {
            final String newNameFinnish = f.group.getNameFinnish() + "CHANGED";
            final String newNameSwedish = f.group.getNameSwedish() + "CHANGED";

            callUpdateWithChanges(f.group, dto -> {
                dto.setNameFI(newNameFinnish);
                dto.setNameSV(newNameSwedish);
            });

            runInTransaction(() -> {
                final HuntingClubGroup updated = huntingClubGroupRepository.getOne(f.group.getId());
                assertEquals(newNameFinnish, updated.getNameFinnish());
                assertEquals(newNameSwedish, updated.getNameSwedish());
            });
        });
    }

    @Test(expected = CannotModifyLockedFieldsForGroupWithHuntingDataException.class)
    public void testUpdate_speciesChangeForbidden() {
        final GameSpecies newSpecies = model().newGameSpecies();

        withMooseGroupWithHuntingData(f -> {
            callUpdateWithChanges(f.group, dto -> {
                dto.setGameSpeciesCode(newSpecies.getOfficialCode());
            });
        });
    }

    @Test(expected = CannotModifyLockedFieldsForGroupWithHuntingDataException.class)
    public void testUpdate_huntingYearChangeForbidden() {
        withMooseGroupWithHuntingData(f -> {
            callUpdateWithChanges(f.group, dto -> {
                dto.setHuntingYear(f.group.getHuntingYear() + 1);
            });
        });
    }

    @Test(expected = CannotModifyLockedFieldsForGroupWithHuntingDataException.class)
    public void testUpdate_huntingAreaChangeForbidden() {
        withMooseGroupWithHuntingData(f -> {
            final HuntingClub club = (HuntingClub) f.group.getParentOrganisation();
            f.group.setHuntingArea(model().newHuntingClubArea(club));
            final HuntingClubArea newArea = model().newHuntingClubArea(club);

            callUpdateWithChanges(f.group, dto -> {
                dto.setHuntingAreaId(newArea.getId());
            });
        });
    }

    @Test(expected = CannotModifyLockedFieldsForGroupWithHuntingDataException.class)
    public void testUpdate_permitChangeForbidden() {
        withMooseGroupWithHuntingData(f-> {
            final HarvestPermit newPermit = model().newHarvestPermit(f.group.getHarvestPermit().getRhy());
            model().newHarvestPermitSpeciesAmount(newPermit, f.group.getSpecies());

            callUpdateWithChanges(f.group, dto -> {
                dto.setPermit(HuntingClubGroupDTO.PermitDTO.create(newPermit));
            });
        });
    }

    @Test(expected = CannotDeleteHuntingGroupWithHuntingDataException.class)
    public void testDelete_asModerator() {
        withMooseGroupWithHuntingData(f -> {
            onSavedAndAuthenticated(createNewModerator(), () -> {
                huntingClubGroupCrudFeature.delete(f.group.getId());
                assertNull(huntingClubGroupRepository.findOne(f.group.getId()));
            });
        });
    }

    @Test(expected = CannotDeleteHuntingGroupWithHuntingDataException.class)
    public void testDelete_asContactPerson() {
        withMooseGroupWithHuntingData(f -> callDeleteAsContactPerson(f.group));
    }

    @Test
    public void testDelete_deerGroupWithOnlyEmptyHuntingDay() {
        withMooseGroupWithHuntingData(f -> {
            f.group.getSpecies().setOfficialCode(GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER);
            callDeleteAsContactPerson(f.group);
        });
    }

    @Test(expected = CannotDeleteHuntingGroupWithHuntingDataException.class)
    public void testDelete_deerGroupWithLinkedDiaryEntries() {
        withMooseGroupWithHuntingData(f -> {
            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(f.group, DateUtil.today().minusDays(1));
            final Harvest harvest = model().newHarvest(f.group.getSpecies());
            harvest.updateHuntingDayOfGroup(huntingDay, harvest.getActor());
            callDeleteAsContactPerson(f.group);
        });
    }

    @Test
    public void testDelete_whenHuntingFinishedButNoHuntingDataOnGroup() {
        withMooseGroupWithHuntingData(f -> {
            final HuntingClubGroup group2 = model().newHuntingClubGroup(f.club, f.speciesAmount);

            // Intermediary flush needed before persisting MooseHuntingSummary in order to have
            // harvest_permit_partners table populated required for foreign key constraint.
            persistInNewTransaction();

            model().newMooseHuntingSummary(f.permit, f.club, true);
            callDeleteAsContactPerson(group2);
        });
    }

    private void callDeleteAsContactPerson(final HuntingClubGroup group) {
        withPerson(person -> {
            model().newOccupation(group.getParentOrganisation(), person, OccupationType.SEURAN_YHDYSHENKILO);

            onSavedAndAuthenticated(createUser(person), () -> {
                huntingClubGroupCrudFeature.delete(group.getId());
                assertNull(huntingClubGroupRepository.findOne(group.getId()));
            });
        });
    }
}
