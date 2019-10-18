package fi.riista.feature.permitplanning.hirvityvitys.dto;

import fi.riista.feature.gis.verotuslohko.GISVerotusLohko;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

public class JyvitysExcelVerotuslohkoDTO {

    public static JyvitysExcelVerotuslohkoDTO from(final @Nonnull GISVerotusLohko entity) {
        requireNonNull(entity);

        return new JyvitysExcelVerotuslohkoDTO(
                entity.getName(),
                entity.getOfficialCode(),
                entity.getStateLandSize(),
                entity.getPrivateLandSize(),
                entity.getLandSize(),
                entity.getWaterSize(),
                entity.getAreaSize());
    }

    private final String name;
    private final String officialCode;

    private final double stateLandSize;
    private final double privateLandSize;
    private final double landSize;
    private final double waterSize;
    private final double areaSize;

    private JyvitysExcelVerotuslohkoDTO(final String name, final String officialCode,
                                        final double stateLandSize, final double privateLandSize,
                                        final double landSize, final double waterSize, final double areaSize) {
        this.name = name;
        this.officialCode = officialCode;
        this.stateLandSize = stateLandSize;
        this.privateLandSize = privateLandSize;
        this.landSize = landSize;
        this.waterSize = waterSize;
        this.areaSize = areaSize;
    }

    public String getName() {
        return name;
    }

    public String getOfficialCode() {
        return officialCode;
    }

    public double getStateLandSize() {
        return stateLandSize;
    }

    public double getPrivateLandSize() {
        return privateLandSize;
    }

    public double getLandSize() {
        return landSize;
    }

    public double getWaterSize() {
        return waterSize;
    }

    public double getAreaSize() {
        return areaSize;
    }
}
