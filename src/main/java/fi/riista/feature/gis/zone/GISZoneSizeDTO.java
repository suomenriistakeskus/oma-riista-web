package fi.riista.feature.gis.zone;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Objects;

public class GISZoneSizeDTO {
    // Including both state and private land
    private final TotalLandWaterSizeDTO all;
    private final double stateLandAreaSize;
    private final double privateLandAreaSize;

    public GISZoneSizeDTO(final TotalLandWaterSizeDTO total,
                          final double stateLandAreaSize, final double privateLandAreaSize) {
        this.all = Objects.requireNonNull(total);
        this.stateLandAreaSize = Math.max(0, stateLandAreaSize);
        this.privateLandAreaSize = Math.max(0, privateLandAreaSize);
    }

    @JsonIgnore
    public boolean hasAreaSizeGreaterThanOneHectare() {
        return Math.round(all.getTotal()) >= 10_000;
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
