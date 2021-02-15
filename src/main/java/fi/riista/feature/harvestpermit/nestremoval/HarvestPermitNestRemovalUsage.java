package fi.riista.feature.harvestpermit.nestremoval;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Entity
@Access(value = AccessType.FIELD)
public class HarvestPermitNestRemovalUsage extends LifecycleEntity<Long> {

    public static final String ID_COLUMN_NAME = "harvest_permit_nest_removal_usage_id";

    private Long id;

    @Column
    private Integer nestAmount;

    @Column
    private Integer eggAmount;

    @Column
    private Integer constructionAmount;

    @OneToMany(mappedBy = "harvestPermitNestRemovalUsage")
    private Set<HarvestPermitNestLocation> harvestPermitNestLocations = new HashSet<>();

    @NotNull
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(unique = true, nullable = false)
    private HarvestPermitSpeciesAmount harvestPermitSpeciesAmount;

    public HarvestPermitNestRemovalUsage() {}

    public HarvestPermitNestRemovalUsage(final HarvestPermitSpeciesAmount harvestPermitSpeciesAmount,
                                         final Integer nestAmount, final Integer eggAmount, final Integer constructionAmount) {
        this.harvestPermitSpeciesAmount = harvestPermitSpeciesAmount;
        this.nestAmount = nestAmount;
        this.eggAmount = eggAmount;
        this.constructionAmount = constructionAmount;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = ID_COLUMN_NAME, nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public Integer getNestAmount() {
        return nestAmount;
    }

    public void setNestAmount(final Integer nestAmount) {
        this.nestAmount = nestAmount;
    }

    public Integer getEggAmount() {
        return eggAmount;
    }

    public void setEggAmount(final Integer eggAmount) {
        this.eggAmount = eggAmount;
    }

    public Integer getConstructionAmount() {
        return constructionAmount;
    }

    public void setConstructionAmount(final Integer constructionAmount) {
        this.constructionAmount = constructionAmount;
    }

    public Set<HarvestPermitNestLocation> getHarvestPermitNestLocations() {
        return harvestPermitNestLocations;
    }

    public void setHarvestPermitNestLocations(final Set<HarvestPermitNestLocation> harvestPermitNestLocations) {
        this.harvestPermitNestLocations = harvestPermitNestLocations;
    }

    public HarvestPermitSpeciesAmount getHarvestPermitSpeciesAmount() {
        return harvestPermitSpeciesAmount;
    }

    public void setHarvestPermitSpeciesAmount(final HarvestPermitSpeciesAmount harvestPermitSpeciesAmount) {
        this.harvestPermitSpeciesAmount = harvestPermitSpeciesAmount;
    }
}
