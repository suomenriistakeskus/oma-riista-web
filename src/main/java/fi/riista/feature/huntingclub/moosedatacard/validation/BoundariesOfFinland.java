package fi.riista.feature.huntingclub.moosedatacard.validation;

import com.google.common.collect.Range;

public final class BoundariesOfFinland {

    public static final long MIN_ETRS_LATITUDE = 6605838L;
    public static final long MAX_ETRS_LATITUDE = 7776450L;
    public static final long MIN_ETRS_LONGITUDE = 61685L;
    public static final long MAX_ETRS_LONGITUDE = 732907L;

    public static final Range<Long> ETRS_LATITUDE_RANGE = Range.closed(MIN_ETRS_LATITUDE, MAX_ETRS_LATITUDE);
    public static final Range<Long> ETRS_LONGITUDE_RANGE = Range.closed(MIN_ETRS_LONGITUDE, MAX_ETRS_LONGITUDE);

    private BoundariesOfFinland() {
        throw new AssertionError();
    }

    public static boolean isWithinBoundaries(final long latitude, final long longitude) {
        return ETRS_LATITUDE_RANGE.contains(latitude) && ETRS_LONGITUDE_RANGE.contains(longitude);
    }

}
