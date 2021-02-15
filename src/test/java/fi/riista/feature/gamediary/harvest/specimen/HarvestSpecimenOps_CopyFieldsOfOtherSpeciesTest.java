package fi.riista.feature.gamediary.harvest.specimen;

import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import static fi.riista.feature.gamediary.GameAge.ADULT;
import static fi.riista.feature.gamediary.GameGender.MALE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BEAR;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_MOOSE;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.ADULT_MALE;
import static fi.riista.feature.gamediary.harvest.HarvestSpecVersion.MOST_RECENT;

@RunWith(Theories.class)
public class HarvestSpecimenOps_CopyFieldsOfOtherSpeciesTest extends HarvestSpecimenOpsTestBase {

    @Theory
    public void testCopyContentToEntity_whenSpeciesNotRelevantToMooselikeExtensionFields(final HarvestSpecVersion version) {
        // Populate entity with all moose fields to ensure they are cleared within copying.
        populateEntity(OFFICIAL_CODE_MOOSE, ADULT_MALE);

        // Populate DTO with all moose fields in order to ensure that they are not copied to entity.
        final HarvestSpecimenDTO dto = createDTO(OFFICIAL_CODE_MOOSE, ADULT_MALE, MOST_RECENT, 2020);
        dto.setWeight(weight());

        createOps(OFFICIAL_CODE_BEAR, version, 2020).copyContentToEntity(dto, specimen);

        HarvestSpecimenAssertionBuilder.builder()
                .onlyCommonFieldsPresent()
                .withAgeAndGender(dto.getAge(), dto.getGender())
                .withWeight(dto.getWeight())
                .verify(specimen);
    }

    @Theory
    public void testCopyContentToDTO_whenSpeciesNotRelevantToMooselikeExtensionFields(final HarvestSpecVersion version) {
        // Populate entity with all moose fields to ensure that they are not copied.
        populateEntity(OFFICIAL_CODE_MOOSE, ADULT_MALE);
        specimen.setWeight(weight());

        // Populate DTO with all moose fields to ensure that only relevant fields are set and others cleared.
        final HarvestSpecimenDTO dto = createDTO(OFFICIAL_CODE_MOOSE, ADULT_MALE, MOST_RECENT, 2020);

        createOps(OFFICIAL_CODE_BEAR, version, 2020).copyContentToDTO(specimen, dto);

        HarvestSpecimenAssertionBuilder.builder()
                .onlyCommonFieldsPresent()
                .withAgeAndGender(ADULT, MALE)
                .withWeight(specimen.getWeight())
                .verify(dto);
    }
}
