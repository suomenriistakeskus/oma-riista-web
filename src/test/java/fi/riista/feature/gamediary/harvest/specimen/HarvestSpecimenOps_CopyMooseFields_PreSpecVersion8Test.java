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
import static fi.riista.feature.gamediary.harvest.HarvestSpecVersion.MOST_RECENT;
import static fi.riista.util.DateUtil.huntingYear;

// Tests covering specVersions before 8.
// This class can be removed as a whole when min specVersion is upgraded to 8.
@RunWith(Theories.class)
public class HarvestSpecimenOps_CopyMooseFields_PreSpecVersion8Test extends HarvestSpecimenOpsTestBase {

    @DataPoints("versions")
    public static final HarvestSpecVersion[] SPEC_VERSIONS = {
            HarvestSpecVersion._3, HarvestSpecVersion._4, HarvestSpecVersion._5, HarvestSpecVersion._6,
            HarvestSpecVersion._7
    };

    private static final int MOOSE_CODE = GameSpecies.OFFICIAL_CODE_MOOSE;

    @Theory
    public void testCopyContentToEntity_adultMale(final HarvestSpecVersion version) {
        IntStream.rangeClosed(2020, huntingYear()).forEach(huntingYear -> {

            final HarvestSpecimenDTO dto = createDTO(MOOSE_CODE, ADULT_MALE, version, huntingYear);

            createOps(MOOSE_CODE, version, huntingYear).copyContentToEntity(dto, specimen);

            HarvestSpecimenAssertionBuilder.builder()
                    .mooseAdultMaleFields2015Present()
                    .mooseFields2017EqualTo(dto, version)
                    .verify(specimen);
        });
    }

    @Theory
    public void testCopyContentToEntity_protectionOfAntlerFields_adultMale(final HarvestSpecVersion version) {
        IntStream.rangeClosed(2020, huntingYear()).forEach(huntingYear -> {

            // Populate entity as adult male with MOST_RECENT version to ensure that antler fields are not cleared
            // with old versions.
            populateEntity(MOOSE_CODE, ADULT_MALE);

            final HarvestSpecimenDTO dto = createDTO(MOOSE_CODE, ADULT_MALE, version, huntingYear);

            createOps(MOOSE_CODE, version, huntingYear).copyContentToEntity(dto, specimen);

            HarvestSpecimenAssertionBuilder.builder()
                    .mooseAdultMaleFields2020Present()
                    .mooseFields2017EqualTo(dto, version)
                    .verify(specimen);
        });
    }

    @Theory
    public void testCopyContentToEntity_protectionOfAntlerFields_antlersLost(final HarvestSpecVersion version) {
        IntStream.rangeClosed(2020, huntingYear()).forEach(huntingYear -> {

            // Populate entity as adult male having antlers lost to ensure that antler fields will not be updated.
            populateEntity(MOOSE_CODE, ANTLERS_LOST);

            final HarvestSpecimenDTO dto = createDTO(MOOSE_CODE, ADULT_MALE, version, huntingYear);

            createOps(MOOSE_CODE, version, huntingYear).copyContentToEntity(dto, specimen);

            HarvestSpecimenAssertionBuilder.builder()
                    .mooseAntlersLostFieldsPresent()
                    .mooseFields2017EqualTo(dto, version, false)
                    .verify(specimen);
        });
    }

    @Theory
    public void testCopyContentToEntity_whenChangingYoungMaleToAdult(final HarvestSpecVersion version) {
        IntStream.rangeClosed(2019, huntingYear()).forEach(huntingYear -> {

            // Populate entity as young moose to ensure that `alone` field will be nulled.
            populateEntity(MOOSE_CODE, YOUNG_MALE);

            // Populate DTO with adult male fields and with MOST_RECENT version to assure that only
            // specVersion-enabled fields will be copied to entity.
            final HarvestSpecimenDTO dto = createDTO(MOOSE_CODE, ADULT_MALE, MOST_RECENT, huntingYear);

            createOps(MOOSE_CODE, version, huntingYear).copyContentToEntity(dto, specimen);

            HarvestSpecimenAssertionBuilder.builder()
                    .mooseAdultMaleFields2015Present()
                    .mooseFields2017EqualTo(dto, version)
                    .verify(specimen);
        });
    }

    @Theory
    public void testCopyContentToDTO_adultMale(final HarvestSpecVersion version) {
        IntStream.rangeClosed(2019, huntingYear()).forEach(huntingYear -> {

            populateEntity(MOOSE_CODE, ADULT_MALE);

            final HarvestSpecimenDTO dto = new HarvestSpecimenDTO();

            createOps(MOOSE_CODE, version, huntingYear).copyContentToDTO(specimen, dto);

            HarvestSpecimenAssertionBuilder.builder()
                    .mooseAdultMaleFields2015Present()
                    .mooseFields2017EqualTo(specimen, version)
                    .verify(dto);
        });
    }

    @Theory
    public void testCopyContentToDTO_antlerFieldsAbsentWhenAntlersLost(final HarvestSpecVersion version) {
        IntStream.rangeClosed(2020, huntingYear()).forEach(huntingYear -> {

            // Populate entity as adult male having antlers lost to test that `antlersLost` field is not carried over
            // because it is not supported in the specVersion range being tested here.
            populateEntity(MOOSE_CODE, ANTLERS_LOST);

            final HarvestSpecimenDTO dto = new HarvestSpecimenDTO();

            createOps(MOOSE_CODE, version, huntingYear).copyContentToDTO(specimen, dto);

            HarvestSpecimenAssertionBuilder.builder()
                    .mooseCommonFieldsPresent()
                    .antlerFieldsAbsent()
                    .mooseFields2017EqualTo(specimen, version)
                    .verify(dto);
        });
    }
}
