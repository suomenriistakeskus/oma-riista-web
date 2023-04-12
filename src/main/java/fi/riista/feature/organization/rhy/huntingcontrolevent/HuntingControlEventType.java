package fi.riista.feature.organization.rhy.huntingcontrolevent;

import fi.riista.util.LocalisedEnum;

public enum HuntingControlEventType implements LocalisedEnum {
    // Hirvieläinten (Deer) metsästyksen valvonta
    MOOSELIKE_HUNTING_CONTROL,
    // Suurpetojen (Apex predator) metsästyksen valvonta
    LARGE_CARNIVORE_HUNTING_CONTROL,
    // (Metsä)Kanalintujen (Galliformes) metsästyksen valvonta
    GROUSE_HUNTING_CONTROL,
    // Vesilintujen (Anseriformes) metsästyksen valvonta
    WATERFOWL_HUNTING_CONTROL,
    // Koirakurin valvonta
    DOG_DISCIPLINE_CONTROL,
    // Muu
    OTHER
}
