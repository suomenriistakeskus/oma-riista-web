package fi.riista.feature.huntingclub.moosedatacard.converter;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.observation.ObservationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardObservation;
import fi.riista.feature.huntingclub.moosedatacard.validation.MooseDataCardMooseObservationValidator;

import javax.annotation.Nonnull;

import java.util.Objects;
import java.util.Optional;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

public class MooseDataCardMooseObservationConverter
        extends MooseDataCardObservationConverter<MooseDataCardObservation> {

    private final GameSpecies mooseSpecies;

    public MooseDataCardMooseObservationConverter(
            @Nonnull final GameSpecies mooseSpecies,
            @Nonnull final Person contactPerson,
            @Nonnull final GeoLocation defaultCoordinates) {

        super(new MooseDataCardMooseObservationValidator(defaultCoordinates), contactPerson);
        this.mooseSpecies = Objects.requireNonNull(mooseSpecies);
    }

    @Override
    public Stream<Observation> apply(@Nonnull final MooseDataCardObservation input) {
        final ToIntFunction<Integer> toIntFn = n -> Optional.ofNullable(n).orElse(0);

        return validateToStream(input).map(validInput -> {

            final Observation observation = createObservation(validInput).withMooselikeAmounts(
                    toIntFn.applyAsInt(validInput.getAU()),
                    toIntFn.applyAsInt(validInput.getN0()),
                    toIntFn.applyAsInt(validInput.getN1()),
                    toIntFn.applyAsInt(validInput.getN2()),
                    toIntFn.applyAsInt(validInput.getN3()),
                    null,
                    toIntFn.applyAsInt(validInput.getT()));

            observation.setSpecies(mooseSpecies);
            observation.setObservationType(ObservationType.NAKO);

            return observation;
        });
    }

}
