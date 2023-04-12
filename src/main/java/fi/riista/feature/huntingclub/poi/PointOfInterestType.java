package fi.riista.feature.huntingclub.poi;

import fi.riista.feature.common.entity.PersistableEnum;
import fi.riista.util.LocalisedEnum;

public enum PointOfInterestType implements PersistableEnum, LocalisedEnum {

    // Passi
    SIGHTING_PLACE,

    // Nuolukivi
    MINERAL_LICK,

    // Ruokintapaikka
    FEEDING_PLACE,

    // Muu
    OTHER;

    @Override
    public String getDatabaseValue() {
        switch (this) {
            case SIGHTING_PLACE:
                return "S";
            case MINERAL_LICK:
                return "M";
            case FEEDING_PLACE:
                return "F";
            case OTHER:
                return "O";
            default:
                throw new IllegalArgumentException();
        }
    }
}
