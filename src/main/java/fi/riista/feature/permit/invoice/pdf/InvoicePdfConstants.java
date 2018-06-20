package fi.riista.feature.permit.invoice.pdf;

import fi.riista.util.LocalisedString;

public class InvoicePdfConstants {
    public static final LocalisedString RK_NAME = new LocalisedString("SUOMEN RIISTAKESKUS", "FINLANDS VILTCENTRAL");
    public static final LocalisedString RK_STREET = new LocalisedString("Sompiontie 1", "Sompiovägen 1");
    public static final LocalisedString RK_POST_OFFICE = new LocalisedString("00730 HELSINKI", "00730 HELSINGFORS");
    public static final String RK_PHONE = "029 431 2001";

    public static final LocalisedString ADDITIONAL_INFO = new LocalisedString(
            "Käytä allaolevaa viitenumeroa, sillä ilman viitettä maksettu\n" +
                    "maksu ei kohdistu oikein\n\n" +
                    "HUOM! Maksettaessa ulkomailta on myös vastaanottajan kulut maksettava\n\n" +
                    "Pyydämme päätöksen käsittelymaksun suoritusta eräpäivään mennessä.\n" +
                    "Saatava on ulosottokelpoinen, mikäli maksua ei suoriteta.",
            "Använd nedannämda referensnummer, för utan referensnummer\n" +
                    "är det omöjligt att kontera betalningen rätt.\n\n" +
                    "OBS! När du betalar utomlands, måste du betala\n" +
                    "också mottagarbankens omkostnader.\n\n" +
                    "Vi begär att ni betalar beslutets handläggningsavgiften senast på förfallodagen.\n" +
                    "En obetald avgift är utmätningsbar.");

    private InvoicePdfConstants() {
        throw new AssertionError();
    }
}
