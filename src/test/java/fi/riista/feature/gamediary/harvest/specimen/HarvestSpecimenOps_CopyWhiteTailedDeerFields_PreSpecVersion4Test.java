package fi.riista.feature.gamediary.harvest.specimen;

import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.util.stream.IntStream;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.ADULT_MALE;
import static fi.riista.util.DateUtil.huntingYear;

// Tests covering specVersions before 4.
// This class can be removed as a whole when min specVersion is upgraded to 4.
@RunWith(Theories.class)
public class HarvestSpecimenOps_CopyWhiteTailedDeerFields_PreSpecVersion4Test
        extends HarvestSpecimenOps_CopyPermitBasedDeerFields_PreSpecVersion4TestBase {

    @Override
    protected int getSpeciesCode() {
        return OFFICIAL_CODE_WHITE_TAILED_DEER;
    }

    @Theory
    public void testCopyContentToEntity_protectionOfExtensionFields_adultMale(final HarvestSpecVersion version) {
        IntStream.rangeClosed(2020, huntingYear()).forEach(huntingYear -> {

            // Populate entity with adult male fields to ensure that they are not changed.
            populateEntity(OFFICIAL_CODE_WHITE_TAILED_DEER, ADULT_MALE, huntingYear);

            final HarvestSpecimenDTO dto = createDTO(OFFICIAL_CODE_WHITE_TAILED_DEER, ADULT_MALE, version, huntingYear);

            createOps(OFFICIAL_CODE_WHITE_TAILED_DEER, version, huntingYear).copyContentToEntity(dto, specimen);

            HarvestSpecimenAssertionBuilder.builder()
                    .whiteTailedDeerAdultMaleFieldsPresent()
                    .verify(specimen);
        });
    }
}
