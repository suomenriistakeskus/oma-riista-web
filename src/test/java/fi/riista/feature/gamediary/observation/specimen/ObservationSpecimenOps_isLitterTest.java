package fi.riista.feature.gamediary.observation.specimen;

import com.google.common.collect.Streams;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.List;
import java.util.stream.IntStream;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BEAR;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_LYNX;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_MOOSE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WOLF;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WOLVERINE;
import static fi.riista.feature.gamediary.GameSpecies.isLargeCarnivore;
import static fi.riista.feature.gamediary.GameSpecies.isWolf;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.generate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(Parameterized.class)
public class ObservationSpecimenOps_isLitterTest {

    private static final ObservedGameAge NULL_AGE = null;

    // Produces 1215 combinations.
    @Parameters(name = "{index}: speciesCode={0}; numAdult={1}; numLT1Y={2}; num1To2Y={3}; numUnknown={4}; numNullAge={5}")
    public static Iterable<Object[]> data() {
        return IntStream
                .of(OFFICIAL_CODE_BEAR, OFFICIAL_CODE_LYNX, OFFICIAL_CODE_WOLF, OFFICIAL_CODE_WOLVERINE,
                        OFFICIAL_CODE_MOOSE)
                .boxed()
                .flatMap(speciesCode -> IntStream.of(0, 1, 2)
                        .boxed()
                        .flatMap(numAdult -> IntStream.of(0, 1, 2)
                                .boxed()
                                .flatMap(numLT1Y -> IntStream.of(0, 1, 2)
                                        .boxed()
                                        .flatMap(num1To2Y -> IntStream.of(0, 1, 2)
                                                .boxed()
                                                .flatMap(numUnknown -> IntStream.of(0, 1, 2)
                                                        .boxed()
                                                        .map(numNullAge -> new Object[] {
                                                                speciesCode, numAdult, numLT1Y, num1To2Y, numUnknown,
                                                                numNullAge
                                                        }))))))
                .collect(toList());
    }

    @Parameter(0)
    public int gameSpeciesCode;

    @Parameter(1)
    public int numAdult;

    @Parameter(2)
    public int numLT1Y;

    @Parameter(3)
    public int num1To2Y;

    @Parameter(4)
    public int numUnknown;

    @Parameter(5)
    public int numNullAge;

    private List<ObservationSpecimen> specimens;

    @Before
    public void setup() {
        specimens = Streams
                .concat(generate(() -> ObservedGameAge.ADULT).limit(numAdult),
                        generate(() -> ObservedGameAge.LT1Y).limit(numLT1Y),
                        generate(() -> ObservedGameAge._1TO2Y).limit(num1To2Y),
                        generate(() -> ObservedGameAge.UNKNOWN).limit(numUnknown),
                        generate(() -> NULL_AGE).limit(numNullAge))
                .map(age -> {
                    final ObservationSpecimen specimen = new ObservationSpecimen();
                    specimen.setAge(age);
                    return specimen;
                })
                .collect(toList());
    }

    @Test
    public void testIsLitter() {
        final Boolean result = ObservationSpecimenOps.isLitter(gameSpeciesCode, specimens);

        if (!isLargeCarnivore(gameSpeciesCode) || isWolf(gameSpeciesCode)) {
            assertNull(result);
        } else {
            assertEquals(numAdult == 1 && numLT1Y > 0, result);
        }
    }
}
