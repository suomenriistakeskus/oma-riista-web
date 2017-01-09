package fi.riista.feature.gamediary.harvest.specimen;

import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.util.F;

import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.joining;

public class MandatoryHarvestSpecimenFieldMissingWithinClubHuntingException extends RuntimeException {

    public MandatoryHarvestSpecimenFieldMissingWithinClubHuntingException(@Nonnull final List<String> missingFields) {
        super(missingFields.stream().collect(joining(", ")));
    }

    public static class Builder {

        private final ArrayList<String> missingFields = new ArrayList<>();

        public Builder validateAge(@Nonnull final GameAge age) {
            if (age == null) {
                missingFields.add("age");
            } else if (age == GameAge.UNKNOWN) {
                missingFields.add("invalid age: " + age.name());
            }
            return this;
        }

        public Builder validateGender(@Nonnull final GameGender gender) {
            if (gender == null) {
                missingFields.add("gender");
            } else if (gender == GameGender.UNKNOWN) {
                missingFields.add("invalid gender: " + gender.name());
            }
            return this;
        }

        public Builder validateWeight(final Double weightEstimated, final Double weightMeasured) {
            if (F.allNull(weightEstimated, weightMeasured)) {
                missingFields.add("either estimated or measured weight");
            }
            return this;
        }

        public Builder validateFitnessClass(final GameFitnessClass fitnessClass) {
            if (fitnessClass == null) {
                missingFields.add("fitnessClass");
            }
            return this;
        }

        public Builder validateAntlersType(final GameAntlersType antlersType) {
            if (antlersType == null) {
                missingFields.add("antlersType");
            }
            return this;
        }

        public Builder validateAntlersWidth(final Integer antlersWidth) {
            if (antlersWidth == null) {
                missingFields.add("antlersWidth");
            }
            return this;
        }

        public Builder validateAntlerPointsLeft(final Integer antlerPointsLeft) {
            if (antlerPointsLeft == null) {
                missingFields.add("antlerPointsLeft");
            }
            return this;
        }

        public Builder validateAntlerPointsRight(final Integer antlerPointsRight) {
            if (antlerPointsRight == null) {
                missingFields.add("antlerPointsRight");
            }
            return this;
        }

        public Builder validateNotEdible(final Boolean notEdible) {
            if (notEdible == null) {
                missingFields.add("notEdible");
            }
            return this;
        }

        public boolean hasMissingFields() {
            return !missingFields.isEmpty();
        }

        public void throwOnMissingFields() {
            if (hasMissingFields()) {
                throw new MandatoryHarvestSpecimenFieldMissingWithinClubHuntingException(missingFields);
            }
        }
    }

}
