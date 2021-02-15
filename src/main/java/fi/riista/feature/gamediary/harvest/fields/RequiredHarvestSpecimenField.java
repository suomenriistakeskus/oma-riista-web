package fi.riista.feature.gamediary.harvest.fields;

public enum RequiredHarvestSpecimenField {

    // Field is always mandatory.
    YES,

    // Field is mandatory if age is young; otherwise illegal.
    YES_IF_YOUNG,

    // Field is mandatory in case of adult male; otherwise illegal.
    YES_IF_ADULT_MALE,

    // Field is mandatory in case of adult male having antlers (not lost); otherwise illegal.
    YES_IF_ANTLERS_PRESENT,

    // Field is voluntary. Either null or non-null is allowed.
    VOLUNTARY,

    // Field is voluntary if age is young; otherwise illegal.
    VOLUNTARY_IF_YOUNG,

    // Field is voluntary in case of adult male; otherwise illegal.
    VOLUNTARY_IF_ADULT_MALE,

    // Field is voluntary in case of adult male having antlers (not lost); otherwise illegal.
    VOLUNTARY_IF_ANTLERS_PRESENT,

    // Field is illegal.
    NO,

    // Field may exist in case of adult male having antlers (not lost); however, the field should not
    // be displayed or be editable in client.
    DEPRECATED_ANTLER_DETAIL,

    // Field is allowed to exist; however, the field should not be displayed or be editable in client.
    ALLOWED_BUT_HIDDEN;
}
