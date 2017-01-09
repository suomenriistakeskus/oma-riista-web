package fi.riista.feature.gamediary.harvest;

public enum HarvestLukeStatus {

    // Odottaa vahvistusta
    PENDING,
    // Vahvistettu, alfayksilö
    CONFIRMED_ALPHA,
    // Vahvistettu, alfayksilö, aikuinen
    CONFIRMED_ALPHA_ADULT,
    // Vahvistettu, aikuinen
    CONFIRMED_ADULT,
    // Vahvistettu, mahdollinen alfayksilö, aikuinen
    CONFIRMED_POTENTIAL_ALPHA_ADULT,
    // Vahvistettu, ei aikuinen
    CONFIRMED_NOT_ADULT,
    // Vahvistettu, ei alfayksilö
    CONFIRMED_NOT_ALPHA,
    // Vahvistettu, ei alfayksilö, aikuinen
    CONFIRMED_NOT_ALPHA_ADULT,
    // Vahvistettu, ei alfayksilö, 1-2-vuotias
    CONFIRMED_NOT_ALPHA_1TO2Y,
    // Vahvistettu, ei alfayksilö, alle 1-vuotias
    CONFIRMED_NOT_ALPHA_LT1Y

}
