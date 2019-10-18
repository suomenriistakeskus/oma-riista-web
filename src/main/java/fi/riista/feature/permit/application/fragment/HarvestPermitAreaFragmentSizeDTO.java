package fi.riista.feature.permit.application.fragment;

import fi.riista.feature.gis.zone.TotalLandWaterSizeDTO;

import javax.annotation.Nonnull;
import java.util.Objects;

public class HarvestPermitAreaFragmentSizeDTO {
    private final String hash;
    private final TotalLandWaterSizeDTO bothSize;
    private final TotalLandWaterSizeDTO stateSize;
    private final TotalLandWaterSizeDTO privateSize;

    public HarvestPermitAreaFragmentSizeDTO(final @Nonnull String hash,
                                            final double areaSize,
                                            final double waterAreaSize,
                                            final double stateAreaSize,
                                            final double stateWaterAreaSize) {
        this.hash = Objects.requireNonNull(hash);

        final double landAreaSize = areaSize - waterAreaSize;
        final double stateLandSize = stateAreaSize - stateWaterAreaSize;

        final double privateAreaSize = areaSize - stateAreaSize;
        final double privateWaterSize = waterAreaSize - stateWaterAreaSize;
        final double privateLandSize = privateAreaSize - privateWaterSize;

        this.bothSize = new TotalLandWaterSizeDTO(areaSize, landAreaSize, waterAreaSize);
        this.stateSize = new TotalLandWaterSizeDTO(stateAreaSize, stateLandSize, stateWaterAreaSize);
        this.privateSize = new TotalLandWaterSizeDTO(privateAreaSize, privateLandSize, privateWaterSize);
    }

    public String getHash() {
        return hash;
    }

    public TotalLandWaterSizeDTO getBothSize() {
        return bothSize;
    }

    public TotalLandWaterSizeDTO getStateSize() {
        return stateSize;
    }

    public TotalLandWaterSizeDTO getPrivateSize() {
        return privateSize;
    }
}
