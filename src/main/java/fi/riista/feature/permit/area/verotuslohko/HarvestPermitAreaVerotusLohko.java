package fi.riista.feature.permit.area.verotuslohko;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.gis.zone.TotalLandWaterSizeDTO;
import fi.riista.feature.permit.area.HarvestPermitArea;

import javax.annotation.Nonnull;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static java.util.Objects.requireNonNull;

@Entity
@Access(AccessType.FIELD)
public class HarvestPermitAreaVerotusLohko extends LifecycleEntity<Long> {
    private Long id;

    @NotNull
    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private HarvestPermitArea harvestPermitArea;

    @NotNull
    @Size(max = 255)
    @Column(nullable = false)
    private String officialCode;

    @NotNull
    @Size(max = 255)
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private double areaSize;

    @Column(nullable = false)
    private double landSize;

    @Column(nullable = false)
    private double waterSize;

    @Column(nullable = false)
    private double stateSize;

    @Column(nullable = false)
    private double stateLandSize;

    @Column(nullable = false)
    private double stateWaterSize;

    @Column(nullable = false)
    private double privateSize;

    @Column(nullable = false)
    private double privateLandSize;

    @Column(nullable = false)
    private double privateWaterSize;

    public void movePrivateToStateArea() {
        stateSize += privateSize;
        stateLandSize += privateLandSize;
        stateWaterSize += privateWaterSize;
        privateSize = 0;
        privateLandSize = 0;
        privateWaterSize = 0;
    }

    protected HarvestPermitAreaVerotusLohko() {
    }

    public HarvestPermitAreaVerotusLohko(@Nonnull final HarvestPermitArea harvestPermitArea,
                                         @Nonnull final String officialCode,
                                         @Nonnull final String name,
                                         @Nonnull final TotalLandWaterSizeDTO bothSize,
                                         @Nonnull final TotalLandWaterSizeDTO stateSize,
                                         @Nonnull final TotalLandWaterSizeDTO privateSize) {
        this.harvestPermitArea = requireNonNull(harvestPermitArea);
        this.officialCode = requireNonNull(officialCode);
        this.name = requireNonNull(name);
        this.areaSize = bothSize.getTotal();
        this.landSize = bothSize.getLand();
        this.waterSize = bothSize.getWater();
        this.stateSize = stateSize.getTotal();
        this.stateLandSize = stateSize.getLand();
        this.stateWaterSize = stateSize.getWater();
        this.privateSize = privateSize.getTotal();
        this.privateLandSize = privateSize.getLand();
        this.privateWaterSize = privateSize.getWater();
    }

    // Accessors -->

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Access(value = AccessType.PROPERTY)
    @Column(name = "harvest_permit_area_verotus_lohko_id", nullable = false)
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public HarvestPermitArea getHarvestPermitArea() {
        return harvestPermitArea;
    }

    public void setHarvestPermitArea(final HarvestPermitArea harvestPermitArea) {
        this.harvestPermitArea = harvestPermitArea;
    }

    public String getOfficialCode() {
        return officialCode;
    }

    public void setOfficialCode(final String officialCode) {
        this.officialCode = officialCode;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public double getAreaSize() {
        return areaSize;
    }

    public void setAreaSize(final double areaSize) {
        this.areaSize = areaSize;
    }

    public double getLandSize() {
        return landSize;
    }

    public void setLandSize(final double landSize) {
        this.landSize = landSize;
    }

    public double getWaterSize() {
        return waterSize;
    }

    public void setWaterSize(final double waterSize) {
        this.waterSize = waterSize;
    }

    public double getStateSize() {
        return stateSize;
    }

    public void setStateSize(final double stateSize) {
        this.stateSize = stateSize;
    }

    public double getStateLandSize() {
        return stateLandSize;
    }

    public void setStateLandSize(final double stateLandSize) {
        this.stateLandSize = stateLandSize;
    }

    public double getStateWaterSize() {
        return stateWaterSize;
    }

    public void setStateWaterSize(final double stateWaterSize) {
        this.stateWaterSize = stateWaterSize;
    }

    public double getPrivateSize() {
        return privateSize;
    }

    public void setPrivateSize(final double privateSize) {
        this.privateSize = privateSize;
    }

    public double getPrivateLandSize() {
        return privateLandSize;
    }

    public void setPrivateLandSize(final double privateLandSize) {
        this.privateLandSize = privateLandSize;
    }

    public double getPrivateWaterSize() {
        return privateWaterSize;
    }

    public void setPrivateWaterSize(final double privateWaterSize) {
        this.privateWaterSize = privateWaterSize;
    }
}
