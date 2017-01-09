package fi.riista.config;

import org.joda.time.DateTimeZone;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class Constants {

    public static final String DEFAULT_TIMEZONE_ID = "Europe/Helsinki";
    public static final DateTimeZone DEFAULT_TIMEZONE = DateTimeZone.forID(DEFAULT_TIMEZONE_ID);
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    public static final String DEFAULT_ENCODING = DEFAULT_CHARSET.name();

    // Top-level Java packages
    public static final String APPLICATION_ROOT_PACKAGE = "fi.riista";
    public static final String API_BASE_PACKAGE = APPLICATION_ROOT_PACKAGE + ".api";
    public static final String FEATURE_BASE_PACKAGE = APPLICATION_ROOT_PACKAGE + ".feature";
    public static final String SECURITY_BASE_PACKAGE = APPLICATION_ROOT_PACKAGE + ".security";
    public static final String INTEGRATION_BASE_PACKAGE = APPLICATION_ROOT_PACKAGE + ".integration";

    // Spring 3.1 profiles
    public static final String EMBEDDED_DATABASE = "embeddedDatabase";
    public static final String STANDARD_DATABASE = "standardDatabase";
    public static final String AMAZON_DATABASE = "amazonDatabase";
    public static final String MOCK_GIS_DATABASE = "mockGisDatabase";

    private Constants() {
        throw new AssertionError();
    }

}
