package fi.riista.feature.gamediary.harvest.fields;

public enum RequiredHarvestField {

    // Field is always mandatory.
    YES,

    // Field is voluntary; either null or non-null value is allowed.
    VOLUNTARY,

    // Field is illegal.
    NO
}
