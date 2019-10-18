package fi.riista.feature.permit.application.bird;

import fi.riista.feature.common.entity.PersistableEnum;

public enum ProtectedAreaType implements PersistableEnum {

    OTHER,
    AIRPORT,
    FOOD_PREMISES,
    WASTE_DISPOSAL,
    BERRY_FARM,
    FUR_FARM,
    FISHERY,
    ANIMAL_SHELTER;


    @Override
    public String getDatabaseValue() {
        return name();
    }
}
