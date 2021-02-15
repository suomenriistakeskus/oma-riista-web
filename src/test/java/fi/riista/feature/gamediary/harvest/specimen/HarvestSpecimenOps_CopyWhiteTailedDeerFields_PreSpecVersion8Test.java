package fi.riista.feature.gamediary.harvest.specimen;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.util.stream.IntStream;

import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.ADULT_MALE;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.ANTLERS_LOST;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.YOUNG_MALE;
import static fi.riista.util.DateUtil.huntingYear;

// Tests covering specVersions before 8.
// This class can be removed as a whole when min specVersion is upgraded to 8.
@RunWith(Theories.class)
public class HarvestSpecimenOps_CopyWhiteTailedDeerFields_PreSpecVersion8Test extends HarvestSpecimenOpsTestBase {

    @DataPoints("versions")
    public static final HarvestSpecVersion[] SPEC_VERSIONS = {
            HarvestSpecVersion._4, HarvestSpecVersion._5, HarvestSpecVersion._6, HarvestSpecVersion._7
    };

    private static final int WTD_CODE = GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER;

    @Theory
    public void testCopyContentToEntity_adultMale(final HarvestSpecVersion version) {
        IntStream.rangeClosed(2019, huntingYear()).forEach(huntingYear -> {

            final HarvestSpecimenDTO dto = createDTO(WTD_CODE, ADULT_MALE, version, huntingYear);

            createOps(WTD_CODE, version, huntingYear).copyContentToEntity(dto, specimen);

            HarvestSpecimenAssertionBuilder.builder()
                    .permitBasedDeerAdultMaleFields2016Present()
                    .permitBasedDeerFields2016EqualTo(dto)
                    .verify(specimen);
        });
    }

    @Theory
    public void testCopyContentToEntity_protectionOfAntlerFields_adultMale(final HarvestSpecVersion version) {
        IntStream.rangeClosed(2020, huntingYear()).forEach(huntingYear -> {

            // Populate entity as adult male with MOST_RECENT version to ensure that antler fields are not cleared
            // with old versions.
            populateEntity(WTD_CODE, ADULT_MALE);

            final HarvestSpecimenDTO dto = createDTO(WTD_CODE, ADULT_MALE, version, huntingYear);

            createOps(WTD_CODE, version, huntingYear).copyContentToEntity(dto, specimen);

            HarvestSpecimenAssertionBuilder.builder()
                    .whiteTailedDeerAdultMaleFieldsPresent()
                    .permitBasedDeerFields2016EqualTo(dto)
                    .verify(specimen);
        });
    }

    @Theory
    public void testCopyContentToEntity_protectionOfAntlerFields_antlersLost(final HarvestSpecVersion version) {
        IntStream.rangeClosed(2020, huntingYear()).forEach(huntingYear -> {

            // Populate entity as adult male having antlers lost to ensure that antler fields will not be updated.
            populateEntity(WTD_CODE, ANTLERS_LOST);

            final HarvestSpecimenDTO dto = createDTO(WTD_CODE, ADULT_MALE, version, huntingYear);

            createOps(WTD_CODE, version, huntingYear).copyContentToEntity(dto, specimen);

            HarvestSpecimenAssertionBuilder.builder()
                    .whiteTailedDeerAntlersLostFieldsPresent()
                    .permitBasedDeerFields2016EqualTo(dto, false)
                    .verify(specimen);
        });
    }

    @Theory
    public void testCopyContentToEntity_whenChangingYoungMaleToAdult(final HarvestSpecVersion version) {
        IntStream.rangeClosed(2019, huntingYear()).forEach(huntingYear -> {

            populateEntity(WTD_CODE, YOUNG_MALE);

            final HarvestSpecimenDTO dto = createDTO(WTD_CODE, ADULT_MALE, version, huntingYear);

            createOps(WTD_CODE, version, huntingYear).copyContentToEntity(dto, specimen);

            HarvestSpecimenAssertionBuilder.builder()
                    .permitBasedDeerAdultMaleFields2016Present()
                    .permitBasedDeerFields2016EqualTo(dto)
                    .verify(specimen);
        });
    }

    @Theory
    public void testCopyContentToDTO_adultMale(final HarvestSpecVersion version) {
        IntStream.rangeClosed(2019, huntingYear()).forEach(huntingYear -> {

            populateEntity(WTD_CODE, ADULT_MALE);

            // Setting antlersWidth separately because MOST_RECENT does not support it.
            specimen.setAntlersWidth(nextPositiveIntAtMost(100));

            final HarvestSpecimenDTO dto = new HarvestSpecimenDTO();

            createOps(WTD_CODE, version, huntingYear).copyContentToDTO(specimen, dto);

            HarvestSpecimenAssertionBuilder.builder()
                    .permitBasedDeerAdultMaleFields2016Present()
                    .permitBasedDeerFields2016EqualTo(specimen)
                    .verify(dto);
        });
    }

    @Theory
    public void testCopyContentToDTO_antlerFieldsAbsentWhenAntlersLost(final HarvestSpecVersion version) {
        IntStream.rangeClosed(2020, huntingYear()).forEach(huntingYear -> {

            // Populate entity as adult male having antlers lost to test that `antlersLost` field is not carried over
            // because it is not supported in the specVersion range being tested here.
            populateEntity(WTD_CODE, ANTLERS_LOST);

            final HarvestSpecimenDTO dto = new HarvestSpecimenDTO();

            createOps(WTD_CODE, version, huntingYear).copyContentToDTO(specimen, dto);

            HarvestSpecimenAssertionBuilder.builder()
                    .whiteTailedDeerCommonFieldsPresent()
                    .antlerFieldsAbsent()
                    .permitBasedDeerFields2016EqualTo(specimen)
                    .verify(dto);
        });
    }
}
