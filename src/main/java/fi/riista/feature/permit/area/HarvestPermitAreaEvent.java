package fi.riista.feature.permit.area;

import fi.riista.feature.common.entity.BaseEntityEvent;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

@Entity
@Access(AccessType.FIELD)
public class HarvestPermitAreaEvent extends BaseEntityEvent {
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HarvestPermitArea.StatusCode status;

    @NotNull
    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private HarvestPermitArea harvestPermitArea;

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Access(value = AccessType.PROPERTY)
    @Column(name = "harvest_permit_area_event_id", nullable = false)
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    protected HarvestPermitAreaEvent() {
    }

    public HarvestPermitAreaEvent(final HarvestPermitArea harvestPermitArea,
                                  final HarvestPermitArea.StatusCode status) {
        this.harvestPermitArea = harvestPermitArea;
        this.status = status;
    }

    public HarvestPermitArea getHarvestPermitArea() {
        return harvestPermitArea;
    }

    public void setHarvestPermitArea(final HarvestPermitArea harvestPermitArea) {
        this.harvestPermitArea = harvestPermitArea;
    }

    public HarvestPermitArea.StatusCode getStatus() {
        return status;
    }

    public void setStatus(final HarvestPermitArea.StatusCode status) {
        this.status = status;
    }
}
