package fi.riista.feature.gamediary.observation.metadata;

import fi.riista.feature.common.entity.Required;
import fi.riista.feature.gamediary.GameSpecies;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class GameSpeciesObservationFieldRequirementsDTOTest implements ValueGeneratorMixin {

    private static final int DEFAULT_METADATA_VERSION = 1;

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
        final ObservationBaseFields baseFields =
                newBaseFields(ctxFields.getSpecies(), ctxFields.isWithinMooseHunting() ? YES : NO);
        final GameSpeciesObservationFieldRequirementsDTO dto = create(baseFields, ctxFields);

        assertEquals(1, dto.getContextSensitiveFieldSets().size());
        return dto.getContextSensitiveFieldSets().get(0);
    }

    private static ObservationBaseFields newBaseFields(final GameSpecies species, final Required withinMooseHunting) {
        // Mocking needed becase JPA static metamodel is not available.
        final ObservationBaseFields baseFields = spy(new ObservationBaseFields());

        doReturn(species).when(baseFields).getSpecies();
        doNothing().when(baseFields).setSpecies(any(GameSpecies.class));

        baseFields.setMetadataVersion(DEFAULT_METADATA_VERSION);
        baseFields.setWithinMooseHunting(withinMooseHunting);

        return baseFields;
    }

    private static ObservationContextSensitiveFields newContextSensitiveFields(final GameSpecies species,
                                                                               final boolean withinMooseHunting,
                                                                               final ObservationType type) {

        // Mocking needed becase JPA static metamodel is not available.
        final ObservationContextSensitiveFields ctxFields = spy(new ObservationContextSensitiveFields());

        doReturn(species).when(ctxFields).getSpecies();
        doNothing().when(ctxFields).setSpecies(any(GameSpecies.class));

        ctxFields.setMetadataVersion(DEFAULT_METADATA_VERSION);
        ctxFields.setWithinMooseHunting(withinMooseHunting);
        ctxFields.setObservationType(type);

        return ctxFields;
    }

    private ObservationContextSensitiveFields newContextSensitiveFields() {
        final GameSpecies species = newGameSpeciesAssociatedWithBaseFields(NO);
        return newContextSensitiveFields(species, false, some(ObservationType.class));
    }

    private GameSpecies newGameSpeciesAssociatedWithBaseFields(final Required withinMooseHunting) {
        final GameSpecies species = new GameSpecies();
        species.setOfficialCode(nextPositiveInt());
        newBaseFields(species, withinMooseHunting);
        return species;
    }
}
