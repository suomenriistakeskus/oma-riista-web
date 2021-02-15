package fi.riista.feature.gamediary.harvest.specimen;

import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theory;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.ADULT_FEMALE;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.ADULT_MALE;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.YOUNG_FEMALE;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.YOUNG_MALE;
import static fi.riista.util.DateUtil.huntingYear;

// Tests covering specVersions before 4.
// This class can be removed as a whole when min specVersion is upgraded to 4.
public abstract class HarvestSpecimenOps_CopyPermitBasedDeerFields_PreSpecVersion4TestBase
        extends HarvestSpecimenOpsTestBase {

    @DataPoints("versions")
    public static final HarvestSpecVersion[] SPEC_VERSIONS = {HarvestSpecVersion._3};

    protected abstract int getSpeciesCode();

    @Theory
    public void testCopyContentToEntity(final HarvestSpecVersion version) {
        final int speciesCode = getSpeciesCode();

        Stream.of(ADULT_MALE, ADULT_FEMALE, YOUNG_MALE, YOUNG_FEMALE).forEach(specimenType -> {

            final int currentHuntingYear = huntingYear();

            IntStream.rangeClosed(2019, currentHuntingYear).forEach(huntingYear -> {

                // Populate the generic weight field to test that it is cleared and that estimatedWeight
                // is populated instead.
                specimen.setWeight(0.0);

                final HarvestSpecimenDTO dto = createDTO(speciesCode, specimenType, version, huntingYear);

                createOps(speciesCode, version, huntingYear).copyContentToEntity(dto, specimen);

                HarvestSpecimenAssertionBuilder.builder()
                        .onlyAgeGenderAndWeightEstimatedPresent()
                        .withAgeAndGender(specimenType.getAge(), specimenType.getGender())
                        .withWeightEstimated(dto.getWeight())
                        .verify(specimen);
            });
        });
    }

    @Theory
    public void testCopyContentToEntity_protectionOfExtensionFields_adultMale_fallowDeerOrWildForestReindeer(final HarvestSpecVersion version) {
        final int speciesCode = getSpeciesCode();

        if (speciesCode != OFFICIAL_CODE_WHITE_TAILED_DEER) {
            IntStream.rangeClosed(2020, huntingYear()).forEach(huntingYear -> {

                // Populate entity with adult male fields to ensure that they are not changed.
                populateEntity(speciesCode, ADULT_MALE, huntingYear);

                final HarvestSpecimenDTO dto = createDTO(speciesCode, ADULT_MALE, version, huntingYear);

                createOps(speciesCode, version, huntingYear).copyContentToEntity(dto, specimen);

                HarvestSpecimenAssertionBuilder.builder()
                        .otherDeerAdultMaleFieldsPresent()
                        .verify(specimen);
            });
        }
    }

    @Theory
    public void testCopyContentToEntity_protectionOfExtensionFields_adultMale_before2020(final HarvestSpecVersion version) {
        final int speciesCode = getSpeciesCode();
        final int huntingYear = 2019;

        // Populate entity with adult male fields to ensure that they are not changed.
        populateEntity(speciesCode, ADULT_MALE, huntingYear);

        final HarvestSpecimenDTO dto = createDTO(speciesCode, ADULT_MALE, version, huntingYear);

        createOps(speciesCode, version, huntingYear).copyContentToEntity(dto, specimen);

        HarvestSpecimenAssertionBuilder.builder()
                .permitBasedDeerAdultMaleFields2016Present()
                .verify(specimen);
    }

    @Theory
    public void testCopyContentToEntity_protectionOfExtensionFields_nonAdultMale(final HarvestSpecVersion version) {
        final int speciesCode = getSpeciesCode();

        Stream.of(ADULT_FEMALE, YOUNG_MALE, YOUNG_FEMALE).forEach(specimenType -> {

            IntStream.rangeClosed(2019, huntingYear()).forEach(huntingYear -> {

                populateEntity(speciesCode, specimenType);

                final HarvestSpecimenDTO dto = createDTO(speciesCode, specimenType, version, huntingYear);

                createOps(speciesCode, version, huntingYear).copyContentToEntity(dto, specimen);

                HarvestSpecimenAssertionBuilder.builder()
                        .permitBasedMooselikeCommonFieldsPresent()
                        .antlerFieldsAbsent()
                        .verify(specimen);
            });
        });
    }

    @Theory
    public void testCopyContentToDTO(final HarvestSpecVersion version) {
        final int speciesCode = getSpeciesCode();

        Stream.of(ADULT_MALE, ADULT_FEMALE, YOUNG_MALE, YOUNG_FEMALE).forEach(specimenType -> {

            IntStream.rangeClosed(2019, huntingYear()).forEach(huntingYear -> {

                populateEntity(speciesCode, specimenType);

                final HarvestSpecimenDTO dto = new HarvestSpecimenDTO();

                createOps(speciesCode, version, huntingYear).copyContentToDTO(specimen, dto);

                HarvestSpecimenAssertionBuilder.builder()
                        .onlyCommonFieldsPresent()
                        .withAgeAndGender(specimenType.getAge(), specimenType.getGender())
                        .withWeight(specimen.getWeightEstimated())
                        .verify(dto);
            });
        });
    }

    @Theory
    public void testCopyContentToDTO_fallbackToGenericWeight(final HarvestSpecVersion version) {
        final int speciesCode = getSpeciesCode();

        Stream.of(ADULT_MALE, ADULT_FEMALE, YOUNG_MALE, YOUNG_FEMALE).forEach(specimenType -> {

            IntStream.rangeClosed(2019, huntingYear()).forEach(huntingYear -> {

                populateEntity(speciesCode, specimenType);
                specimen.setWeight(weight());
                specimen.setWeightEstimated(null);

                final HarvestSpecimenDTO dto = new HarvestSpecimenDTO();

                createOps(speciesCode, version, huntingYear).copyContentToDTO(specimen, dto);

                HarvestSpecimenAssertionBuilder.builder()
                        .onlyCommonFieldsPresent()
                        .withAgeAndGender(specimenType.getAge(), specimenType.getGender())
                        .withWeight(specimen.getWeight())
                        .verify(dto);
            });
        });
    }
}
