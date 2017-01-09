package fi.riista.feature.gamediary.observation.metadata;

import static fi.riista.feature.gamediary.observation.specimen.GameMarking.NOT_MARKED;
import static fi.riista.feature.gamediary.observation.specimen.ObservedGameState.HEALTHY;
import static fi.riista.feature.gamediary.observation.specimen.ObservedGameState.ILL;
import static fi.riista.util.Asserts.assertEmpty;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.common.entity.Required;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.observation.metadata.GameSpeciesObservationFieldRequirementsDTO.ContextSensitiveFieldSetDTO;
import fi.riista.feature.gamediary.observation.specimen.GameMarking;
import fi.riista.feature.gamediary.observation.ObservationType;
import fi.riista.feature.gamediary.observation.specimen.ObservedGameState;
import fi.riista.util.jpa.CriteriaUtils;

import org.junit.Test;

import javax.annotation.Resource;
import javax.persistence.metamodel.SingularAttribute;
import javax.transaction.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class ObservationFieldsMetadataServiceTest extends EmbeddedDatabaseTest {

    private static final int DEFAULT_METADATA_VERSION = 1;

    private static ContextSensitiveFieldSetDTO getContextSensitiveFieldSetExpectingExactlyOneExists(
            final GameSpeciesObservationMetadataDTO dto, final ObservationType observationType) {

        final List<ContextSensitiveFieldSetDTO> ctxFieldSets = dto.getContextSensitiveFieldSets();

        if (ctxFieldSets.size() != 1 || ctxFieldSets.get(0).getType() != observationType) {
            final Set<ObservationType> observationTypes = dto.getContextSensitiveFieldSets().stream()
                    .map(ContextSensitiveFieldSetDTO::getType)
                    .collect(toSet());

            throw new AssertionError(String.format(
                    "Expected only one %s with observation type '%s', but found contexts for these observation types: %s",
                    ContextSensitiveFieldSetDTO.class.getSimpleName(),
                    observationType.name(),
                    observationTypes));
        }

        return ctxFieldSets.get(0);
    }

    @Resource
    private ObservationFieldsMetadataService service;

    @Test
    @Transactional
    public void testGetObservationFieldMetadataForSingleSpecies_stateWhenWoundedCarcassDeadNotEnabled() {
        final GameSpecies species = newGameSpecies();
        final ObservationContextSensitiveFields ctxFields =
                newObservationContextSensitiveFields(species, Required.NO, Required.VOLUNTARY);

        persistInCurrentlyOpenTransaction();

        final ContextSensitiveFieldSetDTO ctxFieldsDTO =
                invokeServiceAndGetContextSensitiveFieldSetDTO(species, ctxFields.getObservationType());

        assertEquals(Required.NO, ctxFieldsDTO.getState());
        assertEmpty(ctxFieldsDTO.getAllowedStates());
    }

    @Test
    @Transactional
    public void testGetObservationFieldMetadataForSingleSpecies_stateWhenWoundedIsVoluntary() {
        testGetObservationFieldMetadataForSingleSpecies_forState(
                ObservationContextSensitiveFields_.wounded, Required.VOLUNTARY, ObservedGameState.WOUNDED);
    }

    @Test
    @Transactional
    public void testGetObservationFieldMetadataForSingleSpecies_stateWhenWoundedIsRequired() {
        testGetObservationFieldMetadataForSingleSpecies_forState(
                ObservationContextSensitiveFields_.wounded, Required.YES, ObservedGameState.WOUNDED);
    }

    @Test
    @Transactional
    public void testGetObservationFieldMetadataForSingleSpecies_stateWhenOnCarcassIsVoluntary() {
        testGetObservationFieldMetadataForSingleSpecies_forState(
                ObservationContextSensitiveFields_.onCarcass, Required.VOLUNTARY, ObservedGameState.CARCASS);
    }

    @Test
    @Transactional
    public void testGetObservationFieldMetadataForSingleSpecies_stateWhenOnCarcassIsRequired() {
        testGetObservationFieldMetadataForSingleSpecies_forState(
                ObservationContextSensitiveFields_.onCarcass, Required.YES, ObservedGameState.CARCASS);
    }

    @Test
    @Transactional
    public void testGetObservationFieldMetadataForSingleSpecies_stateWhenDeadIsVoluntary() {
        testGetObservationFieldMetadataForSingleSpecies_forState(
                ObservationContextSensitiveFields_.dead, Required.VOLUNTARY, ObservedGameState.DEAD);
    }

    @Test
    @Transactional
    public void testGetObservationFieldMetadataForSingleSpecies_stateWhenDeadIsRequired() {
        testGetObservationFieldMetadataForSingleSpecies_forState(
                ObservationContextSensitiveFields_.dead, Required.YES, ObservedGameState.DEAD);
    }

    @Test
    @Transactional
    public void testGetObservationFieldMetadataForSingleSpecies_markingWhenNoneEnabled() {
        final GameSpecies species = newGameSpecies();
        final ObservationContextSensitiveFields ctxFields =
                newObservationContextSensitiveFields(species, Required.VOLUNTARY, Required.NO);

        persistInCurrentlyOpenTransaction();

        final ContextSensitiveFieldSetDTO ctxFieldsDTO =
                invokeServiceAndGetContextSensitiveFieldSetDTO(species, ctxFields.getObservationType());

        assertEquals(Required.NO, ctxFieldsDTO.getMarking());
        assertEmpty(ctxFieldsDTO.getAllowedMarkings());
    }

    @Test
    @Transactional
    public void testGetObservationFieldMetadataForSingleSpecies_markingWhenEarMarkIsVoluntary() {
        testGetObservationFieldMetadataForSingleSpecies_forMarking(
                ObservationContextSensitiveFields_.earMark, Required.VOLUNTARY, GameMarking.EARMARK);
    }

    @Test
    @Transactional
    public void testGetObservationFieldMetadataForSingleSpecies_stateWhenEarMarkIsRequired() {
        testGetObservationFieldMetadataForSingleSpecies_forMarking(
                ObservationContextSensitiveFields_.earMark, Required.YES, GameMarking.EARMARK);
    }

    @Test
    @Transactional
    public void testGetObservationFieldMetadataForSingleSpecies_markingWhenCollarIsVoluntary() {
        testGetObservationFieldMetadataForSingleSpecies_forMarking(
                ObservationContextSensitiveFields_.collarOrRadioTransmitter,
                Required.VOLUNTARY,
                GameMarking.COLLAR_OR_RADIO_TRANSMITTER);
    }

    @Test
    @Transactional
    public void testGetObservationFieldMetadataForSingleSpecies_markingWhenCollarIsRequired() {
        testGetObservationFieldMetadataForSingleSpecies_forMarking(
                ObservationContextSensitiveFields_.collarOrRadioTransmitter,
                Required.YES,
                GameMarking.COLLAR_OR_RADIO_TRANSMITTER);
    }

    @Test
    @Transactional
    public void testGetObservationFieldMetadataForSingleSpecies_markingWhenLegOrWingMarkIsVoluntary() {
        testGetObservationFieldMetadataForSingleSpecies_forMarking(
                ObservationContextSensitiveFields_.legRingOrWingMark,
                Required.VOLUNTARY,
                GameMarking.LEG_RING_OR_WING_TAG);
    }

    @Test
    @Transactional
    public void testGetObservationFieldMetadataForSingleSpecies_markingWhenLegOrWingMarkIsRequired() {
        testGetObservationFieldMetadataForSingleSpecies_forMarking(
                ObservationContextSensitiveFields_.legRingOrWingMark,
                Required.YES,
                GameMarking.LEG_RING_OR_WING_TAG);
    }

    private void testGetObservationFieldMetadataForSingleSpecies_forState(
            final SingularAttribute<ObservationContextSensitiveFields, Required> fieldAttribute,
            final Required stateRequirement,
            final ObservedGameState expectedStateValue) {

        final GameSpecies species = newGameSpecies();
        final ObservationContextSensitiveFields ctxFields =
                newObservationContextSensitiveFields(species, Required.NO, Required.VOLUNTARY);

        CriteriaUtils.createSetterInvoker(fieldAttribute).accept(ctxFields, stateRequirement);

        persistInCurrentlyOpenTransaction();

        final ContextSensitiveFieldSetDTO ctxFieldsDTO =
                invokeServiceAndGetContextSensitiveFieldSetDTO(species, ctxFields.getObservationType());

        assertEquals(stateRequirement, ctxFieldsDTO.getState());
        assertEquals(EnumSet.of(HEALTHY, ILL, expectedStateValue), ctxFieldsDTO.getAllowedStates());
    }

    private void testGetObservationFieldMetadataForSingleSpecies_forMarking(
            final SingularAttribute<ObservationContextSensitiveFields, Required> fieldAttribute,
            final Required markingRequirement,
            final GameMarking expectedMarkingValue) {

        final GameSpecies species = newGameSpecies();
        final ObservationContextSensitiveFields ctxFields =
                newObservationContextSensitiveFields(species, Required.VOLUNTARY, Required.NO);

        CriteriaUtils.createSetterInvoker(fieldAttribute).accept(ctxFields, markingRequirement);

        persistInCurrentlyOpenTransaction();

        final ContextSensitiveFieldSetDTO ctxFieldsDTO =
                invokeServiceAndGetContextSensitiveFieldSetDTO(species, ctxFields.getObservationType());

        assertEquals(markingRequirement, ctxFieldsDTO.getMarking());
        assertEquals(EnumSet.of(NOT_MARKED, expectedMarkingValue), ctxFieldsDTO.getAllowedMarkings());
    }

    private ContextSensitiveFieldSetDTO invokeServiceAndGetContextSensitiveFieldSetDTO(
            final GameSpecies species, final ObservationType observationType) {

        final GameSpeciesObservationMetadataDTO metadataDTO =
                service.getObservationFieldMetadataForSingleSpecies(species, DEFAULT_METADATA_VERSION, false);
        return getContextSensitiveFieldSetExpectingExactlyOneExists(metadataDTO, observationType);
    }

    private GameSpecies newGameSpecies() {
        final GameSpecies species = model().newGameSpecies();
        newObservationBaseFields(species);
        return species;
    }

    private ObservationBaseFields newObservationBaseFields(final GameSpecies species) {
        return model().newObservationBaseFields(species, DEFAULT_METADATA_VERSION);
    }

    private ObservationContextSensitiveFields newObservationContextSensitiveFields(
            final GameSpecies species, final Required stateReq, final Required markingReq) {

        final ObservationContextSensitiveFields ctxFields =
                newObservationContextSensitiveFields(species, some(ObservationType.class));

        ctxFields.setWounded(stateReq);
        ctxFields.setDead(stateReq);
        ctxFields.setOnCarcass(stateReq);

        ctxFields.setCollarOrRadioTransmitter(markingReq);
        ctxFields.setLegRingOrWingMark(markingReq);
        ctxFields.setEarMark(markingReq);

        return ctxFields;
    }

    private ObservationContextSensitiveFields newObservationContextSensitiveFields(
            final GameSpecies species, final ObservationType observationType) {

        return model().newObservationContextSensitiveFields(species, false, observationType, DEFAULT_METADATA_VERSION);
    }

}
