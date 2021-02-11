package fi.riista.feature.common.decision;

public enum AppealStatus {

    // Päätöksestä valitettu
    INITIATED,

    // Valitus jätetty käsittelemättä
    IGNORED,

    // Oikeuden ratkaisu, Ei muutosta
    UNCHANGED,

    // Oikeuden ratkaisu, Päätös kumottu
    REPEALED,

    // Oikeuden ratkaisu, Päätös osittain kumottu
    PARTIALLY_REPEALED,

    // Oikeuden ratkaisu, Palautettu uudelleen käsiteltäväksi
    RETREATMENT
}
