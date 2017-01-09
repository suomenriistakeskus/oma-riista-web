package fi.riista.feature.huntingclub.moosedatacard.converter;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.HasMooseDataCardEncoding;
import fi.riista.feature.gamediary.GameDiaryService;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.observation.ObservationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.huntingclub.moosedatacard.MooseDataCardObjectFactory;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardLargeCarnivoreObservation;

import javaslang.Tuple;
import javaslang.Tuple2;

import org.junit.Before;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@Transactional
public class MooseDataCardLargeCarnivoreObservationConverterTest
        extends MooseDataCardObservationConverterTestBase<MooseDataCardLargeCarnivoreObservation> {

    @Resource
    private GameDiaryService diaryService;

    private GameSpecies bear;
    private GameSpecies lynx;
    private GameSpecies wolf;
    private GameSpecies wolverine;

    @Before
    public void setup() {
        bear = model().newGameSpecies(GameSpecies.OFFICIAL_CODE_BEAR);
        lynx = model().newGameSpecies(GameSpecies.OFFICIAL_CODE_LYNX);
        wolf = model().newGameSpecies(GameSpecies.OFFICIAL_CODE_WOLF);
        wolverine = model().newGameSpecies(GameSpecies.OFFICIAL_CODE_WOLVERINE);

        persistInNewTransaction();
    }

    @Override
    protected MooseDataCardLargeCarnivoreObservation newSourceObject() {
        return MooseDataCardObjectFactory.newLargeCarnivoreObservation();
    }

    @Override
    protected Stream<Observation> convert(
            final MooseDataCardLargeCarnivoreObservation source,
            final Person contactPerson,
            final GeoLocation defaultCoordinates) {

        return new MooseDataCardLargeCarnivoreObservationConverter(diaryService, contactPerson, defaultCoordinates)
                .apply(source);
    }

    @Test
    public void testConversionOfCarnivoreSpecificFields() {
        final MooseDataCardLargeCarnivoreObservation source = newSourceObject();

        final ObservationType expectedObservationType = HasMooseDataCardEncoding
                .enumOf(ObservationType.class, source.getObservationType())
                .getOrElseGet(invalid -> null);

        final List<Tuple2<GameSpecies, Integer>> expectedSpeciesAmountTuples = asList(
                Tuple.of(wolf, source.getNumberOfWolves()),
                Tuple.of(bear, source.getNumberOfBears()),
                Tuple.of(lynx, source.getNumberOfLynxes()),
                Tuple.of(wolverine, source.getNumberOfWolverines()));

        final List<Observation> observations = convert(source, new Person(), geoLocation()).collect(toList());
        assertEquals(4, observations.size());

        observations.forEach(observation -> {
            assertEquals(expectedObservationType, observation.getObservationType());
            assertEquals(source.getAdditionalInfo(), observation.getDescription());
        });

        assertEquals(
                expectedSpeciesAmountTuples,
                observations.stream().map(obs -> Tuple.of(obs.getSpecies(), obs.getAmount())).collect(toList()));
    }

    @Test
    public void testConversionWhenWolfAmountMissing() {
        assertEquals(asList(bear, lynx, wolverine), convertToSpeciesList(newSourceObject().withNumberOfWolves(null)));
    }

    @Test
    public void testConversionWhenBearAmountMissing() {
        assertEquals(asList(wolf, lynx, wolverine), convertToSpeciesList(newSourceObject().withNumberOfBears(null)));
    }

    @Test
    public void testConversionWhenLynxAmountMissing() {
        assertEquals(asList(wolf, bear, wolverine), convertToSpeciesList(newSourceObject().withNumberOfLynxes(null)));
    }

    @Test
    public void testConversionWhenWolverineAmountMissing() {
        assertEquals(asList(wolf, bear, lynx), convertToSpeciesList(newSourceObject().withNumberOfWolverines(null)));
    }

    @Test
    public void testConversionWhenAllAmountsZero() {
        assertEquals(Collections.emptyList(), convertToSpeciesList(newSourceObject()
                .withNumberOfWolves(0)
                .withNumberOfBears(0)
                .withNumberOfLynxes(0)
                .withNumberOfWolverines(0)));
    }

    private List<GameSpecies> convertToSpeciesList(final MooseDataCardLargeCarnivoreObservation source) {
        return convert(source, new Person(), geoLocation())
                .map(Observation::getSpecies)
                .collect(toList());
    }

    // Superclass tests overridden to wrap transactional context around.

    @Override
    @Test
    public void testCommonObservationFields() {
        super.testCommonObservationFields();
    }

    @Override
    @Test
    public void testMissingDate() {
        super.testMissingDate();
    }

    @Override
    @Test
    public void testMissingGeoLocation() {
        super.testMissingGeoLocation();
    }

}
