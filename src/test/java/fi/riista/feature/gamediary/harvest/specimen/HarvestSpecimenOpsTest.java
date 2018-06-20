package fi.riista.feature.gamediary.harvest.specimen;

import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.util.NumberGenerator;
import fi.riista.util.NumberSequence;
import fi.riista.util.ValueGeneratorMixin;
import fi.riista.util.VersionedTestExecutionSupport;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;

import static fi.riista.feature.gamediary.GameAge.ADULT;
import static fi.riista.feature.gamediary.GameAge.YOUNG;
import static fi.riista.feature.gamediary.GameGender.FEMALE;
import static fi.riista.feature.gamediary.GameGender.MALE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BEAR;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_MOOSE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER;
import static fi.riista.feature.gamediary.GameSpecies.isMooseOrDeerRequiringPermitForHunting;
import static fi.riista.feature.gamediary.harvest.HarvestSpecVersion.LOWEST_VERSION_SUPPORTING_EXTENDED_DEER_FIELDS;
import static fi.riista.feature.gamediary.harvest.HarvestSpecVersion.LOWEST_VERSION_SUPPORTING_EXTENDED_MOOSE_FIELDS;
import static fi.riista.feature.gamediary.harvest.HarvestSpecVersion.MOST_RECENT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HarvestSpecimenOpsTest implements ValueGeneratorMixin, VersionedTestExecutionSupport<HarvestSpecVersion> {

    private HarvestSpecimen specimen;

    @Override
    public List<HarvestSpecVersion> getTestExecutionVersions() {
        return new ArrayList<>(EnumSet.allOf(HarvestSpecVersion.class));
    }

    @Override
    public NumberGenerator getNumberGenerator() {
        return NumberSequence.INSTANCE;
    }

    @Override
    public void onBeforeVersionedTestExecution() {
        // Mocked in order to be able to instantiate without providing a reference to a Harvest instance.
        specimen = Mockito.mock(HarvestSpecimen.class, Answers.CALLS_REAL_METHODS);
    }

    @Test
    public void testCopyContentToEntity_forSpeciesNotRelevantToExtendedMooselikeFields() {
        forEachVersion(version -> {

            // Populate entity with all moose fields to ensure they are cleared within copying.
            populateEntity(OFFICIAL_CODE_MOOSE, ADULT, MALE);

            // Populate DTO with all moose fields in order to ensure that they are not copied to
            // entity.
            final HarvestSpecimenDTO dto = createDTO(OFFICIAL_CODE_MOOSE, ADULT, MALE, MOST_RECENT);
            dto.setWeight(weight());

            createOps(OFFICIAL_CODE_BEAR, version).copyContentToEntity(dto, specimen);

            HarvestSpecimenAssertionBuilder.builder()
                    .withAgeAndGender(dto.getAge(), dto.getGender())
                    .weightPresentAndEqualTo(dto.getWeight())
                    .mooseFieldsAbsent()
                    .verify(specimen);
        });
    }

    @Test
    public void testCopyContentToEntity_forMoose_withSupportingVersions() {
        forEachVersionStartingFrom(LOWEST_VERSION_SUPPORTING_EXTENDED_MOOSE_FIELDS, version -> {

            Stream.of(ADULT, YOUNG).forEach(newAge -> Stream.of(MALE, FEMALE).forEach(newGender -> {

                populateEntity(OFFICIAL_CODE_MOOSE, null, null);

                final HarvestSpecimenDTO dto = createDTO(OFFICIAL_CODE_MOOSE, newAge, newGender, MOST_RECENT);

                // Populate generic weight field to ensure it is not carried over.
                dto.setWeight(weight());

                createOps(OFFICIAL_CODE_MOOSE, version).copyContentToEntity(dto, specimen);

                HarvestSpecimenAssertionBuilder.builder()
                        .withAgeAndGender(newAge, newGender)
                        .weightAbsent()
                        .allMooseFieldsPresent(version)
                        .mooseFieldsEqualTo(dto, version)
                        .verify(specimen);
            }));
        });
    }

    @Test
    public void testCopyContentToEntity_forSomePermitBasedDeer_withSupportingVersions() {
        forEachVersionStartingFrom(LOWEST_VERSION_SUPPORTING_EXTENDED_DEER_FIELDS, version -> {

            Stream.of(true, false).forEach(doInitialPopulationWithMooseFields -> {

                Stream.of(ADULT, YOUNG).forEach(newAge -> Stream.of(MALE, FEMALE).forEach(newGender -> {

                    if (doInitialPopulationWithMooseFields) {
                        // Populate entity with all moose fields to ensure that moose-only fields are
                        // cleared.
                        populateEntity(OFFICIAL_CODE_MOOSE, ADULT, MALE);
                    } else {
                        populateEntity(OFFICIAL_CODE_BEAR, null, null);
                    }

                    // Populate DTO with all moose fields to ensure that only deer-supported fields
                    // are copied.
                    final HarvestSpecimenDTO dto = createDTO(OFFICIAL_CODE_MOOSE, newAge, newGender, MOST_RECENT);

                    // Populate DTO with generic weight field to ensure that it is not copied.
                    dto.setWeight(weight());

                    createOps(OFFICIAL_CODE_WHITE_TAILED_DEER, version).copyContentToEntity(dto, specimen);

                    HarvestSpecimenAssertionBuilder.builder()
                            .withAgeAndGender(newAge, newGender)
                            .weightAbsent()
                            .mooseOnlyFieldsAbsent()
                            .allMooselikeFieldsPresent()
                            .mooselikeFieldsEqualTo(dto)
                            .verify(specimen);
                }));
            });
        });
    }

    @Test
    public void testCopyContentToEntity_forMoose_withOldVersions_handlingOfGenericWeight() {
        testCopyContentToEntity_forMooselikeSpecies_withOldVersions_handlingOfGenericWeight(
                OFFICIAL_CODE_MOOSE, LOWEST_VERSION_SUPPORTING_EXTENDED_MOOSE_FIELDS);
    }

    @Test
    public void testCopyContentToEntity_forSomePermitBasedDeer_withOldVersions_handlingOfGenericWeight() {
        testCopyContentToEntity_forMooselikeSpecies_withOldVersions_handlingOfGenericWeight(
                OFFICIAL_CODE_WHITE_TAILED_DEER, LOWEST_VERSION_SUPPORTING_EXTENDED_DEER_FIELDS);
    }

    private void testCopyContentToEntity_forMooselikeSpecies_withOldVersions_handlingOfGenericWeight(
            final int speciesCode,
            final HarvestSpecVersion earliestVersionToTest) {

        forEachVersionBefore(earliestVersionToTest, version -> {

            // Populate the generic weight field to test that it is cleared and that
            // estimatedWeight is populated instead.
            specimen.setWeight(0.0);

            // Populate with all moose fields to ensure that no moose fields are copied excluding
            // estimated weight.
            final HarvestSpecimenDTO dto = createDTO(OFFICIAL_CODE_MOOSE, ADULT, MALE, MOST_RECENT);
            dto.setWeight(weight());

            createOps(speciesCode, version).copyContentToEntity(dto, specimen);

            HarvestSpecimenAssertionBuilder.builder()
                    .withAgeAndGender(dto.getAge(), dto.getGender())
                    .weightAbsentButEstimatedWeightPresentAndEqualTo(dto.getWeight())
                    .mooseFieldsAbsentExceptEstimatedWeight()
                    .verify(specimen);
        });
    }

    @Test
    public void testCopyContentToEntity_forMoose_withOldVersions_protectionOfUnsupportedFields() {
        forEachVersionBefore(LOWEST_VERSION_SUPPORTING_EXTENDED_MOOSE_FIELDS, version -> {

            Stream.of(ADULT, YOUNG).forEach(originalAge -> Stream.of(MALE, FEMALE).forEach(originalGender -> {

                Stream.of(ADULT, YOUNG).forEach(newAge -> Stream.of(MALE, FEMALE).forEach(newGender -> {

                    // Populate entity with all moose fields to ensure that they are not changed.
                    populateEntity(OFFICIAL_CODE_MOOSE, originalAge, originalGender);

                    final HarvestSpecimenDTO dto = createDTO(OFFICIAL_CODE_MOOSE, newAge, newGender, version);

                    createOps(OFFICIAL_CODE_MOOSE, version).copyContentToEntity(dto, specimen);

                    HarvestSpecimenAssertionBuilder.builder()
                            .withAgeAndGender(newAge, newGender)
                            .weightAbsentButEstimatedWeightPresentAndEqualTo(dto.getWeight())
                            .mooseFieldsPreserved(originalAge, originalGender, MOST_RECENT)
                            .verify(specimen);
                }));
            }));
        });
    }

    @Test
    public void testCopyContentToEntity_forSomePermitBasedDeer_withOldVersions_protectionOfUnsupportedFields() {
        forEachVersionBefore(LOWEST_VERSION_SUPPORTING_EXTENDED_DEER_FIELDS, version -> {

            Stream.of(ADULT, YOUNG).forEach(originalAge -> Stream.of(MALE, FEMALE).forEach(originalGender -> {

                Stream.of(ADULT, YOUNG).forEach(newAge -> Stream.of(MALE, FEMALE).forEach(newGender -> {

                    // Populate entity with all mooselike fields to ensure that they are not
                    // changed.
                    populateEntity(OFFICIAL_CODE_WHITE_TAILED_DEER, originalAge, originalGender);

                    final HarvestSpecimenDTO dto =
                            createDTO(OFFICIAL_CODE_WHITE_TAILED_DEER, newAge, newGender, version);

                    createOps(OFFICIAL_CODE_WHITE_TAILED_DEER, version).copyContentToEntity(dto, specimen);

                    HarvestSpecimenAssertionBuilder.builder()
                            .withAgeAndGender(newAge, newGender)
                            .weightAbsentButEstimatedWeightPresentAndEqualTo(dto.getWeight())
                            .mooseOnlyFieldsAbsent()
                            .mooselikeFieldsPreserved(originalAge, originalGender)
                            .verify(specimen);
                }));
            }));
        });
    }

    @Test
    public void testCopyContentToDTO_forSpeciesNotRelevantToExtendedMooselikeFields() {
        forEachVersion(version -> {

            // Populate entity with all moose fields to ensure they are cleared.
            populateEntity(OFFICIAL_CODE_MOOSE, ADULT, MALE);
            specimen.setWeight(weight());

            // Populate DTO with all moose fields in order to ensure that they are cleared.
            final HarvestSpecimenDTO dto = createDTO(OFFICIAL_CODE_MOOSE, ADULT, MALE, MOST_RECENT);

            createOps(OFFICIAL_CODE_BEAR, version).copyContentToDTO(specimen, dto);

            HarvestSpecimenAssertionBuilder.builder()
                    .withAgeAndGender(specimen.getAge(), specimen.getGender())
                    .weightPresentAndEqualTo(specimen.getWeight())
                    .mooseFieldsAbsent()
                    .verify(dto);
        });
    }

    @Test
    public void testCopyContentToDTO_forMoose_withSupportingVersions() {
        forEachVersionStartingFrom(LOWEST_VERSION_SUPPORTING_EXTENDED_MOOSE_FIELDS, version -> {

            Stream.of(ADULT, YOUNG).forEach(age -> Stream.of(MALE, FEMALE).forEach(gender -> {

                populateEntity(OFFICIAL_CODE_MOOSE, age, gender);

                // Populate weight to ensure that it is not copied.
                specimen.setWeight(weight());

                final HarvestSpecimenDTO dto = new HarvestSpecimenDTO();

                createOps(OFFICIAL_CODE_MOOSE, version).copyContentToDTO(specimen, dto);

                HarvestSpecimenAssertionBuilder.builder()
                        .withAgeAndGender(age, gender)
                        .weightAbsent()
                        .allMooseFieldsPresent(version)
                        .mooseFieldsEqualTo(specimen, version)
                        .verify(dto);
            }));
        });
    }

    @Test
    public void testCopyContentToDTO_forSomePermitBasedDeer_withSupportingVersions() {
        forEachVersionStartingFrom(LOWEST_VERSION_SUPPORTING_EXTENDED_DEER_FIELDS, version -> {

            Stream.of(ADULT, YOUNG).forEach(age -> Stream.of(MALE, FEMALE).forEach(gender -> {

                // Populate entity with moose fields to ensure that only deer-specific fields are
                // copied.
                populateEntity(OFFICIAL_CODE_MOOSE, age, gender);

                // Populate entity with weight to ensure that it is not copied.
                specimen.setWeight(weight());

                // Populate DTO with all moose fields to ensure that moose-only fields are cleared.
                final HarvestSpecimenDTO dto = createDTO(OFFICIAL_CODE_MOOSE, ADULT, MALE, MOST_RECENT);

                createOps(OFFICIAL_CODE_WHITE_TAILED_DEER, version).copyContentToDTO(specimen, dto);

                HarvestSpecimenAssertionBuilder.builder()
                        .withAgeAndGender(age, gender)
                        .weightAbsent()
                        .allMooselikeFieldsPresent()
                        .mooselikeFieldsEqualTo(specimen)
                        .mooseOnlyFieldsAbsent()
                        .verify(dto);
            }));
        });
    }

    @Test
    public void testCopyContentToDTO_forMoose_withOldVersions() {
        testCopyContentToDTO_forMooselikeSpecies_withOldVersions(
                OFFICIAL_CODE_MOOSE, LOWEST_VERSION_SUPPORTING_EXTENDED_MOOSE_FIELDS, false);
    }

    @Test
    public void testCopyContentToDTO_forSomePermitBasedDeer_withOldVersions() {
        testCopyContentToDTO_forMooselikeSpecies_withOldVersions(
                OFFICIAL_CODE_WHITE_TAILED_DEER, LOWEST_VERSION_SUPPORTING_EXTENDED_DEER_FIELDS, false);
    }

    @Test
    public void testCopyContentToDTO_forMoose_withOldVersions_withFallbackToGenericWeight() {
        testCopyContentToDTO_forMooselikeSpecies_withOldVersions(
                OFFICIAL_CODE_MOOSE, LOWEST_VERSION_SUPPORTING_EXTENDED_MOOSE_FIELDS, true);
    }

    @Test
    public void testCopyContentToDTO_forSomePermitBasedDeer_withOldVersions_withFallbackToGenericWeight() {
        testCopyContentToDTO_forMooselikeSpecies_withOldVersions(
                OFFICIAL_CODE_WHITE_TAILED_DEER, LOWEST_VERSION_SUPPORTING_EXTENDED_DEER_FIELDS, true);
    }

    private void testCopyContentToDTO_forMooselikeSpecies_withOldVersions(
            final int speciesCode,
            final HarvestSpecVersion firstVersionToSupport,
            final boolean testFallbackToGenericWeightOnEstimatedWeightMissing) {

        forEachVersionBefore(firstVersionToSupport, version -> {

            Stream.of(ADULT, YOUNG).forEach(age -> Stream.of(MALE, FEMALE).forEach(gender -> {

                // Populate entity with moose fields to ensure that no moose fields are copied.
                populateEntity(speciesCode, age, gender);

                if (testFallbackToGenericWeightOnEstimatedWeightMissing) {
                    specimen.setWeight(specimen.getWeightEstimated());
                    specimen.setWeightEstimated(null);
                }

                // Populate DTO with all moose fields to ensure that they are cleared.
                final HarvestSpecimenDTO dto = createDTO(OFFICIAL_CODE_MOOSE, ADULT, MALE, MOST_RECENT);

                createOps(speciesCode, version).copyContentToDTO(specimen, dto);

                HarvestSpecimenAssertionBuilder.builder()
                        .withAgeAndGender(age, gender)
                        .weightPresentAndEqualTo(testFallbackToGenericWeightOnEstimatedWeightMissing
                                ? specimen.getWeight()
                                : specimen.getWeightEstimated())
                        .mooseFieldsAbsent()
                        .verify(dto);
            }));
        });
    }

    @Test
    public void testEqualContent_forSpeciesNotRelevantToExtendedMooselikeFields() {
        testEqualContent(OFFICIAL_CODE_BEAR);
    }

    @Test
    public void testEqualContent_forMoose() {
        testEqualContent(OFFICIAL_CODE_MOOSE);
    }

    @Test
    public void testEqualContent_forSomePermitBasedDeer() {
        testEqualContent(OFFICIAL_CODE_WHITE_TAILED_DEER);
    }

    private void testEqualContent(final int speciesCode) {
        forEachVersion(version -> {

            populateEntity(speciesCode, ADULT, MALE);

            final HarvestSpecimenOpsForTest opsUnderTest = createOps(speciesCode, version);

            final HarvestSpecimenDTO dto = new HarvestSpecimenDTO();
            opsUnderTest.copyContentToDTO(specimen, dto);

            assertTrue("Should be equal after field copying", opsUnderTest.equalContent(specimen, dto));

            opsUnderTest.populateMooseFields(dto, ADULT, MALE);

            if (opsUnderTest.supportsExtendedMooselikeFields()) {
                assertFalse("Should not be equal because of difference between mooselike fields",
                        opsUnderTest.equalContent(specimen, dto));
            } else {
                assertTrue("Should be equal because mooselike fields are not taken into account",
                        opsUnderTest.equalContent(specimen, dto));
            }

            opsUnderTest.mutateContent(dto, !isMooseOrDeerRequiringPermitForHunting(speciesCode));
            assertFalse("Should not be equal because all business fields are different between the objects",
                    opsUnderTest.equalContent(specimen, dto));
        });
    }

    @Test
    public void testEqualContent_forWeightTranslationOfMooselikeSpecies() {
        forEachVersion(version -> {

            specimen.setWeightEstimated(weight());

            final HarvestSpecimenOpsForTest opsUnderTest = createOps(OFFICIAL_CODE_MOOSE, version);
            final HarvestSpecimenDTO dto = new HarvestSpecimenDTO();

            if (opsUnderTest.supportsExtendedMooselikeFields()) {
                dto.setWeightEstimated(specimen.getWeightEstimated());
            } else {
                dto.setWeight(specimen.getWeightEstimated());
            }

            assertTrue(opsUnderTest.equalContent(specimen, dto));
        });
    }

    private void populateEntity(final int speciesCode, final GameAge age, final GameGender gender) {
        specimen.clearMooseFields();
        createOps(speciesCode, MOST_RECENT).mutateContent(specimen, age, gender);
    }

    private HarvestSpecimenDTO createDTO(final int speciesCode,
                                         final GameAge age,
                                         final GameGender gender,
                                         final HarvestSpecVersion version) {

        return createOps(speciesCode, version).createDTO(age, gender);
    }

    private HarvestSpecimenOpsForTest createOps(final int speciesCode, final HarvestSpecVersion version) {
        return new HarvestSpecimenOpsForTest(speciesCode, version, getNumberGenerator());
    }
}
