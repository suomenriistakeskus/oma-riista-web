package fi.riista.feature.gis.geojson;

public class GeoJSONConstants {
    public static final String ID_EXCLUDED = "excluded";
    public static final String ID_PREFIX_MH_HIRVI = "mh-hirvi-";
    public static final String ID_PREFIX_OTHER = "other-";

    public static final String PROPERTY_NUMBER = "number";
    public static final String PROPERTY_NUMBER_NEW = "new_palsta_tunnus";
    public static final String PROPERTY_PALSTA_NEW_ID = "new_palsta_id";

    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_CLUB_NAME = "clubName";
    public static final String PROPERTY_AREA_NAME = "areaName";
    public static final String PROPERTY_YEAR = "year";

    public static final String PROPERTY_SIZE = "size";
    public static final String PROPERTY_SIZE_DIFF = "diff_area";
    public static final String PROPERTY_AREA_SIZE = "areaSize";
    public static final String PROPERTY_WATER_AREA_SIZE = "waterAreaSize";

    public static final String PROPERTY_HASH = "hash";

    public static final String PROPERTY_CHANGED = "changed";
    public static final String PROPERTY_FIXED = "fixed";

    public static final String PROPERTY_SAVE_DATE = "saveDate";
    public static final String PROPERTY_HUNTING_YEAR = "huntingYear";
    public static final String PROPERTY_HUNTING_CLUB_ID = "huntingClubId";

    private GeoJSONConstants() {
        throw new AssertionError();
    }
}
