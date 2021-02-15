package fi.riista.feature.account.pilot;

import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.feature.harvestpermit.HarvestPermit;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

@Entity
@Access(value = AccessType.FIELD)
public class DeerPilot extends BaseEntity<Long> {

    private Long id;

    @NotNull
    @JoinColumn(unique = true, nullable = false)
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    private HarvestPermit harvestPermit;

    // Accessors -->

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "deer_pilot_id", nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public HarvestPermit getHarvestPermit() {
        return harvestPermit;
    }

    public void setHarvestPermit(final HarvestPermit harvestPermit) {
        this.harvestPermit = harvestPermit;
    }
}
