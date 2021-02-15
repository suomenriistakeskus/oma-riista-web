package fi.riista.feature.common.decision;

import fi.riista.util.LocalisedString;

import java.util.Locale;

public class DecisionUtil {

    private static final LocalisedString PERMIT_DECISION_FILENAME_PREFIX =
            new LocalisedString("Päätös", "Beslut");

    private static final LocalisedString NOMINATION_DECISION_FILENAME_PREFIX =
            new LocalisedString("Nimityspäätös", "Utnämningsbeslut");

    public static String getPermitDecisionFileName(final Locale locale, final String permitNumber) {
        return String.format("%s-%s.pdf", PERMIT_DECISION_FILENAME_PREFIX.getAnyTranslation(locale), permitNumber);
    }

    public static String getNominationDecisionFileName(final Locale locale, final String permitNumber) {
        return String.format("%s-%s.pdf", PERMIT_DECISION_FILENAME_PREFIX.getAnyTranslation(locale), permitNumber);
    }

    private DecisionUtil() {
        throw new AssertionError();
    }
}
