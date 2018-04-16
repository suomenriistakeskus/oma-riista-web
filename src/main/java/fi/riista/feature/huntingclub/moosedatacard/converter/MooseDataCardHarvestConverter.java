package fi.riista.feature.huntingclub.moosedatacard.converter;

import com.kscs.util.jaxb.Copyable;

import fi.riista.feature.common.entity.HasMooseDataCardEncoding;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.specimen.GameFitnessClass;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.huntingclub.moosedatacard.MooseDataCardHarvest;
import fi.riista.feature.huntingclub.moosedatacard.validation.MooseDataCardHarvestValidator;
import fi.riista.feature.organization.person.Person;

import javaslang.Tuple;
import javaslang.Tuple2;

import org.joda.time.LocalTime;

import javax.annotation.Nonnull;

import java.util.Objects;
import java.util.function.Function;

public abstract class MooseDataCardHarvestConverter<T extends MooseDataCardHarvest & Copyable>
        implements Function<T, Tuple2<Harvest, HarvestSpecimen>> {

    protected static final LocalTime DEFAULT_ENTRY_TIME = new LocalTime(9, 0);

    protected final MooseDataCardHarvestValidator<T> validator;
    protected final GameSpecies mooseSpecies;
    protected final Person contactPerson;

    public MooseDataCardHarvestConverter(@Nonnull final MooseDataCardHarvestValidator<T> validator,
                                         @Nonnull final GameSpecies mooseSpecies,
                                         @Nonnull final Person contactPerson) {

        this.validator = Objects.requireNonNull(validator, "validator is null");
        this.mooseSpecies = Objects.requireNonNull(mooseSpecies, "mooseSpecies is null");
        this.contactPerson = Objects.requireNonNull(contactPerson, "contactPerson is null");
    }

    @Override
    public final Tuple2<Harvest, HarvestSpecimen> apply(@Nonnull final T source) {
        Objects.requireNonNull(source);

        return validator.validate(source)
                .map(this::convert)
                .getOrElseThrow(() -> new IllegalStateException("Invalid harvest should not have passed validation"));
    }

    protected Tuple2<Harvest, HarvestSpecimen> convert(@Nonnull final T validSource) {
        final Harvest harvest = new Harvest();
        harvest.setSpecies(mooseSpecies);
        harvest.setAuthor(contactPerson);
        harvest.setActualShooter(contactPerson);
        harvest.setAmount(1);

        final HarvestSpecimen specimen =
                new HarvestSpecimen(harvest, getAge(validSource), getGender(validSource), null);

        copyCommonHarvestFields(validSource, harvest, specimen);

        return Tuple.of(harvest, specimen);
    }

    protected abstract GameAge getAge(@Nonnull final T harvest);

    protected abstract GameGender getGender(@Nonnull final T harvest);

    protected static void copyCommonHarvestFields(
            final MooseDataCardHarvest source, final Harvest harvest, final HarvestSpecimen specimen) {

        harvest.setFromMobile(false);

        harvest.setGeoLocation(source.getGeoLocation());
        harvest.setPointOfTime(source.getDate().toLocalDateTime(DEFAULT_ENTRY_TIME).toDate());

        specimen.setWeightEstimated(source.getWeightEstimated());
        specimen.setWeightMeasured(source.getWeightMeasured());
        specimen.setAdditionalInfo(source.getAdditionalInfo());
        specimen.setNotEdible(source.isNotEdible());

        specimen.setFitnessClass(
                HasMooseDataCardEncoding.getEnumOrNull(GameFitnessClass.class, source.getFitnessClass()));
    }

}
