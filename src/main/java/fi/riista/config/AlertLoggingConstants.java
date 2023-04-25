package fi.riista.config;

/**
 * These constants are used to trigger alerts in DataDog.
 */
public class AlertLoggingConstants {

    public static final String CLUB_AREA_ALERT_PREFIX = "CLUB_AREA_ALERT: ";
    public static final String PERMIT_ALERT_PREFIX = "PERMIT_ALERT: ";


    private AlertLoggingConstants() {
        throw new AssertionError("Not supported");
    }
}
