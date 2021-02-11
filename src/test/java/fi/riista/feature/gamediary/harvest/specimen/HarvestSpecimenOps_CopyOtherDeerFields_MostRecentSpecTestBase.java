package fi.riista.feature.gamediary.harvest.specimen;

import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theory;

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

public abstract class HarvestSpecimenOps_CopyOtherDeerFields_MostRecentSpecTestBase extends HarvestSpecimenOpsTestBase {

    @DataPoints("versions")
    public static final HarvestSpecVersion[] SPEC_VERSIONS =
            EnumSet.complementOf(EnumSet.of(HarvestSpecVersion._3)).stream().toArray(HarvestSpecVersion[]::new);

    protected abstract int getSpeciesCode();

    @Theory
    public void testCopyContentToEntity_adultMale(final HarvestSpecVersion version) {
        assumeTrue(version.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_ANTLER_FIELDS_2020));

        final int speciesCode = getSpeciesCode();

        IntStream.rangeClosed(2020, huntingYear()).forEach(huntingYear -> {

            final HarvestSpecimenDTO dto = createDTO(speciesCode, ADULT_MALE, version, huntingYear);

            createOps(speciesCode, version, huntingYear).copyContentToEntity(dto, specimen);

            HarvestSpecimenAssertionBuilder.builder()
                    .otherDeerAdultMaleFieldsPresent()
                    .otherDeerFields2020EqualTo(dto)
                    .verify(specimen);
        });
    }

    @Theory
    public void testCopyContentToEntity_adultMale_before2020(final HarvestSpecVersion version) {
        final int speciesCode = getSpeciesCode();
        final int huntingYear = 2019;

        final HarvestSpecimenDTO dto = createDTO(speciesCode, ADULT_MALE, version, huntingYear);

        createOps(speciesCode, version, huntingYear).copyContentToEntity(dto, specimen);

        HarvestSpecimenAssertionBuilder.builder()
                .permitBasedDeerAdultMaleFields2016Present()
                .permitBasedDeerFields2016EqualTo(dto)
                .verify(specimen);
    }

    @Theory
    public void testCopyContentToEntity_antlersLost(final HarvestSpecVersion version) {
        assumeTrue(version.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_ANTLER_FIELDS_2020));

        final int speciesCode = getSpeciesCode();

        IntStream.rangeClosed(2020, huntingYear()).forEach(huntingYear -> {

            final HarvestSpecimenDTO dto = createDTO(speciesCode, ANTLERS_LOST, version, huntingYear);

            createOps(speciesCode, version, huntingYear).copyContentToEntity(dto, specimen);

            HarvestSpecimenAssertionBuilder.builder()
                    .otherDeerAntlersLostFieldsPresent()
                    .otherDeerFields2020EqualTo(dto)
                    .verify(specimen);
        });
    }

    @Theory
    public void testCopyContentToEntity_nonAdultMale(final HarvestSpecVersion version) {
        final int speciesCode = getSpeciesCode();

        Stream.of(ADULT_FEMALE, YOUNG_MALE, YOUNG_FEMALE).forEach(specimenType -> {

            IntStream.rangeClosed(2019, huntingYear()).forEach(huntingYear -> {

                final HarvestSpecimenDTO dto = createDTO(speciesCode, specimenType, version, huntingYear);

                createOps(speciesCode, version, huntingYear).copyContentToEntity(dto, specimen);

                HarvestSpecimenAssertionBuilder.builder()
                        .otherDeerCommonFieldsPresent()
                        .antlerFieldsAbsent()
                        .otherDeerFields2020EqualTo(dto)
                        .verify(specimen);
            });
        });
    }

    @Theory
    public void testCopyContentToEntity_whenChangingYoungMaleToAdult(final HarvestSpecVersion version) {
        assumeTrue(version.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_ANTLER_FIELDS_2020));

        final int speciesCode = getSpeciesCode();

        IntStream.rangeClosed(2020, huntingYear()).forEach(huntingYear -> {

            populateEntity(speciesCode, YOUNG_MALE);

            final HarvestSpecimenDTO dto = createDTO(speciesCode, ADULT_MALE, version, huntingYear);

            createOps(speciesCode, version, huntingYear).copyContentToEntity(dto, specimen);

            HarvestSpecimenAssertionBuilder.builder()
                    .otherDeerAdultMaleFieldsPresent()
                    .otherDeerFields2020EqualTo(dto)
                    .verify(specimen);
        });
    }

    @Theory
    public void testCopyContentToEntity_whenChangingAdultMaleToYoung(final HarvestSpecVersion version) {
        final int speciesCode = getSpeciesCode();

        IntStream.rangeClosed(2019, huntingYear()).forEach(huntingYear -> {

            // Populate entity as adult male to ensure that antler fields will be cleared.
            populateEntity(speciesCode, ADULT_MALE);

            final HarvestSpecimenDTO dto = createDTO(speciesCode, YOUNG_MALE, version, huntingYear);

            createOps(speciesCode, version, huntingYear).copyContentToEntity(dto, specimen);

            HarvestSpecimenAssertionBuilder.builder()
                    .otherDeerCommonFieldsPresent()
                    .antlerFieldsAbsent()
                    .otherDeerFields2020EqualTo(dto)
                    .verify(specimen);
        });
    }

    @Theory
    public void testCopyContentToEntity_whenUpdatingToAntlersLost(final HarvestSpecVersion version) {
        assumeTrue(version.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_ANTLER_FIELDS_2020));

        final int speciesCode = getSpeciesCode();

        Stream.of(ADULT_MALE, ADULT_FEMALE, YOUNG_MALE, YOUNG_FEMALE).forEach(specimenType -> {

            IntStream.rangeClosed(2020, huntingYear()).forEach(huntingYear -> {

                // Populate entity as non-antlers-lost to ensure that antler detail fields will not be set.
                populateEntity(speciesCode, specimenType);

                final HarvestSpecimenDTO dto = createDTO(speciesCode, ANTLERS_LOST, version, huntingYear);

                createOps(speciesCode, version, huntingYear).copyContentToEntity(dto, specimen);

                HarvestSpecimenAssertionBuilder.builder()
                        .otherDeerAntlersLostFieldsPresent()
                        .otherDeerFields2020EqualTo(dto)
                        .verify(specimen);
            });
        });
    }

    @Theory
    public void testCopyContentToEntity_whenUpdatingFrom2020To2019(final HarvestSpecVersion version) {
        assumeTrue(version.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_ANTLER_FIELDS_2020));

        final int speciesCode = getSpeciesCode();

        populateEntity(speciesCode, ADULT_MALE, 2020);

        final HarvestSpecimenDTO dto = createDTO(speciesCode, ADULT_MALE, version, 2019);

        createOps(speciesCode, version, 2019).copyContentToEntity(dto, specimen);

        HarvestSpecimenAssertionBuilder.builder()
                .permitBasedDeerAdultMaleFields2016Present()
                .permitBasedDeerFields2016EqualTo(dto)
                .verify(specimen);
    }

    @Theory
    public void testCopyContentToEntity_irrelevantFieldsClearedWhenChangingSpecies(final HarvestSpecVersion version) {
        Stream.of(ADULT_MALE, ADULT_FEMALE, YOUNG_MALE, YOUNG_FEMALE).forEach(specimenType -> {

            IntStream.rangeClosed(2019, huntingYear()).forEach(huntingYear -> {

                // Populate entity with all moose fields to ensure that moose-only fields are cleared.
                populateEntity(OFFICIAL_CODE_MOOSE, ADULT_MALE);

                // Populate DTO with all moose fields to ensure that only deer species specific fields are copied.
                final HarvestSpecimenDTO dto =
                        createDTO(OFFICIAL_CODE_MOOSE, specimenType, MOST_RECENT, huntingYear);

                createOps(getSpeciesCode(), version, huntingYear).copyContentToEntity(dto, specimen);

                HarvestSpecimenAssertionBuilder.builder()
                        .otherDeerCommonFieldsPresent()
                        .otherDeerFields2020EqualTo(dto)
                        .verify(specimen);
            });
        });
    }

    @Theory
    public void testCopyContentToDTO_adultMale(final HarvestSpecVersion version) {
        assumeTrue(version.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_ANTLER_FIELDS_2020));

        final int speciesCode = getSpeciesCode();

        IntStream.rangeClosed(2020, huntingYear()).forEach(huntingYear -> {

            populateEntity(speciesCode, ADULT_MALE);

            // Populate DTO with all moose fields to ensure that moose-only fields are cleared.
            final HarvestSpecimenDTO dto = createDTO(OFFICIAL_CODE_MOOSE, ADULT_MALE, MOST_RECENT, huntingYear);

            createOps(speciesCode, version, huntingYear).copyContentToDTO(specimen, dto);

            HarvestSpecimenAssertionBuilder.builder()
                    .otherDeerAdultMaleFieldsPresent()
                    .otherDeerFields2020EqualTo(specimen)
                    .verify(dto);
        });
    }

    @Theory
    public void testCopyContentToDTO_antlersLost(final HarvestSpecVersion version) {
        assumeTrue(version.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_ANTLER_FIELDS_2020));

        final int speciesCode = getSpeciesCode();

        IntStream.rangeClosed(2020, huntingYear()).forEach(huntingYear -> {

            populateEntity(speciesCode, ANTLERS_LOST);

            final HarvestSpecimenDTO dto = new HarvestSpecimenDTO();

            createOps(speciesCode, version, huntingYear).copyContentToDTO(specimen, dto);

            HarvestSpecimenAssertionBuilder.builder()
                    .otherDeerAntlersLostFieldsPresent()
                    .otherDeerFields2020EqualTo(specimen)
                    .verify(dto);
        });
    }

    @Theory
    public void testCopyContentToDTO_nonAdultMale(final HarvestSpecVersion version) {
        final int speciesCode = getSpeciesCode();

        Stream.of(ADULT_FEMALE, YOUNG_MALE, YOUNG_FEMALE).forEach(specimenType -> {

            IntStream.rangeClosed(2019, huntingYear()).forEach(huntingYear -> {

                populateEntity(speciesCode, specimenType);

                final HarvestSpecimenDTO dto = new HarvestSpecimenDTO();

                createOps(speciesCode, version, huntingYear).copyContentToDTO(specimen, dto);

                HarvestSpecimenAssertionBuilder.builder()
                        .otherDeerCommonFieldsPresent()
                        .antlerFieldsAbsent()
                        .otherDeerFields2020EqualTo(specimen)
                        .verify(dto);
            });
        });
    }

    @Theory
    public void testCopyContentToDTO_irrelevantFieldsClearedWhenChangingSpecies(final HarvestSpecVersion version) {
        final int speciesCode = getSpeciesCode();

        Stream.of(ADULT_MALE, ADULT_FEMALE, YOUNG_MALE, YOUNG_FEMALE).forEach(specimenType -> {

            IntStream.rangeClosed(2019, huntingYear()).forEach(huntingYear -> {

                populateEntity(speciesCode, specimenType);

                // Populate DTO initially with moose adult male fields in order to ensure that fields
                // not relevant to deer species are cleared within copying.
                final HarvestSpecimenDTO dto = createDTO(OFFICIAL_CODE_MOOSE, ADULT_MALE, MOST_RECENT, huntingYear);

                createOps(speciesCode, version, huntingYear).copyContentToDTO(specimen, dto);

                HarvestSpecimenAssertionBuilder.builder()
                        .otherDeerCommonFieldsPresent()
                        .permitBasedDeerFields2016EqualTo(specimen)
                        .verify(dto);
            });
        });
    }
}
