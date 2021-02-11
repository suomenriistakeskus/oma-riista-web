package fi.riista.feature.gamediary.observation;

import fi.riista.feature.common.entity.PersistableEnum;
import fi.riista.util.LocalisedEnum;

public enum ObservationCategory implements LocalisedEnum, PersistableEnum {

    NORMAL("N"),
    // Havainto tehty hirvenmetsästyksen yhteydessä
    MOOSE_HUNTING("M"),
    // Havainto tehty peuran metsästyksen yhteydessä
    DEER_HUNTING("D");

    private final String databaseValue;

    ObservationCategory(final String databaseValue) {
        this.databaseValue = databaseValue;
    }

    @Override
    public String getDatabaseValue() {
        return databaseValue;
    }

    public static ObservationCategory fromWithinMooseHunting(final Boolean withinMooseHunting) {
        return Boolean.TRUE.equals(withinMooseHunting) ? MOOSE_HUNTING : NORMAL;
    }

    public boolean isWithinMooseHunting() {
        return this == MOOSE_HUNTING;
    }

    public boolean isWithinDeerHunting() {
        return this == DEER_HUNTING;
    }

}
