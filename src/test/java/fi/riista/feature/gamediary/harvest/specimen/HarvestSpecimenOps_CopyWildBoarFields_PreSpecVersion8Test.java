package fi.riista.feature.gamediary.harvest.specimen;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.fixture.HarvestSpecimenType;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.util.stream.IntStream;

import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.ADULT_FEMALE;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.ADULT_MALE;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.YOUNG_FEMALE;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.YOUNG_MALE;
import static fi.riista.util.DateUtil.huntingYear;

// Tests covering specVersions before 8.
// This class can be removed as a whole when min specVersion is upgraded to 8.
@RunWith(Theories.class)
public class HarvestSpecimenOps_CopyWildBoarFields_PreSpecVersion8Test extends HarvestSpecimenOpsTestBase {

    @DataPoints("versions")
    public static final HarvestSpecVersion[] SPEC_VERSIONS = {
            HarvestSpecVersion._3, HarvestSpecVersion._4, HarvestSpecVersion._5, HarvestSpecVersion._6,
            HarvestSpecVersion._7
    };

    @DataPoints("specimenTypes")
    public static final HarvestSpecimenType[] SPECIMEN_TYPES = {ADULT_MALE, ADULT_FEMALE, YOUNG_MALE, YOUNG_FEMALE};

    private static final int WILD_BOAR_CODE = GameSpecies.OFFICIAL_CODE_WILD_BOAR;

    @Theory
    public void testCopyContentToEntity_startingFrom2020(final HarvestSpecVersion version,
                                                         final HarvestSpecimenType specimenType) {

        IntStream.rangeClosed(2020, huntingYear()).forEach(huntingYear -> {

            // Populate the generic weight field to test that it is cleared and that estimatedWeight
            // is populated instead.
            specimen.setWeight(0.0);

            final HarvestSpecimenDTO dto = createDTO(WILD_BOAR_CODE, specimenType, version, huntingYear);

            createOps(WILD_BOAR_CODE, version, huntingYear).copyContentToEntity(dto, specimen);

            HarvestSpecimenAssertionBuilder.builder()
                    .onlyAgeGenderAndWeightEstimatedPresent()
                    .withWeightEstimated(dto.getWeight())
                    .withAgeAndGender(specimenType.getAge(), specimenType.getGender())
                    .verify(specimen);
        });
    }

    @Theory
    public void testCopyContentToDTO(final HarvestSpecVersion version, final HarvestSpecimenType specimenType) {
        IntStream.rangeClosed(2019, huntingYear()).forEach(huntingYear -> {

            populateEntity(WILD_BOAR_CODE, specimenType);
            specimen.setWeight(weight());
            specimen.setWeightEstimated(weight());

            final HarvestSpecimenDTO dto = new HarvestSpecimenDTO();

            createOps(WILD_BOAR_CODE, version, huntingYear).copyContentToDTO(specimen, dto);

            HarvestSpecimenAssertionBuilder.builder()
                    .onlyCommonFieldsPresent()
                    .withAgeAndGender(specimenType.getAge(), specimenType.getGender())
                    .withWeight(huntingYear >= 2020 ? specimen.getWeightEstimated() : specimen.getWeight())
                    .verify(dto);
        });
    }

    @Theory
    public void testCopyContentToDTO_fallbackToGenericWeight(final HarvestSpecVersion version,
                                                             final HarvestSpecimenType specimenType) {

        IntStream.rangeClosed(2020, huntingYear()).forEach(huntingYear -> {

            populateEntity(WILD_BOAR_CODE, specimenType);
            specimen.setWeight(weight());
            specimen.setWeightEstimated(null);

            final HarvestSpecimenDTO dto = new HarvestSpecimenDTO();

            createOps(WILD_BOAR_CODE, version, huntingYear).copyContentToDTO(specimen, dto);

            HarvestSpecimenAssertionBuilder.builder()
                    .onlyCommonFieldsPresent()
                    .withAgeAndGender(specimenType.getAge(), specimenType.getGender())
                    .withWeight(specimen.getWeight())
                    .verify(dto);
        });
    }
}
