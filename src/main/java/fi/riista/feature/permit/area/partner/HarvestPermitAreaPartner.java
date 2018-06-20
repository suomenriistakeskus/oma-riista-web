package fi.riista.feature.permit.area.partner;

import fi.riista.feature.gis.zone.AreaEntity;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.permit.area.HarvestPermitArea_;
import fi.riista.util.LocalisedString;
import fi.riista.util.jpa.CriteriaUtils;

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
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

import java.util.Objects;
import java.util.Optional;

@Entity
@Access(AccessType.FIELD)
public class HarvestPermitAreaPartner extends AreaEntity<Long> {

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

    protected HarvestPermitAreaPartner() {
    }

    public HarvestPermitAreaPartner(@Nonnull final HarvestPermitArea harvestPermitArea,
                                    @Nonnull final HuntingClubArea sourceArea,
                                    @Nonnull final GISZone zone) {

        setHarvestPermitArea(Objects.requireNonNull(harvestPermitArea, "harvestPermitArea is null"));
        this.sourceArea = Objects.requireNonNull(sourceArea, "sourceArea is null");
        this.zone = Objects.requireNonNull(zone, "zone is null");
    }

    @Override
    public LocalisedString getNameLocalisation() {
        return Optional.ofNullable(sourceArea).map(HuntingClubArea::getNameLocalisation).orElse(null);
    }

    // Accessors -->

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

    public HarvestPermitArea getHarvestPermitArea() {
        return harvestPermitArea;
    }

    public void setHarvestPermitArea(final HarvestPermitArea area) {
        CriteriaUtils.updateInverseCollection(HarvestPermitArea_.partners, this, this.harvestPermitArea, area);
        this.harvestPermitArea = area;
    }

    public HuntingClubArea getSourceArea() {
        return sourceArea;
    }

    public void setSourceArea(final HuntingClubArea sourceArea) {
        this.sourceArea = sourceArea;
    }

    @Override
    public GISZone getZone() {
        return zone;
    }

    @Override
    public void setZone(final GISZone zone) {
        this.zone = zone;
    }
}
