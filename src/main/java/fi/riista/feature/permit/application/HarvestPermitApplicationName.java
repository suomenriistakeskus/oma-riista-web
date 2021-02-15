package fi.riista.feature.permit.application;

import fi.riista.util.LocalisedString;

public class HarvestPermitApplicationName {
    public static final LocalisedString BIRD = LocalisedString.of("Poikkeuslupa riistalinnuille ja rauhoittamattomille linnuille", "Dispens för viltfåglar och icke fredade fåglar");
    public static final LocalisedString BEAR = LocalisedString.of("Kannanhoidollinen poikkeuslupa, karhu", "Stamvårdande dispens, björn");
    public static final LocalisedString LYNX = LocalisedString.of("Kannanhoidollinen poikkeuslupa, ilves", "Stamvårdande dispens, lodjur");
    public static final LocalisedString LYNX_PORONHOITO = LocalisedString.of("Kannanhoidollinen poikkeuslupa poronhoitoalueella, ilves", "Stamvårdande dispens inom renskötselområdet, lodjur");
    public static final LocalisedString WOLF = LocalisedString.of("Kannanhoidollinen poikkeuslupa, susi", "Stamvårdande dispens, varg");
    public static final LocalisedString MAMMAL = LocalisedString.of("Poikkeuslupa riistanisäkkäille", "Dispens för " +
            "däggdjursvilt");
    public static final LocalisedString NEST_REMOVAL = LocalisedString.of("Poikkeuslupa munien, pesien ja niihin liittyvien rakennelmien hävittämiseen ML 41 d §",
            "Dispens för förstöring av bon, ägg samt konstruktioner som hänger samman med bon JL 41 d §");
    public static final LocalisedString LAW_SECTION_TEN = LocalisedString.of("ML 10 §:n mukainen pyyntilupa", "Jaktlicens enligt JL 10 §");
    public static final LocalisedString WEAPON_TRANSPORTATION = LocalisedString.of("Hakemus metsästysaseen ja metsästysjousen kuljettamiseen moottoriajoneuvolla",
            "Ansökan för transport av jaktvapen och jaktbågar i motordrivna fordon");
    public static final LocalisedString DISABILITY = LocalisedString.of("Moottoriajoneuvon käyttö liikuntarajoitteisena",
            "Rörelsehindrades användning av motorfordon");
    public static final LocalisedString DOG_UNLEASH = LocalisedString.of("Koirakokeet tai kouluttaminen", "Hundprov och dressyr");
    public static final LocalisedString DOG_DISTURBANCE = LocalisedString.of("Karhun haukuttaminen ja taipumuskokeet", "Anlagsprov på björn");
    public static final LocalisedString DEPORTATION = LocalisedString.of("Poikkeuslupa riistaeläimen ja rauhoittamattoman eläimen häiritsemiseen (karkottaminen)",
            "Poikkeuslupa riistaeläimen ja rauhoittamattoman eläimen häiritsemiseen (karkottaminen)");
    public static final LocalisedString RESEARCH = LocalisedString.of("Elävänä pyydystäminen",
            "Elävänä pyydystäminen");
    public static final LocalisedString IMPORTING = LocalisedString.of("Vierasperäisen riistaeläimen maahantuonti",
            "Vierasperäisen riistaeläimen maahantuonti");
    public static final LocalisedString GAME_MANAGEMENT = LocalisedString.of("Riistanhoidollinen toimenpide tai eläimen tarhaaminen",
            "Riistanhoidollinen toimenpide tai eläimen tarhaaminen");

    private HarvestPermitApplicationName() {
        throw new AssertionError();
    }
}
