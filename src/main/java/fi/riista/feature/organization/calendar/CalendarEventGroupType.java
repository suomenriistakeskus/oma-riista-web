package fi.riista.feature.organization.calendar;

import com.google.common.collect.ImmutableSet;
import fi.riista.util.LocalisedEnum;

import static com.google.common.collect.Sets.immutableEnumSet;

public enum CalendarEventGroupType implements LocalisedEnum {
    // Composite types
    KOULUTUSTILAISUUDET,
    ETAKOULUTUKSET,
    KILPAILUT,
    MUUT_TAPAHTUMAT,
    //
    METSASTAJATUTKINTO,
    METSASTAJAKURSSI,
    AMPUMAKOE,
    JOUSIAMPUMAKOE,
    VUOSIKOKOUS;

    public static final ImmutableSet<CalendarEventType> getCalenderEventTypes(final CalendarEventGroupType groupType) {

        if (groupType == null) {
            return ImmutableSet.of();
        }

        switch (groupType) {
            case KOULUTUSTILAISUUDET:
            case ETAKOULUTUKSET:
                return TRAINING_EVENTS;
            case KILPAILUT:
                return CONTESTS;
            case MUUT_TAPAHTUMAT:
                return OTHER_EVENTS;
            case METSASTAJATUTKINTO:
                return immutableEnumSet(CalendarEventType.METSASTAJATUTKINTO);
            case METSASTAJAKURSSI:
                return immutableEnumSet(CalendarEventType.METSASTAJAKURSSI);
            case AMPUMAKOE:
                return immutableEnumSet(CalendarEventType.AMPUMAKOE);
            case JOUSIAMPUMAKOE:
                return immutableEnumSet(CalendarEventType.JOUSIAMPUMAKOE);
            case VUOSIKOKOUS:
                return immutableEnumSet(CalendarEventType.VUOSIKOKOUS);
            default:
                return ImmutableSet.of();
        }
    }

    private static final ImmutableSet<CalendarEventType> TRAINING_EVENTS = immutableEnumSet(
            CalendarEventType.METSASTYKSENJOHTAJA_HIRVIELAIMET,
            CalendarEventType.METSASTYKSENJOHTAJA_SUURPEDOT,
            CalendarEventType.METSASTAJAKOULUTUS_HIRVIELAIMET,
            CalendarEventType.METSASTAJAKOULUTUS_SUURPEDOT,
            CalendarEventType.SRVAKOULUTUS,
            CalendarEventType.PETOYHDYSHENKILO_KOULUTUS,
            CalendarEventType.VAHINKOKOULUTUS,
            CalendarEventType.AMPUMAKOKEENVASTAANOTTAJA_KOULUTUS,
            CalendarEventType.METSASTAJATUTKINNONVASTAANOTTAJA_KOULUTUS,
            CalendarEventType.RIISTAVAHINKOTARKASTAJA_KOULUTUS,
            CalendarEventType.METSASTYKSENVALVOJA_KOULUTUS,
            CalendarEventType.PIENPETOJEN_PYYNTI_KOULUTUS,
            CalendarEventType.RIISTALASKENTA_KOULUTUS,
            CalendarEventType.RIISTAKANTOJEN_HOITO_KOULUTUS,
            CalendarEventType.RIISTAN_ELINYMPARISTON_HOITO_KOULUTUS,
            CalendarEventType.MUU_RIISTANHOITOKOULUTUS,
            CalendarEventType.AMPUMAKOULUTUS,
            CalendarEventType.JALJESTAJAKOULUTUS
    );

    private static final ImmutableSet<CalendarEventType> CONTESTS = immutableEnumSet(
            CalendarEventType.AMPUMAKILPAILU,
            CalendarEventType.RIISTAPOLKUKILPAILU);

    private static final ImmutableSet<CalendarEventType> OTHER_EVENTS = immutableEnumSet(
            CalendarEventType.YLIMAARAINEN_KOKOUS,
            CalendarEventType.NUORISOTAPAHTUMA,
            CalendarEventType.ERATAPAHTUMA,
            CalendarEventType.HARJOITUSAMMUNTA,
            CalendarEventType.NUORISOTILAISUUS,
            CalendarEventType.HIRVIELAINTEN_VEROTUSSUUNNITTELU,
            CalendarEventType.MUU_TAPAHTUMA,
            CalendarEventType.TILAISUUS_KOULUILLE,
            CalendarEventType.OPPILAITOSTILAISUUS,
            CalendarEventType.RHY_HALLITUKSEN_KOKOUS,
            CalendarEventType.KOULUTUSTILAISUUS);
}

