package fi.riista.feature.permit.application.fragment;

import fi.riista.feature.gis.GISWGS84Point;

public class HarvestPermitAreaFragmentQueryParams {
    private final long zoneId;
    private final int fragmentSizeLimit;
    private final int metsahallitusYear;
    private final GISWGS84Point location;

    public HarvestPermitAreaFragmentQueryParams(final long zoneId,
                                                final int metsahallitusYear,
                                                final int fragmentSizeLimit,
                                                final GISWGS84Point location) {
        this.zoneId = zoneId;
        this.metsahallitusYear = metsahallitusYear;
        this.fragmentSizeLimit = fragmentSizeLimit;
        this.location = location;
    }

    public boolean hasLocation() {
        return this.location != null;
    }

    public long getZoneId() {
        return zoneId;
    }

    public int getFragmentSizeLimit() {
        return fragmentSizeLimit;
    }

    public int getMetsahallitusYear() {
        return metsahallitusYear;
    }

    public GISWGS84Point getLocation() {
        return location;
    }
}
