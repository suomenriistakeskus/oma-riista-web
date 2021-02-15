package fi.riista.feature.gamediary.harvest.specimen;

import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.fixture.HarvestSpecimenType;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static fi.riista.feature.gamediary.GameAge.YOUNG;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.ADULT_FEMALE;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.ADULT_MALE;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.ANTLERS_LOST;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.YOUNG_FEMALE;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.YOUNG_MALE;
import static fi.riista.feature.gamediary.harvest.HarvestSpecVersion.LOWEST_VERSION_SUPPORTING_ANTLER_FIELDS_2020;
import static fi.riista.feature.gamediary.harvest.HarvestSpecVersion.LOWEST_VERSION_SUPPORTING_SOLITARY_CALVES;
import static fi.riista.feature.gamediary.harvest.HarvestSpecVersion.MOST_RECENT;
import static fi.riista.util.DateUtil.huntingYear;
import static org.junit.Assume.assumeTrue;

@RunWith(Theories.class)
public class HarvestSpecimenOps_CopyMooseFields_MostRecentSpecTest extends HarvestSpecimenOpsTestBase {

    private static final int MOOSE_CODE = GameSpecies.OFFICIAL_CODE_MOOSE;

    @Theory
    public void testCopyContentToEntity_adultMale(final HarvestSpecVersion version) {
        assumeTrue(version.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_ANTLER_FIELDS_2020));

        IntStream.rangeClosed(2020, huntingYear()).forEach(huntingYear -> {

            final HarvestSpecimenDTO dto = createDTO(MOOSE_CODE, ADULT_MALE, version, huntingYear);

            createOps(MOOSE_CODE, version, huntingYear).copyContentToEntity(dto, specimen);

            HarvestSpecimenAssertionBuilder.builder()
                    .mooseAdultMaleFields2020Present()
                    .mooseFields2020EqualTo(dto)
                    .verify(specimen);
        });
    }

    @Theory
    public void testCopyContentToEntity_adultMale_before2020(final HarvestSpecVersion version) {
        final int huntingYear = 2019;

        final HarvestSpecimenDTO dto = createDTO(MOOSE_CODE, ADULT_MALE, version, huntingYear);

        createOps(MOOSE_CODE, version, huntingYear).copyContentToEntity(dto, specimen);

        HarvestSpecimenAssertionBuilder.builder()
                .mooseAdultMaleFields2015Present()
                .mooseFields2017EqualTo(dto, version)
                .verify(specimen);
    }

    @Theory
    public void testCopyContentToEntity_antlersLost(final HarvestSpecVersion version) {
        assumeTrue(version.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_ANTLER_FIELDS_2020));

        IntStream.rangeClosed(2020, huntingYear()).forEach(huntingYear -> {

            final HarvestSpecimenDTO dto = createDTO(MOOSE_CODE, ANTLERS_LOST, version, huntingYear);

            createOps(MOOSE_CODE, version, huntingYear).copyContentToEntity(dto, specimen);

            HarvestSpecimenAssertionBuilder.builder()
                    .mooseAntlersLostFieldsPresent()
                    .mooseFields2020EqualTo(dto)
                    .verify(specimen);
        });
    }

    @Theory
    public void testCopyContentToEntity_adultFemale(final HarvestSpecVersion version) {
        IntStream.rangeClosed(2019, huntingYear()).forEach(huntingYear -> {

            final HarvestSpecimenDTO dto = createDTO(MOOSE_CODE, ADULT_FEMALE, version, huntingYear);

            createOps(MOOSE_CODE, version, huntingYear).copyContentToEntity(dto, specimen);

            HarvestSpecimenAssertionBuilder.builder()
                    .mooseAdultFemaleFieldsPresent()
                    .mooseFields2020EqualTo(dto)
                    .verify(specimen);
        });
    }

    @Theory
    public void testCopyContentToEntity_young(final HarvestSpecVersion version, final GameGender gender) {
        assumeTrue(version.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_SOLITARY_CALVES));
        assumeTrue(gender != GameGender.UNKNOWN);

        IntStream.rangeClosed(2019, huntingYear()).forEach(huntingYear -> {
            testCopyContentToEntity_young(version, gender, huntingYear, false);
        });
    }

    @Theory
    public void testCopyContentToEntity_young_whenAloneIsNull(final HarvestSpecVersion version,
                                                              final GameGender gender) {

        assumeTrue(version.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_SOLITARY_CALVES));
        assumeTrue(gender != GameGender.UNKNOWN);

        IntStream.rangeClosed(2019, huntingYear()).forEach(huntingYear -> {
            testCopyContentToEntity_young(version, gender, huntingYear, true);
        });
    }

    private void testCopyContentToEntity_young(final HarvestSpecVersion version,
                                               final GameGender gender,
                                               final int huntingYear,
                                               final boolean testAloneAbsent) {

        final HarvestSpecimenDTO dto =
                createDTO(MOOSE_CODE, HarvestSpecimenType.fromFields(YOUNG, gender), version, huntingYear);

        if (testAloneAbsent) {
            dto.setAlone(null);
        }

        createOps(MOOSE_CODE, version, huntingYear).copyContentToEntity(dto, specimen);

        if (testAloneAbsent) {
            // Done for equality assertion.
            dto.setAlone(false);
        }

        HarvestSpecimenAssertionBuilder.builder()
                .mooseYoungFieldsPresent()
                .mooseFields2020EqualTo(dto)
                .verify(specimen);
    }

    @Theory
    public void testCopyContentToEntity_whenChangingYoungMaleToAdult(final HarvestSpecVersion version) {
        assumeTrue(version.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_ANTLER_FIELDS_2020));

        IntStream.rangeClosed(2020, huntingYear()).forEach(huntingYear -> {

            // Populate entity as young moose to ensure that `alone` field will be nulled.
            populateEntity(MOOSE_CODE, YOUNG_MALE);

            final HarvestSpecimenDTO dto = createDTO(MOOSE_CODE, ADULT_MALE, version, huntingYear);

            createOps(MOOSE_CODE, version, huntingYear).copyContentToEntity(dto, specimen);

            HarvestSpecimenAssertionBuilder.builder()
                    .mooseAdultMaleFields2020Present()
                    .mooseFields2020EqualTo(dto)
                    .verify(specimen);
        });
    }

    @Theory
    public void testCopyContentToEntity_whenChangingAdultMaleToYoung(final HarvestSpecVersion version) {
        assumeTrue(version.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_SOLITARY_CALVES));

        Stream.of(ADULT_MALE, ANTLERS_LOST).forEach(specimenType -> {

            IntStream.rangeClosed(2019, huntingYear()).forEach(huntingYear -> {

                // Populate entity as adult male moose to ensure that antler fields will be cleared.
                populateEntity(MOOSE_CODE, specimenType);

                final HarvestSpecimenDTO dto = createDTO(MOOSE_CODE, YOUNG_MALE, version, huntingYear);

                createOps(MOOSE_CODE, version, huntingYear).copyContentToEntity(dto, specimen);

                HarvestSpecimenAssertionBuilder.builder()
                        .mooseYoungFieldsPresent()
                        .mooseFields2020EqualTo(dto)
                        .verify(specimen);
            });
        });
    }

    @Theory
    public void testCopyContentToEntity_whenUpdatingToAntlersLost(final HarvestSpecVersion version) {
        assumeTrue(version.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_ANTLER_FIELDS_2020));

        Stream.of(ADULT_MALE, ADULT_FEMALE, YOUNG_MALE, YOUNG_FEMALE).forEach(specimenType -> {

            IntStream.rangeClosed(2020, huntingYear()).forEach(huntingYear -> {

                // Populate entity as non-antlers-lost to ensure that antler detail fields will not be set.
                populateEntity(MOOSE_CODE, specimenType);

                final HarvestSpecimenDTO dto = createDTO(MOOSE_CODE, ANTLERS_LOST, version, huntingYear);

                createOps(MOOSE_CODE, version, huntingYear).copyContentToEntity(dto, specimen);

                HarvestSpecimenAssertionBuilder.builder()
                        .mooseAntlersLostFieldsPresent()
                        .mooseFields2020EqualTo(dto)
                        .verify(specimen);
            });
        });
    }

    @Theory
    public void testCopyContentToEntity_whenUpdatingFrom2020To2019(final HarvestSpecVersion version) {
        assumeTrue(version.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_ANTLER_FIELDS_2020));

        populateEntity(MOOSE_CODE, ADULT_MALE, 2020);

        final HarvestSpecimenDTO dto = createDTO(MOOSE_CODE, ADULT_MALE, version, 2019);

        createOps(MOOSE_CODE, version, 2019).copyContentToEntity(dto, specimen);

        HarvestSpecimenAssertionBuilder.builder()
                .mooseAdultMaleFields2015Present()
                .mooseFields2017EqualTo(dto, version)
                .verify(specimen);
    }

    @Theory
    public void testCopyContentToEntity_irrelevantFieldsClearedWhenChangingSpecies(final HarvestSpecVersion version) {
        Stream.of(ADULT_MALE, ADULT_FEMALE, YOUNG_MALE, YOUNG_FEMALE).forEach(specimenType -> {

            IntStream.rangeClosed(2019, huntingYear()).forEach(huntingYear -> {

                // Populate entity initially with white-tailed deer adult male fields in order to ensure that
                // antler fields not allowed for moose are cleared within copying.
                populateEntity(OFFICIAL_CODE_WHITE_TAILED_DEER, ADULT_MALE);

                final HarvestSpecimenDTO dto = createDTO(MOOSE_CODE, specimenType, version, huntingYear);

                createOps(MOOSE_CODE, version, huntingYear).copyContentToEntity(dto, specimen);

                HarvestSpecimenAssertionBuilder.builder()
                        .mooseCommonFieldsPresent()
                        .mooseFields2017EqualTo(dto, version)
                        .verify(specimen);
            });
        });
    }

    @Theory
    public void testCopyContentToDTO_adultMale(final HarvestSpecVersion version) {
        assumeTrue(version.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_ANTLER_FIELDS_2020));

        IntStream.rangeClosed(2020, huntingYear()).forEach(huntingYear -> {

            populateEntity(MOOSE_CODE, ADULT_MALE);

            final HarvestSpecimenDTO dto = new HarvestSpecimenDTO();

            createOps(MOOSE_CODE, version, huntingYear).copyContentToDTO(specimen, dto);

            HarvestSpecimenAssertionBuilder.builder()
                    .mooseAdultMaleFields2020Present()
                    .mooseFields2020EqualTo(specimen)
                    .verify(dto);
        });
    }

    @Theory
    public void testCopyContentToDTO_antlersLost(final HarvestSpecVersion version) {
        assumeTrue(version.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_ANTLER_FIELDS_2020));

        IntStream.rangeClosed(2020, huntingYear()).forEach(huntingYear -> {

            populateEntity(MOOSE_CODE, ANTLERS_LOST);

            final HarvestSpecimenDTO dto = new HarvestSpecimenDTO();

            createOps(MOOSE_CODE, version, huntingYear).copyContentToDTO(specimen, dto);

            HarvestSpecimenAssertionBuilder.builder()
                    .mooseAntlersLostFieldsPresent()
                    .mooseFields2020EqualTo(specimen)
                    .verify(dto);
        });
    }

    @Theory
    public void testCopyContentToDTO_adultFemale(final HarvestSpecVersion version) {
        IntStream.rangeClosed(2019, huntingYear()).forEach(huntingYear -> {

            populateEntity(MOOSE_CODE, ADULT_FEMALE);

            final HarvestSpecimenDTO dto = new HarvestSpecimenDTO();

            createOps(MOOSE_CODE, version, huntingYear).copyContentToDTO(specimen, dto);

            HarvestSpecimenAssertionBuilder.builder()
                    .mooseAdultFemaleFieldsPresent()
                    .mooseFields2020EqualTo(specimen)
                    .verify(dto);
        });
    }

    @Theory
    public void testCopyContentToDTO_young(final HarvestSpecVersion version, final GameGender gender) {
        assumeTrue(version.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_SOLITARY_CALVES));
        assumeTrue(gender != GameGender.UNKNOWN);

        IntStream.rangeClosed(2019, huntingYear()).forEach(huntingYear -> {

            populateEntity(MOOSE_CODE, HarvestSpecimenType.fromFields(YOUNG, gender));

            final HarvestSpecimenDTO dto = new HarvestSpecimenDTO();

            createOps(MOOSE_CODE, version, huntingYear).copyContentToDTO(specimen, dto);

            HarvestSpecimenAssertionBuilder.builder()
                    .mooseYoungFieldsPresent()
                    .mooseFields2020EqualTo(specimen)
                    .verify(dto);
        });
    }

    @Theory
    public void testCopyContentToDTO_irrelevantFieldsClearedWhenChangingSpecies(final HarvestSpecVersion version) {
        Stream.of(ADULT_MALE, ADULT_FEMALE, YOUNG_MALE, YOUNG_FEMALE).forEach(specimenType -> {

            final int currentHuntingYear = huntingYear();

            IntStream.rangeClosed(2019, currentHuntingYear).forEach(huntingYear -> {

                populateEntity(MOOSE_CODE, specimenType);

                // Populate DTO initially with white-tailed adult male deer fields in order to ensure that
                // fields not relevant to moose are cleared within copying.
                final HarvestSpecimenDTO dto =
                        createDTO(OFFICIAL_CODE_WHITE_TAILED_DEER, ADULT_MALE, MOST_RECENT, currentHuntingYear);

                createOps(MOOSE_CODE, version, huntingYear).copyContentToDTO(specimen, dto);

                HarvestSpecimenAssertionBuilder.builder()
                        .mooseCommonFieldsPresent()
                        .mooseFields2017EqualTo(specimen, version)
                        .verify(dto);
            });
        });
    }
}
