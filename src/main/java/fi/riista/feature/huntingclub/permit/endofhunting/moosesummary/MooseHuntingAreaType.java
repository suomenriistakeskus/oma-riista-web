package fi.riista.feature.huntingclub.permit.endofhunting.moosesummary;

import fi.riista.feature.common.entity.PersistableEnum;

public enum MooseHuntingAreaType implements PersistableEnum {

    SUMMER_PASTURE("S"), WINTER_PASTURE("W"), BOTH("B");

    private final String databaseValue;

    MooseHuntingAreaType(final String databaseValue) {
        this.databaseValue = databaseValue;
    }

    @Override
    public String getDatabaseValue() {
        return databaseValue;
    }

}
