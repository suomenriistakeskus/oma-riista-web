package fi.riista.feature.permit.invoice.harvest;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.permit.invoice.Invoice;
import fi.riista.feature.permit.invoice.InvoiceType;

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

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

/**
 * Pyyntiluvan eläinlajikohtainen saalismaksu
 */
@Entity
@Access(value = AccessType.FIELD)
public class PermitHarvestInvoice extends LifecycleEntity<Long> {

    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = Invoice.ID_COLUMN_NAME, nullable = false, unique = true)
    private Invoice invoice;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = HarvestPermitSpeciesAmount.ID_COLUMN_NAME, nullable = false, unique = true)
    private HarvestPermitSpeciesAmount speciesAmount;

    // For Hibernate
    PermitHarvestInvoice() {
    }

    public PermitHarvestInvoice(final Invoice invoice, final HarvestPermitSpeciesAmount speciesAmount) {
        this.invoice = requireNonNull(invoice, "invoice is null");
        this.speciesAmount = requireNonNull(speciesAmount, "speciesAmount is null");

        checkState(invoice.getType() == InvoiceType.PERMIT_HARVEST, "Incorrect invoice type");
    }

    // Accessors -->

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permit_harvest_invoice_id", nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(final Invoice invoice) {
        this.invoice = invoice;
    }

    public HarvestPermitSpeciesAmount getSpeciesAmount() {
        return speciesAmount;
    }

    public void setSpeciesAmount(final HarvestPermitSpeciesAmount speciesAmount) {
        this.speciesAmount = speciesAmount;
    }
}
