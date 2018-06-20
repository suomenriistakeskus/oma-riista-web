package fi.riista.feature.permit.invoice;

import fi.riista.feature.common.entity.BaseEntityEvent;

import javax.annotation.Nonnull;
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
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import static java.util.Objects.requireNonNull;

@Entity
@Access(value = AccessType.FIELD)
public class InvoiceStateChangeEvent extends BaseEntityEvent {

    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Invoice invoice;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private InvoiceStateChangeEventType type;

    InvoiceStateChangeEvent() {
        // For Hibernate
    }

    public InvoiceStateChangeEvent(@Nonnull final Invoice invoice, @Nonnull final InvoiceStateChangeEventType type) {
        this.invoice = requireNonNull(invoice, "invoice is null");
        this.type = requireNonNull(type, "type is null");
    }

    // Accessors -->

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoice_state_change_event_id", nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public InvoiceStateChangeEventType getType() {
        return type;
    }
}
