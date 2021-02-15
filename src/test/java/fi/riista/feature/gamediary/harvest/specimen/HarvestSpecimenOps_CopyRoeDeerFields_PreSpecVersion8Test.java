package fi.riista.feature.gamediary.harvest.specimen;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static fi.riista.feature.gamediary.GameAge.ADULT;
import static fi.riista.feature.gamediary.GameGender.MALE;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.ADULT_FEMALE;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.ADULT_MALE;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.ANTLERS_LOST;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.YOUNG_FEMALE;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.YOUNG_MALE;
import static fi.riista.util.DateUtil.huntingYear;

// Tests covering specVersions before 8.
// This class can be removed as a whole when min specVersion is upgraded to 8.
@RunWith(Theories.class)
public class HarvestSpecimenOps_CopyRoeDeerFields_PreSpecVersion8Test extends HarvestSpecimenOpsTestBase {

    @DataPoints("versions")
    public static final HarvestSpecVersion[] SPEC_VERSIONS = {
            HarvestSpecVersion._3, HarvestSpecVersion._4, HarvestSpecVersion._5, HarvestSpecVersion._6,
            HarvestSpecVersion._7
    };

    private static final int ROE_DEER_CODE = GameSpecies.OFFICIAL_CODE_ROE_DEER;

    @Theory
    public void testCopyContentToEntity_startingFrom2020(final HarvestSpecVersion version) {
        Stream.of(ADULT_MALE, ADULT_FEMALE, YOUNG_MALE, YOUNG_FEMALE).forEach(specimenType -> {

            IntStream.rangeClosed(2020, huntingYear()).forEach(huntingYear -> {

                // Populate the generic weight field to test that it is cleared and that estimatedWeight
                // is populated instead.
                specimen.setWeight(0.0);

                final HarvestSpecimenDTO dto = createDTO(ROE_DEER_CODE, specimenType, version, huntingYear);

                createOps(ROE_DEER_CODE, version, huntingYear).copyContentToEntity(dto, specimen);

                HarvestSpecimenAssertionBuilder.builder()
                        .onlyAgeGenderAndWeightEstimatedPresent()
                        .withWeightEstimated(dto.getWeight())
                        .withAgeAndGender(specimenType.getAge(), specimenType.getGender())
                        .verify(specimen);
            });
        });
    }

    @Theory
    public void testCopyContentToEntity_protectionOfAntlerFields_adultMale(final HarvestSpecVersion version) {
        IntStream.rangeClosed(2020, huntingYear()).forEach(huntingYear -> {

            // Populate entity with adult male fields to ensure that they are not changed.
            populateEntity(ROE_DEER_CODE, ADULT_MALE);

            final HarvestSpecimenDTO dto = createDTO(ROE_DEER_CODE, ADULT_MALE, version, huntingYear);

            createOps(ROE_DEER_CODE, version, huntingYear).copyContentToEntity(dto, specimen);

            HarvestSpecimenAssertionBuilder.builder()
                    .roeDeerAdultMaleFieldsPresent()
                    .verify(specimen);
        });
    }

    @Theory
    public void testCopyContentToEntity_protectionOfAntlerFields_antlersLost(final HarvestSpecVersion version) {
        IntStream.rangeClosed(2020, huntingYear()).forEach(huntingYear -> {

            // Populate entity with adult male fields to ensure that they are not changed.
            populateEntity(ROE_DEER_CODE, ANTLERS_LOST);

            final HarvestSpecimenDTO dto = createDTO(ROE_DEER_CODE, ADULT_MALE, version, huntingYear);

            createOps(ROE_DEER_CODE, version, huntingYear).copyContentToEntity(dto, specimen);

            HarvestSpecimenAssertionBuilder.builder()
                    .roeDeerAntlersLostFieldsPresent()
                    .verify(specimen);
        });
    }

    @Theory
    public void testCopyContentToEntity_protectionOfExtensionFields_nonAdultMale(final HarvestSpecVersion version) {
        Stream.of(ADULT_FEMALE, YOUNG_MALE, YOUNG_FEMALE).forEach(specimenType -> {

            IntStream.rangeClosed(2020, huntingYear()).forEach(huntingYear -> {

                populateEntity(ROE_DEER_CODE, specimenType);

                final HarvestSpecimenDTO dto = createDTO(ROE_DEER_CODE, specimenType, version, huntingYear);

                createOps(ROE_DEER_CODE, version, huntingYear).copyContentToEntity(dto, specimen);

                HarvestSpecimenAssertionBuilder.builder()
                        .roeDeerCommonFieldsPresent()
                        .antlerFieldsAbsent()
                        .verify(specimen);
            });
        });
    }

    @Theory
    public void testCopyContentToDTO(final HarvestSpecVersion version) {
        Stream.of(ADULT_MALE, ADULT_FEMALE, YOUNG_MALE, YOUNG_FEMALE).forEach(specimenType -> {

            IntStream.rangeClosed(2019, huntingYear()).forEach(huntingYear -> {

                populateEntity(ROE_DEER_CODE, specimenType);
                specimen.setWeight(weight());
                specimen.setWeightEstimated(weight());

                final HarvestSpecimenDTO dto = new HarvestSpecimenDTO();

                createOps(ROE_DEER_CODE, version, huntingYear).copyContentToDTO(specimen, dto);

                HarvestSpecimenAssertionBuilder.builder()
                        .onlyCommonFieldsPresent()
                        .withAgeAndGender(specimenType.getAge(), specimenType.getGender())
                        .withWeight(huntingYear >= 2020 ? specimen.getWeightEstimated() : specimen.getWeight())
                        .verify(dto);
            });
        });
    }

    @Theory
    public void testCopyContentToDTO_antlerFieldsAbsentWhenAntlersLost(final HarvestSpecVersion version) {
        IntStream.rangeClosed(2020, huntingYear()).forEach(huntingYear -> {

            // Populate entity as adult male having antlers lost to test that `antlersLost` field is not carried
            // over because it is not supported in the specVersion range being tested here.
            populateEntity(ROE_DEER_CODE, ANTLERS_LOST);

            final HarvestSpecimenDTO dto = new HarvestSpecimenDTO();

            createOps(ROE_DEER_CODE, version, huntingYear).copyContentToDTO(specimen, dto);

            HarvestSpecimenAssertionBuilder.builder()
                    .onlyCommonFieldsPresent()
                    .withAgeAndGender(ADULT, MALE)
                    .withWeight(specimen.getWeightEstimated())
                    .verify(dto);
        });
    }

    @Theory
    public void testCopyContentToDTO_fallbackToGenericWeight(final HarvestSpecVersion version) {
        Stream.of(ADULT_MALE, ADULT_FEMALE, YOUNG_MALE, YOUNG_FEMALE).forEach(specimenType -> {

            IntStream.rangeClosed(2020, huntingYear()).forEach(huntingYear -> {

                populateEntity(ROE_DEER_CODE, specimenType);
                specimen.setWeight(weight());
                specimen.setWeightEstimated(null);

                final HarvestSpecimenDTO dto = new HarvestSpecimenDTO();

                createOps(ROE_DEER_CODE, version, huntingYear).copyContentToDTO(specimen, dto);

                HarvestSpecimenAssertionBuilder.builder()
                        .onlyCommonFieldsPresent()
                        .withAgeAndGender(specimenType.getAge(), specimenType.getGender())
                        .withWeight(specimen.getWeight())
                        .verify(dto);
            });
        });
    }
}
