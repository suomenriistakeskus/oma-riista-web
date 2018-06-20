package fi.riista.feature.gamediary.harvest.specimen;

import fi.riista.feature.common.entity.FieldPresence;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.fields.RequiredHarvestFields;
import fi.riista.util.F;

import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HarvestSpecimenValidator {
    private final RequiredHarvestFields.Specimen requirements;
    private final HarvestSpecimenBusinessFields specimenFields;
    private final int gameSpeciesCode;
    private final boolean associatedWithHuntingDay;

    private final EnumSet<HarvestSpecimenFieldName> missingFields = EnumSet.noneOf(HarvestSpecimenFieldName.class);
    private final EnumSet<HarvestSpecimenFieldName> illegalFields = EnumSet.noneOf(HarvestSpecimenFieldName.class);
    private final Map<HarvestSpecimenFieldName, String> illegalValues = new HashMap<>();
    private boolean missingMooseWeight;

    public HarvestSpecimenValidator(@Nonnull final RequiredHarvestFields.Specimen requirements,
                                    @Nonnull final HarvestSpecimenBusinessFields specimenFields,
                                    final int gameSpeciesCode,
                                    final boolean associatedWithHuntingDay) {

        this.requirements = requirements;
        this.specimenFields = Objects.requireNonNull(specimenFields);
        this.gameSpeciesCode = gameSpeciesCode;
        this.associatedWithHuntingDay = associatedWithHuntingDay;
    }

    public HarvestSpecimenValidator validateAll() {
        return validateAge()
                .validateGender()
                .validateWeight()
                .validateMooselikeWeight()
                .validateNotEdible()
                .validateFitnessClass()
                .validateAntlersWidth()
                .validateAntlerPointsLeft()
                .validateAntlerPointsRight()
                .validateAntlersType();
    }

    private void validateField(HarvestSpecimenFieldName fieldName,
                               FieldPresence required,
                               Object fieldValue) {
        if (required.nonNullValueRequired()) {
            mustNotBeNull(fieldName, fieldValue);
        } else if (required.nullValueRequired()) {
            mustBeNull(fieldName, fieldValue);
        }
    }

    public HarvestSpecimenValidator validateAge() {
        final GameAge age = specimenFields.getAge();

        validateField(HarvestSpecimenFieldName.AGE, requirements.getAge(), age);

        if (associatedWithHuntingDay && isMooselikeRequiringPermitForHunting() && age == GameAge.UNKNOWN) {
            illegalValues.put(HarvestSpecimenFieldName.AGE, age.name());
        }

        return this;
    }

    public HarvestSpecimenValidator validateGender() {
        final GameGender gender = specimenFields.getGender();

        if (associatedWithHuntingDay && isMooselikeRequiringPermitForHunting() && gender == GameGender.UNKNOWN) {
            illegalValues.put(HarvestSpecimenFieldName.GENDER, gender.name());
        }

        validateField(HarvestSpecimenFieldName.GENDER, requirements.getGender(), gender);

        return this;
    }

    public HarvestSpecimenValidator validateWeight() {
        validateField(HarvestSpecimenFieldName.WEIGHT, requirements.getWeight(), specimenFields.getWeight());
        return this;
    }

    public HarvestSpecimenValidator validateMooselikeWeight() {
        final Double weightEstimated = specimenFields.getWeightEstimated();
        final Double weightMeasured = specimenFields.getWeightMeasured();

        if (isMoose() && associatedWithHuntingDay && F.allNull(weightEstimated, weightMeasured)) {
            missingMooseWeight = true;
        }

        validateField(HarvestSpecimenFieldName.WEIGHT_MEASURED,
                requirements.getWeightMeasured(),
                weightMeasured);

        validateField(HarvestSpecimenFieldName.WEIGHT_ESTIMATED,
                requirements.getWeightEstimated(),
                weightEstimated);

        return this;
    }

    public HarvestSpecimenValidator validateNotEdible() {
        validateField(HarvestSpecimenFieldName.NOT_EDIBLE, requirements.getNotEdible(), specimenFields.getNotEdible());
        return this;
    }

    public HarvestSpecimenValidator validateFitnessClass() {
        validateField(HarvestSpecimenFieldName.FITNESS_CLASS,
                requirements.getFitnessClass(),
                specimenFields.getFitnessClass());
        return this;
    }

    public HarvestSpecimenValidator validateAntlersWidth() {
        validateField(HarvestSpecimenFieldName.ANTLERS_WIDTH,
                requirements.getAntlersWidth(specimenFields.getAge(), specimenFields.getGender()),
                specimenFields.getAntlersWidth());
        return this;
    }

    public HarvestSpecimenValidator validateAntlerPointsLeft() {
        validateField(HarvestSpecimenFieldName.ANTLER_POINTS_LEFT,
                requirements.getAntlerPoints(specimenFields.getAge(), specimenFields.getGender()),
                specimenFields.getAntlerPointsLeft());
        return this;
    }

    public HarvestSpecimenValidator validateAntlerPointsRight() {
        validateField(HarvestSpecimenFieldName.ANTLER_POINTS_RIGHT,
                requirements.getAntlerPoints(specimenFields.getAge(), specimenFields.getGender()),
                specimenFields.getAntlerPointsRight());
        return this;
    }

    public HarvestSpecimenValidator validateAntlersType() {
        validateField(HarvestSpecimenFieldName.ANTLERS_TYPE,
                requirements.getAntlersType(specimenFields.getAge(), specimenFields.getGender()),
                specimenFields.getAntlersType());
        return this;
    }

    public HarvestSpecimenValidator validateAlone() {
        validateField(HarvestSpecimenFieldName.ALONE,
                requirements.getAlone(specimenFields.getAge()),
                specimenFields.getAlone());
        return this;
    }

    public boolean hasErrors() {
        return !illegalFields.isEmpty() || !missingFields.isEmpty() || !illegalValues.isEmpty() || missingMooseWeight;
    }

    public void throwOnErrors() {
        if (hasErrors()) {
            throw new HarvestSpecimenValidationException(missingFields, illegalFields, illegalValues, missingMooseWeight);
        }
    }

    private boolean isMoose() {
        return GameSpecies.isMoose(gameSpeciesCode);
    }

    private boolean isMooselikeRequiringPermitForHunting() {
        return GameSpecies.isMooseOrDeerRequiringPermitForHunting(gameSpeciesCode);
    }

    private void mustBeNull(final HarvestSpecimenFieldName fieldName, final Object value) {
        if (value != null) {
            illegal(fieldName);
        }
    }

    private void mustNotBeNull(final HarvestSpecimenFieldName fieldName, final Object value) {
        if (value == null) {
            missing(fieldName);
        }
    }

    private void illegal(final HarvestSpecimenFieldName fieldName) {
        illegalFields.add(Objects.requireNonNull(fieldName));
    }

    private void missing(final HarvestSpecimenFieldName fieldName) {
        missingFields.add(Objects.requireNonNull(fieldName));
    }

    public EnumSet<HarvestSpecimenFieldName> getMissingFields() {
        return missingFields;
    }

    public EnumSet<HarvestSpecimenFieldName> getIllegalFields() {
        return illegalFields;
    }

    public Map<HarvestSpecimenFieldName, String> getIllegalValues() {
        return illegalValues;
    }

    public boolean isMissingMooseWeight() {
        return missingMooseWeight;
    }
}
