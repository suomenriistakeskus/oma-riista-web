package fi.riista.feature.gis.zone;

public class TotalLandWaterSizeDTO {
    private final double total;
    private final double land;
    private final double water;

    public TotalLandWaterSizeDTO(final double total, final double land, final double water) {
        this.total = Math.max(0, total);
        this.land = Math.max(0, land);
        this.water = Math.max(0, water);
    }

    public double getTotal() {
        return total;
    }

    public double getLand() {
        return land;
    }

    public double getWater() {
        return water;
    }
}
