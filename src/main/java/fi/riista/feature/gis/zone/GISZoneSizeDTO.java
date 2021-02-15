package fi.riista.feature.gis.zone;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.Objects;

public class GISZoneSizeDTO implements Serializable {

    public enum Status {
        OK,
        PROCESSING,
        FAILED
    }

    public static GISZoneSizeDTO create(final TotalLandWaterSizeDTO total,
                                        final double stateLandAreaSize,
                                        final double privateLandAreaSize) {
        return new GISZoneSizeDTO(total, stateLandAreaSize, privateLandAreaSize, Status.OK);
    }

    public static GISZoneSizeDTO createEmpty() {
        final TotalLandWaterSizeDTO total = new TotalLandWaterSizeDTO(0, 0, 0);
        return new GISZoneSizeDTO(total, 0, 0, Status.OK);
    }

    public static GISZoneSizeDTO createCalculating() {
        final TotalLandWaterSizeDTO total = new TotalLandWaterSizeDTO(0, 0, 0);
        return new GISZoneSizeDTO(total, 0, 0, Status.PROCESSING);
    }

    public static GISZoneSizeDTO createCalculationFailed() {
        final TotalLandWaterSizeDTO total = new TotalLandWaterSizeDTO(0, 0, 0);
        return new GISZoneSizeDTO(total, 0, 0, Status.FAILED);
    }

    // Including both state and private land
    private final TotalLandWaterSizeDTO all;
    private final double stateLandAreaSize;
    private final double privateLandAreaSize;
    private final Status status;

    private GISZoneSizeDTO(final TotalLandWaterSizeDTO total,
                          final double stateLandAreaSize,
                          final double privateLandAreaSize,
                          final Status status) {

        this.all = Objects.requireNonNull(total);
        this.stateLandAreaSize = stateLandAreaSize;
        this.privateLandAreaSize = privateLandAreaSize;
        this.status = status;
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

    public Status getStatus() {
        return status;
    }
}
