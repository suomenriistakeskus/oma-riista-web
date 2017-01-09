package fi.riista.feature.huntingclub.permit.harvestreport;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;

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
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

@Entity
@Access(AccessType.FIELD)
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"species_amount_id"})})
public class MooseHarvestReport extends LifecycleEntity<Long> {

    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(unique = true, nullable = false)
    private HarvestPermitSpeciesAmount speciesAmount;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(unique = true)
    private PersistentFileMetadata receiptFileMetadata;

    @Column(nullable = false)
    private boolean noHarvests;

    @Column(nullable = false)
    private boolean moderatorOverride;

    public MooseHarvestReport() {
    }

    @AssertTrue
    public boolean isOk() {
        return (moderatorOverride && !noHarvests && receiptFileMetadata == null)
                || (!moderatorOverride && noHarvests && receiptFileMetadata == null)
                || (!moderatorOverride && !noHarvests && receiptFileMetadata != null);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "moose_harvest_report_id", nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public HarvestPermitSpeciesAmount getSpeciesAmount() {
        return speciesAmount;
    }

    public void setSpeciesAmount(HarvestPermitSpeciesAmount speciesAmount) {
        this.speciesAmount = speciesAmount;
    }

    public PersistentFileMetadata getReceiptFileMetadata() {
        return receiptFileMetadata;
    }

    public void setReceiptFileMetadata(PersistentFileMetadata receiptFileMetadata) {
        this.receiptFileMetadata = receiptFileMetadata;
    }

    public boolean isNoHarvests() {
        return noHarvests;
    }

    public void setNoHarvests(boolean noHarvests) {
        this.noHarvests = noHarvests;
    }

    public boolean isModeratorOverride() {
        return moderatorOverride;
    }

    public void setModeratorOverride(boolean moderatorOverride) {
        this.moderatorOverride = moderatorOverride;
    }
}
