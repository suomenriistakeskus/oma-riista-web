package fi.riista.feature.gis;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class GISBounds {
    private double minLng;
    private double minLat;
    private double maxLng;
    private double maxLat;

    public GISBounds() {
    }

    public GISBounds(final double minLng, final double minLat, final double maxLng, final double maxLat) {
        this.minLng = minLng;
        this.minLat = minLat;
        this.maxLng = maxLng;
        this.maxLat = maxLat;
    }

    @JsonIgnore
    public double[] toBBox() {
        return new double[]{this.minLng, this.minLat, this.maxLng, this.maxLat};
    }

    public double getMinLng() {
        return minLng;
    }

    public void setMinLng(double minLng) {
        this.minLng = minLng;
    }

    public double getMinLat() {
        return minLat;
    }

    public void setMinLat(double minLat) {
        this.minLat = minLat;
    }

    public double getMaxLng() {
        return maxLng;
    }

    public void setMaxLng(double maxLng) {
        this.maxLng = maxLng;
    }

    public double getMaxLat() {
        return maxLat;
    }

    public void setMaxLat(double maxLat) {
        this.maxLat = maxLat;
    }
}
