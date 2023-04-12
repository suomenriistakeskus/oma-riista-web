package fi.riista.integration.paytrail.checkout.model;

import fi.riista.util.Locales;

import java.util.Locale;

public enum PaytrailLocale {

    FI,
    SV;

    public static PaytrailLocale fromLocale(final Locale locale) {
        return Locales.isSwedish(locale) ? SV : FI;
    }

}
