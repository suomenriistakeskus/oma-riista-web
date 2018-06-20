package fi.riista.feature.gamediary.observation.specimen;

import fi.riista.util.F;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BEAR;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_LYNX;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_MOOSE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WOLF;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WOLVERINE;
import static fi.riista.feature.gamediary.GameSpecies.isWolf;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(Parameterized.class)
public class ObservationSpecimenOps_isPackTest {

    @Parameters(name = "{index}: speciesCode={0}; amount={1}")
    public static Iterable<Object[]> data() {
        return IntStream
                .of(OFFICIAL_CODE_BEAR, OFFICIAL_CODE_LYNX, OFFICIAL_CODE_WOLF, OFFICIAL_CODE_WOLVERINE,
                        OFFICIAL_CODE_MOOSE)
                .boxed()
                .flatMap(speciesCode -> {
                    return Stream.of(null, 0, 1, 2, 3, 4, 10, 999)
                            .map(amount -> new Object[] { speciesCode, amount });
                })
                .collect(toList());
    }

    @Parameter(0)
    public int gameSpeciesCode;

    @Parameter(1)
    public Integer amount;

    @Test
    public void testIsPack() {
        final Boolean result = ObservationSpecimenOps.isPack(gameSpeciesCode, amount);

        if (!isWolf(gameSpeciesCode)) {
            assertNull(result);
        } else {
            assertEquals(F.coalesceAsInt(amount, 0) >= 3, result);
        }
    }
}
