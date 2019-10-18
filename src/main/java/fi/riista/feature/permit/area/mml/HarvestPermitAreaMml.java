package fi.riista.feature.permit.area.mml;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.permit.area.HarvestPermitArea;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
public class HarvestPermitAreaMml extends LifecycleEntity<Long> {

    private Long id;

    @NotNull
    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private HarvestPermitArea harvestPermitArea;

    @Column(nullable = false)
    private long kiinteistoTunnus;

    @Column(nullable = false)
    private int palstaId;

    @Size(max = 255)
    @Column
    private String name;

    // Palsta intersection with zone geometry in square meters
    @Column(nullable = false)
    private double intersectionArea;

    public HarvestPermitAreaMml() {
    }

    public HarvestPermitAreaMml(@Nonnull final HarvestPermitArea harvestPermitArea,
                                final long kiinteistoTunnus,
                                final int palstaId,
                                @Nullable final String name,
                                final double intersectionArea) {

        this.harvestPermitArea = requireNonNull(harvestPermitArea, "harvestPermitArea is null");
        this.kiinteistoTunnus = kiinteistoTunnus;
        this.palstaId = palstaId;
        this.name = name;
        this.intersectionArea = intersectionArea;
    }

    // Accessors -->

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Access(value = javax.persistence.AccessType.PROPERTY)
    @Column(name = "harvest_permit_area_mml_id", nullable = false)
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

    public void setHarvestPermitArea(HarvestPermitArea harvestPermitArea) {
        this.harvestPermitArea = harvestPermitArea;
    }

    public long getKiinteistoTunnus() {
        return kiinteistoTunnus;
    }

    public void setKiinteistoTunnus(long kiinteistoTunnus) {
        this.kiinteistoTunnus = kiinteistoTunnus;
    }

    public int getPalstaId() {
        return palstaId;
    }

    public void setPalstaId(int palstaId) {
        this.palstaId = palstaId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getIntersectionArea() {
        return intersectionArea;
    }

    public void setIntersectionArea(double intersectionArea) {
        this.intersectionArea = intersectionArea;
    }

}
