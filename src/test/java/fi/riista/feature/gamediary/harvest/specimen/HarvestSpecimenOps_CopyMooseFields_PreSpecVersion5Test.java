package fi.riista.feature.gamediary.harvest.specimen;

import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.fixture.HarvestSpecimenType;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.util.stream.IntStream;

import static fi.riista.feature.gamediary.GameAge.YOUNG;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.ADULT_MALE;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.YOUNG_MALE;
import static fi.riista.feature.gamediary.harvest.HarvestSpecVersion.MOST_RECENT;
import static org.junit.Assume.assumeTrue;
import static fi.riista.util.DateUtil.huntingYear;

// Tests covering specVersions before 5.
// This class can be removed as a whole when min specVersion is upgraded to 5.
@RunWith(Theories.class)
public class HarvestSpecimenOps_CopyMooseFields_PreSpecVersion5Test extends HarvestSpecimenOpsTestBase {

    @DataPoints("versions")
    public static final HarvestSpecVersion[] SPEC_VERSIONS = {HarvestSpecVersion._3, HarvestSpecVersion._4};

    @DataPoints("huntingYears")
    public static final int[] HUNTING_YEAR_DATA_POINT = IntStream.rangeClosed(2019, huntingYear()).toArray();

    private static final int MOOSE_CODE = GameSpecies.OFFICIAL_CODE_MOOSE;

    @Theory
    public void testCopyContentToEntity_young(final HarvestSpecVersion version,
                                              final GameGender gender,
                                              final int huntingYear) {

        assumeTrue(gender != GameGender.UNKNOWN);

        final HarvestSpecimenDTO dto =
                createDTO(MOOSE_CODE, HarvestSpecimenType.fromFields(YOUNG, gender), version, huntingYear);

        createOps(MOOSE_CODE, version, huntingYear).copyContentToEntity(dto, specimen);

        HarvestSpecimenAssertionBuilder.builder()
                .mooseCommonFieldsPresent()
                .aloneAbsent()
                .antlerFieldsAbsent()
                .mooseFields2017EqualTo(dto, version)
                .verify(specimen);
    }

    @Theory
    public void testCopyContentToEntity_whenChangingAdultMaleToYoung(final HarvestSpecVersion version,
                                                                     final int huntingYear) {

        // Populate entity as adult male moose to ensure that antler fields will be cleared.
        populateEntity(MOOSE_CODE, ADULT_MALE);

        final HarvestSpecimenDTO dto = createDTO(MOOSE_CODE, YOUNG_MALE, MOST_RECENT, huntingYear);

        createOps(MOOSE_CODE, version, huntingYear).copyContentToEntity(dto, specimen);

        HarvestSpecimenAssertionBuilder.builder()
                .mooseCommonFieldsPresent()
                .aloneAbsent()
                .antlerFieldsAbsent()
                .mooseFields2017EqualTo(dto, version)
                .verify(specimen);
    }

    @Theory
    public void testCopyContentToDTO_young(final HarvestSpecVersion version,
                                           final GameGender gender,
                                           final int huntingYear) {

        assumeTrue(gender != GameGender.UNKNOWN);

        populateEntity(MOOSE_CODE, HarvestSpecimenType.fromFields(YOUNG, gender));

        final HarvestSpecimenDTO dto = new HarvestSpecimenDTO();

        createOps(MOOSE_CODE, version, huntingYear).copyContentToDTO(specimen, dto);

        HarvestSpecimenAssertionBuilder.builder()
                .mooseCommonFieldsPresent()
                .aloneAbsent()
                .antlerFieldsAbsent()
                .mooseFields2017EqualTo(specimen, version)
                .verify(dto);
    }
}
