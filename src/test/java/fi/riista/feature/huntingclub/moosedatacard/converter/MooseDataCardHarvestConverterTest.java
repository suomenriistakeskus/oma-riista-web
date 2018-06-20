package fi.riista.feature.huntingclub.moosedatacard.converter;

import com.kscs.util.jaxb.Copyable;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.HasMooseDataCardEncoding;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.specimen.GameFitnessClass;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.moosedatacard.MooseDataCardHarvest;
import fi.riista.feature.organization.person.Person;
import fi.riista.test.EmbeddedDatabaseTest;
import io.vavr.Tuple2;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

import static fi.riista.test.TestUtils.ld;
import static fi.riista.util.DateUtil.copyDateForHuntingYear;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public abstract class MooseDataCardHarvestConverterTest<T extends MooseDataCardHarvest & Copyable>
        extends EmbeddedDatabaseTest {

    private static final GeoLocation DEFAULT_COORDINATES = new GeoLocation(1, 1);

    protected HarvestPermitSpeciesAmount mooseSpeciesAmount;

    @Before
    public void initSpeciesAmount() {
        mooseSpeciesAmount = new HarvestPermitSpeciesAmount();
        mooseSpeciesAmount.setGameSpecies(new GameSpecies());
        mooseSpeciesAmount.setBeginDate(ld(2015, 9, 1));
        mooseSpeciesAmount.setEndDate(ld(2015, 12, 31));
    }

    protected abstract MooseDataCardHarvestConverter<T> newConverter(@Nonnull HarvestPermitSpeciesAmount speciesAmount,
                                                                     @Nonnull Person person,
                                                                     @Nonnull GeoLocation defaultCoordinates);

    protected abstract T newHarvestSource(@Nullable LocalDate date);

    protected T newHarvestSourceWithinSeason() {
        return newHarvestSource(mooseSpeciesAmount.getFirstDate());
    }

    @Test
    public void testCommonHarvestFields() {
        final T source = newHarvestSourceWithinSeason();

        testConversion(source, harvest -> {

            assertEquals(
                    source.getDate().toLocalDateTime(MooseDataCardHarvestConverter.DEFAULT_ENTRY_TIME).toDate(),
                    harvest.getPointOfTime());
            assertEquals(source.getGeoLocation(), harvest.getGeoLocation());

        }, specimen -> {

            assertEquals(source.getWeightEstimated(), specimen.getWeightEstimated());
            assertEquals(source.getWeightMeasured(), specimen.getWeightMeasured());
            assertEquals(
                    HasMooseDataCardEncoding.getEnum(GameFitnessClass.class, source.getFitnessClass()),
                    specimen.getFitnessClass());
            assertEquals(source.getAdditionalInfo(), specimen.getAdditionalInfo());

        });
    }

    @Test(expected = IllegalStateException.class)
    public void testMissingDate() {
        convert(newHarvestSource(null), new Person());
    }

    @Test
    public void testCorrectionOfWrongHuntingYear() {
        final LocalDate firstPermitDate = mooseSpeciesAmount.getFirstDate();
        final T input = newHarvestSource(firstPermitDate.plusYears(2));

        testConversion(input, harvest -> assertEquals(firstPermitDate, harvest.getPointOfTimeAsLocalDate()), s -> {});
    }

    @Test
    public void testMissingGeoLocation() {
        final T source = newHarvestSourceWithinSeason();
        source.setGeoLocation(null);

        testConversion(source, harvest -> assertEquals(DEFAULT_COORDINATES, harvest.getGeoLocation()), specimen -> {});
    }

    @Test
    public void testInvalidCommonHarvestFields() {
        final T source = newHarvestSourceWithinSeason();
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
        final T source = newHarvestSourceWithinSeason();
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

    protected void testConversion(final T source,
                                  final Consumer<Harvest> harvestAssertions,
                                  final Consumer<HarvestSpecimen> specimenAssertions) {

        final Person contactPerson = new Person();

        final Tuple2<Harvest, HarvestSpecimen> result = convert(source, contactPerson);
        assertNotNull(result);

        final Harvest harvest = result._1;
        assertNotNull(harvest);
        assertEquals(mooseSpeciesAmount.getGameSpecies(), harvest.getSpecies());
        assertEquals(contactPerson, harvest.getAuthor());
        assertEquals(contactPerson, harvest.getActualShooter());

        final LocalDate outputDate = harvest.getPointOfTimeAsLocalDate();
        assertEquals(copyDateForHuntingYear(source.getDate(), mooseSpeciesAmount.resolveHuntingYear()), outputDate);
        assertTrue(mooseSpeciesAmount.containsDate(outputDate));

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

    protected Tuple2<Harvest, HarvestSpecimen> convert(final T source, final Person person) {
        return newConverter(mooseSpeciesAmount, person, DEFAULT_COORDINATES).apply(source);
    }
}
