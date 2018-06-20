package fi.riista.feature.gamediary.harvest.specimen;

import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.util.F;
import fi.riista.util.NumberUtils;

import javax.annotation.Nonnull;

public interface HarvestSpecimenBusinessFields extends HasMooseFields {

    GameAge getAge();

    void setAge(GameAge age);

    GameGender getGender();

    void setGender(GameGender gender);

    Double getWeight();

    void setWeight(Double weight);

    default boolean hasEqualBusinessFields(@Nonnull final HarvestSpecimenBusinessFields that) {
        return hasEqualMooseFields(that) &&
                getAge() == that.getAge() &&
                getGender() == that.getGender() &&
                NumberUtils.equal(getWeight(), that.getWeight());
    }

    default boolean allBusinessFieldsNull() {
        return F.allNull(
                getAge(), getGender(), getWeight(), getWeightEstimated(), getWeightMeasured(), getFitnessClass(),
                getAntlersType(), getAntlersWidth(), getAntlerPointsLeft(), getAntlerPointsRight(), getNotEdible(),
                getAlone(), getAdditionalInfo());
    }

    default void clearBusinessFields() {
        clearMooseFields();
        setAge(null);
        setGender(null);
        setWeight(null);
    }
}
