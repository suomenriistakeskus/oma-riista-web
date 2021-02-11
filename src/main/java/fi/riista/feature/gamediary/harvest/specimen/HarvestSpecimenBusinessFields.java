package fi.riista.feature.gamediary.harvest.specimen;

import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.util.F;

import javax.validation.constraints.AssertTrue;

import static fi.riista.feature.gamediary.GameAge.ADULT;
import static fi.riista.feature.gamediary.GameAge.YOUNG;
import static fi.riista.feature.gamediary.GameGender.MALE;

public interface HarvestSpecimenBusinessFields {

    // Fields common to most species -->

    GameAge getAge();

    void setAge(GameAge age);

    GameGender getGender();

    void setGender(GameGender gender);

    Double getWeight();

    void setWeight(Double weight);

    // Common moose or mooselike fields -->

    Double getWeightEstimated();

    void setWeightEstimated(Double weightEstimated);

    Double getWeightMeasured();

    void setWeightMeasured(Double weightMeasured);

    GameFitnessClass getFitnessClass();

    void setFitnessClass(GameFitnessClass fitnessClass);

    Boolean getNotEdible();

    void setNotEdible(Boolean notEdible);

    String getAdditionalInfo();

    void setAdditionalInfo(String additionalInfo);

    // Mooselike adult male -->

    Boolean getAntlersLost();

    void setAntlersLost(Boolean antlersLost);

    GameAntlersType getAntlersType();

    void setAntlersType(GameAntlersType antlersType);

    Integer getAntlersWidth();

    void setAntlersWidth(Integer antlersWidth);

    Integer getAntlerPointsLeft();

    void setAntlerPointsLeft(Integer antlerPointsLeft);

    Integer getAntlerPointsRight();

    void setAntlerPointsRight(Integer antlerPointsRight);

    Integer getAntlersGirth();

    void setAntlersGirth(Integer antlersGirth);

    Integer getAntlersLength();

    void setAntlersLength(Integer antlersLength);

    Integer getAntlersInnerWidth();

    void setAntlersInnerWidth(Integer antlersInnerWidth);

    Integer getAntlerShaftWidth();

    void setAntlerShaftWidth(Integer antlerShaftWidth);

    // Moose young -->

    Boolean getAlone();

    void setAlone(Boolean alone);

    // Helper methods -->

    default boolean isAdultMale() {
        return getAge() == ADULT && getGender() == MALE;
    }

    default boolean isYoung() {
        return getAge() == YOUNG;
    }

    default boolean isNotEdible() {
        return Boolean.TRUE.equals(getNotEdible());
    }

    default boolean isAntlersLost() {
        return Boolean.TRUE.equals(getAntlersLost());
    }

    @AssertTrue
    default boolean isAloneNullWhenNotYoung() {
        return isYoung() || getAlone() == null;
    }

    @AssertTrue
    default boolean isAntlerDetailFieldsNullWhenAntlersLost() {
        return !isAntlersLost() || antlerDetailFieldsNull();
    }

    default boolean allAntlerFieldsNull() {
        return getAntlersLost() == null && antlerDetailFieldsNull();
    }

    default boolean antlerDetailFieldsNull() {
        return F.allNull(
                getAntlersType(), getAntlersWidth(), getAntlerPointsLeft(), getAntlerPointsRight(), getAntlersGirth(),
                getAntlersLength(), getAntlersInnerWidth(), getAntlerShaftWidth());
    }

    default boolean allBusinessFieldsNull() {
        return allAntlerFieldsNull() && F.allNull(
                getAge(), getGender(), getWeight(),
                getWeightEstimated(), getWeightMeasured(), getFitnessClass(), getNotEdible(), getAdditionalInfo(),
                getAlone());
    }

    default void clearBusinessFields() {
        setAge(null);
        setGender(null);
        setWeight(null);

        clearExtensionFields();
    }

    default void clearExtensionFields() {
        setWeightEstimated(null);
        setWeightMeasured(null);

        setFitnessClass(null);
        setNotEdible(null);
        setAdditionalInfo(null);

        clearAllAntlerFields();

        setAlone(null);
    }

    default void clearAllAntlerFields() {
        setAntlersLost(null);
        clearAntlerDetailFields();
    }

    default void clearAntlerDetailFields() {
        setAntlersType(null);
        setAntlersWidth(null);
        setAntlerPointsLeft(null);
        setAntlerPointsRight(null);
        setAntlersGirth(null);
        setAntlersLength(null);
        setAntlersInnerWidth(null);
        setAntlerShaftWidth(null);
    }
}
