package fi.riista.feature.huntingclub.moosedatacard.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.kscs.util.jaxb.Copyable;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.HasMooseDataCardEncoding;
import fi.riista.feature.gamediary.harvest.specimen.GameFitnessClass;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.huntingclub.moosedatacard.MooseDataCardHarvest;

import javaslang.Tuple2;

import org.junit.Test;

import javax.annotation.Nonnull;

import java.util.function.Consumer;

public abstract class MooseDataCardHarvestConverterTest<T extends MooseDataCardHarvest & Copyable>
        extends EmbeddedDatabaseTest {

    private static final GeoLocation DEFAULT_COORDINATES = new GeoLocation(1, 1);

    protected abstract T newHarvestSource();

    protected abstract MooseDataCardHarvestConverter<T> newConverter(
            @Nonnull GameSpecies mooseSpecies, @Nonnull Person person, @Nonnull GeoLocation defaultCoordinates);

    @Test
    public void testCommonHarvestFields() {
        final T source = newHarvestSource();

        testConversion(source, harvest -> {

            assertEquals(
                    source.getDate().toLocalDateTime(MooseDataCardHarvestConverter.DEFAULT_ENTRY_TIME).toDate(),
                    harvest.getPointOfTime());
            assertEquals(source.getGeoLocation(), harvest.getGeoLocation());

        }, specimen -> {

            assertEquals(source.getWeightEstimated(), specimen.getWeightEstimated());
            assertEquals(source.getWeightMeasured(), specimen.getWeightMeasured());
            assertEquals(HasMooseDataCardEncoding
                    .enumOf(GameFitnessClass.class, source.getFitnessClass())
                    .getOrElseThrow(invalid -> new IllegalStateException("Could not convert fitness class")),
                    specimen.getFitnessClass());
            assertEquals(source.getAdditionalInfo(), specimen.getAdditionalInfo());

        });
    }

    @Test(expected = IllegalStateException.class)
    public void testMissingDate() {
        final T source = newHarvestSource();
        source.setDate(null);
        convert(source, new GameSpecies(), new Person());
    }

    @Test
    public void testMissingGeoLocation() {
        final T source = newHarvestSource();
        source.setGeoLocation(null);
        testConversion(source, harvest -> assertEquals(DEFAULT_COORDINATES, harvest.getGeoLocation()), specimen -> {});
    }

    @Test
    public void testInvalidCommonHarvestFields() {
        final T source = newHarvestSource();
        source.setWeightEstimated(Double.MAX_VALUE);
        source.setWeightMeasured(Double.MAX_VALUE);
        source.setFitnessClass("invalid");

        testConversion(source, harvest -> {}, specimen -> {
            assertNull(specimen.getWeightEstimated());
            assertNull(specimen.getWeightMeasured());
            assertNull(specimen.getFitnessClass());
        });
    }

    @Test
    public void testMissingCommonHarvestFields() {
        final T source = newHarvestSource();
        source.setWeightEstimated(null);
        source.setWeightMeasured(null);
        source.setFitnessClass(null);
        source.setAdditionalInfo(null);

        testConversion(source, harvest -> {}, specimen -> {
            assertNull(specimen.getWeightEstimated());
            assertNull(specimen.getWeightMeasured());
            assertNull(specimen.getFitnessClass());
            assertNull(specimen.getAdditionalInfo());
        });
    }

    protected void testConversion(
            final T source,
            final Consumer<Harvest> harvestAssertions,
            final Consumer<HarvestSpecimen> specimenAssertions) {

        final GameSpecies moose = new GameSpecies();
        final Person contactPerson = new Person();

        final Tuple2<Harvest, HarvestSpecimen> result = convert(source, moose, contactPerson);
        assertNotNull(result);

        final Harvest harvest = result._1;
        assertNotNull(harvest);
        assertEquals(moose, harvest.getSpecies());
        assertEquals(contactPerson, harvest.getAuthor());
        assertEquals(contactPerson, harvest.getActualShooter());

        final GeoLocation location = harvest.getGeoLocation();
        assertNotNull(location);
        assertEquals(GeoLocation.Source.MANUAL, location.getSource());

        assertEquals(1, harvest.getAmount());
        assertEquals(Boolean.FALSE, harvest.getFromMobile());

        final HarvestSpecimen specimen = result._2;
        assertNotNull(specimen);
        assertNotNull(specimen.getAge());
        assertNotNull(specimen.getGender());
        assertEquals(Boolean.valueOf(source.isNotEdible()), specimen.getNotEdible());

        harvestAssertions.accept(harvest);
        specimenAssertions.accept(specimen);
    }

    protected Tuple2<Harvest, HarvestSpecimen> convert(
            final T source, final GameSpecies mooseSpecies, final Person person) {

        return newConverter(mooseSpecies, person, DEFAULT_COORDINATES).apply(source);
    }

}
