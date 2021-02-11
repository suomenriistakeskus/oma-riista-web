package fi.riista.feature.gamediary.observation.metadata;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import fi.riista.feature.common.entity.Required;
import fi.riista.feature.common.entity.RequiredWithinDeerPilot;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.observation.ObservationCategory;
import fi.riista.feature.gamediary.observation.ObservationSpecVersion;
import fi.riista.feature.gamediary.observation.ObservationType;
import fi.riista.feature.gamediary.observation.metadata.GameSpeciesObservationFieldRequirementsDTO.ContextSensitiveFieldSetDTO;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static fi.riista.feature.common.entity.Required.NO;
import static fi.riista.feature.common.entity.Required.YES;
import static fi.riista.feature.gamediary.observation.ObservationCategory.MOOSE_HUNTING;
import static fi.riista.feature.gamediary.observation.ObservationCategory.NORMAL;
import static fi.riista.feature.gamediary.observation.ObservationType.JALKI;
import static fi.riista.feature.gamediary.observation.ObservationType.NAKO;
import static fi.riista.feature.gamediary.observation.ObservationType.RIISTAKAMERA;
import static fi.riista.feature.gamediary.observation.ObservationType.ULOSTE;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ObservationFieldsMetadataServiceTest extends EmbeddedDatabaseTest {

    private static final ObservationSpecVersion DEFAULT_SPEC_VERSION = ObservationSpecVersion.MOST_RECENT;

    @Resource
    private ObservationFieldsMetadataService service;

    @Test
    public void testGetObservationFieldMetadataForSingleSpecies_forSelectionOfBaseFields() {
        final GameSpecies species = model().newGameSpecies();

        // Should be used as input.
        newBaseFields(species, NO);
        newContextSensitiveFields(species, NORMAL, NAKO);

        // differing metadata version
        newBaseFields(species, YES).setMetadataVersion(Integer.MAX_VALUE);

        // differing species
        newBaseFields(model().newGameSpecies(), YES);

        persistInNewTransaction();

        final GameSpeciesObservationMetadataDTO metadata = getMetadataForSpecies(species);

        assertTrue(Objects.equal(species.getId(), metadata.getGameSpeciesId()));
        assertEquals(species.getOfficialCode(), metadata.getGameSpeciesCode());
        assertEquals(
                RequiredWithinDeerPilot.NO,
                metadata.getBaseFields().get(ObservationFieldRequirements.FIELD_WITHIN_MOOSE_HUNTING));
    }

    @Test
    public void testGetObservationFieldMetadataForSingleSpecies_forSelectionOfContextSensitiveFields() {
        final GameSpecies species = model().newGameSpecies();

        // Should be used as input.
        newBaseFields(species, YES);
        newContextSensitiveFields(species, MOOSE_HUNTING, NAKO);
        newContextSensitiveFields(species, NORMAL, JALKI);

        // differing metadata version
        newContextSensitiveFields(species, MOOSE_HUNTING, RIISTAKAMERA).setMetadataVersion(Integer.MAX_VALUE);

        // differing species
        newContextSensitiveFields(model().newGameSpecies(), MOOSE_HUNTING, ULOSTE);

        persistInNewTransaction();

        final GameSpeciesObservationMetadataDTO metadata = getMetadataForSpecies(species);

        assertTrue(Objects.equal(species.getId(), metadata.getGameSpeciesId()));
        assertEquals(species.getOfficialCode(), metadata.getGameSpeciesCode());
        assertEquals(2, metadata.getContextSensitiveFieldSets().size());

        final Set<ObservationType> resultTypes =
                metadata.getContextSensitiveFieldSets().stream().map(dto -> dto.getType()).collect(toSet());

        assertEquals(EnumSet.of(NAKO, JALKI), resultTypes);
    }

    @Test
    public void testGetObservationFieldsMetadata_isComposedOfMetadataForSingleSpecies() {
        final GameSpecies species1 = model().newGameSpecies();
        newBaseFields(species1, NO);
        randomizeRequirements(newContextSensitiveFields(species1, NORMAL, NAKO));
        randomizeRequirements(newContextSensitiveFields(species1, NORMAL, JALKI));

        final GameSpecies species2 = model().newGameSpecies();
        newBaseFields(species2, YES);
        randomizeRequirements(newContextSensitiveFields(species2, MOOSE_HUNTING, NAKO));
        randomizeRequirements(newContextSensitiveFields(species2, NORMAL, JALKI));

        // differing metadata version for this species
        newBaseFields(species2, NO).setMetadataVersion(Integer.MAX_VALUE);
        newContextSensitiveFields(species2, NORMAL, RIISTAKAMERA).setMetadataVersion(Integer.MAX_VALUE);

        persistInNewTransaction();

        final ObservationMetadataDTO result = service.getObservationFieldsMetadata(DEFAULT_SPEC_VERSION);

        final List<GameSpeciesObservationFieldRequirementsDTO> speciesList = result.getSpeciesList();
        assertEquals(2, speciesList.size());

        assertEqual(getMetadataForSpecies(species1), speciesList.get(0));
        assertEqual(getMetadataForSpecies(species2), speciesList.get(1));
    }

    private static void assertEqual(final GameSpeciesObservationFieldRequirementsDTO expected,
                                    final GameSpeciesObservationFieldRequirementsDTO actual) {

        assertEquals(expected.getGameSpeciesCode(), actual.getGameSpeciesCode());
        assertEquals(Maps.filterValues(expected.getBaseFields(), val -> val != RequiredWithinDeerPilot.NO), actual.getBaseFields());
        assertEquals(expected.getSpecimenFields(), actual.getSpecimenFields());

        final int numExpectedCtxFieldsets = expected.getContextSensitiveFieldSets().size();
        assertEquals(numExpectedCtxFieldsets, actual.getContextSensitiveFieldSets().size());

        for (int i = 0; i < numExpectedCtxFieldsets; i++) {
            assertEqual(expected.getContextSensitiveFieldSets().get(i), actual.getContextSensitiveFieldSets().get(i));
        }
    }

    private static void assertEqual(final ContextSensitiveFieldSetDTO expected,
                                    final ContextSensitiveFieldSetDTO actual) {

        final Predicate<Enum<?>> filter =
                required -> required != Required.NO && required != DynamicObservationFieldPresence.NO;

        assertEquals(expected.isWithinMooseHunting(), actual.isWithinMooseHunting());
        assertEquals(expected.getType(), actual.getType());
        assertEquals(Maps.filterValues(expected.getBaseFields(), filter), actual.getBaseFields());
        assertEquals(Maps.filterValues(expected.getSpecimenFields(), filter), actual.getSpecimenFields());
        assertEquals(expected.getAllowedAges(), actual.getAllowedAges());
        assertEquals(expected.getAllowedStates(), actual.getAllowedStates());
        assertEquals(expected.getAllowedMarkings(), actual.getAllowedMarkings());
    }

    private GameSpeciesObservationMetadataDTO getMetadataForSpecies(final GameSpecies species) {
        final int speciesCode = species.getOfficialCode();
        return service.getObservationFieldMetadataForSingleSpecies(speciesCode, DEFAULT_SPEC_VERSION, false);
    }

    private ObservationBaseFields newBaseFields(final GameSpecies species, final Required withinMooseHunting) {
        return model().newObservationBaseFields(species, withinMooseHunting, DEFAULT_SPEC_VERSION);
    }

    private ObservationContextSensitiveFields newContextSensitiveFields(final GameSpecies species,
                                                                        final ObservationCategory observationCategory,
                                                                        final ObservationType observationType) {

        return model().newObservationContextSensitiveFields(
                species, observationCategory, observationType, DEFAULT_SPEC_VERSION);
    }

    private void randomizeRequirements(final ObservationContextSensitiveFields ctxFields) {
        ctxFields.setAge(some(Required.class));
        ctxFields.setExtendedAgeRange(ctxFields.getAge() == NO ? false : someBoolean());
        ctxFields.setGender(some(Required.class));
        ctxFields.setWounded(some(Required.class));
        ctxFields.setDead(some(Required.class));
        ctxFields.setOnCarcass(some(Required.class));
        ctxFields.setCollarOrRadioTransmitter(some(Required.class));
        ctxFields.setLegRingOrWingMark(some(Required.class));
        ctxFields.setEarMark(some(Required.class));
    }
}
