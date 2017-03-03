package fi.riista.feature.harvestpermit.area;

import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;

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
import java.util.Objects;

@Entity
@Access(AccessType.FIELD)
public class HarvestPermitAreaRhy extends BaseEntity<Long> {
    private Long id;

    @NotNull
    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private HarvestPermitArea harvestPermitArea;

    @Column(nullable = false)
    private double areaSize;

    @NotNull
    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Riistanhoitoyhdistys rhy;

    protected HarvestPermitAreaRhy() {
    }

    public HarvestPermitAreaRhy(@Nonnull final HarvestPermitArea harvestPermitArea,
                                @Nonnull final Riistanhoitoyhdistys rhy,
                                final double areaSize) {

        this.harvestPermitArea = Objects.requireNonNull(harvestPermitArea, "harvestPermitArea is null");
        this.rhy = Objects.requireNonNull(rhy, "rhy is null");
        this.areaSize = areaSize;
    }

    // Accessors -->

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Access(value = AccessType.PROPERTY)
    @Column(name = "harvest_permit_area_rhy_id", nullable = false)
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

    public double getAreaSize() {
        return areaSize;
    }

    public void setAreaSize(final double areaSize) {
        this.areaSize = areaSize;
    }

    public Riistanhoitoyhdistys getRhy() {
        return rhy;
    }

    public void setRhy(final Riistanhoitoyhdistys rhy) {
        this.rhy = rhy;
    }
}
