package fi.riista.feature.gamediary.harvest.specimen;

import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.util.stream.IntStream;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BEAR;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_FALLOW_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_MOOSE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_ROE_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WILD_BOAR;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WILD_FOREST_REINDEER;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.ADULT_MALE;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.ANTLERS_LOST;
import static fi.riista.test.Asserts.assertThat;
import static fi.riista.util.DateUtil.huntingYear;
import static org.hamcrest.Matchers.is;

@RunWith(Theories.class)
public class HarvestSpecimenOps_EqualContentTest extends HarvestSpecimenOpsTestBase {

    @Theory
    public void testEqualContent_fallowDeer(final HarvestSpecVersion version) {
        final int speciesCode = OFFICIAL_CODE_FALLOW_DEER;

        IntStream.rangeClosed(2019, huntingYear()).forEach(huntingYear -> {

            populateEntity(speciesCode, ADULT_MALE, huntingYear);

            final HarvestSpecimenOps opsUnderTest = createOps(speciesCode, version, huntingYear);

            final HarvestSpecimenDTO dto = new HarvestSpecimenDTO();
            opsUnderTest.copyContentToDTO(specimen, dto);
            assertEquality(dto, opsUnderTest, true);

            final HarvestSpecimenPopulator populator = createPopulator(speciesCode, version, huntingYear);

            populator.populateExtensionFieldsForPermitBasedDeerWithSpecVersion4(dto, ADULT_MALE);
            assertEquality(dto, opsUnderTest, !version.supportsExtendedFieldsForDeers());

            // Reset DTO and set `antlersLost` which is supported in specVersion 8.
            opsUnderTest.copyContentToDTO(specimen, dto);
            dto.setAntlersLost(true);
            assertEquality(dto, opsUnderTest, !version.supportsAntlerFields2020() || huntingYear < 2020);

            populator.mutateContent(dto);
            // Should not be equal because all business fields are different between the objects.
            assertEquality(dto, opsUnderTest, false);
        });
    }

    @Theory
    public void testEqualContent_moose(final HarvestSpecVersion version) {
        final int speciesCode = OFFICIAL_CODE_MOOSE;

        IntStream.rangeClosed(2019, huntingYear()).forEach(huntingYear -> {

            populateEntity(speciesCode, ADULT_MALE, huntingYear);

            final HarvestSpecimenOps opsUnderTest = createOps(speciesCode, version, huntingYear);

            final HarvestSpecimenDTO dto = new HarvestSpecimenDTO();
            opsUnderTest.copyContentToDTO(specimen, dto);
            assertEquality(dto, opsUnderTest, true);

            // `alone` is supported in specVersion 5.
            dto.setAlone(true);
            assertEquality(dto, opsUnderTest, !version.supportsSolitaryMooseCalves());

            // Reset DTO and set `antlersGirth` which is supported in specVersion 8.
            opsUnderTest.copyContentToDTO(specimen, dto);
            dto.setAntlersGirth(50);
            assertEquality(dto, opsUnderTest, !version.supportsAntlerFields2020() || huntingYear < 2020);

            createPopulator(speciesCode, version, huntingYear).mutateContent(dto);
            // Should not be equal because all business fields are different between the objects.
            assertEquality(dto, opsUnderTest, false);
        });
    }

    @Theory
    public void testEqualContent_roeDeer(final HarvestSpecVersion version) {
        final int speciesCode = OFFICIAL_CODE_ROE_DEER;

        IntStream.rangeClosed(2019, huntingYear()).forEach(huntingYear -> {

            populateEntity(speciesCode, ADULT_MALE, huntingYear);

            final HarvestSpecimenOps opsUnderTest = createOps(speciesCode, version, huntingYear);

            final HarvestSpecimenDTO dto = new HarvestSpecimenDTO();
            opsUnderTest.copyContentToDTO(specimen, dto);
            assertEquality(dto, opsUnderTest, true);

            final HarvestSpecimenPopulator populator = createPopulator(speciesCode, version, huntingYear);

            populator.populateExtensionFieldsForRoeDeerWithSpecVersion8(dto, ADULT_MALE);
            assertEquality(dto, opsUnderTest, !version.supportsAntlerFields2020() || huntingYear < 2020);

            populator.mutateContent(dto);
            // Should not be equal because all business fields are different between the objects.
            assertEquality(dto, opsUnderTest, false);
        });
    }

    @Theory
    public void testEqualContent_whiteTailedDeer(final HarvestSpecVersion version) {
        final int speciesCode = OFFICIAL_CODE_WHITE_TAILED_DEER;

        IntStream.rangeClosed(2019, huntingYear()).forEach(huntingYear -> {

            populateEntity(speciesCode, ADULT_MALE, huntingYear);

            final HarvestSpecimenOps opsUnderTest = createOps(speciesCode, version, huntingYear);

            final HarvestSpecimenDTO dto = new HarvestSpecimenDTO();
            opsUnderTest.copyContentToDTO(specimen, dto);
            assertEquality(dto, opsUnderTest, true);

            final HarvestSpecimenPopulator populator = createPopulator(speciesCode, version, huntingYear);

            populator.populateExtensionFieldsForPermitBasedDeerWithSpecVersion4(dto, ADULT_MALE);
            assertEquality(dto, opsUnderTest, !version.supportsExtendedFieldsForDeers());

            // Reset DTO and set `antlersGirth` which is supported in specVersion 8.
            opsUnderTest.copyContentToDTO(specimen, dto);
            dto.setAntlersGirth(50);
            assertEquality(dto, opsUnderTest, !version.supportsAntlerFields2020() || huntingYear < 2020);

            populator.mutateContent(dto);
            // Should not be equal because all business fields are different between the objects.
            assertEquality(dto, opsUnderTest, false);
        });
    }

    @Theory
    public void testEqualContent_whiteTailedDeer_weightTranslation(final HarvestSpecVersion version) {
        IntStream.rangeClosed(2019, huntingYear()).forEach(huntingYear -> {

            final double weight = weight();
            specimen.setWeightEstimated(weight);

            final HarvestSpecimenOps opsUnderTest = createOps(OFFICIAL_CODE_WHITE_TAILED_DEER, version, huntingYear);
            final HarvestSpecimenDTO dto = new HarvestSpecimenDTO();

            if (opsUnderTest.isPresenceOfDeerExtensionFieldsLegitimate()) {
                dto.setWeightEstimated(weight);
            } else {
                dto.setWeight(weight);
            }

            assertEquality(dto, opsUnderTest, true);
        });
    }

    @Theory
    public void testEqualContent_wildBoar(final HarvestSpecVersion version) {
        final int speciesCode = OFFICIAL_CODE_WILD_BOAR;

        IntStream.rangeClosed(2020, huntingYear()).forEach(huntingYear -> {

            populateEntity(speciesCode, harvestSpecimenType(type -> type != ANTLERS_LOST), huntingYear);

            final HarvestSpecimenOps opsUnderTest = createOps(speciesCode, version, huntingYear);

            final HarvestSpecimenDTO dto = new HarvestSpecimenDTO();
            opsUnderTest.copyContentToDTO(specimen, dto);
            assertEquality(dto, opsUnderTest, true);

            final HarvestSpecimenPopulator populator = createPopulator(speciesCode, version, huntingYear);

            populator.populateExtendedWeightFields(dto);
            assertEquality(dto, opsUnderTest, !version.supportsExtendedWeightFieldsForRoeDeerAndWildBoar() || huntingYear < 2020);

            populator.mutateContent(dto);
            // Should not be equal because all business fields are different between the objects.
            assertEquality(dto, opsUnderTest, false);
        });
    }

    @Theory
    public void testEqualContent_wildForestReindeer(final HarvestSpecVersion version) {
        final int speciesCode = OFFICIAL_CODE_WILD_FOREST_REINDEER;

        IntStream.rangeClosed(2019, huntingYear()).forEach(huntingYear -> {

            populateEntity(speciesCode, ADULT_MALE, huntingYear);

            final HarvestSpecimenOps opsUnderTest = createOps(speciesCode, version, huntingYear);

            final HarvestSpecimenDTO dto = new HarvestSpecimenDTO();
            opsUnderTest.copyContentToDTO(specimen, dto);
            assertEquality(dto, opsUnderTest, true);

            final HarvestSpecimenPopulator populator = createPopulator(speciesCode, version, huntingYear);

            populator.populateExtensionFieldsForPermitBasedDeerWithSpecVersion4(dto, ADULT_MALE);
            assertEquality(dto, opsUnderTest, !version.supportsExtendedFieldsForDeers());

            // Reset DTO and set `antlersLost` which is supported in specVersion 8.
            opsUnderTest.copyContentToDTO(specimen, dto);
            dto.setAntlersLost(true);
            assertEquality(dto, opsUnderTest, !version.supportsAntlerFields2020() || huntingYear < 2020);

            populator.mutateContent(dto);
            // Should not be equal because all business fields are different between the objects.
            assertEquality(dto, opsUnderTest, false);
        });
    }

    @Theory
    public void testEqualContent_otherSpecies(final HarvestSpecVersion version) {
        final int speciesCode = OFFICIAL_CODE_BEAR;

        IntStream.rangeClosed(2019, huntingYear()).forEach(huntingYear -> {

            populateEntity(speciesCode, ADULT_MALE);

            final HarvestSpecimenOps opsUnderTest = createOps(speciesCode, version, huntingYear);

            final HarvestSpecimenDTO dto = new HarvestSpecimenDTO();
            opsUnderTest.copyContentToDTO(specimen, dto);
            assertEquality(dto, opsUnderTest, true);

            final HarvestSpecimenPopulator populator = createPopulator(speciesCode, version, huntingYear);

            // Intentionally populating moose extension fields even though species is not moose!
            populator.populateExtensionFieldsForMooseWithSpecVersion8(dto, ADULT_MALE);
            // Should be equal because moose extension fields should not affect.
            assertEquality(dto, opsUnderTest, true);

            populator.mutateContent(dto);
            // Should not be equal because all business fields are different between the objects.
            assertEquality(dto, opsUnderTest, false);
        });
    }

    private void assertEquality(final HarvestSpecimenDTO dto,
                                final HarvestSpecimenOps ops,
                                final boolean expectedToBeEqual) {

        if (expectedToBeEqual) {
            assertThat(ops.equalContent(specimen, dto), is(true), "Should be equal");
        } else {
            assertThat(ops.equalContent(specimen, dto), is(false), "Should not be equal");
        }
    }
}
