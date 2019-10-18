package fi.riista.feature.permit.invoice.pdf;

import fi.riista.util.LocalisedString;

class InvoicePdfConstants {

    static final LocalisedString INFO_LINE_1 = new LocalisedString(
            "Käytä allaolevaa viitenumeroa, sillä ilman viitettä maksettu maksu ei kohdistu oikein",
            "Använd nedannämda referensnummer, för utan referensnummer är det omöjligt att kontera betalningen rätt.");

    static final LocalisedString INFO_LINE_2 = new LocalisedString(
            "HUOM! Maksettaessa ulkomailta on myös vastaanottajan kulut maksettava",
            "OBS! När du betalar utomlands, måste du betala också mottagarbankens omkostnader.");

    static final LocalisedString INFO_LINE_3 = new LocalisedString(
            "Pyydämme päätöksen käsittelymaksun suoritusta eräpäivään mennessä. Saatava on ulosottokelpoinen, mikäli maksua ei suoriteta.",
            "Vi begär att ni betalar beslutets handläggningsavgiften senast på förfallodagen. En obetald avgift är utmätningsbar.");

    private InvoicePdfConstants() {
        throw new AssertionError();
    }
}
