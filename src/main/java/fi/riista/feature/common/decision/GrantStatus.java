package fi.riista.feature.common.decision;

public enum GrantStatus {
    // Myönnetään kuten anottu
    UNCHANGED,
    // Myönnetään mutta muutoksin
    RESTRICTED,
    // Hylätty
    REJECTED
}
