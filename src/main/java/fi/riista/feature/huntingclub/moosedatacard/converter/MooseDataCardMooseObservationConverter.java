package fi.riista.feature.huntingclub.moosedatacard.converter;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationType;
import fi.riista.feature.huntingclub.moosedatacard.validation.MooseDataCardMooseObservationValidator;
import fi.riista.feature.organization.person.Person;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardObservation;
import fi.riista.util.F;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.stream.Stream;

public class MooseDataCardMooseObservationConverter
        extends MooseDataCardObservationConverter<MooseDataCardObservation> {

    private final GameSpecies mooseSpecies;

    public MooseDataCardMooseObservationConverter(@Nonnull final GameSpecies mooseSpecies,
                                                  @Nonnull final Person contactPerson,
                                                  final int huntingYear,
                                                  @Nonnull final GeoLocation defaultCoordinates) {

        super(new MooseDataCardMooseObservationValidator(huntingYear, defaultCoordinates), contactPerson);
        this.mooseSpecies = Objects.requireNonNull(mooseSpecies);
    }

    @Override
    public Stream<Observation> apply(@Nonnull final MooseDataCardObservation input) {
        return validateToStream(input).map(validInput -> {

            final Observation observation = createObservation(validInput);

            observation.setSpecies(mooseSpecies);
            observation.setObservationType(ObservationType.NAKO);

            observation.setMooselikeMaleAmount(F.coalesceAsInt(validInput.getAU(), 0));
            observation.setMooselikeFemaleAmount(F.coalesceAsInt(validInput.getN0(), 0));
            observation.setMooselikeFemale1CalfAmount(F.coalesceAsInt(validInput.getN1(), 0));
            observation.setMooselikeFemale2CalfsAmount(F.coalesceAsInt(validInput.getN2(), 0));
            observation.setMooselikeFemale3CalfsAmount(F.coalesceAsInt(validInput.getN3(), 0));
            observation.setMooselikeCalfAmount(F.coalesceAsInt(validInput.getY(), 0));
            observation.setMooselikeUnknownSpecimenAmount(F.coalesceAsInt(validInput.getT(), 0));

            observation.setAmountToSumOfMooselikeAmounts();

            return observation;
        });
    }
}
