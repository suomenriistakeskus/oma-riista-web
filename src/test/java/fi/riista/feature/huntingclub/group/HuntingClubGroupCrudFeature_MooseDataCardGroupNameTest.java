package fi.riista.feature.huntingclub.group;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.util.LocalisedString;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class HuntingClubGroupCrudFeature_MooseDataCardGroupNameTest extends EmbeddedDatabaseTest {

    @Resource
    private HuntingClubGroupCrudFeature huntingClubGroupCrudFeature;

    @Test
    public void testCreateHuntingGroup_whenUsingGroupNameReservedForMooseDataCardImport() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final GameSpecies species = model().newGameSpeciesMoose();
        final HarvestPermit permit = model().newHarvestPermit(rhy);
        model().newHarvestPermitSpeciesAmount(permit, species);
        final HuntingClub club = model().newHuntingClub(rhy);

        final LocalisedString localisedMooseDataCardGroupName =
                HuntingClubGroup.generateNameForMooseDataCardGroup(prefix -> prefix + permit.getPermitNumber());

        onSavedAndAuthenticated(createNewModerator(), () -> {
            final HuntingClubGroup transientGroup = model().newHuntingClubGroup(club);
            final HuntingClubGroupDTO dto = HuntingClubGroupDTO.create(transientGroup, species, permit);

            // Test that Finnish name reserved for moose data card import is handled case-insensitively.
            dto.setNameFI(capitalizeEveryOtherChar(localisedMooseDataCardGroupName.getFinnish()));
            createGroupAndCheckNameViolations(dto, true);

            // Test that Swedish name (in place of Finnish name field) reserved for moose data
            // card import is handled case-insensitively.
            dto.setNameFI(capitalizeEveryOtherChar(localisedMooseDataCardGroupName.getSwedish()));
            createGroupAndCheckNameViolations(dto, true);

            // Test that English name (in place of Finnish name field) reserved for moose data
            // card import is handled case-insensitively.
            dto.setNameFI(capitalizeEveryOtherChar(localisedMooseDataCardGroupName.getEnglish()));
            createGroupAndCheckNameViolations(dto, true);
        });
    }

    @Test
    public void testUpdateHuntingGroup_whenUsingGroupNameReservedForMooseDataCardImport() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final GameSpecies species = model().newGameSpeciesMoose();
        final HarvestPermit permit = model().newHarvestPermit(rhy);
        final HarvestPermitSpeciesAmount speciesAmount = model().newHarvestPermitSpeciesAmount(permit, species);
        final HuntingClubGroup group = model().newHuntingClubGroup(model().newHuntingClub(rhy), speciesAmount);

        final LocalisedString localisedMooseDataCardGroupName =
                HuntingClubGroup.generateNameForMooseDataCardGroup(prefix -> prefix + permit.getPermitNumber());

        onSavedAndAuthenticated(createNewModerator(), () -> {
            final HuntingClubGroupDTO dto = HuntingClubGroupDTO.create(group, species, permit);

            // Test that Finnish name reserved for moose data card import is handled case-insensitively.
            dto.setNameFI(capitalizeEveryOtherChar(localisedMooseDataCardGroupName.getFinnish()));
            updateGroupAndCheckNameViolations(dto, true);

            // Test that Swedish name (in place of Finnish name field) reserved for moose data
            // card import is handled case-insensitively.
            dto.setNameFI(capitalizeEveryOtherChar(localisedMooseDataCardGroupName.getSwedish()));
            updateGroupAndCheckNameViolations(dto, true);

            // Test that English name (in place of Finnish name field) reserved for moose data
            // card import is handled case-insensitively.
            dto.setNameFI(capitalizeEveryOtherChar(localisedMooseDataCardGroupName.getEnglish()));
            updateGroupAndCheckNameViolations(dto, true);

            // This tests that fromMooseDataCard flag cannot be updated.
            dto.setFromMooseDataCard(true);
            updateGroupAndCheckNameViolations(dto, true);
        });
    }

    private void createGroupAndCheckNameViolations(final HuntingClubGroupDTO dto, final boolean expectExceptionThrown) {
        saveGroupAndCheckNameViolations(dto, expectExceptionThrown, huntingClubGroupCrudFeature::create);
    }

    private void updateGroupAndCheckNameViolations(final HuntingClubGroupDTO dto, final boolean expectExceptionThrown) {
        saveGroupAndCheckNameViolations(dto, expectExceptionThrown, huntingClubGroupCrudFeature::update);
    }

    private static void saveGroupAndCheckNameViolations(
            final HuntingClubGroupDTO dto,
            final boolean expectExceptionThrown,
            final Consumer<HuntingClubGroupDTO> saveAction) {

        try {
            saveAction.accept(dto);

            if (expectExceptionThrown) {
                fail("Should have thrown " + HuntingGroupNameIsReservedException.class.getSimpleName());
            }
        } catch (final HuntingGroupNameIsReservedException e) {
            final String conflictingName = e.getConflictingGroupName();

            if (!expectExceptionThrown) {
                fail(String.format("Exception not expected. Group name '%s' should have passed.", conflictingName));
            }

            assertTrue(conflictingName.equals(dto.getNameFI()) || conflictingName.equals(dto.getNameSV()));
        }
    }

    private static String capitalizeEveryOtherChar(final String str) {
        final StringBuilder buf = new StringBuilder();
        IntStream.range(0, str.length())
                .mapToObj(i -> {
                    final char c = str.charAt(i);
                    return i % 2 == 0 ? c : Character.toUpperCase(c);
                })
                .forEach(buf::append);
        return buf.toString();
    }
}
