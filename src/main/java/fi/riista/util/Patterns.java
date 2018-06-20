package fi.riista.util;

public final class Patterns {

    public static final String UUID = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";

    public static final String BY_NAME = "[ .-[\\p{IsAlphabetic}]]+";

    public static final String DATE_YYYYMMDD = "\\d{4}-\\d{2}-\\d{2}";
    public static final String TIME_HH_MM_SS = "\\d{2}:\\d{2}:\\d{2}";
    public static final String DATETIME_ISO_8601 = DATE_YYYYMMDD + "T" + TIME_HH_MM_SS;
    public static final String RECENT_TIMESTAMP = "20[1-9]\\d[0-1]\\d[0-3]\\d" + "[0-2]\\d[0-5]\\d[0-5]\\d";
    public static final String DATE_DDMM = "([0-3]?\\d)\\.([0-1]?\\d)\\.";

    public static final String IPV4 = "\\b((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)(\\.|$)){4}\\b";

    public static final String IBAN_FINNISH = "FI\\d{2}\\s?\\d{4}\\s?\\d{4}\\s?\\d{4}\\s?\\d{2}";
    public static final String HUNTING_CLUB_CODE = "\\d{6,8}";

    public static final String PERMIT_NUMBER = "[1-9]\\d{3}-[1-5]-\\d{3}-\\d{5}-\\d";

    public static final String PROPERTY_IDENTIFIER_DELIMITED = "(\\d{1,3})-(\\d{1,3})-(\\d{1,4})-(\\d{1,4})";
    public static final String PROPERTY_IDENTIFIER_NORMALISED = "\\d{14}";

    private Patterns() {
        throw new AssertionError();
    }

}
