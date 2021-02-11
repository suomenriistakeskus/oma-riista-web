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

    private static final LocalisedString CANCEL_ANNUAL_RENEWAL = LocalisedString.of(
            "ILMOITUSMENETTELYN PERUUTTAMINEN", "ANNULLERING AV ANMÄLNINGSFÖRFARANDET");

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
    public static final LocalisedString NEST_REMOVAL = LocalisedString.of(
            "Poikkeuslupa munien, pesien ja niihin liittyvien rakennelmien hävittämiseen ML 41 d §",
            "Dispens för förstöring av bon, ägg samt konstruktioner som hänger samman med bon JL 41 d §");
    public static final LocalisedString LAW_SECTION_TEN = LocalisedString.of(
            "ML 10 §:n mukainen pyyntilupa",
            "Jaktlicens enligt JL 10 §");
    public static final LocalisedString WEAPON_TRANSPORTATION = LocalisedString.of(
            "Hakemus metsästysaseen ja metsästysjousen kuljettamiseen moottoriajoneuvolla",
            "Ansökan för transport av jaktvapen och jaktbågar i motordrivna fordon");
    public static final LocalisedString DISABILITY = LocalisedString.of(
            "Moottoriajoneuvon käyttö liikuntarajoitteisena",
            "Rörelsehindrades användning av motorfordon");
    public static final LocalisedString DOG_UNLEASH = LocalisedString.of(
            "Koirakokeet tai kouluttaminen",
            "Hundprov och dressyr");
    public static final LocalisedString DOG_DISTURBANCE = LocalisedString.of(
            "Karhun haukuttaminen ja taipumuskokeet",
            "Anlagsprov på björn");
    public static final LocalisedString DEPORTATION = LocalisedString.of(
            "Poikkeuslupa riistaeläimen ja rauhoittamattoman eläimen häiritsemiseen (karkottaminen)",
            "Poikkeuslupa riistaeläimen ja rauhoittamattoman eläimen häiritsemiseen (karkottaminen)");
    public static final LocalisedString RESEARCH = LocalisedString.of(
            "Elävänä pyydystäminen",
            "Elävänä pyydystäminen");
    public static final LocalisedString IMPORTING = LocalisedString.of(
            "Vierasperäisen riistaeläimen ja riistaeläinkannan maahantuonti ja luontoon laskeminen",
            "Vierasperäisen riistaeläimen ja riistaeläinkannan maahantuonti ja luontoon laskeminen");
    public static final LocalisedString GAME_MANAGEMENT = LocalisedString.of(
            "Riistanhoidollinen toimenpide tai eläimen tarhaaminen",
            "Riistanhoidollinen toimenpide tai eläimen tarhaaminen");

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

            case CANCEL_ANNUAL_RENEWAL:
                return CANCEL_ANNUAL_RENEWAL;

            default:
                throw new IllegalArgumentException("Unknown decision type");
        }
    }

    private PermitDecisionName() {
        throw new AssertionError();
    }
}
