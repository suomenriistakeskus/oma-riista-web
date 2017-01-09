package fi.riista.feature.huntingclub.moosedatacard.converter;

import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.huntingclub.moosedatacard.DateAndLocation;
import fi.riista.feature.huntingclub.moosedatacard.validation.MooseDataCardObservationValidator;

import org.joda.time.LocalTime;

import javax.annotation.Nonnull;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class MooseDataCardObservationConverter<T extends DateAndLocation>
        implements Function<T, Stream<Observation>> {

    protected static final LocalTime DEFAULT_ENTRY_TIME = new LocalTime(9, 0);

    private final MooseDataCardObservationValidator<T> validator;
    private final Person contactPerson;

    public MooseDataCardObservationConverter(
            @Nonnull final MooseDataCardObservationValidator<T> validator, @Nonnull final Person contactPerson) {

        this.validator = Objects.requireNonNull(validator, "validator is null");
        this.contactPerson = Objects.requireNonNull(contactPerson, "contactPerson is null");
    }

    protected Stream<T> validateToStream(@Nonnull final T input) {
        return validator.validate(input).<Stream<T>> fold(abandonReason -> Stream.empty(), Stream::of);
    }

    protected Observation createObservation(final T source) {
        final Observation observation = new Observation();
        observation.setAuthor(contactPerson);
        observation.setObserver(contactPerson);
        observation.setFromMobile(false);
        observation.setWithinMooseHunting(true);
        observation.setGeoLocation(source.getGeoLocation());
        observation.setPointOfTime(source.getDate().toLocalDateTime(DEFAULT_ENTRY_TIME).toDate());
        return observation;
    }

}
