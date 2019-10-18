package fi.riista.util;

public class RiistakeskusConstants {
    public static final LocalisedString NAME = new LocalisedString("SUOMEN RIISTAKESKUS", "FINLANDS VILTCENTRAL");
    public static final LocalisedString STREET_ADDRESS = new LocalisedString("Sompiontie 1", "Sompiovägen 1");
    public static final LocalisedString POST_OFFICE = new LocalisedString("00730 HELSINKI", "00730 HELSINGFORS");
    public static final String PHONE_NUMBER = "029 431 2001";

    private RiistakeskusConstants() {
        throw new AssertionError();
    }
}
