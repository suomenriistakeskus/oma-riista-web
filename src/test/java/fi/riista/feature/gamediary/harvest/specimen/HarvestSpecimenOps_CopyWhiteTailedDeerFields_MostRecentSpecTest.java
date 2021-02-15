package fi.riista.feature.gamediary.harvest.specimen;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.util.EnumSet;
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
public class HarvestSpecimenOps_CopyWhiteTailedDeerFields_MostRecentSpecTest extends HarvestSpecimenOpsTestBase {

    @DataPoints("versions")
    public static final HarvestSpecVersion[] SPEC_VERSIONS =
            EnumSet.complementOf(EnumSet.of(HarvestSpecVersion._3)).stream().toArray(HarvestSpecVersion[]::new);

    private static final int WTD_CODE = GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER;

    @Theory
    public void testCopyContentToEntity_adultMale(final HarvestSpecVersion version) {
        assumeTrue(version.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_ANTLER_FIELDS_2020));

        IntStream.rangeClosed(2020, huntingYear()).forEach(huntingYear -> {

            final HarvestSpecimenDTO dto = createDTO(WTD_CODE, ADULT_MALE, version, huntingYear);

            // `antlersWidth` should pass through validation even though it is not supported or copied into entity.
            dto.setAntlersWidth(nextPositiveIntAtMost(100));

            createOps(WTD_CODE, version, huntingYear).copyContentToEntity(dto, specimen);

            HarvestSpecimenAssertionBuilder.builder()
                    .whiteTailedDeerAdultMaleFieldsPresent()
                    .whiteTailedDeerFields2020EqualTo(dto)
                    .verify(specimen);
        });
    }

    @Theory
    public void testCopyContentToEntity_adultMale_before2020(final HarvestSpecVersion version) {
        final int huntingYear = 2019;

        final HarvestSpecimenDTO dto = createDTO(WTD_CODE, ADULT_MALE, version, huntingYear);

        createOps(WTD_CODE, version, huntingYear).copyContentToEntity(dto, specimen);

        HarvestSpecimenAssertionBuilder.builder()
                .permitBasedDeerAdultMaleFields2016Present()
                .permitBasedDeerFields2016EqualTo(dto)
                .verify(specimen);
    }

    @Theory
    public void testCopyContentToEntity_antlersLost(final HarvestSpecVersion version) {
        assumeTrue(version.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_ANTLER_FIELDS_2020));

        IntStream.rangeClosed(2020, huntingYear()).forEach(huntingYear -> {

            final HarvestSpecimenDTO dto = createDTO(WTD_CODE, ANTLERS_LOST, version, huntingYear);

            createOps(WTD_CODE, version, huntingYear).copyContentToEntity(dto, specimen);

            HarvestSpecimenAssertionBuilder.builder()
                    .whiteTailedDeerAntlersLostFieldsPresent()
                    .whiteTailedDeerFields2020EqualTo(dto)
                    .verify(specimen);
        });
    }

    @Theory
    public void testCopyContentToEntity_nonAdultMale(final HarvestSpecVersion version) {
        Stream.of(ADULT_FEMALE, YOUNG_MALE, YOUNG_FEMALE).forEach(specimenType -> {

            IntStream.rangeClosed(2019, huntingYear()).forEach(huntingYear -> {

                final HarvestSpecimenDTO dto = createDTO(WTD_CODE, specimenType, version, huntingYear);

                createOps(WTD_CODE, version, huntingYear).copyContentToEntity(dto, specimen);

                HarvestSpecimenAssertionBuilder.builder()
                        .whiteTailedDeerCommonFieldsPresent()
                        .antlerFieldsAbsent()
                        .whiteTailedDeerFields2020EqualTo(dto)
                        .verify(specimen);
            });
        });
    }

    @Theory
    public void testCopyContentToEntity_whenChangingYoungMaleToAdult(final HarvestSpecVersion version) {
        assumeTrue(version.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_ANTLER_FIELDS_2020));

        IntStream.rangeClosed(2020, huntingYear()).forEach(huntingYear -> {

            populateEntity(WTD_CODE, YOUNG_MALE);

            final HarvestSpecimenDTO dto = createDTO(WTD_CODE, ADULT_MALE, version, huntingYear);

            createOps(WTD_CODE, version, huntingYear).copyContentToEntity(dto, specimen);

            HarvestSpecimenAssertionBuilder.builder()
                    .whiteTailedDeerAdultMaleFieldsPresent()
                    .whiteTailedDeerFields2020EqualTo(dto)
                    .verify(specimen);
        });
    }

    @Theory
    public void testCopyContentToEntity_whenChangingAdultMaleToYoung(final HarvestSpecVersion version) {
        IntStream.rangeClosed(2019, huntingYear()).forEach(huntingYear -> {

            // Populate entity as adult male to ensure that antler fields will be cleared.
            populateEntity(WTD_CODE, ADULT_MALE);

            final HarvestSpecimenDTO dto = createDTO(WTD_CODE, YOUNG_MALE, version, huntingYear);

            createOps(WTD_CODE, version, huntingYear).copyContentToEntity(dto, specimen);

            HarvestSpecimenAssertionBuilder.builder()
                    .whiteTailedDeerCommonFieldsPresent()
                    .antlerFieldsAbsent()
                    .whiteTailedDeerFields2020EqualTo(dto)
                    .verify(specimen);
        });
    }

    @Theory
    public void testCopyContentToEntity_whenUpdatingToAntlersLost(final HarvestSpecVersion version) {
        assumeTrue(version.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_ANTLER_FIELDS_2020));

        Stream.of(ADULT_MALE, ADULT_FEMALE, YOUNG_MALE, YOUNG_FEMALE).forEach(specimenType -> {

            IntStream.rangeClosed(2020, huntingYear()).forEach(huntingYear -> {

                // Populate entity as non-antlers-lost to ensure that antler detail fields will not be set.
                populateEntity(WTD_CODE, specimenType);

                final HarvestSpecimenDTO dto = createDTO(WTD_CODE, ANTLERS_LOST, version, huntingYear);

                createOps(WTD_CODE, version, huntingYear).copyContentToEntity(dto, specimen);

                HarvestSpecimenAssertionBuilder.builder()
                        .whiteTailedDeerAntlersLostFieldsPresent()
                        .whiteTailedDeerFields2020EqualTo(dto)
                        .verify(specimen);
            });
        });
    }

    @Theory
    public void testCopyContentToEntity_whenUpdatingFrom2020To2019(final HarvestSpecVersion version) {
        assumeTrue(version.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_ANTLER_FIELDS_2020));

        populateEntity(WTD_CODE, ADULT_MALE, 2020);

        final HarvestSpecimenDTO dto = createDTO(WTD_CODE, ADULT_MALE, version, 2019);

        createOps(WTD_CODE, version, 2019).copyContentToEntity(dto, specimen);

        HarvestSpecimenAssertionBuilder.builder()
                .permitBasedDeerAdultMaleFields2016Present()
                .permitBasedDeerFields2016EqualTo(dto)
                .verify(specimen);
    }

    @Theory
    public void testCopyContentToEntity_irrelevantFieldsClearedWhenChangingSpecies(final HarvestSpecVersion version) {
        Stream.of(ADULT_FEMALE, YOUNG_MALE, YOUNG_FEMALE).forEach(specimenType -> {

            IntStream.rangeClosed(2019, huntingYear()).forEach(huntingYear -> {

                // Populate entity with all moose fields to ensure that moose-only fields are cleared.
                populateEntity(OFFICIAL_CODE_MOOSE, ADULT_MALE);

                // Populate DTO with all moose fields to ensure that only wtd-supported fields are copied.
                final HarvestSpecimenDTO dto =
                        createDTO(OFFICIAL_CODE_MOOSE, specimenType, MOST_RECENT, huntingYear);

                createOps(WTD_CODE, version, huntingYear).copyContentToEntity(dto, specimen);

                HarvestSpecimenAssertionBuilder.builder()
                        .whiteTailedDeerCommonFieldsPresent()
                        .antlerFieldsAbsent()
                        .permitBasedDeerFields2016EqualTo(dto)
                        .verify(specimen);
            });
        });
    }

    @Theory
    public void testCopyContentToDTO_adultMale(final HarvestSpecVersion version) {
        assumeTrue(version.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_ANTLER_FIELDS_2020));

        IntStream.rangeClosed(2020, huntingYear()).forEach(huntingYear -> {

            populateEntity(WTD_CODE, ADULT_MALE);

            final HarvestSpecimenDTO dto = new HarvestSpecimenDTO();

            createOps(WTD_CODE, version, huntingYear).copyContentToDTO(specimen, dto);

            HarvestSpecimenAssertionBuilder.builder()
                    .whiteTailedDeerAdultMaleFieldsPresent()
                    .whiteTailedDeerFields2020EqualTo(specimen)
                    .verify(dto);
        });
    }

    @Theory
    public void testCopyContentToDTO_antlersLost(final HarvestSpecVersion version) {
        assumeTrue(version.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_ANTLER_FIELDS_2020));

        IntStream.rangeClosed(2020, huntingYear()).forEach(huntingYear -> {

            populateEntity(WTD_CODE, ANTLERS_LOST);

            final HarvestSpecimenDTO dto = new HarvestSpecimenDTO();

            createOps(WTD_CODE, version, huntingYear).copyContentToDTO(specimen, dto);

            HarvestSpecimenAssertionBuilder.builder()
                    .whiteTailedDeerAntlersLostFieldsPresent()
                    .whiteTailedDeerFields2020EqualTo(specimen)
                    .verify(dto);
        });
    }

    @Theory
    public void testCopyContentToDTO_nonAdultMale(final HarvestSpecVersion version) {
        Stream.of(ADULT_FEMALE, YOUNG_MALE, YOUNG_FEMALE).forEach(specimenType -> {

            IntStream.rangeClosed(2019, huntingYear()).forEach(huntingYear -> {

                populateEntity(WTD_CODE, specimenType);

                final HarvestSpecimenDTO dto = new HarvestSpecimenDTO();

                createOps(WTD_CODE, version, huntingYear).copyContentToDTO(specimen, dto);

                HarvestSpecimenAssertionBuilder.builder()
                        .whiteTailedDeerCommonFieldsPresent()
                        .antlerFieldsAbsent()
                        .whiteTailedDeerFields2020EqualTo(specimen)
                        .verify(dto);
            });
        });
    }

    @Theory
    public void testCopyContentToDTO_irrelevantFieldsClearedWhenChangingSpecies(final HarvestSpecVersion version) {
        Stream.of(ADULT_MALE, ADULT_FEMALE, YOUNG_MALE, YOUNG_FEMALE).forEach(specimenType -> {

            IntStream.rangeClosed(2019, huntingYear()).forEach(huntingYear -> {

                populateEntity(WTD_CODE, specimenType);

                // Populate DTO initially with moose adult male fields in order to ensure that fields
                // not relevant to white-tailed deer are cleared within copying.
                final HarvestSpecimenDTO dto =
                        createDTO(OFFICIAL_CODE_MOOSE, ADULT_MALE, MOST_RECENT, huntingYear);

                createOps(WTD_CODE, version, huntingYear).copyContentToDTO(specimen, dto);

                HarvestSpecimenAssertionBuilder.builder()
                        .whiteTailedDeerCommonFieldsPresent()
                        .permitBasedDeerFields2016EqualTo(specimen, specimenType != ADULT_MALE)
                        .verify(dto);
            });
        });
    }
}
