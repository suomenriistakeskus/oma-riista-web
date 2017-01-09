package fi.riista.feature.harvestpermit.area;

import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.huntingclub.area.HuntingClubArea;

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
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

@Entity
@Access(AccessType.FIELD)
public class HarvestPermitAreaPartner extends BaseEntity<Long> {

    private Long id;

    @NotNull
    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private HarvestPermitArea harvestPermitArea;

    @NotNull
    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private HuntingClubArea sourceArea;

    // Copy created from sourceArea.zone
    @NotNull
    @JoinColumn(nullable = false, unique = true)
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    private GISZone zone;

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Access(value = AccessType.PROPERTY)
    @Column(name = "harvest_permit_area_partner_id", nullable = false)
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    protected HarvestPermitAreaPartner() {
    }

    public HarvestPermitAreaPartner(final HarvestPermitArea harvestPermitArea,
                                    final HuntingClubArea sourceArea,
                                    final GISZone zone) {
        this.harvestPermitArea = harvestPermitArea;
        this.sourceArea = sourceArea;
        this.zone = zone;
    }

    public HarvestPermitArea getHarvestPermitArea() {
        return harvestPermitArea;
    }

    public void setHarvestPermitArea(final HarvestPermitArea harvestPermitArea) {
        this.harvestPermitArea = harvestPermitArea;
    }

    public HuntingClubArea getSourceArea() {
        return sourceArea;
    }

    public void setSourceArea(final HuntingClubArea sourceArea) {
        this.sourceArea = sourceArea;
    }

    public GISZone getZone() {
        return zone;
    }

    public void setZone(final GISZone zone) {
        this.zone = zone;
    }
}
