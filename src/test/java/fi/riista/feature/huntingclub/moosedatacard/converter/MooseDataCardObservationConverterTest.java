package fi.riista.feature.huntingclub.moosedatacard.converter;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.moosedatacard.DateAndLocation;
import fi.riista.feature.organization.person.Person;
import fi.riista.test.EmbeddedDatabaseTest;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Stream;

import static fi.riista.test.TestUtils.ld;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public abstract class MooseDataCardObservationConverterTest<T extends DateAndLocation>
        extends EmbeddedDatabaseTest {

    protected HarvestPermitSpeciesAmount mooseSpeciesAmount;

    @Before
    public void setup() {
        mooseSpeciesAmount = new HarvestPermitSpeciesAmount();
        mooseSpeciesAmount.setGameSpecies(new GameSpecies());
        mooseSpeciesAmount.setBeginDate(ld(2015, 9, 1));
        mooseSpeciesAmount.setEndDate(ld(2015, 12, 31));
    }

    protected abstract Stream<Observation> convert(T source, Person contactPerson, GeoLocation defaultCoordinates);

    protected Stream<Observation> convert(T source) {
        return convert(source, new Person(), geoLocation());
    }

    protected abstract T newObservationSource(@Nullable final LocalDate date);

    protected T newObservationSourceWithinSeason() {
        return newObservationSource(mooseSpeciesAmount.getFirstDate());
    }

    @Test
    public void testCommonObservationFields() {
        final T source = newObservationSourceWithinSeason();
        final Person contactPerson = new Person();

        final List<Observation> results = convert(source, contactPerson, geoLocation()).collect(toList());
        assertFalse(results.isEmpty());

        results.forEach(observation -> {
            assertEquals(contactPerson, observation.getAuthor());
            assertEquals(contactPerson, observation.getObserver());

            assertEquals(
                    source.getDate().toLocalDateTime(MooseDataCardObservationConverter.DEFAULT_ENTRY_TIME).toDate(),
                    observation.getPointOfTime());

            final GeoLocation location = observation.getGeoLocation();
            assertNotNull(location);
            assertEquals(source.getGeoLocation(), location);
            assertEquals(GeoLocation.Source.MANUAL, location.getSource());

            assertNotNull(observation.getAmount());

            assertFalse(observation.isFromMobile());
            assertEquals(Boolean.TRUE, observation.getWithinMooseHunting());
        });
    }

    @Test
    public void testMissingDate() {
        assertEmptyStreamResult(newObservationSource(null));
    }

    @Test
    public void testCorrectionOfWrongHuntingYear() {
        final LocalDate firstPermitDate = mooseSpeciesAmount.getFirstDate();
        final T input = newObservationSource(firstPermitDate.plusYears(2));

        final List<Observation> result = convert(input).collect(toList());
        assertFalse(result.isEmpty());

        convert(input).forEach(observation -> assertEquals(firstPermitDate, observation.getPointOfTimeAsLocalDate()));
    }

    @Test
    public void testMissingGeoLocation() {
        final T source = newObservationSourceWithinSeason();
        source.setGeoLocation(null);

        final GeoLocation defaultCoordinates = geoLocation();
        defaultCoordinates.setSource(GeoLocation.Source.MANUAL);

        final List<Observation> results = convert(source, new Person(), defaultCoordinates).collect(toList());
        assertFalse(results.isEmpty());

        results.forEach(observation -> assertEquals(defaultCoordinates, observation.getGeoLocation()));
    }

    protected void assertEmptyStreamResult(final T input) {
        assertEquals(0L, convert(input).count());
    }
}
