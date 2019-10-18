package fi.riista.feature.organization.calendar;

import com.google.common.collect.ImmutableSet;
import fi.riista.util.LocalisedEnum;

import java.util.EnumSet;

import static com.google.common.collect.Sets.immutableEnumSet;

public enum CalendarEventType implements LocalisedEnum {

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
    HARJOITUSAMMUNTA,
    METSASTYKSENJOHTAJA_HIRVIELAIMET,
    METSASTYKSENJOHTAJA_SUURPEDOT,
    METSASTAJAKOULUTUS_HIRVIELAIMET,
    METSASTAJAKOULUTUS_SUURPEDOT,
    SRVAKOULUTUS,
    PETOYHDYSHENKILO_KOULUTUS,
    VAHINKOKOULUTUS,
    TILAISUUS_KOULUILLE,
    OPPILAITOSTILAISUUS,
    NUORISOTILAISUUS,
    AMPUMAKOKEENVASTAANOTTAJA_KOULUTUS,
    METSASTAJATUTKINNONVASTAANOTTAJA_KOULUTUS,
    RIISTAVAHINKOTARKASTAJA_KOULUTUS,
    METSASTYKSENVALVOJA_KOULUTUS,
    PIENPETOJEN_PYYNTI_KOULUTUS,
    RIISTALASKENTA_KOULUTUS,
    RIISTAKANTOJEN_HOITO_KOULUTUS,
    RIISTAN_ELINYMPARISTON_HOITO_KOULUTUS,
    MUU_RIISTANHOITOKOULUTUS,
    AMPUMAKOULUTUS,
    JALJESTAJAKOULUTUS,
    MUU_TAPAHTUMA,
    RHY_HALLITUKSEN_KOKOUS;

    private static final ImmutableSet<CalendarEventType> JHT_TYPES = immutableEnumSet(
            AMPUMAKOE, JOUSIAMPUMAKOE, METSASTAJAKURSSI, METSASTAJATUTKINTO);

    private static final ImmutableSet<CalendarEventType> SHOOTING_TEST_TYPES =
            immutableEnumSet(AMPUMAKOE, JOUSIAMPUMAKOE);

    private static final ImmutableSet<CalendarEventType> ADDITIONAL_EVENTS_ALLOWED_TYPES =
            immutableEnumSet(METSASTAJAKURSSI);

    private static final ImmutableSet<CalendarEventType> INACTIVE_CALENDAR_EVENTS =
            immutableEnumSet(KOULUTUSTILAISUUS, NUORISOTAPAHTUMA);

    public static EnumSet<CalendarEventType> jhtTypes() {
        return EnumSet.copyOf(JHT_TYPES);
    }

    public static EnumSet<CalendarEventType> shootingTestTypes() {
        return EnumSet.copyOf(SHOOTING_TEST_TYPES);
    }

    public static EnumSet<CalendarEventType> nonShootingTestTypes() {
        return EnumSet.complementOf(shootingTestTypes()).complementOf(EnumSet.copyOf(INACTIVE_CALENDAR_EVENTS));
    }

    public static EnumSet<CalendarEventType> additionalEventsAllowedTypes() {
        return EnumSet.copyOf(ADDITIONAL_EVENTS_ALLOWED_TYPES);
    }

    public static final EnumSet<CalendarEventType> activeCalendarEventTypes() {
        return EnumSet.complementOf(EnumSet.copyOf(INACTIVE_CALENDAR_EVENTS));
    }

    public boolean isJht() {
        return JHT_TYPES.contains(this);
    }

    public boolean isShootingTest() {
        return SHOOTING_TEST_TYPES.contains(this);
    }
}
