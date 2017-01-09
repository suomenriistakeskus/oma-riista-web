package fi.riista.feature.gamediary.harvest.specimen;

import javax.annotation.Nonnull;

public interface HasMooseFields extends HasMooselikeFields {

    GameFitnessClass getFitnessClass();

    void setFitnessClass(GameFitnessClass fitnessClass);

    GameAntlersType getAntlersType();

    void setAntlersType(GameAntlersType antlersType);

    default void copyMooseFieldsTo(@Nonnull final HasMooseFields that) {
        copyMooselikeFieldsTo(that);
        that.setFitnessClass(getFitnessClass());
        that.setAntlersType(getAntlersType());
    }

    default boolean hasEqualMooseFields(@Nonnull final HasMooseFields that) {
        return hasEqualMooselikeFields(that) &&
                getFitnessClass() == that.getFitnessClass() &&
                getAntlersType() == that.getAntlersType();
    }

    default void clearMooseFields() {
        clearMooselikeFields();
        clearMooseOnlyFields();
    }

    default void clearMooseOnlyFields() {
        setFitnessClass(null);
        setAntlersType(null);
    }

}
