package fi.riista.feature.huntingclub.moosedatacard.converter;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.huntingclub.moosedatacard.DateAndLocation;

import org.junit.Test;

import java.util.List;
import java.util.stream.Stream;

public abstract class MooseDataCardObservationConverterTestBase<T extends DateAndLocation>
        extends EmbeddedDatabaseTest {

    protected abstract T newSourceObject();

    protected abstract Stream<Observation> convert(T source, Person contactPerson, GeoLocation defaultCoordinates);

    @Test
    public void testCommonObservationFields() {
        final T source = newSourceObject();
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
        final T source = newSourceObject();
        source.setDate(null);
        assertEmptyStreamResult(source);
    }

    @Test
    public void testMissingGeoLocation() {
        final T source = newSourceObject();
        source.setGeoLocation(null);

        final GeoLocation defaultCoordinates = geoLocation();
        defaultCoordinates.setSource(GeoLocation.Source.MANUAL);

        final List<Observation> results = convert(source, new Person(), defaultCoordinates).collect(toList());
        assertFalse(results.isEmpty());

        results.forEach(observation -> assertEquals(defaultCoordinates, observation.getGeoLocation()));
    }

    protected void assertEmptyStreamResult(final T input) {
        assertEquals(0L, convert(input, new Person(), geoLocation()).count());
    }

}
