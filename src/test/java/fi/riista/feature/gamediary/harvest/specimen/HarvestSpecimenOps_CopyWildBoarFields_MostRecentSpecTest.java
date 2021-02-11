package fi.riista.feature.gamediary.harvest.specimen;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.fixture.HarvestSpecimenType;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.util.stream.IntStream;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_MOOSE;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.ADULT_FEMALE;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.ADULT_MALE;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.YOUNG_FEMALE;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.YOUNG_MALE;
import static fi.riista.feature.gamediary.harvest.HarvestSpecVersion.LOWEST_VERSION_SUPPORTING_EXTENDED_WEIGHT_FIELDS_FOR_ROE_DEER_WILD_BOAR;
import static fi.riista.feature.gamediary.harvest.HarvestSpecVersion.MOST_RECENT;
import static fi.riista.util.DateUtil.huntingYear;
import static org.junit.Assume.assumeTrue;

@RunWith(Theories.class)
public class HarvestSpecimenOps_CopyWildBoarFields_MostRecentSpecTest extends HarvestSpecimenOpsTestBase {

    @DataPoints("specimenTypes")
    public static final HarvestSpecimenType[] SPECIMEN_TYPES = {ADULT_MALE, ADULT_FEMALE, YOUNG_MALE, YOUNG_FEMALE};

    private static final int WILD_BOAR_CODE = GameSpecies.OFFICIAL_CODE_WILD_BOAR;

    @Theory
    public void testCopyContentToEntity(final HarvestSpecVersion version, final HarvestSpecimenType specimenType) {
        assumeTrue(version.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_EXTENDED_WEIGHT_FIELDS_FOR_ROE_DEER_WILD_BOAR));

        IntStream.rangeClosed(2020, huntingYear()).forEach(huntingYear -> {

            final HarvestSpecimenDTO dto = createDTO(WILD_BOAR_CODE, specimenType, version, huntingYear);

            createOps(WILD_BOAR_CODE, version, huntingYear).copyContentToEntity(dto, specimen);

            HarvestSpecimenAssertionBuilder.builder()
                    .wildBoarFieldsPresent()
                    .wildBoarFields2020EqualTo(dto)
                    .verify(specimen);
        });
    }

    @Theory
    public void testCopyContentToEntity_before2020(final HarvestSpecVersion version,
                                                   final HarvestSpecimenType specimenType) {
        final int huntingYear = 2019;

        final HarvestSpecimenDTO dto = createDTO(WILD_BOAR_CODE, specimenType, version, huntingYear);

        createOps(WILD_BOAR_CODE, version, huntingYear).copyContentToEntity(dto, specimen);

        HarvestSpecimenAssertionBuilder.builder()
                .onlyCommonFieldsPresent()
                .withWeight(dto.getWeight())
                .withAgeAndGender(specimenType.getAge(), specimenType.getGender())
                .verify(specimen);
    }

    @Theory
    public void testCopyContentToEntity_whenUpdatingFrom2020To2019(final HarvestSpecVersion version,
                                                                   final HarvestSpecimenType specimenType) {

        assumeTrue(version.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_EXTENDED_WEIGHT_FIELDS_FOR_ROE_DEER_WILD_BOAR));

        populateEntity(WILD_BOAR_CODE, specimenType, 2020);

        final HarvestSpecimenDTO dto = createDTO(WILD_BOAR_CODE, specimenType, version, 2019);

        createOps(WILD_BOAR_CODE, version, 2019).copyContentToEntity(dto, specimen);

        HarvestSpecimenAssertionBuilder.builder()
                .onlyCommonFieldsPresent()
                .withWeight(dto.getWeight())
                .withAgeAndGender(specimenType.getAge(), specimenType.getGender())
                .verify(specimen);
    }

    @Theory
    public void testCopyContentToEntity_irrelevantFieldsClearedWhenChangingSpecies(final HarvestSpecVersion version,
                                                                                   final HarvestSpecimenType specimenType) {
        final int currentHuntingYear = huntingYear();

        IntStream.rangeClosed(2019, currentHuntingYear).forEach(huntingYear -> {

            // Populate entity with all moose fields to ensure that moose-only fields are cleared.
            populateEntity(OFFICIAL_CODE_MOOSE, ADULT_MALE);

            // Populate DTO with all moose fields to ensure that only wild boar specific fields are remaining.
            final HarvestSpecimenDTO dto =
                    createDTO(OFFICIAL_CODE_MOOSE, specimenType, MOST_RECENT, currentHuntingYear);

            createOps(WILD_BOAR_CODE, version, huntingYear).copyContentToEntity(dto, specimen);

            HarvestSpecimenAssertionBuilder.builder()
                    .nonWildBoarFieldsAbsent()
                    .verify(specimen);
        });
    }

    @Theory
    public void testCopyContentToDTO_startingFrom2020(final HarvestSpecVersion version,
                                                      final HarvestSpecimenType specimenType) {

        assumeTrue(version.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_EXTENDED_WEIGHT_FIELDS_FOR_ROE_DEER_WILD_BOAR));

        IntStream.rangeClosed(2020, huntingYear()).forEach(huntingYear -> {

            populateEntity(WILD_BOAR_CODE, specimenType);

            final HarvestSpecimenDTO dto = new HarvestSpecimenDTO();

            createOps(WILD_BOAR_CODE, version, huntingYear).copyContentToDTO(specimen, dto);

            HarvestSpecimenAssertionBuilder.builder()
                    .wildBoarFieldsPresent()
                    .wildBoarFields2020EqualTo(specimen)
                    .verify(dto);
        });
    }

    @Theory
    public void testCopyContentToDTO_before2020(final HarvestSpecVersion version,
                                                final HarvestSpecimenType specimenType) {
        final int huntingYear = 2019;

        populateEntity(WILD_BOAR_CODE, specimenType, huntingYear);

        final HarvestSpecimenDTO dto = new HarvestSpecimenDTO();

        createOps(WILD_BOAR_CODE, version, huntingYear).copyContentToDTO(specimen, dto);

        HarvestSpecimenAssertionBuilder.builder()
                .onlyCommonFieldsPresent()
                .withWeight(dto.getWeight())
                .withAgeAndGender(specimenType.getAge(), specimenType.getGender())
                .verify(dto);
    }

    @Theory
    public void testCopyContentToDTO_irrelevantFieldsClearedWhenChangingSpecies(final HarvestSpecVersion version,
                                                                                final HarvestSpecimenType specimenType) {

        assumeTrue(version.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_EXTENDED_WEIGHT_FIELDS_FOR_ROE_DEER_WILD_BOAR));

        IntStream.rangeClosed(2020, huntingYear()).forEach(huntingYear -> {

            populateEntity(WILD_BOAR_CODE, specimenType);

            // Populate DTO initially with moose adult male fields in order to ensure that fields
            // not relevant to wild boar are cleared within copying.
            final HarvestSpecimenDTO dto = createDTO(OFFICIAL_CODE_MOOSE, ADULT_MALE, MOST_RECENT, huntingYear);

            createOps(WILD_BOAR_CODE, version, huntingYear).copyContentToDTO(specimen, dto);

            HarvestSpecimenAssertionBuilder.builder()
                    .wildBoarFieldsPresent()
                    .wildBoarFields2020EqualTo(specimen)
                    .verify(dto);
        });
    }
}
