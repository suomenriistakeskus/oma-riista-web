package fi.riista.feature.gis.zone;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.Objects;

public class GISZoneSizeDTO implements Serializable {

    public static GISZoneSizeDTO createEmpty() {
        final TotalLandWaterSizeDTO total = new TotalLandWaterSizeDTO(0, 0, 0);
        return new GISZoneSizeDTO(total, 0, 0);
    }

    // Including both state and private land
    private final TotalLandWaterSizeDTO all;
    private final double stateLandAreaSize;
    private final double privateLandAreaSize;

    public GISZoneSizeDTO(final TotalLandWaterSizeDTO total,
                          final double stateLandAreaSize,
                          final double privateLandAreaSize) {

        this.all = Objects.requireNonNull(total);
        this.stateLandAreaSize = stateLandAreaSize;
        this.privateLandAreaSize = privateLandAreaSize;
    }

    @JsonIgnore
    public boolean hasAreaSizeGreaterThanTenAres() {
        return Math.round(all.getTotal()) >= 1000;
    }

    public TotalLandWaterSizeDTO getAll() {
        return all;
    }

    public double getStateLandAreaSize() {
        return stateLandAreaSize;
    }

    public double getPrivateLandAreaSize() {
        return privateLandAreaSize;
    }
}
