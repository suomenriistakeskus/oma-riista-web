package fi.riista.integration.metsastajarekisteri;

public final class InnofactorConstants {

    // 994 = Ahvenanmaa
    public static final String RHY_AHVENANMAA = "994";

    // 997 = Ei kuulu RH-yhdistykseen
    public static final String RHY_NOT_MEMBER_CODE = "997";

    // 998 = Ulkomaalaiset
    public static final String RHY_FOREIGN_MEMBER_CODE = "998";

    public static final int MAX_PERSON_AGE = 99;
    public static final int MIN_FOREIGN_PERSON_AGE = 18;

    private InnofactorConstants() {
        throw new AssertionError();
    }
}
