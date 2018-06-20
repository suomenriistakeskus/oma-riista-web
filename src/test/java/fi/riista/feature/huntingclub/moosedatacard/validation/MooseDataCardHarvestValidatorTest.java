package fi.riista.feature.huntingclub.moosedatacard.validation;

import com.kscs.util.jaxb.Copyable;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.Has2BeginEndDates;
import fi.riista.feature.common.entity.Has2BeginEndDatesDTO;
import fi.riista.feature.huntingclub.moosedatacard.MooseDataCardHarvest;
import io.vavr.control.Validation;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.diaryEntryMissingDate;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.harvestDateNotWithinPermittedSeason;
import static fi.riista.test.Asserts.assertValid;
import static fi.riista.test.Asserts.assertValidationErrors;
import static fi.riista.test.TestUtils.ld;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public abstract class MooseDataCardHarvestValidatorTest<T extends MooseDataCardHarvest & Copyable> {

    protected static final GeoLocation DEFAULT_COORDINATES = new GeoLocation(1, 1);

    protected Has2BeginEndDates permitSeason;

    @Before
    public void setup() {
        permitSeason = new Has2BeginEndDatesDTO(ld(2015, 9, 1), ld(2015, 12, 31));
    }

    protected abstract MooseDataCardHarvestValidator<T> getValidator(@Nonnull Has2BeginEndDates season,
                                                                     @Nonnull GeoLocation defaultCoordinates);

    protected abstract T newHarvest(@Nullable LocalDate date);

    protected T newHarvestWithinSeason() {
        return newHarvest(permitSeason.getFirstDate());
    }

    @Test
    public void testMissingDate() {
        final T input = newHarvest(null);

        assertValidationErrors(validate(input), diaryEntryMissingDate(input));
    }

    @Test
    public void testDateNotWithinPermittedSeason() {
        final T input = newHarvest(permitSeason.getLastDate().plusDays(1));

        assertValidationErrors(validate(input), harvestDateNotWithinPermittedSeason(input, permitSeason));
    }

    @Test
    public void testCorrectionOfWrongHuntingYear() {
        final LocalDate firstPermitDate = permitSeason.getFirstDate();
        final T input = newHarvest(firstPermitDate.plusYears(2));

        assertValid(validate(input), output -> assertEquals(firstPermitDate, output.getDate()));
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
        final T input = newHarvestWithinSeason();
        harvestMutator.accept(input);

        assertValid(validate(input), output -> assertEquals(DEFAULT_COORDINATES, output.getGeoLocation()));
    }

    @Test
    public void testValidCommonFields() {
        final T input = newHarvestWithinSeason();

        assertValid(validate(input), output -> {
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
        final T input = newHarvestWithinSeason();
        input.setWeightEstimated(null);
        input.setWeightMeasured(null);
        input.setFitnessClass(null);
        input.setAdditionalInfo(null);

        assertValid(validate(input), output -> {
            assertNull(output.getWeightEstimated());
            assertNull(output.getAdditionalInfo());
            assertNull(output.getFitnessClass());
            assertNull(output.getAdditionalInfo());
        });
    }

    @Test
    public void testIllegalFitnessClass() {
        final T input = newHarvestWithinSeason();
        input.setFitnessClass("invalid");

        assertValid(validate(input), output -> assertNull(output.getFitnessClass()));
    }

    protected Validation<List<String>, T> validate(final T input) {
        return getValidator(permitSeason, DEFAULT_COORDINATES).validate(input);
    }
}
