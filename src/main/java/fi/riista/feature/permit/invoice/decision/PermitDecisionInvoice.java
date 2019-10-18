package fi.riista.feature.permit.invoice.decision;

import com.google.common.base.Preconditions;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.invoice.Invoice;
import fi.riista.feature.permit.invoice.InvoiceType;
import fi.riista.feature.permit.invoice.batch.PermitDecisionInvoiceBatch;

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

import static java.util.Objects.requireNonNull;

/**
 * Pyyntilupapäätöksen käsittelymaksun lasku
 */
@Entity
@Access(value = AccessType.FIELD)
public class PermitDecisionInvoice extends LifecycleEntity<Long> {

    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = PermitDecision.ID_COLUMN_NAME, nullable = false, unique = true)
    private PermitDecision decision;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = Invoice.ID_COLUMN_NAME, nullable = false, unique = true)
    private Invoice invoice;

    @ManyToOne(fetch = FetchType.LAZY)
    private PermitDecisionInvoiceBatch batch;

    // For Hibernate
    PermitDecisionInvoice() {
    }

    public PermitDecisionInvoice(final PermitDecision decision, final Invoice invoice) {
        this.decision = requireNonNull(decision, "decision is null");
        this.invoice = requireNonNull(invoice, "invoice is null");

        Preconditions.checkState(invoice.getType() == InvoiceType.PERMIT_PROCESSING, "Incorrect invoice type");
    }

    // Accessors -->

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permit_decision_invoice_id", nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public PermitDecision getDecision() {
        return decision;
    }

    void setDecision(final PermitDecision decision) {
        this.decision = decision;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    void setInvoice(final Invoice invoice) {
        this.invoice = invoice;
    }

    public PermitDecisionInvoiceBatch getBatch() {
        return batch;
    }

    public void setBatch(final PermitDecisionInvoiceBatch batch) {
        this.batch = batch;
    }
}
