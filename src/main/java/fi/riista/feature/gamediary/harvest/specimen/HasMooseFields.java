package fi.riista.feature.gamediary.harvest.specimen;

import javax.annotation.Nonnull;
import java.util.Objects;

public interface HasMooseFields extends HasMooselikeFields {

    GameFitnessClass getFitnessClass();

    void setFitnessClass(GameFitnessClass fitnessClass);

    GameAntlersType getAntlersType();

    void setAntlersType(GameAntlersType antlersType);

    Boolean getAlone();

    void setAlone(Boolean alone);

    default boolean hasEqualMooseFields(@Nonnull final HasMooseFields that) {
        return hasEqualMooselikeFields(that) &&
                getFitnessClass() == that.getFitnessClass() &&
                getAntlersType() == that.getAntlersType() &&
                Objects.equals(getAlone(), that.getAlone());
    }

    default void clearMooseFields() {
        clearMooselikeFields();
        clearMooseOnlyFields();
    }

    default void clearMooseOnlyFields() {
        setFitnessClass(null);
        setAntlersType(null);
        setAlone(null);
    }
}
