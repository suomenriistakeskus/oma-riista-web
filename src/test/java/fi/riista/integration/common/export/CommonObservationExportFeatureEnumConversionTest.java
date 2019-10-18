package fi.riista.integration.common.export;

import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.harvest.specimen.GameFitnessClass;
import fi.riista.feature.gamediary.observation.ObservationType;
import fi.riista.feature.gamediary.observation.specimen.GameMarking;
import fi.riista.feature.gamediary.observation.specimen.ObservedGameAge;
import fi.riista.feature.gamediary.observation.specimen.ObservedGameState;
import fi.riista.integration.common.export.observations.COBS_GameFitnessClass;
import fi.riista.integration.common.export.observations.COBS_GameGender;
import fi.riista.integration.common.export.observations.COBS_GameMarking;
import fi.riista.integration.common.export.observations.COBS_ObservationType;
import fi.riista.integration.common.export.observations.COBS_ObservedGameAge;
import fi.riista.integration.common.export.observations.COBS_ObservedGameState;
import fi.riista.util.EnumUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CommonObservationExportFeatureEnumConversionTest {

    private static <A extends Enum<A>, B extends Enum<B>>
    void assertEnumValuesMatch(final Class<A> first, final Class<B> second) {
        assertEquals(first.getEnumConstants().length, second.getEnumConstants().length);

        for (final A a : first.getEnumConstants()) {
            final B b = EnumUtils.convertNullableByEnumName(second, a);
            assertEquals(a.name(), b.name());
        }
    }

    @Test
    public void testObservationType() {
        assertEnumValuesMatch(ObservationType.class, COBS_ObservationType.class);
    }

    @Test
    public void testGameFitness() {
        assertEnumValuesMatch(GameFitnessClass.class, COBS_GameFitnessClass.class);
    }
    @Test
    public void testGameGender() {
        assertEnumValuesMatch(GameGender.class, COBS_GameGender.class);
    }

    @Test
    public void testGameAge() {
        assertEnumValuesMatch(ObservedGameAge.class, COBS_ObservedGameAge.class);
    }

    @Test
    public void testObservedGameState() {
        assertEnumValuesMatch(ObservedGameState.class, COBS_ObservedGameState.class);
    }

    @Test
    public void testGameMarking() {
        assertEnumValuesMatch(GameMarking.class, COBS_GameMarking.class);
    }

}
