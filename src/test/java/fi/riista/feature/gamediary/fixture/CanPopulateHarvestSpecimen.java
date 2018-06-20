package fi.riista.feature.gamediary.fixture;

import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.harvest.specimen.GameAntlersType;
import fi.riista.feature.gamediary.harvest.specimen.GameFitnessClass;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenBusinessFields;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenOps;
import fi.riista.feature.gamediary.harvest.specimen.HasMooseFields;
import fi.riista.feature.gamediary.harvest.specimen.HasMooselikeFields;
import fi.riista.util.ValueGeneratorMixin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Objects;

public interface CanPopulateHarvestSpecimen extends ValueGeneratorMixin {

    HarvestSpecimenOps getSpecimenOps();

    static EnumSet<GameAge> getAgeOptions(final boolean allowUnknown) {
        return allowUnknown ? EnumSet.allOf(GameAge.class) : EnumSet.complementOf(EnumSet.of(GameAge.UNKNOWN));
    }

    static EnumSet<GameGender> getGenderOptions(final boolean allowUnknown) {
        return allowUnknown ? EnumSet.allOf(GameGender.class) : EnumSet.complementOf(EnumSet.of(GameGender.UNKNOWN));
    }

    /**
     * Mutates content of given HarvestSpecimenDTO. ID and revision fields are
     * left intact.
     */
    default void mutateContent(@Nonnull final HarvestSpecimenBusinessFields obj,
                               final boolean allowUnknownAgeAndGender) {

        Objects.requireNonNull(obj);

        final GameAge age = someOtherThan(obj.getAge(), getAgeOptions(allowUnknownAgeAndGender));
        final GameGender gender = someOtherThan(obj.getGender(), getGenderOptions(allowUnknownAgeAndGender));

        mutateContent(obj, age, gender);
    }

    default void mutateContent(@Nonnull final HarvestSpecimenBusinessFields obj,
                               @Nullable final GameAge age,
                               @Nullable final GameGender gender) {

        Objects.requireNonNull(obj, "obj is null");

        obj.setAge(age);
        obj.setGender(gender);

        final HarvestSpecimenOps ops = getSpecimenOps();

        if (ops.supportsExtendedMooselikeFields()) {
            if (ops.supportsExtendedMooseFields()) {
                populateMooseFields(obj, age, gender);
            } else {
                populateMooselikeFields(obj, age, gender);
            }
            obj.setWeight(null);
        } else {
            obj.setWeight(weight());
        }
    }

    default void populateMooselikeFields(@Nonnull final HasMooselikeFields obj,
                                         @Nullable final GameAge age,
                                         @Nullable final GameGender gender) {
        Objects.requireNonNull(obj);

        obj.setWeightEstimated(weight());
        obj.setWeightMeasured(weight());
        obj.setNotEdible(false);
        obj.setAdditionalInfo("additional info " + nextPositiveInt());

        if (age == GameAge.ADULT && gender == GameGender.MALE) {
            obj.setAntlersWidth(nextPositiveIntAtMost(100));
            obj.setAntlerPointsLeft(nextPositiveIntAtMost(10));
            obj.setAntlerPointsRight(nextPositiveIntAtMost(10));
        }
    }

    default void populateMooseFields(@Nonnull final HasMooseFields obj,
                                     @Nullable final GameAge age,
                                     @Nullable final GameGender gender) {

        populateMooselikeFields(obj, age, gender);
        obj.setFitnessClass(some(GameFitnessClass.class));

        if (age == GameAge.ADULT && gender == GameGender.MALE) {
            obj.setAntlersType(some(GameAntlersType.class));
        }

        if (age == GameAge.YOUNG && getSpecimenOps().supportsSolitaryMooseCalves()) {
            obj.setAlone(someBoolean());
        }
    }
}
