package fi.riista.feature.gamediary.harvest.specimen;

import fi.riista.feature.gamediary.harvest.HarvestTestUtils.MooselikeFieldsPresence;
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

import static fi.riista.feature.gamediary.harvest.HarvestSpecVersion.LOWEST_VERSION_SUPPORTING_EXTENDED_DEER_FIELDS;
import static fi.riista.feature.gamediary.harvest.HarvestSpecVersion.LOWEST_VERSION_SUPPORTING_EXTENDED_MOOSE_FIELDS;
import static fi.riista.feature.gamediary.harvest.HarvestSpecVersion.MOST_RECENT;
import static fi.riista.feature.gamediary.harvest.HarvestTestUtils.assertMooseFieldsNotPresent;
import static fi.riista.feature.gamediary.harvest.HarvestTestUtils.assertMooseFieldsPresent;
import static fi.riista.feature.gamediary.harvest.HarvestTestUtils.assertMooseOnlyFieldsNotPresent;
import static fi.riista.feature.gamediary.harvest.HarvestTestUtils.assertMooselikeFieldsPresent;
import static fi.riista.feature.gamediary.harvest.HarvestTestUtils.assertPresenceOfMooseFields;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BEAR;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_MOOSE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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

            // Populate entity with all moose fields to ensure they are cleared.
            populateWithMostRecentVersion(specimen, OFFICIAL_CODE_MOOSE);

            // Populate DTO with all moose fields in order to ensure that they are not copied to
            // entity.
            final HarvestSpecimenDTO dto = newPopulatedDTOUsingMostRecentSpec(OFFICIAL_CODE_MOOSE);
            dto.setWeight(weight());

            createOps(OFFICIAL_CODE_BEAR, version).copyContentToEntity(dto, specimen);

            assertCommonFieldsPresent(specimen);
            assertCommonFieldsEqual(specimen, dto);
            assertMooseFieldsNotPresent(singletonList(specimen));
        });
    }

    @Test
    public void testCopyContentToEntity_forMoose_withSupportingVersions() {
        forEachVersionStartingFrom(LOWEST_VERSION_SUPPORTING_EXTENDED_MOOSE_FIELDS, version -> {

            final HarvestSpecimenDTO dto = newPopulatedDTOUsingMostRecentSpec(OFFICIAL_CODE_MOOSE);

            // Populate generic weight field to ensure it is not copied.
            dto.setWeight(weight());

            createOps(OFFICIAL_CODE_MOOSE, version).copyContentToEntity(dto, specimen);

            assertCommonFieldsPresentExceptWeight(specimen);
            assertEqualAgeAndGender(specimen, dto);

            assertMooseFieldsPresent(singletonList(specimen));
            assertTrue(specimen.hasEqualMooseFields(dto));
        });
    }

    @Test
    public void testCopyContentToEntity_forSomePermitBasedDeer_withSupportingVersions() {
        forEachVersionStartingFrom(LOWEST_VERSION_SUPPORTING_EXTENDED_DEER_FIELDS, version -> {

            // Populate DTO with all moose fields to ensure that only deer-specific fields are
            // copied.
            final HarvestSpecimenDTO dto = newPopulatedDTOUsingMostRecentSpec(OFFICIAL_CODE_MOOSE);

            // Populate DTO with generic weight field to ensure that it is not copied.
            dto.setWeight(weight());

            // Populate entity with all moose fields to ensure that moose-only fields are cleared.
            populateWithMostRecentVersion(specimen, OFFICIAL_CODE_MOOSE);

            createOps(OFFICIAL_CODE_WHITE_TAILED_DEER, version).copyContentToEntity(dto, specimen);

            assertCommonFieldsPresentExceptWeight(specimen);
            assertEqualAgeAndGender(specimen, dto);

            final List<HarvestSpecimen> singleton = singletonList(specimen);
            assertMooselikeFieldsPresent(singleton);
            assertMooseOnlyFieldsNotPresent(singleton);

            assertTrue(specimen.hasEqualMooselikeFields(dto));
        });
    }

    @Test
    public void testCopyContentToEntity_forMoose_withOldVersions() {
        testCopyContentToEntity_forMooselikeSpecies_withOldVersions(
                OFFICIAL_CODE_MOOSE, LOWEST_VERSION_SUPPORTING_EXTENDED_MOOSE_FIELDS);
    }

    @Test
    public void testCopyContentToEntity_forSomePermitBasedDeer_withOldVersions() {
        testCopyContentToEntity_forMooselikeSpecies_withOldVersions(
                OFFICIAL_CODE_WHITE_TAILED_DEER, LOWEST_VERSION_SUPPORTING_EXTENDED_DEER_FIELDS);
    }

    private void testCopyContentToEntity_forMooselikeSpecies_withOldVersions(final int speciesCode,
                                                                             final HarvestSpecVersion firstVersionToSupport) {

        forEachVersionBefore(firstVersionToSupport, version -> {

            // Populate generic weight fields to test that it is cleared and that estimatedWeight
            // is populated instead.
            specimen.setWeight(0.0);

            // Populate with all moose fields to ensure that no moose fields are copied excluding
            // estimated weight.
            final HarvestSpecimenDTO dto = newPopulatedDTOUsingMostRecentSpec(OFFICIAL_CODE_MOOSE);
            dto.setWeight(weight());

            createOps(speciesCode, version).copyContentToEntity(dto, specimen);

            assertCommonFieldsPresentExceptWeight(specimen);
            assertCommonFieldsEqualAndWeightTranslated(specimen, dto);
            assertPresenceOfMooseFields(singletonList(specimen), MooselikeFieldsPresence.ESTIMATED_WEIGHT);
        });
    }

    @Test
    public void testCopyContentToEntity_forMoose_withOldVersions_ensureExistingFieldsNotOverridden() {
        forEachVersionBefore(LOWEST_VERSION_SUPPORTING_EXTENDED_MOOSE_FIELDS, version -> {

            // Populate entity with all moose fields to ensure that they are not changed.
            populateWithMostRecentVersion(specimen, OFFICIAL_CODE_MOOSE);

            final HarvestSpecimenDTO dto = newPopulatedDTO(OFFICIAL_CODE_MOOSE, version);

            createOps(OFFICIAL_CODE_MOOSE, version).copyContentToEntity(dto, specimen);

            assertCommonFieldsPresentExceptWeight(specimen);
            assertCommonFieldsEqualAndWeightTranslated(specimen, dto);
            assertMooseFieldsPresent(singletonList(specimen));
        });
    }

    @Test
    public void testCopyContentToEntity_forSomePermitBasedDeer_withOldVersions_ensureExistingFieldsNotOverridden() {
        forEachVersionBefore(LOWEST_VERSION_SUPPORTING_EXTENDED_DEER_FIELDS, version -> {

            // Populate entity with all moose fields to ensure that they are not changed.
            populateWithMostRecentVersion(specimen, OFFICIAL_CODE_WHITE_TAILED_DEER);

            final HarvestSpecimenDTO dto = newPopulatedDTO(OFFICIAL_CODE_WHITE_TAILED_DEER, version);

            createOps(OFFICIAL_CODE_WHITE_TAILED_DEER, version).copyContentToEntity(dto, specimen);

            assertCommonFieldsPresentExceptWeight(specimen);
            assertCommonFieldsEqualAndWeightTranslated(specimen, dto);

            final List<HarvestSpecimen> singleton = singletonList(specimen);
            assertMooselikeFieldsPresent(singleton);
            assertMooseOnlyFieldsNotPresent(singleton);
        });
    }

    @Test
    public void testCopyContentToDTO_forSpeciesNotRelevantToExtendedMooselikeFields() {
        forEachVersion(version -> {

            // Populate entity with all moose fields to ensure they are cleared.
            populateWithMostRecentVersion(specimen, OFFICIAL_CODE_MOOSE);
            specimen.setWeight(weight());

            // Populate DTO with all moose fields in order to ensure that they are cleared.
            final HarvestSpecimenDTO dto = newPopulatedDTOUsingMostRecentSpec(OFFICIAL_CODE_MOOSE);

            createOps(OFFICIAL_CODE_BEAR, version).copyContentToDTO(specimen, dto);

            assertCommonFieldsPresent(dto);
            assertCommonFieldsEqual(specimen, dto);
            assertMooseFieldsNotPresent(singletonList(dto));
        });
    }

    @Test
    public void testCopyContentToDTO_forMoose_withSupportingVersions() {
        forEachVersionStartingFrom(LOWEST_VERSION_SUPPORTING_EXTENDED_MOOSE_FIELDS, version -> {

            final HarvestSpecimenDTO dto = new HarvestSpecimenDTO();
            populateWithMostRecentVersion(specimen, OFFICIAL_CODE_MOOSE);

            // Populate weight to ensure that it is not copied.
            specimen.setWeight(weight());

            createOps(OFFICIAL_CODE_MOOSE, version).copyContentToDTO(specimen, dto);

            assertCommonFieldsPresentExceptWeight(dto);
            assertEqualAgeAndGender(specimen, dto);
            assertMooseFieldsPresent(singletonList(dto));
            assertTrue(specimen.hasEqualMooseFields(dto));
        });
    }

    @Test
    public void testCopyContentToDTO_forSomePermitBasedDeer_withSupportingVersions() {
        forEachVersionStartingFrom(LOWEST_VERSION_SUPPORTING_EXTENDED_DEER_FIELDS, version -> {

            // Populate entity with all moose fields to ensure that only deer-specific fields are
            // copied.
            populateWithMostRecentVersion(specimen, OFFICIAL_CODE_MOOSE);

            // Populate DTO with all moose fields to ensure that moose-only fields are cleared.
            final HarvestSpecimenDTO dto = newPopulatedDTOUsingMostRecentSpec(OFFICIAL_CODE_MOOSE);

            // Populate entity with weight to ensure that it is not copied.
            specimen.setWeight(weight());

            createOps(OFFICIAL_CODE_WHITE_TAILED_DEER, version).copyContentToDTO(specimen, dto);

            assertCommonFieldsPresentExceptWeight(dto);
            assertEqualAgeAndGender(specimen, dto);

            final List<HarvestSpecimenDTO> singleton = singletonList(dto);
            assertMooselikeFieldsPresent(singleton);
            assertMooseOnlyFieldsNotPresent(singleton);

            assertTrue(specimen.hasEqualMooselikeFields(dto));
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

    private void testCopyContentToDTO_forMooselikeSpecies_withOldVersions(final int speciesCode,
                                                                          final HarvestSpecVersion firstVersionToSupport,
                                                                          final boolean testFallbackToGenericWeightOnEstimatedWeightMissing) {
        forEachVersionBefore(firstVersionToSupport, version -> {

            // Populate entity with all moose fields to ensure that no moose fields are copied.
            populateWithMostRecentVersion(specimen, speciesCode);

            if (testFallbackToGenericWeightOnEstimatedWeightMissing) {
                specimen.setWeight(specimen.getWeightEstimated());
                specimen.setWeightEstimated(null);
            }

            // Populate DTO with all moose fields to ensure that they are cleared.
            final HarvestSpecimenDTO dto = newPopulatedDTOUsingMostRecentSpec(OFFICIAL_CODE_MOOSE);

            createOps(speciesCode, version).copyContentToDTO(specimen, dto);

            assertCommonFieldsPresent(dto);
            assertMooseFieldsNotPresent(singletonList(dto));

            if (testFallbackToGenericWeightOnEstimatedWeightMissing) {
                assertCommonFieldsEqual(specimen, dto);
            } else {
                assertCommonFieldsEqualAndWeightTranslated(specimen, dto);
            }
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

            populateWithMostRecentVersion(specimen, speciesCode);

            final HarvestSpecimenOpsForTest opsUnderTest = createOps(speciesCode, version);

            final HarvestSpecimenDTO dto = new HarvestSpecimenDTO();
            opsUnderTest.copyContentToDTO(specimen, dto);

            assertTrue("Should be equal after field copying", opsUnderTest.equalContent(specimen, dto));

            opsUnderTest.populateMooseFields(dto);

            if (opsUnderTest.supportsExtendedMooselikeFields()) {
                assertFalse("Should not be equal because of difference between mooselike fields",
                        opsUnderTest.equalContent(specimen, dto));
            } else {
                assertTrue("Should be equal because mooselike fields are not taken into account",
                        opsUnderTest.equalContent(specimen, dto));
            }

            opsUnderTest.mutateContent(dto, true);
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

    private void populateWithMostRecentVersion(final HarvestSpecimen entity, final int speciesCode) {
        createMostRecentOps(speciesCode).mutateContent(entity, true);
    }

    private HarvestSpecimenDTO newPopulatedDTOUsingMostRecentSpec(final int speciesCode) {
        return newPopulatedDTO(speciesCode, MOST_RECENT);
    }

    private HarvestSpecimenDTO newPopulatedDTO(final int speciesCode, final HarvestSpecVersion version) {
        return createOps(speciesCode, version).newHarvestSpecimenDTO(true);
    }

    private HarvestSpecimenOpsForTest createMostRecentOps(final int speciesCode) {
        return createOps(speciesCode, MOST_RECENT);
    }

    private HarvestSpecimenOpsForTest createOps(final int speciesCode, final HarvestSpecVersion version) {
        return new HarvestSpecimenOpsForTest(speciesCode, version, getNumberGenerator());
    }

    private static void assertCommonFieldsPresent(final HarvestSpecimen specimen) {
        assertNotNull(specimen.getAge());
        assertNotNull(specimen.getGender());
        assertNotNull(specimen.getWeight());
    }

    private static void assertCommonFieldsPresentExceptWeight(final HarvestSpecimen specimen) {
        assertNotNull(specimen.getAge());
        assertNotNull(specimen.getGender());
        assertNull(specimen.getWeight());
    }

    private static void assertCommonFieldsPresent(final HarvestSpecimenDTO specimen) {
        assertNotNull(specimen.getAge());
        assertNotNull(specimen.getGender());
        assertNotNull(specimen.getWeight());
    }

    private static void assertCommonFieldsPresentExceptWeight(final HarvestSpecimenDTO specimen) {
        assertNotNull(specimen.getAge());
        assertNotNull(specimen.getGender());
        assertNull(specimen.getWeight());
    }

    private static void assertEqualAgeAndGender(final HarvestSpecimen entity, final HarvestSpecimenDTO dto) {
        assertEquals(entity.getAge(), dto.getAge());
        assertEquals(entity.getGender(), dto.getGender());
    }

    private static void assertCommonFieldsEqual(final HarvestSpecimen entity, final HarvestSpecimenDTO dto) {
        assertEqualAgeAndGender(entity, dto);
        assertEquals(entity.getWeight(), dto.getWeight());
    }

    private static void assertCommonFieldsEqualAndWeightTranslated(final HarvestSpecimen entity,
                                                                   final HarvestSpecimenDTO dto) {
        assertEqualAgeAndGender(entity, dto);
        assertEquals(entity.getWeightEstimated(), dto.getWeight());
    }

}
