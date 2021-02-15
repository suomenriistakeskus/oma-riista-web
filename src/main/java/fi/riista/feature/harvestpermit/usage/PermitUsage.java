package fi.riista.feature.harvestpermit.usage;

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
import javax.persistence.OneToOne;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Entity
@Access(value = AccessType.FIELD)
public class PermitUsage extends LifecycleEntity<Long> {

    public static final String ID_COLUMN_NAME = "permit_usage_id";

    private Long id;

    @Column
    @Min(0)
    private Integer specimenAmount;

    @Column
    @Min(0)
    private Integer eggAmount;

    @NotNull
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(unique = true, nullable = false)
    private HarvestPermitSpeciesAmount harvestPermitSpeciesAmount;

    public PermitUsage() {
    }

    public PermitUsage(final Integer specimenAmount,
                       final Integer eggAmount,
                       final @NotNull HarvestPermitSpeciesAmount harvestPermitSpeciesAmount) {
        this.specimenAmount = specimenAmount;
        this.eggAmount = eggAmount;
        this.harvestPermitSpeciesAmount = harvestPermitSpeciesAmount;
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

    public Integer getSpecimenAmount() {
        return specimenAmount;
    }

    public void setSpecimenAmount(final Integer specimenAmount) {
        this.specimenAmount = specimenAmount;
    }

    public Integer getEggAmount() {
        return eggAmount;
    }

    public void setEggAmount(final Integer eggAmount) {
        this.eggAmount = eggAmount;
    }

    public HarvestPermitSpeciesAmount getHarvestPermitSpeciesAmount() {
        return harvestPermitSpeciesAmount;
    }

}
