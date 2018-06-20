package fi.riista.feature.gamediary.harvest.specimen;

import fi.riista.util.NumberUtils;

import javax.annotation.Nonnull;
import java.util.Objects;

public interface HasMooselikeFields {

    Double getWeightEstimated();

    void setWeightEstimated(Double weightEstimated);

    Double getWeightMeasured();

    void setWeightMeasured(Double weightMeasured);

    Integer getAntlersWidth();

    void setAntlersWidth(Integer antlersWidth);

    Integer getAntlerPointsLeft();

    void setAntlerPointsLeft(Integer antlerPointsLeft);

    Integer getAntlerPointsRight();

    void setAntlerPointsRight(Integer antlerPointsRight);

    Boolean getNotEdible();

    void setNotEdible(Boolean notEdible);

    String getAdditionalInfo();

    void setAdditionalInfo(String additionalInfo);

    default boolean hasEqualMooselikeFields(@Nonnull final HasMooselikeFields that) {
        Objects.requireNonNull(that);

        return NumberUtils.equal(getWeightEstimated(), that.getWeightEstimated()) &&
                NumberUtils.equal(getWeightMeasured(), that.getWeightMeasured()) &&
                Objects.equals(getAntlersWidth(), that.getAntlersWidth()) &&
                Objects.equals(getAntlerPointsLeft(), that.getAntlerPointsLeft()) &&
                Objects.equals(getAntlerPointsRight(), that.getAntlerPointsRight()) &&
                Objects.equals(getNotEdible(), that.getNotEdible()) &&
                Objects.equals(getAdditionalInfo(), that.getAdditionalInfo());
    }

    default void clearMooselikeFields() {
        setWeightEstimated(null);
        setWeightMeasured(null);
        setAntlersWidth(null);
        setAntlerPointsLeft(null);
        setAntlerPointsRight(null);
        setNotEdible(null);
        setAdditionalInfo(null);
    }
}
