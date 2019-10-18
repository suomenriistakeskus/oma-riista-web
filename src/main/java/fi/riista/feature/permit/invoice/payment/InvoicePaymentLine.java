package fi.riista.feature.permit.invoice.payment;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.permit.invoice.Invoice;
import fi.riista.integration.mmm.transfer.AccountTransfer;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.joda.time.LocalDate;

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
import java.math.BigDecimal;

import static java.util.Objects.requireNonNull;

@Entity
@Access(value = AccessType.FIELD)
public class InvoicePaymentLine extends LifecycleEntity<Long> {

    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false, updatable = false)
    private Invoice invoice;

    // Denormalized value in case the amount is originating from AccountTransfer.
    @NotNull
    @Column(nullable = false, updatable = false)
    private LocalDate paymentDate;

    // Denormalized value in case the amount is originating from AccountTransfer.
    @NotNull
    @Column(nullable = false, updatable = false)
    private BigDecimal amount;

    // Null when payment line is manually added by moderator.
    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = AccountTransfer.ID_COLUMN_NAME, nullable = true, unique = true, updatable = false)
    private AccountTransfer accountTransfer;

    InvoicePaymentLine() {
        // For Hibernate
    }

    public InvoicePaymentLine(@Nonnull final Invoice invoice, @Nonnull final AccountTransfer accountTransfer) {
        this.invoice = requireNonNull(invoice, "invoice is null");
        this.accountTransfer = requireNonNull(accountTransfer, "accountTransfer is null");
        this.paymentDate = requireNonNull(accountTransfer.getTransactionDate(), "paymentDate is null");
        this.amount = requireNonNull(accountTransfer.getAmount(), "amount is null");
    }

    // For moderator-inserted case
    public InvoicePaymentLine(@Nonnull final Invoice invoice,
                              @Nonnull final LocalDate paymentDate,
                              @Nonnull final BigDecimal amount) {

        this.invoice = requireNonNull(invoice, "invoice is null");
        this.paymentDate = requireNonNull(paymentDate, "paymentDate is null");
        this.amount = requireNonNull(amount, "amount is null");
    }

    public boolean isAddedByModerator() {
        return accountTransfer == null;
    }

    // Helpful in test code in case of assertion errors.
    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    // Accessors -->

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Access(value = AccessType.PROPERTY)
    @Column(name = "invoice_payment_line_id", nullable = false)
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

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public AccountTransfer getAccountTransfer() {
        return accountTransfer;
    }
}
