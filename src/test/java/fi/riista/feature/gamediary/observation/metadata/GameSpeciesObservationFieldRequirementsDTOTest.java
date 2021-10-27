package fi.riista.feature.gamediary.observation.metadata;

import com.google.common.collect.ImmutableMap;
import fi.riista.feature.common.entity.Required;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.observation.ObservationCategory;
import fi.riista.feature.gamediary.observation.ObservationType;
import fi.riista.feature.gamediary.observation.metadata.GameSpeciesObservationFieldRequirementsDTO.ContextSensitiveFieldSetDTO;
import fi.riista.feature.gamediary.observation.specimen.GameMarking;
import fi.riista.feature.gamediary.observation.specimen.ObservedGameState;
import fi.riista.util.NumberGenerator;
import fi.riista.util.NumberSequence;
import fi.riista.util.ValueGeneratorMixin;
import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static fi.riista.feature.common.entity.Required.NO;
import static fi.riista.feature.common.entity.Required.VOLUNTARY;
import static fi.riista.feature.common.entity.Required.YES;
import static fi.riista.feature.gamediary.observation.ObservationCategory.DEER_HUNTING;
import static fi.riista.feature.gamediary.observation.ObservationCategory.MOOSE_HUNTING;
import static fi.riista.feature.gamediary.observation.ObservationCategory.NORMAL;
import static fi.riista.feature.gamediary.observation.specimen.GameMarking.COLLAR_OR_RADIO_TRANSMITTER;
import static fi.riista.feature.gamediary.observation.specimen.GameMarking.EARMARK;
import static fi.riista.feature.gamediary.observation.specimen.GameMarking.LEG_RING_OR_WING_TAG;
import static fi.riista.feature.gamediary.observation.specimen.GameMarking.NOT_MARKED;
import static fi.riista.feature.gamediary.observation.specimen.ObservedGameState.CARCASS;
import static fi.riista.feature.gamediary.observation.specimen.ObservedGameState.DEAD;
import static fi.riista.feature.gamediary.observation.specimen.ObservedGameState.HEALTHY;
import static fi.riista.feature.gamediary.observation.specimen.ObservedGameState.ILL;
import static fi.riista.feature.gamediary.observation.specimen.ObservedGameState.WOUNDED;
import static fi.riista.test.Asserts.assertEmpty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class GameSpeciesObservationFieldRequirementsDTOTest implements ValueGeneratorMixin {

    private static final int DEFAULT_METADATA_VERSION = 1;
    private static final int DEER_HUNTING_METADATA_VERSION = 4;

    @Override
    public NumberGenerator getNumberGenerator() {
        return NumberSequence.INSTANCE;
    }

    @Test
    public void testState_whenWoundedCarcassDeadNotEnabled() {
        final ContextSensitiveFieldSetDTO ctxFieldsDTO = transformToDTO(newContextSensitiveFields());
        assertEquals(NO, ctxFieldsDTO.getState());
        assertEmpty(ctxFieldsDTO.getAllowedStates());
    }

    @Test
    public void testState_whenWoundedIsVoluntary() {
        final ObservationContextSensitiveFields ctxFields = newContextSensitiveFields();
        ctxFields.setWounded(VOLUNTARY);
        assertStateTranslationToDTO(ctxFields, WOUNDED, VOLUNTARY);
    }

    @Test
    public void testState_whenWoundedIsRequired() {
        final ObservationContextSensitiveFields ctxFields = newContextSensitiveFields();
        ctxFields.setWounded(YES);
        assertStateTranslationToDTO(ctxFields, WOUNDED, YES);
    }

    @Test
    public void testState_whenOnCarcassIsVoluntary() {
        final ObservationContextSensitiveFields ctxFields = newContextSensitiveFields();
        ctxFields.setOnCarcass(VOLUNTARY);
        assertStateTranslationToDTO(ctxFields, CARCASS, VOLUNTARY);
    }

    @Test
    public void testState_whenOnCarcassIsRequired() {
        final ObservationContextSensitiveFields ctxFields = newContextSensitiveFields();
        ctxFields.setOnCarcass(YES);
        assertStateTranslationToDTO(ctxFields, CARCASS, YES);
    }

    @Test
    public void testState_whenDeadIsVoluntary() {
        final ObservationContextSensitiveFields ctxFields = newContextSensitiveFields();
        ctxFields.setDead(VOLUNTARY);
        assertStateTranslationToDTO(ctxFields, DEAD, VOLUNTARY);
    }

    @Test
    public void testState_whenDeadIsRequired() {
        final ObservationContextSensitiveFields ctxFields = newContextSensitiveFields();
        ctxFields.setDead(YES);
        assertStateTranslationToDTO(ctxFields, DEAD, YES);
    }

    @Test
    public void testMarking_whenNoneEnabled() {
        final ContextSensitiveFieldSetDTO ctxFieldsDTO = transformToDTO(newContextSensitiveFields());
        assertEquals(NO, ctxFieldsDTO.getMarking());
        assertEmpty(ctxFieldsDTO.getAllowedMarkings());
    }

    @Test
    public void testMarking_whenEarMarkIsVoluntary() {
        final ObservationContextSensitiveFields ctxFields = newContextSensitiveFields();
        ctxFields.setEarMark(VOLUNTARY);
        assertMarkingTranslationToDTO(ctxFields, EARMARK, VOLUNTARY);
    }

    @Test
    public void testMarking_whenEarMarkIsRequired() {
        final ObservationContextSensitiveFields ctxFields = newContextSensitiveFields();
        ctxFields.setEarMark(YES);
        assertMarkingTranslationToDTO(ctxFields, EARMARK, YES);
    }

    @Test
    public void testMarking_whenCollarIsVoluntary() {
        final ObservationContextSensitiveFields ctxFields = newContextSensitiveFields();
        ctxFields.setCollarOrRadioTransmitter(VOLUNTARY);
        assertMarkingTranslationToDTO(ctxFields, COLLAR_OR_RADIO_TRANSMITTER, VOLUNTARY);
    }

    @Test
    public void testMarking_whenCollarIsRequired() {
        final ObservationContextSensitiveFields ctxFields = newContextSensitiveFields();
        ctxFields.setCollarOrRadioTransmitter(YES);
        assertMarkingTranslationToDTO(ctxFields, COLLAR_OR_RADIO_TRANSMITTER, YES);
    }

    @Test
    public void testMarking_whenLegOrWingMarkIsVoluntary() {
        final ObservationContextSensitiveFields ctxFields = newContextSensitiveFields();
        ctxFields.setLegRingOrWingMark(VOLUNTARY);
        assertMarkingTranslationToDTO(ctxFields, LEG_RING_OR_WING_TAG, VOLUNTARY);
    }

    @Test
    public void testMarking_whenLegOrWingMarkIsRequired() {
        final ObservationContextSensitiveFields ctxFields = newContextSensitiveFields();
        ctxFields.setLegRingOrWingMark(YES);
        assertMarkingTranslationToDTO(ctxFields, LEG_RING_OR_WING_TAG, YES);
    }

    @Test
    public void testBaseFields_withPreAndPostDeerHuntingMetadata() {
        final int[] metadataVersions = {DEFAULT_METADATA_VERSION, DEER_HUNTING_METADATA_VERSION};
        final ImmutableMap<String, Required> expectedNormal =
                ImmutableMap.of("withinMooseHunting", NO, "withinDeerHunting", NO);
        final ImmutableMap<String, Required> expectedMooseHunting =
                ImmutableMap.of("withinMooseHunting", YES, "withinDeerHunting", NO);
        final ImmutableMap<String, Required> expectedDeerHunting =
                ImmutableMap.of("withinMooseHunting", NO, "withinDeerHunting", YES);

        for (final int metadataVersion : metadataVersions) {
            assertEquals(expectedNormal, createDTO(NORMAL, metadataVersion).getBaseFields());
            assertEquals(expectedMooseHunting, createDTO(MOOSE_HUNTING, metadataVersion).getBaseFields());
            assertEquals(expectedDeerHunting, createDTO(DEER_HUNTING, metadataVersion).getBaseFields());
        }
    }

    @Test
    public void testBaseFields_withUnsupportedCombination() {
        final GameSpecies species = newGameSpeciesAssociatedWithBaseFields();
        final ObservationContextSensitiveFields ctxFields = newContextSensitiveFields(species, NORMAL, some(ObservationType.class), DEFAULT_METADATA_VERSION);
        final ObservationBaseFields baseFields = newBaseFields(ctxFields.getSpecies(), NORMAL, DEFAULT_METADATA_VERSION);
        baseFields.setWithinMooseHunting(YES);
        baseFields.setWithinDeerHunting(Required.YES);
        final ImmutableMap<String, Required> expected = ImmutableMap.of("withinMooseHunting", Required.YES, "withinDeerHunting", Required.YES);
        assertEquals(expected, create(baseFields, ctxFields).getBaseFields());
    }

    @Test
    public void testContextSensitiveField_withPreDeerHuntingMetadata() {
        final ObservationCategory[] categories = {NORMAL, MOOSE_HUNTING};
        for (final ObservationCategory category : categories) {
            final GameSpeciesObservationFieldRequirementsDTO dto = createDTO(category, DEFAULT_METADATA_VERSION);
            assertEquals(category == MOOSE_HUNTING, dto.getContextSensitiveFieldSets().get(0).isWithinMooseHunting());
            assertNull(dto.getContextSensitiveFieldSets().get(0).getCategory());
        }
    }

    @Test
    public void testContextSensitiveField_withUnsupportedCombination() {
        final GameSpeciesObservationFieldRequirementsDTO dto = createDTO(DEER_HUNTING, DEFAULT_METADATA_VERSION);
        assertFalse(dto.getContextSensitiveFieldSets().get(0).isWithinMooseHunting());
        assertNull(dto.getContextSensitiveFieldSets().get(0).getCategory());
    }

    @Test
    public void testContextSensitiveFields_withPostDeerHuntingMetadata() {
        final ObservationCategory[] categories = {NORMAL, MOOSE_HUNTING, DEER_HUNTING};
        for (final ObservationCategory category : categories) {
            final GameSpeciesObservationFieldRequirementsDTO dto = createDTO(category, DEER_HUNTING_METADATA_VERSION);
            assertNull(dto.getContextSensitiveFieldSets().get(0).isWithinMooseHunting());
            assertEquals(category, dto.getContextSensitiveFieldSets().get(0).getCategory());
        }
    }

    private static void assertStateTranslationToDTO(final ObservationContextSensitiveFields ctxFields,
                                    final ObservedGameState expectedStateValue,
                                    final Required stateRequirement) {

        final ContextSensitiveFieldSetDTO ctxFieldsDTO = transformToDTO(ctxFields);
        assertEquals(stateRequirement, ctxFieldsDTO.getState());
        assertEquals(EnumSet.of(HEALTHY, ILL, expectedStateValue), ctxFieldsDTO.getAllowedStates());
    }

    private static void assertMarkingTranslationToDTO(final ObservationContextSensitiveFields ctxFields,
                                      final GameMarking expectedMarkingValue,
                                      final Required markingRequirement) {

        final ContextSensitiveFieldSetDTO ctxFieldsDTO = transformToDTO(ctxFields);
        assertEquals(markingRequirement, ctxFieldsDTO.getMarking());
        assertEquals(EnumSet.of(NOT_MARKED, expectedMarkingValue), ctxFieldsDTO.getAllowedMarkings());
    }

    private static GameSpeciesObservationFieldRequirementsDTO create(final ObservationBaseFields baseFields,
                                                                     final ObservationContextSensitiveFields ctxFields) {

        return create(baseFields, Collections.singletonList(ctxFields));
    }

    private static GameSpeciesObservationFieldRequirementsDTO create(
            final ObservationBaseFields baseFields,
            final List<ObservationContextSensitiveFields> ctxFieldsets) {

        return new GameSpeciesObservationFieldRequirementsDTO(baseFields, ctxFieldsets, false);
    }

    private static ContextSensitiveFieldSetDTO transformToDTO(final ObservationContextSensitiveFields ctxFields) {
        final ObservationBaseFields baseFields = newBaseFields(
                ctxFields.getSpecies(), ctxFields.getObservationCategory(), DEFAULT_METADATA_VERSION);
        final GameSpeciesObservationFieldRequirementsDTO dto = create(baseFields, ctxFields);

        assertEquals(1, dto.getContextSensitiveFieldSets().size());
        return dto.getContextSensitiveFieldSets().get(0);
    }

    private static ObservationBaseFields newBaseFields(final GameSpecies species,
                                                       final ObservationCategory category,
                                                       final int metadataVersion) {

        // Mocking needed becase JPA static metamodel is not available.
        final ObservationBaseFields baseFields = spy(new ObservationBaseFields());

        doReturn(species).when(baseFields).getSpecies();
        doNothing().when(baseFields).setSpecies(any(GameSpecies.class));
        baseFields.setMetadataVersion(metadataVersion);

        switch (category) {
            case NORMAL:
                baseFields.setWithinMooseHunting(NO);
                baseFields.setWithinDeerHunting(NO);
                break;
            case MOOSE_HUNTING:
                baseFields.setWithinMooseHunting(YES);
                baseFields.setWithinDeerHunting(NO);
                break;
            case DEER_HUNTING:
                baseFields.setWithinMooseHunting(NO);
                baseFields.setWithinDeerHunting(YES);
                break;
            default:
                throw new IllegalArgumentException("Invalid ObservationCategory");
        }

        return baseFields;
    }

    private static ObservationContextSensitiveFields newContextSensitiveFields(final GameSpecies species,
                                                                               final ObservationCategory category,
                                                                               final ObservationType type,
                                                                               final int metadataVersion) {

        // Mocking needed becase JPA static metamodel is not available.
        final ObservationContextSensitiveFields ctxFields = spy(new ObservationContextSensitiveFields());

        doReturn(species).when(ctxFields).getSpecies();
        doNothing().when(ctxFields).setSpecies(any(GameSpecies.class));

        ctxFields.setMetadataVersion(metadataVersion);
        ctxFields.setObservationCategory(category);
        ctxFields.setObservationType(type);

        return ctxFields;
    }

    private GameSpeciesObservationFieldRequirementsDTO createDTO(final ObservationCategory category, final int metadataVersion) {
        final GameSpecies species = newGameSpeciesAssociatedWithBaseFields();
        final ObservationContextSensitiveFields ctxFields = newContextSensitiveFields(species, category, some(ObservationType.class), metadataVersion);
        final ObservationBaseFields baseFields = newBaseFields(ctxFields.getSpecies(), category, metadataVersion);
        return create(baseFields, ctxFields);
    }

    private ObservationContextSensitiveFields newContextSensitiveFields() {
        final GameSpecies species = newGameSpeciesAssociatedWithBaseFields();
        return newContextSensitiveFields(species, NORMAL, some(ObservationType.class), DEFAULT_METADATA_VERSION);
    }

    private GameSpecies newGameSpeciesAssociatedWithBaseFields() {
        final GameSpecies species = new GameSpecies();
        species.setOfficialCode(nextPositiveInt());
        newBaseFields(species, NORMAL, DEFAULT_METADATA_VERSION);
        return species;
    }
}
