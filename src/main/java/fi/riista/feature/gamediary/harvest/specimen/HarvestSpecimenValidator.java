package fi.riista.feature.gamediary.harvest.specimen;

import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.HasGameSpeciesCode;
import fi.riista.feature.gamediary.harvest.fields.RequiredHarvestFields;
import fi.riista.feature.gamediary.harvest.fields.RequiredHarvestSpecimenField;
import fi.riista.util.F;

import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class HarvestSpecimenValidator implements HasGameSpeciesCode {

    private final RequiredHarvestFields.Specimen requirements;
    private final HarvestSpecimenBusinessFields specimenFields;
    private final int gameSpeciesCode;
    private final boolean associatedWithHuntingDay;
    private final boolean legallyMandatoryFieldsOnly;

    private final EnumSet<HarvestSpecimenFieldName> missingFields = EnumSet.noneOf(HarvestSpecimenFieldName.class);
    private final EnumSet<HarvestSpecimenFieldName> illegalFields = EnumSet.noneOf(HarvestSpecimenFieldName.class);
    private final Map<HarvestSpecimenFieldName, String> illegalValues = new HashMap<>();
    private boolean missingMooseWeight;

    public HarvestSpecimenValidator(@Nonnull final RequiredHarvestFields.Specimen requirements,
                                    @Nonnull final HarvestSpecimenBusinessFields specimenFields,
                                    final int gameSpeciesCode,
                                    final boolean associatedWithHuntingDay,
                                    final boolean legallyMandatoryFieldsOnly) {

        this.requirements = requirements;
        this.specimenFields = requireNonNull(specimenFields);
        this.gameSpeciesCode = gameSpeciesCode;
        this.associatedWithHuntingDay = associatedWithHuntingDay;
        this.legallyMandatoryFieldsOnly = legallyMandatoryFieldsOnly;
    }

    @Override
    public int getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    public HarvestSpecimenValidator validateAll() {
        return validateAge()
                .validateGender()
                .validateWeight()
                .validateMooselikeWeight()
                .validateNotEdible()
                .validateFitnessClass()
                .validateAntlersLost()
                .validateAntlersType()
                .validateAntlersWidth()
                .validateAntlerPointsLeft()
                .validateAntlerPointsRight()
                .validateAntlersGirth()
                .validateAntlersLength()
                .validateAntlersInnerWidth()
                .validateAntlerShaftWidth()
                .validateAlone();
    }

    private void validateField(final HarvestSpecimenFieldName fieldName,
                               final RequiredHarvestSpecimenField requirement,
                               final Object fieldValue) {

        final boolean antlersLost = specimenFields.isAntlersLost();

        switch (requirement) {
            case YES:
                mustNotBeNull(fieldName, fieldValue);
                break;
            case YES_IF_YOUNG:
                if (specimenFields.isYoung()) {
                    mustNotBeNull(fieldName, fieldValue);
                } else {
                    mustBeNull(fieldName, fieldValue);
                }
                break;
            case YES_IF_ADULT_MALE:
                if (specimenFields.isAdultMale()) {
                    mustNotBeNull(fieldName, fieldValue);
                } else {
                    mustBeNull(fieldName, fieldValue);
                }
                break;
            case YES_IF_ANTLERS_PRESENT:
                if (specimenFields.isAdultMale() && !antlersLost) {
                    mustNotBeNull(fieldName, fieldValue);
                } else {
                    mustBeNull(fieldName, fieldValue);
                }
                break;
            case VOLUNTARY:
            case ALLOWED_BUT_HIDDEN:
                // Either null or non-null value will pass.
                break;
            case VOLUNTARY_IF_YOUNG:
                if (!specimenFields.isYoung()) {
                    mustBeNull(fieldName, fieldValue);
                }
                break;
            case VOLUNTARY_IF_ADULT_MALE:
                if (!specimenFields.isAdultMale()) {
                    mustBeNull(fieldName, fieldValue);
                }
                break;
            case VOLUNTARY_IF_ANTLERS_PRESENT:
            case DEPRECATED_ANTLER_DETAIL:
                if (!specimenFields.isAdultMale() || antlersLost) {
                    mustBeNull(fieldName, fieldValue);
                }
                break;
            case NO:
                mustBeNull(fieldName, fieldValue);
                break;
            default:
                throw new IllegalArgumentException("Unsupported RequiredHarvestSpecimenField: " + requirement);
        }
    }

    public HarvestSpecimenValidator validateAge() {
        final GameAge age = specimenFields.getAge();

        validateField(HarvestSpecimenFieldName.AGE, requirements.getAge(), age);

        if (associatedWithHuntingDay && isMooseOrDeerRequiringPermitForHunting() && age == GameAge.UNKNOWN) {
            illegalValues.put(HarvestSpecimenFieldName.AGE, age.name());
        }

        return this;
    }

    public HarvestSpecimenValidator validateGender() {
        final GameGender gender = specimenFields.getGender();

        if (associatedWithHuntingDay && isMooseOrDeerRequiringPermitForHunting() && gender == GameGender.UNKNOWN) {
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

        if (!legallyMandatoryFieldsOnly) {
            if (isMoose() && associatedWithHuntingDay && F.allNull(weightEstimated, weightMeasured)) {
                missingMooseWeight = true;
            }
        }

        validateField(HarvestSpecimenFieldName.WEIGHT_MEASURED, requirements.getWeightMeasured(), weightMeasured);
        validateField(HarvestSpecimenFieldName.WEIGHT_ESTIMATED, requirements.getWeightEstimated(), weightEstimated);

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

    public HarvestSpecimenValidator validateAntlersLost() {
        validateField(HarvestSpecimenFieldName.ANTLERS_LOST,
                requirements.getAntlersLost(),
                specimenFields.getAntlersLost());

        return this;
    }

    public HarvestSpecimenValidator validateAntlersType() {
        validateField(HarvestSpecimenFieldName.ANTLERS_TYPE,
                requirements.getAntlersType(),
                specimenFields.getAntlersType());

        return this;
    }

    public HarvestSpecimenValidator validateAntlersWidth() {
        validateField(HarvestSpecimenFieldName.ANTLERS_WIDTH,
                requirements.getAntlersWidth(),
                specimenFields.getAntlersWidth());

        return this;
    }

    public HarvestSpecimenValidator validateAntlerPointsLeft() {
        validateField(HarvestSpecimenFieldName.ANTLER_POINTS_LEFT,
                requirements.getAntlerPoints(),
                specimenFields.getAntlerPointsLeft());

        return this;
    }

    public HarvestSpecimenValidator validateAntlerPointsRight() {
        validateField(HarvestSpecimenFieldName.ANTLER_POINTS_RIGHT,
                requirements.getAntlerPoints(),
                specimenFields.getAntlerPointsRight());

        return this;
    }

    public HarvestSpecimenValidator validateAntlersGirth() {
        validateField(HarvestSpecimenFieldName.ANTLERS_GIRTH,
                requirements.getAntlersGirth(),
                specimenFields.getAntlersGirth());

        return this;
    }

    public HarvestSpecimenValidator validateAntlersLength() {
        validateField(HarvestSpecimenFieldName.ANTLERS_LENGTH,
                requirements.getAntlersLength(),
                specimenFields.getAntlersLength());

        return this;
    }

    public HarvestSpecimenValidator validateAntlersInnerWidth() {
        validateField(HarvestSpecimenFieldName.ANTLERS_INNER_WIDTH,
                requirements.getAntlersInnerWidth(),
                specimenFields.getAntlersInnerWidth());

        return this;
    }

    public HarvestSpecimenValidator validateAntlerShaftWidth() {
        validateField(HarvestSpecimenFieldName.ANTLER_SHAFT_WIDTH,
                requirements.getAntlerShaftWidth(),
                specimenFields.getAntlerShaftWidth());

        return this;
    }

    public HarvestSpecimenValidator validateAlone() {
        validateField(HarvestSpecimenFieldName.ALONE, requirements.getAlone(), specimenFields.getAlone());
        return this;
    }

    public boolean hasErrors() {
        return !illegalFields.isEmpty() || !missingFields.isEmpty() || !illegalValues.isEmpty() || missingMooseWeight;
    }

    public void throwOnErrors() {
        if (hasErrors()) {
            throw new HarvestSpecimenValidationException(
                    gameSpeciesCode, missingFields, illegalFields, illegalValues, missingMooseWeight);
        }
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
        illegalFields.add(requireNonNull(fieldName));
    }

    private void missing(final HarvestSpecimenFieldName fieldName) {
        missingFields.add(requireNonNull(fieldName));
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
