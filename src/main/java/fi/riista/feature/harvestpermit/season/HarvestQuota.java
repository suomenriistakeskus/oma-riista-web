package fi.riista.feature.harvestpermit.season;

import fi.riista.feature.common.entity.LifecycleEntity;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Entity
@Access(AccessType.FIELD)
public class HarvestQuota extends LifecycleEntity<Long> {

    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private HarvestSeason harvestSeason;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private HarvestArea harvestArea;

    @Min(0)
    @Column(nullable = true)
    private Integer quota;

    @Column(nullable = false)
    private boolean huntingSuspended = false;

    public HarvestQuota() {
    }

    public HarvestQuota(HarvestSeason harvestSeason, HarvestArea harvestArea, Integer quota) {
        this.harvestSeason = harvestSeason;
        this.harvestArea = harvestArea;
        this.quota = quota;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "harvest_quota_id", nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public HarvestSeason getHarvestSeason() {
        return harvestSeason;
    }

    public void setHarvestSeason(HarvestSeason harvestSeason) {
        this.harvestSeason = harvestSeason;
    }

    public HarvestArea getHarvestArea() {
        return harvestArea;
    }

    public void setHarvestArea(HarvestArea harvestArea) {
        this.harvestArea = harvestArea;
    }

    public Integer getQuota() {
        return quota;
    }

    public void setQuota(Integer quota) {
        this.quota = quota;
    }

    public boolean hasQuota() {
        return quota != null;
    }

    public Boolean getHuntingSuspended() {
        return huntingSuspended;
    }

    public void setHuntingSuspended(Boolean huntingSuspended) {
        this.huntingSuspended = huntingSuspended;
    }
}
