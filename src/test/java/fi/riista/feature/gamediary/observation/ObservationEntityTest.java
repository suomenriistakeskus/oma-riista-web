package fi.riista.feature.gamediary.observation;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ObservationEntityTest {

    @Test
    public void testGetTotalAmountOfMooselikeAmountFields() {
        final Observation observation = new Observation().withMooselikeAmounts(3, 5, 7, 11, 13, 17, 19);
        assertEquals(
                3 + 5 + 2 * 7 + 3 * 11 + 4 * 13 + 5 * 17 + 19,
                observation.getTotalAmountOfMooselikeAmountFields());
    }
}
