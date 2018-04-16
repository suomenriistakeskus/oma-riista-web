package fi.riista.feature.huntingclub.moosedatacard.validation;

import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.diaryEntryMissingDate;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.harvestDateNotWithinPermittedSeason;
import static fi.riista.util.DateUtil.today;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.kscs.util.jaxb.Copyable;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.Has2BeginEndDates;
import fi.riista.feature.common.entity.Has2BeginEndDatesDTO;
import fi.riista.feature.huntingclub.moosedatacard.MooseDataCardHarvest;
import fi.riista.util.Asserts;

import javaslang.control.Validation;

import org.joda.time.LocalDate;
import org.junit.Test;

import javax.annotation.Nonnull;

import java.util.List;
import java.util.function.Consumer;

public abstract class MooseDataCardHarvestValidatorTest<T extends MooseDataCardHarvest & Copyable> {

    protected static final GeoLocation DEFAULT_COORDINATES = new GeoLocation(1, 1);

    protected abstract MooseDataCardHarvestValidator<T> getValidator(
            @Nonnull Has2BeginEndDates permitSeason, @Nonnull GeoLocation defaultCoordinates);

    protected abstract T newHarvest();

    @Test
    public void testMissingDate() {
        final T input = newHarvest();
        input.setDate(null);
        Asserts.assertValidationErrors(validate(input, newSeason()), diaryEntryMissingDate(input));
    }

    @Test
    public void testDateNotWithinPermittedSeason() {
        final LocalDate today = today();

        final T input = newHarvest();
        input.setDate(today);

        final Has2BeginEndDates permitSeason = newSeason(today.plusDays(1), today.plusDays(2));

        Asserts.assertValidationErrors(
                validate(input, permitSeason),
                harvestDateNotWithinPermittedSeason(input, permitSeason));
    }

    @Test
    public void testMissingLatitude() {
        testValidate_expectDefaultCoordinates(harvest -> harvest.setLatitude(null));
    }

    @Test
    public void testMissingLongitude() {
        testValidate_expectDefaultCoordinates(harvest -> harvest.setLongitude(null));
    }

    private void testValidate_expectDefaultCoordinates(final Consumer<T> harvestMutator) {
        final T input = newHarvest();
        harvestMutator.accept(input);
        Asserts.assertValid(
                validate(input, newSeason()), output -> assertEquals(DEFAULT_COORDINATES, output.getGeoLocation()));
    }

    @Test
    public void testValidCommonFields() {
        final T input = newHarvest();

        Asserts.assertValid(validate(input, newSeason()), output -> {
            assertEquals(input.getDate(), output.getDate());
            assertEquals(input.getGeoLocation(), output.getGeoLocation());
            assertEquals(input.getWeightEstimated(), output.getWeightEstimated());
            assertEquals(input.getAdditionalInfo(), output.getAdditionalInfo());
            assertEquals(input.getFitnessClass(), output.getFitnessClass());
            assertEquals(input.getAdditionalInfo(), output.getAdditionalInfo());
            assertEquals(input.isNotEdible(), output.isNotEdible());
        });
    }

    @Test
    public void testNonMandatoryCommonFieldsWhenNull() {
        final T input = newHarvest();
        input.setWeightEstimated(null);
        input.setWeightMeasured(null);
        input.setFitnessClass(null);
        input.setAdditionalInfo(null);

        Asserts.assertValid(validate(input, newSeason()), output -> {
            assertNull(output.getWeightEstimated());
            assertNull(output.getAdditionalInfo());
            assertNull(output.getFitnessClass());
            assertNull(output.getAdditionalInfo());
        });
    }

    @Test
    public void testIllegalFitnessClass() {
        final T input = newHarvest();
        input.setFitnessClass("invalid");
        Asserts.assertValid(validate(input, newSeason()), output -> assertNull(output.getFitnessClass()));
    }

    protected static Has2BeginEndDates newSeason() {
        final LocalDate today = today();
        return newSeason(today, today.plusYears(1));
    }

    protected static Has2BeginEndDates newSeason(final LocalDate startDate, final LocalDate endDate) {
        return new Has2BeginEndDatesDTO(startDate, endDate);
    }

    protected Validation<List<String>, T> validate(final T input, final Has2BeginEndDates permitSeason) {
        return getValidator(permitSeason, DEFAULT_COORDINATES).validate(input);
    }

}
