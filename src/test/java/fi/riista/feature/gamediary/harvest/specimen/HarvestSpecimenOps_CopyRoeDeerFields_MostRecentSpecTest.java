package fi.riista.feature.gamediary.harvest.specimen;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_MOOSE;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.ADULT_FEMALE;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.ADULT_MALE;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.ANTLERS_LOST;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.YOUNG_FEMALE;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.YOUNG_MALE;
import static fi.riista.feature.gamediary.harvest.HarvestSpecVersion.LOWEST_VERSION_SUPPORTING_ANTLER_FIELDS_2020;
import static fi.riista.feature.gamediary.harvest.HarvestSpecVersion.MOST_RECENT;
import static fi.riista.util.DateUtil.huntingYear;
import static org.junit.Assume.assumeTrue;

@RunWith(Theories.class)
public class HarvestSpecimenOps_CopyRoeDeerFields_MostRecentSpecTest extends HarvestSpecimenOpsTestBase {

    private static final int ROE_DEER_CODE = GameSpecies.OFFICIAL_CODE_ROE_DEER;

    @Theory
    public void testCopyContentToEntity_adultMale(final HarvestSpecVersion version) {
        assumeTrue(version.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_ANTLER_FIELDS_2020));

        IntStream.rangeClosed(2020, huntingYear()).forEach(huntingYear -> {

            final HarvestSpecimenDTO dto = createDTO(ROE_DEER_CODE, ADULT_MALE, version, huntingYear);

            createOps(ROE_DEER_CODE, version, huntingYear).copyContentToEntity(dto, specimen);

            HarvestSpecimenAssertionBuilder.builder()
                    .roeDeerAdultMaleFieldsPresent()
                    .roeDeerFields2020EqualTo(dto)
                    .verify(specimen);
        });
    }

    @Theory
    public void testCopyContentToEntity_antlersLost(final HarvestSpecVersion version) {
        assumeTrue(version.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_ANTLER_FIELDS_2020));

        IntStream.rangeClosed(2020, huntingYear()).forEach(huntingYear -> {

            final HarvestSpecimenDTO dto = createDTO(ROE_DEER_CODE, ANTLERS_LOST, version, huntingYear);

            createOps(ROE_DEER_CODE, version, huntingYear).copyContentToEntity(dto, specimen);

            HarvestSpecimenAssertionBuilder.builder()
                    .roeDeerAntlersLostFieldsPresent()
                    .roeDeerFields2020EqualTo(dto)
                    .verify(specimen);
        });
    }

    @Theory
    public void testCopyContentToEntity_nonAdultMale(final HarvestSpecVersion version) {
        assumeTrue(version.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_ANTLER_FIELDS_2020));

        Stream.of(ADULT_FEMALE, YOUNG_FEMALE, YOUNG_MALE).forEach(specimenType -> {

            IntStream.rangeClosed(2020, huntingYear()).forEach(huntingYear -> {

                final HarvestSpecimenDTO dto = createDTO(ROE_DEER_CODE, specimenType, version, huntingYear);

                createOps(ROE_DEER_CODE, version, huntingYear).copyContentToEntity(dto, specimen);

                HarvestSpecimenAssertionBuilder.builder()
                        .roeDeerCommonFieldsPresent()
                        .antlerFieldsAbsent()
                        .roeDeerFields2020EqualTo(dto)
                        .verify(specimen);
            });
        });
    }

    @Theory
    public void testCopyContentToEntity_before2020(final HarvestSpecVersion version) {
        final int huntingYear = 2019;

        Stream.of(ADULT_MALE, ADULT_FEMALE, YOUNG_MALE, YOUNG_FEMALE).forEach(specimenType -> {

            final HarvestSpecimenDTO dto = createDTO(ROE_DEER_CODE, specimenType, version, huntingYear);

            createOps(ROE_DEER_CODE, version, huntingYear).copyContentToEntity(dto, specimen);

            HarvestSpecimenAssertionBuilder.builder()
                    .onlyCommonFieldsPresent()
                    .withWeight(dto.getWeight())
                    .withAgeAndGender(specimenType.getAge(), specimenType.getGender())
                    .verify(specimen);
        });
    }

    @Theory
    public void testCopyContentToEntity_whenChangingYoungMaleToAdult(final HarvestSpecVersion version) {
        assumeTrue(version.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_ANTLER_FIELDS_2020));

        IntStream.rangeClosed(2020, huntingYear()).forEach(huntingYear -> {

            populateEntity(ROE_DEER_CODE, YOUNG_MALE);

            final HarvestSpecimenDTO dto = createDTO(ROE_DEER_CODE, ADULT_MALE, version, huntingYear);

            createOps(ROE_DEER_CODE, version, huntingYear).copyContentToEntity(dto, specimen);

            HarvestSpecimenAssertionBuilder.builder()
                    .roeDeerAdultMaleFieldsPresent()
                    .roeDeerFields2020EqualTo(dto)
                    .verify(specimen);
        });
    }

    @Theory
    public void testCopyContentToEntity_whenChangingAdultMaleToYoung(final HarvestSpecVersion version) {
        assumeTrue(version.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_ANTLER_FIELDS_2020));

        IntStream.rangeClosed(2020, huntingYear()).forEach(huntingYear -> {

            // Populate entity as adult male to ensure that antlers fields will be cleared.
            populateEntity(ROE_DEER_CODE, ADULT_MALE);

            final HarvestSpecimenDTO dto = createDTO(ROE_DEER_CODE, YOUNG_MALE, version, huntingYear);

            createOps(ROE_DEER_CODE, version, huntingYear).copyContentToEntity(dto, specimen);

            HarvestSpecimenAssertionBuilder.builder()
                    .roeDeerCommonFieldsPresent()
                    .antlerFieldsAbsent()
                    .roeDeerFields2020EqualTo(dto)
                    .verify(specimen);
        });
    }

    @Theory
    public void testCopyContentToEntity_whenUpdatingToAntlersLost(final HarvestSpecVersion version) {
        assumeTrue(version.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_ANTLER_FIELDS_2020));

        Stream.of(ADULT_MALE, ADULT_FEMALE, YOUNG_MALE, YOUNG_FEMALE).forEach(specimenType -> {

            IntStream.rangeClosed(2020, huntingYear()).forEach(huntingYear -> {

                // Populate entity as non-antlers-lost to ensure that antler detail fields will not be set.
                populateEntity(ROE_DEER_CODE, specimenType);

                final HarvestSpecimenDTO dto = createDTO(ROE_DEER_CODE, ANTLERS_LOST, version, huntingYear);

                createOps(ROE_DEER_CODE, version, huntingYear).copyContentToEntity(dto, specimen);

                HarvestSpecimenAssertionBuilder.builder()
                        .roeDeerAntlersLostFieldsPresent()
                        .roeDeerFields2020EqualTo(dto)
                        .verify(specimen);
            });
        });
    }

    @Theory
    public void testCopyContentToEntity_whenUpdatingFrom2020To2019(final HarvestSpecVersion version) {
        assumeTrue(version.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_ANTLER_FIELDS_2020));

        Stream.of(ADULT_MALE, ADULT_FEMALE, YOUNG_MALE, YOUNG_FEMALE).forEach(specimenType -> {

            populateEntity(ROE_DEER_CODE, specimenType, 2020);

            final HarvestSpecimenDTO dto = createDTO(ROE_DEER_CODE, specimenType, version, 2019);

            createOps(ROE_DEER_CODE, version, 2019).copyContentToEntity(dto, specimen);

            HarvestSpecimenAssertionBuilder.builder()
                    .onlyCommonFieldsPresent()
                    .withWeight(dto.getWeight())
                    .withAgeAndGender(specimenType.getAge(), specimenType.getGender())
                    .verify(specimen);
        });
    }

    @Theory
    public void testCopyContentToEntity_irrelevantFieldsClearedWhenChangingSpecies(final HarvestSpecVersion version) {
        assumeTrue(version.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_ANTLER_FIELDS_2020));

        Stream.of(ADULT_MALE, ADULT_FEMALE, YOUNG_FEMALE, YOUNG_MALE).forEach(specimenType -> {

            IntStream.rangeClosed(2020, huntingYear()).forEach(huntingYear -> {

                // Populate entity with all moose fields to ensure that moose-only fields are cleared.
                populateEntity(OFFICIAL_CODE_MOOSE, ADULT_MALE);

                // Populate DTO with all moose fields to ensure that only fields relevant to roe deer are copied.
                final HarvestSpecimenDTO dto =
                        createDTO(OFFICIAL_CODE_MOOSE, specimenType, MOST_RECENT, huntingYear);

                createOps(ROE_DEER_CODE, version, huntingYear).copyContentToEntity(dto, specimen);

                HarvestSpecimenAssertionBuilder.builder()
                        .roeDeerCommonFieldsPresent()
                        .roeDeerFields2020EqualTo(dto)
                        .verify(specimen);
            });
        });
    }

    @Theory
    public void testCopyContentToDTO_adultMale(final HarvestSpecVersion version) {
        assumeTrue(version.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_ANTLER_FIELDS_2020));

        IntStream.rangeClosed(2020, huntingYear()).forEach(huntingYear -> {

            populateEntity(ROE_DEER_CODE, ADULT_MALE);

            final HarvestSpecimenDTO dto = new HarvestSpecimenDTO();

            createOps(ROE_DEER_CODE, version, huntingYear).copyContentToDTO(specimen, dto);

            HarvestSpecimenAssertionBuilder.builder()
                    .roeDeerAdultMaleFieldsPresent()
                    .roeDeerFields2020EqualTo(specimen)
                    .verify(dto);
        });
    }

    @Theory
    public void testCopyContentToDTO_antlersLost(final HarvestSpecVersion version) {
        assumeTrue(version.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_ANTLER_FIELDS_2020));

        IntStream.rangeClosed(2020, huntingYear()).forEach(huntingYear -> {

            populateEntity(ROE_DEER_CODE, ANTLERS_LOST);

            final HarvestSpecimenDTO dto = new HarvestSpecimenDTO();

            createOps(ROE_DEER_CODE, version, huntingYear).copyContentToDTO(specimen, dto);

            HarvestSpecimenAssertionBuilder.builder()
                    .roeDeerAntlersLostFieldsPresent()
                    .roeDeerFields2020EqualTo(specimen)
                    .verify(dto);
        });
    }

    @Theory
    public void testCopyContentToDTO_nonAdultMale(final HarvestSpecVersion version) {
        assumeTrue(version.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_ANTLER_FIELDS_2020));

        Stream.of(ADULT_FEMALE, YOUNG_FEMALE, YOUNG_MALE).forEach(specimenType -> {

            IntStream.rangeClosed(2020, huntingYear()).forEach(huntingYear -> {

                populateEntity(ROE_DEER_CODE, specimenType);

                final HarvestSpecimenDTO dto = new HarvestSpecimenDTO();

                createOps(ROE_DEER_CODE, version, huntingYear).copyContentToDTO(specimen, dto);

                HarvestSpecimenAssertionBuilder.builder()
                        .roeDeerCommonFieldsPresent()
                        .antlerFieldsAbsent()
                        .roeDeerFields2020EqualTo(specimen)
                        .verify(dto);
            });
        });
    }

    @Theory
    public void testCopyContentToDTO_irrelevantFieldsClearedWhenChangingSpecies(final HarvestSpecVersion version) {
        assumeTrue(version.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_ANTLER_FIELDS_2020));

        Stream.of(ADULT_MALE, ADULT_FEMALE, YOUNG_MALE, YOUNG_FEMALE).forEach(specimenType -> {

            IntStream.rangeClosed(2020, huntingYear()).forEach(huntingYear -> {

                populateEntity(ROE_DEER_CODE, specimenType);

                // Populate DTO initially with moose adult male fields in order to ensure that fields
                // not relevant to roe deer are cleared within copying.
                final HarvestSpecimenDTO dto = createDTO(OFFICIAL_CODE_MOOSE, ADULT_MALE, MOST_RECENT, huntingYear);

                createOps(ROE_DEER_CODE, version, huntingYear).copyContentToDTO(specimen, dto);

                HarvestSpecimenAssertionBuilder.builder()
                        .roeDeerCommonFieldsPresent()
                        .roeDeerFields2020EqualTo(specimen)
                        .verify(dto);
            });
        });
    }
}
