package fi.riista.feature.permit.decision;

import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.util.LocalisedString;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

public class PermitDecisionName {
    private static final LocalisedString CANCEL_APPLICATION = LocalisedString.of(
            "PÄÄTÖS HAKEMUKSEN PERUUTTAMISEEN", "BESLUT OM ÅTERKALLANDE AV ANSÖKAN");

    private static final LocalisedString IGNORE_APPLICATION = LocalisedString.of(
            "HAKEMUKSEN TUTKIMATTA JÄTTÄMINEN", "ANSÖKAN LÄMNAS UTAN PRÖVNING");

    public static final LocalisedString MOOSELIKE = LocalisedString.of("Hirvieläinten pyyntilupa", "Jaktlicens för " +
            "hjortdjur");
    public static final LocalisedString MOOSELIKE_AMENDMENT = LocalisedString.of("Uusi hirvieläimen pyyntilupa", "Ny " +
            "jaktlicens för hjortdjur");
    public static final LocalisedString BIRD = LocalisedString.of("ML 41 B §:n mukainen poikkeuslupa", "Dispens " +
            "enligt JL 41 B §");
    public static final LocalisedString LARGE_CARNIVORE = LocalisedString.of(
            "ML 41 A §:n mukainen kannanhoidollinen poikkeuslupa",
            "Stamvårdande dispens enligt JL 41 A §");
    public static final LocalisedString MAMMAL = LocalisedString.of(
            "Poikkeuslupa riistanisäkkäille",
            "Dispens för däggdjursvilt");


    @Nonnull
    public static LocalisedString getDecisionName(final @Nonnull PermitDecision.DecisionType decisionType,
                                                  final @Nonnull HarvestPermitCategory permitCategory) {
        requireNonNull(decisionType);
        requireNonNull(permitCategory);

        switch (decisionType) {
            case HARVEST_PERMIT:
                return permitCategory.getDecisionName();

            case CANCEL_APPLICATION:
                return CANCEL_APPLICATION;

            case IGNORE_APPLICATION:
                return IGNORE_APPLICATION;

            default:
                throw new IllegalArgumentException("Unknown decision type");
        }
    }

    private PermitDecisionName() {
        throw new AssertionError();
    }
}
