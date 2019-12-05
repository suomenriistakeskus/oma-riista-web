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

    private HarvestPermitApplicationName() {
        throw new AssertionError();
    }
}
