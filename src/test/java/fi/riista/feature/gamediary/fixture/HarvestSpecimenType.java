package fi.riista.feature.gamediary.fixture;

import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenBusinessFields;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.riista.feature.gamediary.GameAge.ADULT;
import static fi.riista.feature.gamediary.GameAge.YOUNG;
import static fi.riista.feature.gamediary.GameGender.FEMALE;
import static fi.riista.feature.gamediary.GameGender.MALE;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public enum HarvestSpecimenType {

    ADULT_MALE(ADULT, MALE, false),
    ANTLERS_LOST(ADULT, MALE, true),
    ADULT_FEMALE(ADULT, FEMALE),
    ADULT_UNKNOWN_GENDER(ADULT, GameGender.UNKNOWN),
    ADULT_NO_GENDER(ADULT, null),

    YOUNG_MALE(YOUNG, MALE),
    YOUNG_FEMALE(YOUNG, FEMALE),
    YOUNG_UNKNOWN_GENDER(YOUNG, GameGender.UNKNOWN),
    YOUNG_NO_GENDER(YOUNG, null),

    MALE_UNKNOWN_AGE(GameAge.UNKNOWN, MALE),
    MALE_NO_AGE(null, MALE),
    FEMALE_UNKNOWN_AGE(GameAge.UNKNOWN, FEMALE),
    FEMALE_NO_AGE(null, FEMALE),

    UNKNOWN_AGE_AND_GENDER(GameAge.UNKNOWN, GameGender.UNKNOWN),
    UNKNOWN_AGE_NO_GENDER(GameAge.UNKNOWN, null),
    NO_AGE_UNKNOWN_GENDER(null, GameGender.UNKNOWN),
    NO_AGE_OR_GENDER(null, null);

    private final GameAge age;
    private final GameGender gender;
    private final Boolean antlersLost;

    HarvestSpecimenType(final GameAge age, final GameGender gender) {
        this(age, gender, null);
    }

    HarvestSpecimenType(final GameAge age, final GameGender gender, final Boolean antlersLost) {
        this.age = age;
        this.gender = gender;

        // Check parameter integrity.
        checkArgument(antlersLost == null || age == ADULT && gender == MALE);
        this.antlersLost = antlersLost;
    }

    @Nonnull
    public static HarvestSpecimenType fromFields(@Nonnull final HarvestSpecimenBusinessFields obj) {
        requireNonNull(obj);
        return fromFields(obj.getAge(), obj.getGender(), obj.isAntlersLost());
    }

    @Nonnull
    public static HarvestSpecimenType fromFields(@Nullable final GameAge age, @Nullable final GameGender gender) {
        return fromFields(age, gender, false);
    }

    @Nonnull
    private static HarvestSpecimenType fromFields(@Nullable final GameAge age,
                                                  @Nullable final GameGender gender,
                                                  final boolean antlersLostIfAdultMale) {
        if (age == ADULT) {
            if (gender == null) {
                return ADULT_NO_GENDER;
            }
            switch (gender) {
                case MALE:
                    return antlersLostIfAdultMale ? ANTLERS_LOST : ADULT_MALE;
                case FEMALE:
                    return ADULT_FEMALE;
                case UNKNOWN:
                    return ADULT_UNKNOWN_GENDER;
            }
        } else if (age == YOUNG) {
            if (gender == null) {
                return YOUNG_NO_GENDER;
            }
            switch (gender) {
                case MALE:
                    return YOUNG_MALE;
                case FEMALE:
                    return YOUNG_FEMALE;
                case UNKNOWN:
                    return YOUNG_UNKNOWN_GENDER;
            }
        } else if (age == GameAge.UNKNOWN) {
            if (gender == null) {
                return UNKNOWN_AGE_NO_GENDER;
            }
            switch (gender) {
                case MALE:
                    return MALE_UNKNOWN_AGE;
                case FEMALE:
                    return FEMALE_UNKNOWN_AGE;
                case UNKNOWN:
                    return UNKNOWN_AGE_AND_GENDER;
            }
        } else if (age == null) {
            if (gender == null) {
                return NO_AGE_OR_GENDER;
            }
            switch (gender) {
                case MALE:
                    return MALE_NO_AGE;
                case FEMALE:
                    return FEMALE_NO_AGE;
                case UNKNOWN:
                    return NO_AGE_UNKNOWN_GENDER;
            }
        }
        throw new UnsupportedOperationException(format("Age %s and gender %s combination not supported", age, gender));
    }

    public boolean isAdult() {
        return age == ADULT;
    }

    public boolean isYoung() {
        return age == YOUNG;
    }

    public boolean isAgeUnknown() {
        return age == GameAge.UNKNOWN;
    }

    public boolean isAgePresent() {
        return age != null;
    }

    public boolean isMale() {
        return gender == MALE;
    }

    public boolean isFemale() {
        return gender == FEMALE;
    }

    public boolean isGenderUnknown() {
        return gender == GameGender.UNKNOWN;
    }

    public boolean isGenderPresent() {
        return gender != null;
    }

    public boolean isAdultMale() {
        return isAdult() && isMale();
    }

    public boolean isAdultMaleAndAntlersPresent() {
        return this == ADULT_MALE;
    }

    public boolean isAgeAndGenderPresent() {
        return isAgePresent() && isGenderPresent();
    }

    public boolean isAgeAndGenderKnown() {
        return isAgeAndGenderPresent() && !isAgeUnknown() && !isGenderUnknown();
    }

    public boolean isAntlersLostDefined() {
        return antlersLost != null;
    }

    public boolean isAntlersLost() {
        return Optional.ofNullable(antlersLost).orElse(false);
    }

    // Accessors -->

    public GameAge getAge() {
        return age;
    }

    public GameGender getGender() {
        return gender;
    }
}
