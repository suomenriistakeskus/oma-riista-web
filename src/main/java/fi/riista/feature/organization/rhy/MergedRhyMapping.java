package fi.riista.feature.organization.rhy;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class MergedRhyMapping {

    // 74 Savonlinnan riistanhoitoyhdistys
    // 67 Punkaharjun riistanhoitoyhdistys
    // -->
    // 77 Savonlinnan riistanhoitoyhdistys
    public static final String SAVONLINNAN_RHY_074 = "074";
    public static final String PUNKAHARJUN_RHY_067 = "067";
    public static final String SAVONLINNAN_RHY_077 = "077";

    // 54 Haukivuoren riistanhoitoyhdistys
    // 75 Virtasalmen riistanhoitoyhdistys
    // -->
    // 76 Haukivuori-Virtasalmi riistanhoitoyhdistys
    public static final String HAUKIVUOREN_RHY_054 = "054";
    public static final String VIRTASALMEN_RHY_075 = "075";
    public static final String HAUKIVUORI_VIRTASALMEN_RHY_076 = "076";

    // 325 Nurmon riistanhoitoyhdistys
    // 328 Seinäjoen riistanhoitoyhdistys
    // -->
    // 334 Lakeuden riistanhoitoyhdistys
    public static final String NURMON_RHY_325 = "325";
    public static final String SEINAJOEN_RHY_328 = "328";
    public static final String LAKEUDEN_RHY_334 = "334";

    /**
     * Old rhy code mapped to new rhy code.
     */
    private static final Map<String, String> MAP = new ImmutableMap.Builder<String, String>()
            .put(SAVONLINNAN_RHY_074, SAVONLINNAN_RHY_077)
            .put(PUNKAHARJUN_RHY_067, SAVONLINNAN_RHY_077)

            .put(HAUKIVUOREN_RHY_054, HAUKIVUORI_VIRTASALMEN_RHY_076)
            .put(VIRTASALMEN_RHY_075, HAUKIVUORI_VIRTASALMEN_RHY_076)

            .put(NURMON_RHY_325, LAKEUDEN_RHY_334)
            .put(SEINAJOEN_RHY_328, LAKEUDEN_RHY_334)

            .build();

    public static boolean isMappedToNewRhy(String oldRhyCode) {
        return MAP.containsKey(oldRhyCode);
    }

    public static String getNewRhyCode(String oldRhyCode) {
        return MAP.get(oldRhyCode);
    }

}
