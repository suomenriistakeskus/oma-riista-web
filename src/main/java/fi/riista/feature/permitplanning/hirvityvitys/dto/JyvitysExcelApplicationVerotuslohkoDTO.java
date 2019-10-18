package fi.riista.feature.permitplanning.hirvityvitys.dto;

import fi.riista.feature.permit.area.verotuslohko.HarvestPermitAreaVerotusLohko;

public class JyvitysExcelApplicationVerotuslohkoDTO {


    public static JyvitysExcelApplicationVerotuslohkoDTO EMPTY = new JyvitysExcelApplicationVerotuslohkoDTO.Builder().build();

    private final String officialCode;

    private final String name;

    private final double areaSize;


    private final double landSize;

    private final double waterSize;

    private final double stateSize;

    private final double stateLandSize;

    private final double stateWaterSize;

    private final double privateSize;

    private final double privateLandSize;

    private final double privateWaterSize;

    public JyvitysExcelApplicationVerotuslohkoDTO(final HarvestPermitAreaVerotusLohko entity) {

        this.officialCode = entity.getOfficialCode();
        this.name = entity.getName();
        this.areaSize = entity.getAreaSize();
        this.landSize = entity.getLandSize();
        this.waterSize = entity.getWaterSize();
        this.stateSize = entity.getStateSize();
        this.stateLandSize = entity.getStateLandSize();
        this.stateWaterSize = entity.getStateWaterSize();
        this.privateSize = entity.getPrivateSize();
        this.privateLandSize = entity.getPrivateLandSize();
        this.privateWaterSize = entity.getPrivateWaterSize();
    }

    public JyvitysExcelApplicationVerotuslohkoDTO(final String officialCode, final String name, final double areaSize,
                                                  final double landSize, final double waterSize, final double stateSize,
                                                  final double stateLandSize, final double stateWaterSize, final double privateSize,
                                                  final double privateLandSize, final double privateWaterSize) {
        this.officialCode = officialCode;
        this.name = name;
        this.areaSize = areaSize;
        this.landSize = landSize;
        this.waterSize = waterSize;
        this.stateSize = stateSize;
        this.stateLandSize = stateLandSize;
        this.stateWaterSize = stateWaterSize;
        this.privateSize = privateSize;
        this.privateLandSize = privateLandSize;
        this.privateWaterSize = privateWaterSize;
    }

    public String getOfficialCode() {
        return officialCode;
    }

    public String getName() {
        return name;
    }

    public double getAreaSize() {
        return areaSize;
    }

    public double getLandSize() {
        return landSize;
    }

    public double getWaterSize() {
        return waterSize;
    }

    public double getStateSize() {
        return stateSize;
    }

    public double getStateLandSize() {
        return stateLandSize;
    }

    public double getStateWaterSize() {
        return stateWaterSize;
    }

    public double getPrivateSize() {
        return privateSize;
    }

    public double getPrivateLandSize() {
        return privateLandSize;
    }

    public double getPrivateWaterSize() {
        return privateWaterSize;
    }


    public static final class Builder {
        private String officialCode;
        private String name;
        private double areaSize;
        private double landSize;
        private double waterSize;
        private double stateSize;
        private double stateLandSize;
        private double stateWaterSize;
        private double privateSize;
        private double privateLandSize;
        private double privateWaterSize;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder withOfficialCode(String officialCode) {
            this.officialCode = officialCode;
            return this;
        }

        public Builder withName(final String name) {
            this.name = name;
            return this;
        }

        public Builder withAreaSize(final double areaSize) {
            this.areaSize = areaSize;
            return this;
        }

        public Builder withLandSize(final double landSize) {
            this.landSize = landSize;
            return this;
        }

        public Builder withWaterSize(final double waterSize) {
            this.waterSize = waterSize;
            return this;
        }

        public Builder withStateSize(final double stateSize) {
            this.stateSize = stateSize;
            return this;
        }

        public Builder withStateLandSize(final double stateLandSize) {
            this.stateLandSize = stateLandSize;
            return this;
        }

        public Builder withStateWaterSize(final double stateWaterSize) {
            this.stateWaterSize = stateWaterSize;
            return this;
        }

        public Builder withPrivateSize(final double privateSize) {
            this.privateSize = privateSize;
            return this;
        }

        public Builder withPrivateLandSize(final double privateLandSize) {
            this.privateLandSize = privateLandSize;
            return this;
        }

        public Builder withPrivateWaterSize(final double privateWaterSize) {
            this.privateWaterSize = privateWaterSize;
            return this;
        }

        public JyvitysExcelApplicationVerotuslohkoDTO build() {
            return new JyvitysExcelApplicationVerotuslohkoDTO(officialCode, name, areaSize, landSize, waterSize,
                    stateSize, stateLandSize, stateWaterSize, privateSize, privateLandSize, privateWaterSize);
        }
    }
}
