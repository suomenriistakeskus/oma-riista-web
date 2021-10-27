package fi.riista.feature.otherwisedeceased;

import fi.riista.util.LocalisedEnum;

public enum OtherwiseDeceasedCause implements LocalisedEnum {

    // Liikenneonnettomuus
    HIGHWAY_ACCIDENT,
    // Onnettomuus rautatiellä
    RAILWAY_ACCIDENT,
    // Sairaus / nääntyminen
    SICKNESS_OR_STARVATION,
    // Poliisin määräyksellä lopetettu
    KILLED_BY_POLICES_ORDER,
    // Pakkotila
    NECESSITY,
    // Laiton tappaminen (metsästysrikos, lainvoimainen tuomio)
    ILLEGAL_KILLING,
    // Tutkinnassa
    UNDER_INVESTIGATION,
    // Muu, check separate field
    OTHER

}
