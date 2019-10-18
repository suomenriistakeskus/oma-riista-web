package fi.riista.integration.mmm.transfer;

import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.feature.common.entity.CreditorReference;
import fi.riista.feature.common.entity.IbanConverter;
import org.hibernate.validator.constraints.NotBlank;
import org.iban4j.Iban;
import org.joda.time.LocalDate;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * MMM:n tiliotteelta luettu yksittäinen tilisiirto
 */
@Entity
@Access(value = AccessType.FIELD)
public class AccountTransfer extends BaseEntity<Long> {

    public static final String ID_COLUMN_NAME = "account_transfer_id";

    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private AccountTransferBatch batch;

    /** IBAN account number of creditor */
    @NotNull
    @Column(nullable = false, length = 18)
    @Convert(converter = IbanConverter.class)
    private Iban creditorIban;

    /**
     * The date on which the transaction is initiated, may not be a business
     * day.
     */
    @NotNull
    @Column(nullable = false)
    private LocalDate transactionDate;

    /**
     * The date on which the transaction is processed i.e. the first business
     * day after last cut-off time. Also known as "effective date".
     */
    @NotNull
    @Column(nullable = false)
    private LocalDate bookingDate;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String debtorName;

    @NotNull
    @Column(nullable = false)
    private BigDecimal amount;

    @NotNull
    @Valid
    @Embedded
    private CreditorReference creditorReference;

    /** "arkistointitunnus" */
    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String accountServiceReference;

    // Accessors -->

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Access(value = AccessType.PROPERTY)
    @Column(name = ID_COLUMN_NAME, nullable = false)
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public AccountTransferBatch getBatch() {
        return batch;
    }

    public void setBatch(final AccountTransferBatch batch) {
        this.batch = batch;
    }

    public Iban getCreditorIban() {
        return creditorIban;
    }

    public void setCreditorIban(final Iban creditorIban) {
        this.creditorIban = creditorIban;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(final LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(final LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getDebtorName() {
        return debtorName;
    }

    public void setDebtorName(final String debtorName) {
        this.debtorName = debtorName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(final BigDecimal amount) {
        this.amount = amount;
    }

    public CreditorReference getCreditorReference() {
        return creditorReference;
    }

    public void setCreditorReference(final CreditorReference creditorReference) {
        this.creditorReference = creditorReference;
    }

    public String getAccountServiceReference() {
        return accountServiceReference;
    }

    public void setAccountServiceReference(final String accountServiceReference) {
        this.accountServiceReference = accountServiceReference;
    }
}
