package fi.riista.feature.organization.calendar;

import java.util.EnumSet;

public enum CalendarEventType {

    AMPUMAKOE,
    JOUSIAMPUMAKOE,
    METSASTAJAKURSSI,
    METSASTAJATUTKINTO,
    KOULUTUSTILAISUUS,
    VUOSIKOKOUS,
    YLIMAARAINEN_KOKOUS,
    NUORISOTAPAHTUMA,
    AMPUMAKILPAILU,
    RIISTAPOLKUKILPAILU,
    ERATAPAHTUMA,
    HARJOITUSAMMUNTA;

    private static final EnumSet<CalendarEventType> SHOOTING_TEST_TYPES = shootingTestTypes();

    public static EnumSet<CalendarEventType> getTypes(final boolean applicableForShootingTest) {
        return applicableForShootingTest ? shootingTestTypes() : EnumSet.complementOf(shootingTestTypes());
    }

    public static EnumSet<CalendarEventType> shootingTestTypes() {
        return EnumSet.of(AMPUMAKOE, JOUSIAMPUMAKOE);
    }

    public boolean isShootingTest() {
        return SHOOTING_TEST_TYPES.contains(this);
    }
}
